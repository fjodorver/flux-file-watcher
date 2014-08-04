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
package com.codenvy.flux.watcher.core;

import com.codenvy.flux.watcher.core.utils.ResourceHelper;

import static com.codenvy.flux.watcher.core.Resource.ResourceType.FILE;
import static com.codenvy.flux.watcher.core.Resource.ResourceType.FOLDER;
import static com.codenvy.flux.watcher.core.Resource.ResourceType.UNKNOWN;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a resource in a repository.
 *
 * @author Kevin Pollet
 */
public class Resource {
    private final String       projectId;
    private final String       path;
    private final long         timestamp;
    private final String       hash;
    private final ResourceType type;
    private final byte[]       content;

    /**
     * Constructs an instance of {@link Resource} representing a {@link
     * Resource.ResourceType#UNKNOWN}.
     *
     * @param projectId
     *         the project id this {@link Resource} belongs to.
     * @param path
     *         the {@link Resource} relative path.
     * @param timestamp
     *         the {@link Resource} timestamp.
     * @return the new {@link Resource} instance.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} or {@code path} parameter is {@code null}.
     */
    public static Resource newUnknown(String projectId, String path, long timestamp) {
        return new Resource(projectId, path, timestamp, UNKNOWN, new byte[0]);
    }

    /**
     * Constructs an instance of {@link Resource} representing a {@link
     * Resource.ResourceType#FOLDER}.
     *
     * @param projectId
     *         the project id this {@link Resource} belongs to.
     * @param path
     *         the {@link Resource} relative path.
     * @param timestamp
     *         the {@link Resource} timestamp.
     * @return the new {@link Resource} instance.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} or {@code path} parameter is {@code null}.
     */
    public static Resource newFolder(String projectId, String path, long timestamp) {
        return new Resource(projectId, path, timestamp, FOLDER, new byte[0]);
    }

    /**
     * Constructs an instance of {@link Resource} representing a {@link
     * Resource.ResourceType#FILE}.
     *
     * @param projectId
     *         the project id this {@link Resource} belongs to.
     * @param path
     *         the {@link Resource} relative path.
     * @param timestamp
     *         the {@link Resource} timestamp.
     * @param content
     *         the {@link Resource} content.
     * @return the new {@link Resource} instance.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} or {@code path} parameter is {@code null}.
     */
    public static Resource newFile(String projectId, String path, long timestamp, byte[] content) {
        return new Resource(projectId, path, timestamp, FILE, content);
    }

    /**
     * Constructs an instance of {@link Resource}.
     *
     * @param projectId
     *         the project id this {@link Resource} belongs to.
     * @param path
     *         the {@link Resource} relative path.
     * @param timestamp
     *         the {@link Resource} timestamp.
     * @param type
     *         the {@link Resource} {@link Resource.ResourceType}.
     * @param content
     *         the {@link Resource} content.
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
     * Returns the project id this {@link Resource}.
     *
     * @return this {@link Resource} project id, never {@code null}.
     */
    public String projectId() {
        return projectId;
    }

    /**
     * Returns the relative path of this {@link Resource}.
     *
     * @return this {@link Resource} relative path, never {@code null}.
     */
    public String path() {
        return path;
    }

    /**
     * Returns the timestamp of this {@link Resource}.
     *
     * @return this {@link Resource} timestamp.
     */
    public long timestamp() {
        return timestamp;
    }

    /**
     * Returns the type hash this {@link Resource}.
     *
     * @return this {@link Resource} hash, never {@code null}.
     */
    public String hash() {
        return hash;
    }

    /**
     * Returns the type of this {@link Resource}.
     *
     * @return this {@link Resource} type, never {@code null}.
     */
    public ResourceType type() {
        return type;
    }

    /**
     * Returns the content of this {@link Resource}.
     *
     * @return this {@link Resource} content, never {@code null}.
     */
    public byte[] content() {
        return content;
    }

    /**
     * The {@link Resource} type.
     */
    public enum ResourceType {
        FILE,
        FOLDER,
        UNKNOWN
    }
}
