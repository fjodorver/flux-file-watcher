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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(FluxSyncInitService.class);

    private FluxVFSEventService watchService;
    private ProjectManager      projectManager;

    @Inject
    public VFSProjectFactory(EventService eventService, RepositoryEventBus repositoryEventBus, ProjectManager projectManager) {
        this.projectManager = checkNotNull(projectManager);
        watchService = new FluxVFSEventService(checkNotNull(eventService), checkNotNull(repositoryEventBus),
                                                    checkNotNull(projectManager));
    }

    @Override
    public Project newProject(String projectId, String projectPath) {
        checkNotNull(projectId);
        checkNotNull(projectPath);

        // make sure that project folder exists in Codenvy
        VirtualFile projectFolder = null;
        try {
            // TODO workspace should not be hardcoded
            FolderEntry root = projectManager.getProjectsRoot("1q2w3e");
            projectFolder = root.getVirtualFile().getChild(projectPath);
        } catch (ServerException | ForbiddenException e) {
            LOG.error(e.getMessage());
        }
        checkArgument(projectFolder != null && projectFolder.isFolder());

        return new VFSProject(watchService, projectManager, projectId, projectPath);
    }
}
