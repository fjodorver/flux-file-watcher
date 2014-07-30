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
package com.codenvy.flux.watcher.core.spi;

import com.codenvy.flux.watcher.core.utils.ResourceHelper;

import static com.codenvy.flux.watcher.core.spi.Resource.ResourceType.FILE;
import static com.codenvy.flux.watcher.core.spi.Resource.ResourceType.FOLDER;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a resource in a repository.
 *
 * @author Kevin Pollet
 */
public final class Resource {
    private final String       projectId;
    private final String       path;
    private final long         timestamp;
    private final String       hash;
    private final ResourceType type;
    private final byte[]       content;

    /**
     * Constructs an instance of {@link com.codenvy.flux.watcher.core.spi.Resource} representing a {@link
     * com.codenvy.flux.watcher.core.spi.Resource.ResourceType#FOLDER}.
     *
     * @param projectId
     *         the project id this {@link com.codenvy.flux.watcher.core.spi.Resource} belongs to.
     * @param path
     *         the {@link com.codenvy.flux.watcher.core.spi.Resource} relative path.
     * @param timestamp
     *         the {@link com.codenvy.flux.watcher.core.spi.Resource} timestamp.
     * @return the new {@link com.codenvy.flux.watcher.core.spi.Resource} instance.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} or {@code path} parameter is {@code null}.
     */
    public static Resource newFolder(String projectId, String path, long timestamp) {
        return new Resource(projectId, path, timestamp, FOLDER, new byte[0]);
    }

    /**
     * Constructs an instance of {@link com.codenvy.flux.watcher.core.spi.Resource} representing a {@link
     * com.codenvy.flux.watcher.core.spi.Resource.ResourceType#FILE}.
     *
     * @param projectId
     *         the project id this {@link com.codenvy.flux.watcher.core.spi.Resource} belongs to.
     * @param path
     *         the {@link com.codenvy.flux.watcher.core.spi.Resource} relative path.
     * @param timestamp
     *         the {@link com.codenvy.flux.watcher.core.spi.Resource} timestamp.
     * @param content
     *         the {@link com.codenvy.flux.watcher.core.spi.Resource} content.
     * @return the new {@link com.codenvy.flux.watcher.core.spi.Resource} instance.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} or {@code path} parameter is {@code null}.
     */
    public static Resource newFile(String projectId, String path, long timestamp, byte[] content) {
        return new Resource(projectId, path, timestamp, FILE, content);
    }

    /**
     * Constructs an instance of {@link com.codenvy.flux.watcher.core.spi.Resource}.
     *
     * @param projectId
     *         the project id this {@link com.codenvy.flux.watcher.core.spi.Resource} belongs to.
     * @param path
     *         the {@link com.codenvy.flux.watcher.core.spi.Resource} relative path.
     * @param timestamp
     *         the {@link com.codenvy.flux.watcher.core.spi.Resource} timestamp.
     * @param type
     *         the {@link com.codenvy.flux.watcher.core.spi.Resource} {@link com.codenvy.flux.watcher.core.spi.Resource.ResourceType}.
     * @param content
     *         the {@link com.codenvy.flux.watcher.core.spi.Resource} content.
     * @throws java.lang.NullPointerException
     *         if {@code projectId}, {@code path}, {@code type} or {@code content} parameter is {@code null}.
     */
    private Resource(String projectId, String path, long timestamp, ResourceType type, byte[] content) {
        this.projectId = checkNotNull(projectId);
        this.path = checkNotNull(path);
        this.timestamp = timestamp;
        this.type = checkNotNull(type);
        this.content = checkNotNull(content);
        this.hash = ResourceHelper.sha1Hash(type, content);
    }

    /**
     * Returns the project id this {@link com.codenvy.flux.watcher.core.spi.Resource}.
     *
     * @return this {@link com.codenvy.flux.watcher.core.spi.Resource} project id, never {@code null}.
     */
    public String projectId() {
        return projectId;
    }

    /**
     * Returns the relative path of this {@link com.codenvy.flux.watcher.core.spi.Resource}.
     *
     * @return this {@link com.codenvy.flux.watcher.core.spi.Resource} relative path, never {@code null}.
     */
    public String path() {
        return path;
    }

    /**
     * Returns the timestamp of this {@link com.codenvy.flux.watcher.core.spi.Resource}.
     *
     * @return this {@link com.codenvy.flux.watcher.core.spi.Resource} timestamp.
     */
    public long timestamp() {
        return timestamp;
    }

    /**
     * Returns the type hash this {@link com.codenvy.flux.watcher.core.spi.Resource}.
     *
     * @return this {@link com.codenvy.flux.watcher.core.spi.Resource} hash, never {@code null}.
     */
    public String hash() {
        return hash;
    }

    /**
     * Returns the type of this {@link com.codenvy.flux.watcher.core.spi.Resource}.
     *
     * @return this {@link com.codenvy.flux.watcher.core.spi.Resource} type, never {@code null}.
     */
    public ResourceType type() {
        return type;
    }

    /**
     * Returns the content of this {@link com.codenvy.flux.watcher.core.spi.Resource}.
     *
     * @return this {@link com.codenvy.flux.watcher.core.spi.Resource} content, never {@code null}.
     */
    public byte[] content() {
        return content;
    }

    /**
     * The {@link com.codenvy.flux.watcher.core.spi.Resource} type.
     */
    public enum ResourceType {
        FILE,
        FOLDER
    }
}
