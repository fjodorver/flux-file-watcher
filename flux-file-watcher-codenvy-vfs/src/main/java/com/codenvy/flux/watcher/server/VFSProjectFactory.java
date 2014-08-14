/*******************************************************************************
 * Copyright (c) 2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.flux.watcher.server;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.flux.watcher.core.RepositoryEventBus;
import com.codenvy.flux.watcher.core.spi.Project;
import com.codenvy.flux.watcher.core.spi.ProjectFactory;

@Singleton
public class VFSProjectFactory implements ProjectFactory {

    private FluxSyncEventService        watchService;
    private ProjectManager              projectManager;
    private HashMap<String, VFSProject> projects;

    @Inject
    public VFSProjectFactory(EventService eventService, RepositoryEventBus repositoryEventBus, ProjectManager projectManager) {
        this.projectManager = checkNotNull(projectManager);
        this.watchService = new FluxSyncEventService(checkNotNull(eventService), checkNotNull(repositoryEventBus),
                                                     checkNotNull(projectManager));
        projects = new HashMap<String, VFSProject>();
    }

    @Override
    public Project newProject(String projectId, String projectPath) {
        checkNotNull(projectId);
        checkNotNull(projectPath);

        if (!projects.containsKey(projectPath)) {
            // make sure that project folder exists
            VirtualFile projectFolder = null;
            try {
                // TODO workspace should not be hardcoded
                FolderEntry root = projectManager.getProjectsRoot("workspace");
                projectFolder = root.getVirtualFile().getChild(projectPath);
            } catch (ServerException | ForbiddenException e) {
                e.getMessage();
            }
            checkArgument(projectFolder != null && projectFolder.isFolder());

            VFSProject project = new VFSProject(watchService, projectManager, projectId, projectPath);
            project.setSynchronized(true);
            projects.put(projectPath, project);
            return project;
        } else {
            return projects.get(projectPath);
        }
    }
}
