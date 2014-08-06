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
package com.codenvy.flux.watcher.fs;

import com.codenvy.flux.watcher.core.RepositoryEventBus;
import com.codenvy.flux.watcher.core.Resource;
import com.codenvy.flux.watcher.core.spi.Repository;
import com.codenvy.flux.watcher.core.spi.RepositoryResourceProvider;
import com.google.inject.Inject;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.codenvy.flux.watcher.core.Resource.ResourceType.FILE;
import static com.codenvy.flux.watcher.core.Resource.ResourceType.FOLDER;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.setLastModifiedTime;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.Files.write;
import static java.util.Collections.unmodifiableMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * {@link com.codenvy.flux.watcher.core.spi.RepositoryResourceProvider} implementation backed by Java {@code FileSystem}.
 *
 * @author Kevin Pollet
 */
//TODO two separate implementation?
@Singleton
public class FileSystemRepository implements RepositoryResourceProvider, Repository {
    private final ConcurrentMap<String, Path> projects;
    private final FileSystemWatchService      watchService;
    private final FileSystem                  fileSystem;
    private final RepositoryEventBus          repositoryEventBus;

    @Inject
    public FileSystemRepository(FileSystem fileSystem, RepositoryEventBus repositoryEventBus) {
        this.fileSystem = fileSystem;
        this.projects = new ConcurrentHashMap<>();
        this.watchService = new FileSystemWatchService(fileSystem, this, repositoryEventBus);
        this.repositoryEventBus = repositoryEventBus;

        // start the watch service
        this.watchService.start();
    }

    @Override
    public boolean hasProject(String projectId) {
        return projects.containsKey(checkNotNull(projectId));
    }

    @Override
    public boolean addProject(String projectId, String projectPath) {
        checkNotNull(projectId);
        checkNotNull(projectPath);

        final Path newProjectPath = fileSystem.getPath(projectPath);
        checkArgument(exists(newProjectPath) && isDirectory(newProjectPath) && newProjectPath.isAbsolute());

        final Path previousProjectPath = projects.putIfAbsent(projectId, newProjectPath);
        if (previousProjectPath == null) {
            watchService.watch(newProjectPath);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeProject(String projectId) {
        checkNotNull(projectId);

        final Path projectPath = projects.remove(projectId);
        if (projectPath != null) {
            watchService.unwatch(projectPath);
        }
        return projectPath != null;
    }

    @Override
    public Set<Resource> getProjectResources(final String projectId) {
        checkNotNull(projectId);
        checkArgument(projects.containsKey(projectId));

        final Set<Resource> resources = new HashSet<>();
        final Path projectPath = projects.get(projectId);
        try {

            walkFileTree(projectPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!dir.equals(projectPath)) {
                        final long timestamp = getLastModifiedTime(dir).toMillis();
                        final String relativeResourcePath = projectPath.relativize(dir).toString();

                        resources.add(Resource.newFolder(projectId, relativeResourcePath, timestamp));
                    }

                    return CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    final long timestamp = getLastModifiedTime(file).toMillis();
                    final String relativeResourcePath = projectPath.relativize(file).toString();
                    final byte[] content = readAllBytes(file);

                    resources.add(Resource.newFile(projectId, relativeResourcePath, timestamp, content));

                    return CONTINUE;
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return resources;
    }

    @Override
    public Resource getResource(String projectId, String path) {
        checkNotNull(projectId);
        checkNotNull(path);

        final Path projectPath = projects.get(projectId);
        if (projectPath != null) {
            final Path resourcePath = projectPath.resolve(path);
            if (exists(resourcePath)) {
                try {
                    final boolean isDirectory = isDirectory(resourcePath);
                    final long timestamp = getLastModifiedTime(resourcePath).toMillis();

                    return isDirectory ? Resource.newFolder(projectId, path, timestamp)
                                       : Resource.newFile(projectId, path, timestamp, readAllBytes(resourcePath));

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    @Override
    public void createResource(Resource resource) {
        checkNotNull(resource);

        final Path projectPath = projects.get(resource.projectId());
        if (projectPath != null) {
            final Path resourcePath = projectPath.resolve(resource.path());
            if (!exists(resourcePath)) {
                try {

                    if (resource.type() == FOLDER) {
                        createDirectory(resourcePath);
                        setLastModifiedTime(resourcePath, FileTime.from(resource.timestamp(), MILLISECONDS));
                        watchService.watch(resourcePath);

                    } else if (resource.type() == FILE) {
                        write(resourcePath, resource.content());
                        setLastModifiedTime(resourcePath, FileTime.from(resource.timestamp(), MILLISECONDS));
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void updateResource(Resource resource) {
        checkNotNull(resource);
        checkArgument(resource.type() == FILE);

        final Path projectPath = projects.get(resource.projectId());
        if (projectPath != null) {
            final Path resourcePath = projectPath.resolve(resource.path());
            if (exists(resourcePath)) {
                try {

                    write(resourcePath, resource.content());
                    setLastModifiedTime(resourcePath, FileTime.from(resource.timestamp(), MILLISECONDS));

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void deleteResource(Resource resource) {
        checkNotNull(resource);

        final Path projectPath = projects.get(resource.projectId());
        if (projectPath != null) {
            final Path resourcePath = projectPath.resolve(resource.path());
            if (exists(resourcePath)) {
                try {

                    Files.walkFileTree(resourcePath, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            delete(file);
                            return CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path folder, IOException exc) throws IOException {
                            delete(folder);
                            return CONTINUE;
                        }
                    });

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public RepositoryEventBus repositoryEventBus() {
        return repositoryEventBus;
    }

    public Map<String, Path> projects() {
        return unmodifiableMap(projects);
    }

    @Override
    public RepositoryResourceProvider repositoryResourceProvider() {
        return this;
    }
}
