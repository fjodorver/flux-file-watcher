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
import java.util.HashSet;
import java.util.Set;

import com.codenvy.api.project.server.ProjectService;
import com.codenvy.flux.watcher.core.Resource;
import com.codenvy.flux.watcher.core.Resource.ResourceType;
import com.codenvy.flux.watcher.core.spi.Project;

/**
 * {@link com.codenvy.flux.watcher.core.spi.Project} implementation.
 * 
 * @author Stéphane Tournié
 */
public class VFSProject implements Project {

    private final String               id;
    private final String               path;
    private final FluxSyncEventService watchService;
    private final ProjectService       projectService;

    public VFSProject(FluxSyncEventService watchService, ProjectService projectService, String id, String path) {
        this.id = checkNotNull(id);
        this.path = checkNotNull(path);
        this.watchService = watchService;
        this.projectService = projectService;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public Set<Resource> getResources() {
        final Set<Resource> resources = new HashSet<>();
        return null;
    }

    @Override
    public Resource getResource(String resourcePath) {
        checkNotNull(resourcePath);

        if (path != null) {
            // TODO get file/folder from Codenvy VFS & return it as a Resource
            try {
                projectService.getFile(null, resourcePath);
            } catch (Exception e) {
                e.getMessage();
            }
        }
        return null;
    }

    @Override
    public void createResource(Resource resource) {
        checkNotNull(resource);

        // TODO check first that resource.path() exists in codenvy VFS
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

    @Override
    public void updateResource(Resource resource) {

    }

    @Override
    public void deleteResource(Resource resource) {
        checkNotNull(resource);

        // TODO check first that resource.path() exists in codenvy VFS
        try {
            // TODO set workspace field
            projectService.delete(null, resource.path());
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    public void setSynchronized(boolean synchronize) {
        if (synchronize) {
            watchService.startSync(this);
        } else {
            watchService.stopSync(this);
        }
    }
}
