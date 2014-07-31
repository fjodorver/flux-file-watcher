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

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.codenvy.api.project.server.ProjectService;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.flux.watcher.core.spi.RepositoryEvent;
import com.codenvy.flux.watcher.core.spi.RepositoryEventTypes;
import com.codenvy.flux.watcher.core.spi.RepositoryListener;
import com.codenvy.flux.watcher.core.spi.RepositoryProvider;
import com.codenvy.flux.watcher.core.spi.Resource;
import com.codenvy.flux.watcher.core.spi.Resource.ResourceType;
import com.google.inject.Inject;

/**
 * {@link com.codenvy.flux.watcher.core.spi.RepositoryProvider} implementation.
 * 
 * @author Stéphane Tournié
 */
public class VFSRepository implements RepositoryProvider {
    private final Set<RepositoryListener> repositoryListeners;
    private final ProjectService          projectService;

    @Inject
    public VFSRepository(ProjectService projectService) {
        this.repositoryListeners = new CopyOnWriteArraySet<>();
        this.projectService = projectService;
    }

    @Override
    public boolean addProject(String projectId, String path) {
        checkNotNull(projectId);
        checkNotNull(path);
        ProjectDescriptor project = null;
        try {
            // TODO set workspace & NewProject fields
            project = projectService.createProject(null, projectId, null);
        } catch (Exception e) {
            e.getMessage();
        }
        return (project != null);
    }

    @Override
    public boolean removeProject(String projectId) {
        checkNotNull(projectId);
        try {
            // TODO set workspace & path fields
            projectService.delete(null, null);
        } catch (Exception e) {
            e.getMessage();
        }
        return true;
    }

    @Override
    public Resource getResource(String projectId, String path) {
        checkNotNull(projectId);
        checkNotNull(path);
        return null;
    }

    @Override
    public void createResource(Resource resource) {
        checkNotNull(resource);
        // TODO set workspace, parentPath & fileName field
        String resourcePath = resource.path().substring(0);
        String resourceName = resource.path().substring(0);
        try {
            if (resource.type() == ResourceType.FOLDER) {
                projectService.createFile(null, resourcePath, resourceName, "folder", new ByteArrayInputStream(resource.content()));
            } else {
                projectService.createFile(null, resourcePath, resourceName, "file", new ByteArrayInputStream(resource.content()));
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    public void deleteResource(Resource resource) {
        checkNotNull(resource);
        try {
            // TODO set workspace field
            projectService.delete(null, resource.path());
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    public boolean addRepositoryListener(RepositoryListener listener) {
        checkNotNull(listener);
        return repositoryListeners.add(listener);
    }

    @Override
    public boolean removeRepositoryListener(RepositoryListener listener) {
        checkNotNull(listener);
        return repositoryListeners.remove(listener);
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        checkNotNull(clazz);
        if (clazz.isAssignableFrom(this.getClass())) {
            return clazz.cast(this);
        }
        throw new IllegalArgumentException("Repository provider cannot be unwrapped to '" + clazz.getName() + "'");
    }

    void fireRepositoryEvent(RepositoryEvent event) {
        final Set<RepositoryListener> filteredListeners = new HashSet<>();
        for (RepositoryListener oneRepositoryListener : repositoryListeners) {
            final RepositoryEventTypes repositoryEventTypes = oneRepositoryListener.getClass().getAnnotation(RepositoryEventTypes.class);
            if (Arrays.asList(repositoryEventTypes.value()).contains(event.type())) {
                filteredListeners.add(oneRepositoryListener);
            }
        }
        // send event to flux repository
        for (RepositoryListener oneRepositoryListener : filteredListeners) {
            oneRepositoryListener.onEvent(event);
        }
    }
}
