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

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.notification.EventSubscriber;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.api.project.server.VirtualFileEntry;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.api.vfs.server.observation.VirtualFileEvent;
import com.codenvy.flux.watcher.core.RepositoryEvent;
import com.codenvy.flux.watcher.core.RepositoryEventBus;
import com.codenvy.flux.watcher.core.RepositoryEventType;
import com.codenvy.flux.watcher.core.Resource;

/**
 * Service firing VFS events from Codenvy to the repository event bus
 * 
 * @author Stéphane Tournié
 */
public class FluxSyncEventService {
    private static final Logger                     LOG = LoggerFactory.getLogger(FluxSyncEventService.class);

    private final EventService                      eventService;
    private final EventSubscriber<VirtualFileEvent> subscriber;

    FluxSyncEventService(EventService eventService, RepositoryEventBus repositoryEventBus, ProjectManager projectManager) {
        this.eventService = eventService;
        this.subscriber = new VirtualFileEventSubscriber(repositoryEventBus, projectManager);
    }

    @PostConstruct
    void start() {
        eventService.subscribe(subscriber);
    }

    @PreDestroy
    void stop() {
        eventService.unsubscribe(subscriber);
    }

    public void setProjectSync(VFSProject project) {
        ((VirtualFileEventSubscriber)subscriber).setProject(project);
    }

    /**
     * Subscriber receiving notification events from Codenvy VFS.
     */
    public class VirtualFileEventSubscriber implements EventSubscriber<VirtualFileEvent> {
        private final RepositoryEventBus repositoryEventBus;
        private final ProjectManager     projectManager;
        private VFSProject               vfsProject;

        public VirtualFileEventSubscriber(RepositoryEventBus repositoryEventBus, ProjectManager projectManager) {
            this.repositoryEventBus = checkNotNull(repositoryEventBus);
            this.projectManager = checkNotNull(projectManager);
        }

        public void setProject(VFSProject vfsProject) {
            this.vfsProject = vfsProject;
        }

        @Override
        public void onEvent(VirtualFileEvent event) {
            String eventWorkspace = event.getWorkspaceId();
            String eventPath = event.getPath();
            FolderEntry workspaceBaseFolder = null;
            try {
                workspaceBaseFolder = projectManager.getProjectsRoot(eventWorkspace);
            } catch (ServerException e) {
                LOG.error(e.getMessage());
            }
            if (vfsProject != null && workspaceBaseFolder != null) {
                VirtualFileEvent.ChangeType eventType = event.getType();
                RepositoryEventType repoEventType = null;
                if ((eventType == VirtualFileEvent.ChangeType.CREATED) || (eventType == VirtualFileEvent.ChangeType.MOVED)
                    || (eventType == VirtualFileEvent.ChangeType.RENAMED)) {
                    repoEventType = RepositoryEventType.PROJECT_RESOURCE_CREATED;
                } else if (eventType == VirtualFileEvent.ChangeType.CONTENT_UPDATED) {
                    repoEventType = RepositoryEventType.PROJECT_RESOURCE_MODIFIED;
                } else if (eventType == VirtualFileEvent.ChangeType.DELETED) {
                    repoEventType = RepositoryEventType.PROJECT_RESOURCE_DELETED;
                }

                RepositoryEvent repoEvent = null;
                if (repoEventType != null) {
                    try {
                        VirtualFileEntry entry = workspaceBaseFolder.getChild(eventPath);
                        if (entry != null) {
                            VirtualFile file = entry.getVirtualFile();
                            long lastModificationDate = file.getLastModificationDate();
                            Resource resource = null;

                            if (entry.isFile() && !event.isFolder()) {
                                byte[] content = IOUtils.toByteArray(file.getContent().getStream());
                                resource = Resource.newFile(eventPath, lastModificationDate, content);
                            } else if (entry.isFolder() && event.isFolder()) {
                                resource = Resource.newFolder(eventPath, lastModificationDate);
                            }
                            repoEvent = new RepositoryEvent(repoEventType, resource, vfsProject);
                        }
                    } catch (ServerException | ForbiddenException | IOException e) {
                        e.getMessage();
                    }
                }

                repositoryEventBus.fireRepositoryEvent(repoEvent);
            }
        }
    }
}
