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
import com.codenvy.api.vfs.server.observation.MoveEvent;
import com.codenvy.api.vfs.server.observation.RenameEvent;
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
        VirtualFileEvent.ChangeType eventType = event.getType();
        String eventWorkspace = event.getWorkspaceId();
        LOG.debug("new event in workspace " + eventWorkspace);

        // TODO workspace should not be hardcoded
        if (eventWorkspace.equals("1q2w3e")) {

            RepositoryEventType repositoryEventType = null;
            Resource resource;

            final String eventPath = event.getPath();
            LOG.debug("eventPath : >" + eventPath + "<");

            Set<Project> projects = repository.getSynchronizedProjects();
            Project project = FluentIterable.from(projects)
                                            .firstMatch(new Predicate<Project>() {
                                                @Override
                                                public boolean apply(Project project) {
                                                    return eventPath.startsWith(project.path());
                                                }
                                            })
                                            .orNull();

            if (project != null) {
                String projectPath = project.path();

                if (eventPath.startsWith(projectPath)) {
                    String resourceRelativePath = eventPath.substring(projectPath.length());
                    if (eventType == VirtualFileEvent.ChangeType.CONTENT_UPDATED) {
                        repositoryEventType = RepositoryEventType.PROJECT_RESOURCE_MODIFIED;
                    } else if (eventType == VirtualFileEvent.ChangeType.CREATED) {
                        repositoryEventType = RepositoryEventType.PROJECT_RESOURCE_CREATED;
                    } else if (eventType == VirtualFileEvent.ChangeType.DELETED) {
                        repositoryEventType = RepositoryEventType.PROJECT_RESOURCE_DELETED;
                    } else if (eventType == VirtualFileEvent.ChangeType.MOVED) {
                        repositoryEventType = RepositoryEventType.PROJECT_RESOURCE_CREATED;
                    } else if (eventType == VirtualFileEvent.ChangeType.RENAMED) {
                        repositoryEventType = RepositoryEventType.PROJECT_RESOURCE_CREATED;
                    }

                    // getting concret resource data from VFS
                    try {
                        FolderEntry workspaceRootFolder = projectManager.getProjectsRoot(eventWorkspace);
                        VirtualFileEntry vfEntry = workspaceRootFolder.getChild(resourceRelativePath);

                        VirtualFile vFile = (vfEntry != null ? vfEntry.getVirtualFile() : null);
                        if (event.isFolder()) {
                            resource = Resource.newFolder(resourceRelativePath,
                                                          (vFile != null ? vFile.getLastModificationDate() : System.currentTimeMillis()));
                            LOG.debug("newFolder : " + resourceRelativePath);
                        } else {
                            byte[] content = (vFile != null ? IOUtils.toByteArray(vFile.getContent().getStream()) : new byte[0]);
                            resource = Resource.newFile(resourceRelativePath,
                                                        (vFile != null ? vFile.getLastModificationDate() : System.currentTimeMillis()), content);
                            LOG.debug("newFile : " + resourceRelativePath + ", containing "+(vFile != null ? IOUtils.toString(vFile.getContent().getStream(), "UTF-8") : "nothing"));
                        }
                        // and firing event to Flux clients
                        if (repositoryEventType != null) {
                            repositoryEventBus.fireRepositoryEvent(new RepositoryEvent(repositoryEventType, resource, project));
                        }

                    } catch (ServerException | ForbiddenException | IOException e) {
                        LOG.error("Couldn't get projects", e);
                    }
                }

                String eventOldPath = null;
                // rename and move are treated as create and delete
                if (eventType == VirtualFileEvent.ChangeType.MOVED) {
                    eventOldPath = ((MoveEvent)event).getOldPath();
                } else if (eventType == VirtualFileEvent.ChangeType.RENAMED) {
                    eventOldPath = ((RenameEvent)event).getOldPath();
                }
                if (eventOldPath != null && eventOldPath.startsWith(projectPath)) {
                    Resource oldResource;
                    if (event.isFolder()) {
                        oldResource = Resource.newFolder(eventOldPath.substring(projectPath.length()), System.currentTimeMillis());
                    } else {
                        oldResource = Resource.newFile(eventOldPath.substring(projectPath.length()), System.currentTimeMillis(), new byte[0]);
                    }
                    repositoryEventBus.fireRepositoryEvent(new RepositoryEvent(RepositoryEventType.PROJECT_RESOURCE_DELETED, oldResource, project));
                }
            }
        }
    }
}
