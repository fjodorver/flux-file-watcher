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
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventSubscriber;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.api.project.server.VirtualFileEntry;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.api.vfs.server.observation.VirtualFileEvent;
import com.codenvy.flux.watcher.core.Repository;
import com.codenvy.flux.watcher.core.RepositoryEvent;
import com.codenvy.flux.watcher.core.RepositoryEventBus;
import com.codenvy.flux.watcher.core.RepositoryEventType;
import com.codenvy.flux.watcher.core.Resource;
import com.codenvy.flux.watcher.core.spi.Project;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

/**
 * Subscriber receiving notification events from Codenvy VFS.
 */
public class VirtualFileEventSubscriber implements EventSubscriber<VirtualFileEvent> {

    private static final Logger      LOG = LoggerFactory.getLogger(VirtualFileEventSubscriber.class);

    private final RepositoryEventBus repositoryEventBus;
    private final ProjectManager     projectManager;
    private final Repository         repository;

    public VirtualFileEventSubscriber(RepositoryEventBus repositoryEventBus, ProjectManager projectManager, Repository repository) {
        this.repositoryEventBus = checkNotNull(repositoryEventBus);
        this.projectManager = checkNotNull(projectManager);
        this.repository = repository;
    }

    @Override
    public void onEvent(VirtualFileEvent event) {
        final String eventWorkspace = event.getWorkspaceId();
        final String eventPath = event.getPath();
        LOG.debug("new event in workspace " + eventWorkspace + " concerning >" + eventPath + "<");
        FolderEntry workspaceBaseFolder = null;
        try {
            workspaceBaseFolder = projectManager.getProjectsRoot(eventWorkspace);
        } catch (ServerException e) {
            LOG.error("Couldn't get projects", e);
        }

        Set<Project> projects = repository.getSynchronizedProjects();
        Project project = FluentIterable.from(projects)
                                        .firstMatch(new Predicate<Project>() {
                                            @Override
                                            public boolean apply(Project project) {
                                                return eventPath.startsWith(project.path());
                                            }
                                        })
                                        .orNull();

        if (workspaceBaseFolder != null && project != null) {
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
                    VirtualFileEntry eventPathVFEntry = workspaceBaseFolder.getChild(eventPath);
                    Resource resource = null;
                    if (eventPathVFEntry != null) {
                        VirtualFile eventPathVFile = eventPathVFEntry.getVirtualFile();
                        long lastModificationDate = eventPathVFile.getLastModificationDate();

                        if (eventPathVFEntry.isFile() && !event.isFolder()) {
                            byte[] content = IOUtils.toByteArray(eventPathVFile.getContent().getStream());
                            resource = Resource.newFile(eventPath, lastModificationDate, content);
                        } else if (eventPathVFEntry.isFolder() && event.isFolder()) {
                            resource = Resource.newFolder(eventPath, lastModificationDate);
                        }
                    } else {
                        // case of file/folder deletion event
                        if (!event.isFolder()) {
                            resource = Resource.newFile(eventPath, System.currentTimeMillis(), new byte[0]);
                        } else if (event.isFolder()) {
                            resource = Resource.newFolder(eventPath, System.currentTimeMillis());
                        }
                    }
                    repoEvent = new RepositoryEvent(repoEventType, resource, project);
                } catch (ServerException | ForbiddenException | IOException e) {
                    LOG.error("Couldn't get child named " + eventPath + " in " + workspaceBaseFolder.getPath(), e);
                }
            }

            repositoryEventBus.fireRepositoryEvent(repoEvent);
        }
    }
}
