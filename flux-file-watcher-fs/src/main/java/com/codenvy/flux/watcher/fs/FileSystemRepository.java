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

import com.codenvy.flux.watcher.core.spi.RepositoryEvent;
import com.codenvy.flux.watcher.core.spi.RepositoryEventTypes;
import com.codenvy.flux.watcher.core.spi.RepositoryListener;
import com.codenvy.flux.watcher.core.spi.RepositoryProvider;
import com.codenvy.flux.watcher.core.spi.Resource;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.codenvy.flux.watcher.core.spi.Resource.ResourceType.FOLDER;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.setLastModifiedTime;
import static java.nio.file.Files.write;
import static java.util.Collections.unmodifiableMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * {@link com.codenvy.flux.watcher.core.spi.RepositoryProvider} implementation.
 *
 * @author Kevin Pollet
 */
@Singleton
public class FileSystemRepository implements RepositoryProvider {
    private final ConcurrentMap<String, Path> projects;
    private final WatchRepositoryService      watchService;
    private final Set<RepositoryListener>     repositoryListeners;

    @Inject
    public FileSystemRepository(Set<RepositoryListener> repositoryListeners) {
        this.projects = new ConcurrentHashMap<>();
        this.watchService = new WatchRepositoryService(this);
        this.repositoryListeners = new CopyOnWriteArraySet<>(repositoryListeners);

        // start the watch service
        this.watchService.start();
    }

    @Override
    public boolean addProject(String projectId, String path) {
        checkNotNull(projectId);
        checkNotNull(path);

        final Path projectPath = Paths.get(path);
        checkArgument(Files.isDirectory(projectPath));

        if (exists(projectPath)) {
            final Path previousProjectPath = projects.putIfAbsent(projectId, projectPath);
            if (previousProjectPath == null) {
                watchService.watch(projectPath);
                return true;
            }
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
    public Resource getResource(String projectId, String path) {
        checkNotNull(projectId);
        checkNotNull(path);

        final Path projectPath = projects.get(projectId);
        if (projectPath != null) {
            final Path resourcePath = projectPath.resolve(path);
            if (exists(resourcePath)) {
                try {
                    final boolean isDirectory = Files.isDirectory(resourcePath);
                    final long timestamp = Files.getLastModifiedTime(resourcePath).toMillis();

                    return isDirectory ? Resource.newFolder(projectId, path, timestamp)
                                       : Resource.newFile(projectId, path, timestamp, Files.readAllBytes(resourcePath));

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
            Path resourcePath = projectPath.resolve(resource.path());
            if (!exists(resourcePath)) {
                try {

                    if (resource.type() == FOLDER) {
                        resourcePath = createDirectory(resourcePath);
                    } else {
                        resourcePath = createFile(resourcePath);
                        write(resourcePath, resource.content());
                    }

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
    public boolean addRepositoryListener(RepositoryListener listener) {
        checkNotNull(listener);
        return repositoryListeners.add(listener);
    }

    @Override
    public boolean removeRepositoryListener(RepositoryListener listener) {
        checkNotNull(listener);
        return repositoryListeners.remove(listener);
    }

    public Map<String, Path> projects() {
        return unmodifiableMap(projects);
    }

    void fireRepositoryEvent(final RepositoryEvent event) {
        final Set<RepositoryListener> filteredRepositoryListeners = FluentIterable
                .from(repositoryListeners)
                .filter(new Predicate<RepositoryListener>() {
                    @Override
                    public boolean apply(@Nullable RepositoryListener listener) {
                        if (listener == null) {
                            return false;
                        }
                        final RepositoryEventTypes repositoryEventTypes = listener.getClass().getAnnotation(RepositoryEventTypes.class);
                        return Arrays.asList(repositoryEventTypes.value()).contains(event.type());
                    }
                })
                .toImmutableSet();

        for (RepositoryListener oneRepositoryListener : filteredRepositoryListeners) {
            oneRepositoryListener.onEvent(event);
        }
    }
}
