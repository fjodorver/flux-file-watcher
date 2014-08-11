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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.notification.EventSubscriber;
import com.codenvy.api.project.server.ProjectService;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.api.vfs.server.observation.VirtualFileEvent;
import com.codenvy.flux.watcher.core.RepositoryEvent;
import com.codenvy.flux.watcher.core.RepositoryEventBus;
import com.codenvy.flux.watcher.core.RepositoryEventType;
import com.codenvy.flux.watcher.core.Resource;

/**
 * Service notifying Flux repository about publishing of VFS events for a project in Codenvy.
 * 
 * @author Stéphane Tournié
 */
// @Singleton
public class FluxSyncEventService {
    private static final Logger                     LOG = LoggerFactory.getLogger(FluxSyncEventService.class);

    private final EventService                      eventService;
    private final EventSubscriber<VirtualFileEvent> subscriber;

    // @Inject
    FluxSyncEventService(EventService eventService, RepositoryEventBus repositoryEventBus, ProjectService projectService) {
        this.eventService = eventService;
        this.subscriber = new VirtualFileEventSubscriber(repositoryEventBus, projectService);
    }

    @PostConstruct
    void start() {
        eventService.subscribe(subscriber);
    }

    @PreDestroy
    void stop() {
        eventService.unsubscribe(subscriber);
    }

    public void startSync(VFSProject project) {
        ((VirtualFileEventSubscriber)subscriber).setProject(project);
    }

    public void stopSync(VFSProject project) {
        ((VirtualFileEventSubscriber)subscriber).setProject(null);
    }

    /**
     * Subscriber receiving notification events from Codenvy VFS.
     */
    public class VirtualFileEventSubscriber implements EventSubscriber<VirtualFileEvent> {
        private final RepositoryEventBus repositoryEventBus;
        private final ProjectService     projectService;
        private VFSProject               project;

        public VirtualFileEventSubscriber(RepositoryEventBus repositoryEventBus, ProjectService projectService) {
            this.repositoryEventBus = checkNotNull(repositoryEventBus);
            this.projectService = checkNotNull(projectService);
        }

        public void setProject(VFSProject project) {
            this.project = project;
        }

        @Override
        public void onEvent(VirtualFileEvent event) {
            // TODO get project id from event
            if (project != null && event.getWorkspaceId().equals(project.id())) {
                VirtualFileEvent.ChangeType eventType = event.getType();
                RepositoryEventType repoType = null;
                if ((eventType == VirtualFileEvent.ChangeType.CREATED) || (eventType == VirtualFileEvent.ChangeType.MOVED)
                    || (eventType == VirtualFileEvent.ChangeType.RENAMED)) {
                    repoType = RepositoryEventType.PROJECT_RESOURCE_CREATED;
                } else if (eventType == VirtualFileEvent.ChangeType.CONTENT_UPDATED) {
                    repoType = RepositoryEventType.PROJECT_RESOURCE_MODIFIED;
                } else if (eventType == VirtualFileEvent.ChangeType.DELETED) {
                    repoType = RepositoryEventType.PROJECT_RESOURCE_DELETED;
                }

                RepositoryEvent repoEvent = null;
                if (repoType != null) {
                    Resource resource = null;
                    String eventWorkspace = event.getWorkspaceId();
                    String eventPath = event.getPath();
                    ProjectDescriptor project = null;
                    try {
                        project = projectService.getProject(eventWorkspace, eventPath);
                    } catch (Exception e) {
                        LOG.error(e.getMessage());
                    }
                    String eventProject = project.getName();
                    // TODO get file/folder modification time from VFS and set as timestamp
                    long timestamp = project.getModificationDate();

                    if (event.isFolder()) {
                        resource = Resource.newFolder(eventPath, timestamp);
                    } else {
                        // TODO get file content from where?
                        resource = Resource.newFile(eventPath, timestamp, new byte[0]);
                    }
                    // TODO convert from codenvy path/project to {@link com.codenvy.flux.watcher.core.spi.Project}
                    repoEvent = new RepositoryEvent(repoType, resource, null);
                }

                repositoryEventBus.fireRepositoryEvent(repoEvent);
            }
        }
    }
}
