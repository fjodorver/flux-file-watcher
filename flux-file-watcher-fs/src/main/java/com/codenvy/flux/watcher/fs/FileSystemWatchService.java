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

import com.codenvy.flux.watcher.core.RepositoryEvent;
import com.codenvy.flux.watcher.core.RepositoryEventBus;
import com.codenvy.flux.watcher.core.RepositoryEventType;
import com.codenvy.flux.watcher.core.Resource;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import static com.codenvy.flux.watcher.core.RepositoryEventType.ENTRY_CREATED;
import static com.codenvy.flux.watcher.core.RepositoryEventType.ENTRY_DELETED;
import static com.codenvy.flux.watcher.core.RepositoryEventType.ENTRY_MODIFIED;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.nio.file.WatchEvent.Kind;

/**
 * Thread watching the file system to notify clients about repository modifications.
 *
 * @author Kevin Pollet
 */
public class FileSystemWatchService extends Thread {
    private final WatchService          watchService;
    private final BiMap<WatchKey, Path> watchKeys;
    private final Object                watchKeysMutex;
    private final FileSystemRepository  repository;
    private final RepositoryEventBus    repositoryEventBus;

    /**
     * Constructs an instance of {@link FileSystemWatchService}.
     *
     * @param fileSystem
     *         the {@link java.nio.file.FileSystem} to watch.
     * @param repository
     *         the {@link FileSystemRepository} instance.
     * @param repositoryEventBus
     *         the {@link com.codenvy.flux.watcher.core.RepositoryEvent} bus.
     * @throws java.lang.NullPointerException
     *         if {@code repository} parameter is {@code null}.
     */
    FileSystemWatchService(FileSystem fileSystem, FileSystemRepository repository, RepositoryEventBus repositoryEventBus) {
        this.repository = checkNotNull(repository);
        this.watchKeys = HashBiMap.create();
        this.watchKeysMutex = new Object();
        this.repositoryEventBus = repositoryEventBus;

        try {

            this.watchService = fileSystem.newWatchService();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Watch the given {@link java.nio.file.Path} and all its sub-directories.
     *
     * @param path
     *         the {@link java.nio.file.Path} to watch.
     * @throws java.lang.NullPointerException
     *         if {@code path} parameter is {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if the given {@code path} is a file, cannot be found or is not absolute.
     */
    public void watch(Path path) {
        checkNotNull(path);
        checkArgument(exists(path) && isDirectory(path) && path.isAbsolute());

        synchronized (watchKeysMutex) {
            try {

                walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        if (!watchKeys.containsValue(dir)) {
                            final WatchKey watchKey = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                            watchKeys.put(watchKey, dir);
                        }
                        return CONTINUE;
                    }
                });

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Unwatch the given {@link java.nio.file.Path} and all its sub-directories.
     *
     * @param path
     *         the {@link java.nio.file.Path} to watch.
     * @throws java.lang.NullPointerException
     *         if {@code path} parameter is {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if the given {@code path} is a file, cannot be found or is not absolute.
     */
    public void unwatch(Path path) {
        checkNotNull(path);
        checkArgument(exists(path) && isDirectory(path) && path.isAbsolute());

        synchronized (watchKeysMutex) {
            try {

                walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        if (!watchKeys.containsValue(dir)) {
                            final WatchKey watchKey = watchKeys.inverse().remove(dir);
                            if (watchKey != null) {
                                watchKey.cancel();
                            }
                        }
                        return CONTINUE;
                    }
                });

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Process all events for keys queued to the watcher.
     */
    @Override
    public void run() {
        while (!isInterrupted()) {
            try {

                final WatchKey watchKey = watchService.take();
                synchronized (watchKeysMutex) {
                    if (!watchKeys.containsKey(watchKey)) {
                        continue;
                    }
                }

                for (WatchEvent<?> oneEvent : watchKey.pollEvents()) {
                    final Path watchablePath = (Path)watchKey.watchable();
                    final WatchEvent<Path> pathEvent = cast(oneEvent);
                    final Path resourcePath = watchablePath.resolve(pathEvent.context());

                    if (oneEvent.kind() == OVERFLOW) {
                        continue;
                    }

                    if (oneEvent.kind() == ENTRY_CREATE && isDirectory(resourcePath)) {
                        watch(resourcePath);
                    }

                    final RepositoryEventType repositoryEventType = kindToRepositoryEventType(pathEvent.kind());
                    final Resource resource = pathToResource(pathEvent.kind(), resourcePath);
                    if (repositoryEventType != null && resource != null) {
                        repositoryEventBus.fireRepositoryEvent(new RepositoryEvent(repositoryEventType, resource));
                    }
                }

                final boolean isValid = watchKey.reset();
                if (!isValid) {
                    synchronized (watchKeysMutex) {
                        watchKeys.remove(watchKey);
                    }
                }

            } catch (ClosedWatchServiceException | InterruptedException e) {
                return;
            }
        }
    }

    /**
     * Converts a {@link java.nio.file.WatchEvent} {@link java.nio.file.WatchEvent.Kind} to a {@link
     * com.codenvy.flux.watcher.core.RepositoryEventType}.
     *
     * @param kind
     *         the {@link java.nio.file.WatchEvent.Kind} to convert.
     * @return the corresponding {@link com.codenvy.flux.watcher.core.RepositoryEventType} or {@code null} if none.
     */
    private RepositoryEventType kindToRepositoryEventType(Kind<?> kind) {
        if (kind == ENTRY_CREATE) {
            return ENTRY_CREATED;
        }
        if (kind == ENTRY_MODIFY) {
            return ENTRY_MODIFIED;
        }
        if (kind == ENTRY_DELETE) {
            return ENTRY_DELETED;
        }
        return null;
    }

    /**
     * Converts the given {@link java.nio.file.Path} to a {@link com.codenvy.flux.watcher.core.Resource}.
     *
     * @param kind
     *         the {@link java.nio.file.WatchEvent.Kind}.
     * @param resourcePath
     *         the absolute resource {@link java.nio.file.Path}.
     * @return the corresponding {@link com.codenvy.flux.watcher.core.Resource} instance or {@code null} if conversion is impossible.
     * @throws java.lang.NullPointerException
     *         if {@code kind} or {@code resourcePath} parameter is {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if {@code kind} is not {@link java.nio.file.StandardWatchEventKinds#ENTRY_DELETE} and the resource doesn't exist or
     *         {@code resourcePath} is not absolute.
     */
    private Resource pathToResource(Kind<Path> kind, Path resourcePath) {
        checkNotNull(kind);
        checkNotNull(resourcePath);
        checkArgument(resourcePath.isAbsolute());

        try {

            final boolean exists = exists(resourcePath);
            checkArgument(kind == ENTRY_DELETE || exists);


            final long timestamp = exists ? getLastModifiedTime(resourcePath).toMillis() : System.currentTimeMillis();

            // TODO better?
            for (Map.Entry<String, Path> oneEntry : repository.projects().entrySet()) {
                final String projectId = oneEntry.getKey();
                final Path projectPath = oneEntry.getValue();

                if (resourcePath.startsWith(oneEntry.getValue())) {
                    final String relativeResourcePath = projectPath.relativize(resourcePath).toString();

                    if (exists) {
                        final boolean isDirectory = isDirectory(resourcePath);
                        return isDirectory ? Resource.newFolder(projectId, relativeResourcePath, timestamp)
                                           : Resource.newFile(projectId, relativeResourcePath, timestamp, readAllBytes(resourcePath));

                    } else {
                        return Resource.newUnknown(projectId, relativeResourcePath, timestamp);
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    /**
     * Cast the given {@link java.nio.file.WatchEvent} to a {@link java.nio.file.Path} {@link java.nio.file.WatchEvent}.
     *
     * @param event
     *         the {@link java.nio.file.WatchEvent} to cast.
     * @return the casted {@link java.nio.file.WatchEvent}.
     * @throws java.lang.NullPointerException
     *         if {@code event} parameter is {@code null}.
     */
    @SuppressWarnings("unchecked")
    private WatchEvent<Path> cast(WatchEvent<?> event) {
        return (WatchEvent<Path>)checkNotNull(event);
    }
}
