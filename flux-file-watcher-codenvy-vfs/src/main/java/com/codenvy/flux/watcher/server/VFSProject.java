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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.VirtualFileEntry;
import com.codenvy.api.project.server.FileEntry;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.flux.watcher.core.Resource;
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
    private final ProjectManager       projectManager;

    public VFSProject(FluxSyncEventService watchService, ProjectManager projectManager, String id, String path) {
        this.id = checkNotNull(id);
        this.path = checkNotNull(path);
        this.watchService = watchService;
        this.projectManager = projectManager;
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
        try {
            // TODO workspace should not be hardcoded
            com.codenvy.api.project.server.Project project = projectManager.getProject("worksapce", path);
            FolderEntry baseFolder = project.getBaseFolder();

            List<FolderEntry> folders = baseFolder.getChildFolders();
            for (FolderEntry folder : folders) {
                resources.addAll(getResources(folder));
            }
            List<FileEntry> files = baseFolder.getChildFiles();
            for (FileEntry file : files) {
                VirtualFile vFile = file.getVirtualFile();
                byte[] content = IOUtils.toByteArray(vFile.getContent().getStream());
                resources.add(Resource.newFile(vFile.getPath(), vFile.getLastModificationDate(), content));
            }
        } catch (IOException | ServerException | ForbiddenException e) {
            e.getMessage();
        }
        return resources;
    }

    private Set<Resource> getResources(FolderEntry folder) {
        final Set<Resource> resources = new HashSet<>();
        try {
            // current folder is not project base folder
            if (!folder.getPath().equals(path)) {
                resources.add(Resource.newFolder(folder.getPath(), folder.getVirtualFile().getLastModificationDate()));
            }
            List<FileEntry> files = folder.getChildFiles();
            for (FileEntry file : files) {
                VirtualFile vFile = file.getVirtualFile();
                byte[] content = IOUtils.toByteArray(vFile.getContent().getStream());
                resources.add(Resource.newFile(vFile.getPath(), vFile.getLastModificationDate(), content));
            }
            List<FolderEntry> folders = folder.getChildFolders();
            for (FolderEntry folderr : folders) {
                resources.addAll(getResources(folderr));
            }
        } catch (IOException | ForbiddenException | ServerException e) {
            e.getMessage();
        }
        return resources;
    }

    @Override
    public Resource getResource(String resourcePath) {
        checkNotNull(resourcePath);

        try {
            // TODO workspace should not be hardcoded
            com.codenvy.api.project.server.Project project = projectManager.getProject("worksapce", path);
            FolderEntry baseFolder = project.getBaseFolder();
            VirtualFileEntry vfEntry = baseFolder.getChild(resourcePath);

            if (vfEntry != null) {
                VirtualFile vFile = vfEntry.getVirtualFile();
                if (vfEntry.isFolder()) {
                    return Resource.newFolder(vFile.getPath(), vFile.getLastModificationDate());
                } else if (vfEntry.isFile()) {
                    byte[] content = IOUtils.toByteArray(vFile.getContent().getStream());
                    return Resource.newFile(vFile.getPath(), vFile.getLastModificationDate(), content);
                }
            }
        } catch (IOException | ForbiddenException | ServerException e) {
            e.getMessage();
        }
        return null;
    }

    @Override
    public void createResource(Resource resource) {
        checkNotNull(resource);

        try {
            // TODO workspace should not be hardcoded
            com.codenvy.api.project.server.Project project = projectManager.getProject("worksapce", path);
            FolderEntry baseFolder = project.getBaseFolder();
            VirtualFileEntry vfEntry = baseFolder.getChild(resource.path());
            if (vfEntry == null) {
                // TODO
            } else {
                // resource at given resource.path() already exist & cannot be created
            }
        } catch (ForbiddenException | ServerException e) {
            e.getMessage();
        }
    }

    @Override
    public void updateResource(Resource resource) {
        checkNotNull(resource);

        try {
            // TODO workspace should not be hardcoded
            com.codenvy.api.project.server.Project project = projectManager.getProject("worksapce", path);
            FolderEntry baseFolder = project.getBaseFolder();
            VirtualFileEntry vfEntry = baseFolder.getChild(resource.path());
            if (vfEntry != null) {
                // TODO
            } else {
                // resource at given resource.path() does not exist & cannot be updated
            }
        } catch (ForbiddenException | ServerException e) {
            e.getMessage();
        }
    }

    @Override
    public void deleteResource(Resource resource) {
        checkNotNull(resource);

        try {
            // TODO workspace should not be hardcoded
            com.codenvy.api.project.server.Project project = projectManager.getProject("worksapce", path);
            FolderEntry baseFolder = project.getBaseFolder();
            VirtualFileEntry vfEntry = baseFolder.getChild(resource.path());
            if (vfEntry != null) {
                // TODO
            } else {
                // resource at given resource.path() does not exist & cannot be deleted
            }
        } catch (ForbiddenException | ServerException e) {
            e.getMessage();
        }
    }

    @Override
    public void setSynchronized(boolean synchronize) {
        if (synchronize) {
            watchService.setProjectSync(this);
        } else {
            watchService.setProjectSync(null);
        }
    }
}
