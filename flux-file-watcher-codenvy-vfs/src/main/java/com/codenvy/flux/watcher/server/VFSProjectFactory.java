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

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.project.server.ProjectService;
import com.codenvy.flux.watcher.core.RepositoryEventBus;
import com.codenvy.flux.watcher.core.spi.Project;
import com.codenvy.flux.watcher.core.spi.ProjectFactory;

public class VFSProjectFactory implements ProjectFactory {

    private FluxSyncEventService watchService;
    private ProjectService       projectService;

    @Inject
    public VFSProjectFactory(EventService eventService, RepositoryEventBus repositoryEventBus, ProjectService projectService) {
        this.projectService = projectService;
        watchService = new FluxSyncEventService(eventService, repositoryEventBus, projectService);
    }

    @Override
    public Project newProject(String projectId, String projectPath) {
        checkNotNull(projectId);
        checkNotNull(projectPath);
        
        return new VFSProject(watchService, projectService, projectId, projectPath);
    }
}
