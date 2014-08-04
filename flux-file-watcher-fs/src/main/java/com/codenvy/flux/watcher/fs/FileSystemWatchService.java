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
     *         if the given {@code path} is a file or cannot be found.
     */
    public void watch(Path path) {
        checkNotNull(path);
        checkArgument(exists(path) && isDirectory(path));

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
     *         if the given {@code path} is a file or cannot be found.
     */
    public void unwatch(Path path) {
        checkNotNull(path);
        checkArgument(exists(path) && isDirectory(path));

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
                    final WatchEvent<Path> pathEvent = cast(oneEvent);
                    if (oneEvent.kind() == OVERFLOW) {
                        continue;
                    }

                    if (isDirectory(pathEvent.context()) && oneEvent.kind() == ENTRY_CREATE) {
                        watch(pathEvent.context());
                    }

                    final Path watchablePath = (Path)watchKey.watchable();
                    final RepositoryEventType repositoryEventType = kindToRepositoryEventType(pathEvent.kind());
                    final Resource resource = pathToResource(pathEvent.kind(), watchablePath, pathEvent.context());
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
     * @param watchablePath
     *         the watchable {@link java.nio.file.Path}.
     * @param resourcePath
     *         the {@link java.nio.file.Path} to the {@code watchablePath}.
     * @return the corresponding {@link com.codenvy.flux.watcher.core.Resource} instance or {@code null} if conversion is impossible.
     * @throws java.lang.NullPointerException
     *         if {@code kind}, {@code watchablePath} or {@code resourcePath} parameter is {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if the {@code kind} is not {@link java.nio.file.StandardWatchEventKinds#ENTRY_DELETE} and the resource doesn't exist.
     */
    private Resource pathToResource(Kind<Path> kind, Path watchablePath, Path resourcePath) {
        checkNotNull(kind);
        checkNotNull(watchablePath);
        checkNotNull(resourcePath);

        try {

            final Path absoluteResourcePath = watchablePath.resolve(resourcePath);
            final boolean exists = exists(absoluteResourcePath);
            checkArgument(kind == ENTRY_DELETE || exists);


            final long timestamp = exists ? getLastModifiedTime(absoluteResourcePath).toMillis() : System.currentTimeMillis();

            // TODO better?
            for (Map.Entry<String, Path> oneEntry : repository.projects().entrySet()) {
                final String projectId = oneEntry.getKey();
                final Path projectPath = oneEntry.getValue();

                if (absoluteResourcePath.startsWith(oneEntry.getValue())) {
                    final String relativeProjectResourcePath = projectPath.relativize(absoluteResourcePath).toString();

                    if (exists) {
                        final boolean isDirectory = isDirectory(absoluteResourcePath);
                        return isDirectory ? Resource.newFolder(projectId, relativeProjectResourcePath, timestamp)
                                           : Resource
                                       .newFile(projectId, relativeProjectResourcePath, timestamp, readAllBytes(absoluteResourcePath));

                    } else {
                        return Resource.newUnknown(projectId, relativeProjectResourcePath, timestamp);
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
