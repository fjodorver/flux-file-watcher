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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.codenvy.api.project.server.ProjectService;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.flux.watcher.core.RepositoryEventBus;
import com.codenvy.flux.watcher.core.RepositoryListener;
import com.codenvy.flux.watcher.core.spi.RepositoryProvider;
import com.codenvy.flux.watcher.core.Resource;
import com.codenvy.flux.watcher.core.Resource.ResourceType;
import com.google.inject.Inject;

/**
 * {@link com.codenvy.flux.watcher.core.spi.RepositoryProvider} implementation.
 *
 * @author Stéphane Tournié
 */
public class VFSRepository implements RepositoryProvider {
    private final ProjectService          projectService;
    private final ConcurrentMap<String, String> projects;
    private final RepositoryEventBus repositoryEventBus;

    @Inject
    public VFSRepository(ProjectService projectService, RepositoryEventBus repositoryEventBus) {
        this.projectService = projectService;
        this.repositoryEventBus = repositoryEventBus;
        this.projects = new ConcurrentHashMap<>();
    }

    @Override
    public boolean hasProject(String projectId) {
        return projects.containsKey(checkNotNull(projectId));
    }

    @Override
    public boolean addProject(String projectId, String projectPath) {
        checkNotNull(projectId);
        checkNotNull(projectPath);

        ProjectDescriptor projectDesc = null;
        try {
            // TODO set workspace field
            projectDesc = projectService.getProject(null, projectId);
        } catch (Exception e) {
            e.getMessage();
        }
        if (projectDesc != null) {
            final String previousProjectPath = projects.putIfAbsent(projectId, projectPath);
            if (previousProjectPath == null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeProject(String projectId) {
        checkNotNull(projectId);
        
        final String projectPath = projects.remove(projectId);
        return projectPath != null;
    }

    @Override
    public Set<Resource> getProjectResources(String projectId) {
        return null;
    }

    @Override
    public Resource getResource(String projectId, String resourcePath) {
        checkNotNull(projectId);
        checkNotNull(resourcePath);
        
        final String projectPath = projects.get(projectId);
        if (projectPath != null) {
            // TODO get file/folder from Codenvy VFS & return it as a Resource  
            //projectService.getFile(null, path);
        }
        return null;
    }

    @Override
    public void createResource(Resource resource) {
        checkNotNull(resource);
        
        final String projectPath = projects.get(resource.projectId());
        if (projectPath != null) {
            try {
                // TODO set workspace, parentPath & fileName field
                // TODO create a field 'name' in Resource?
                if (resource.type() == ResourceType.FOLDER) {
                    projectService.createFile(null, null, null, "folder", new ByteArrayInputStream(resource.content()));
                } else {
                    projectService.createFile(null, null, null, "file", new ByteArrayInputStream(resource.content()));
                }
            } catch (Exception e) {
                e.getMessage();
            }
        }
    }

    @Override
    public void updateResource(Resource resource) {

    }

    @Override
    public void deleteResource(Resource resource) {
        checkNotNull(resource);
        
        final String projectPath = projects.get(resource.projectId());
        if (projectPath != null) {
            // TODO check that resource.path() exists in codenvy VFS
            try {
                // TODO set workspace field
                projectService.delete(null, resource.path());
            } catch (Exception e) {
                e.getMessage();
            }
        }
    }

    @Override
    public RepositoryEventBus eventBus() {
        return repositoryEventBus;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        checkNotNull(clazz);
        if (clazz.isAssignableFrom(this.getClass())) {
            return clazz.cast(this);
        }
        throw new IllegalArgumentException("Repository provider cannot be unwrapped to '" + clazz.getName() + "'");
    }
}
