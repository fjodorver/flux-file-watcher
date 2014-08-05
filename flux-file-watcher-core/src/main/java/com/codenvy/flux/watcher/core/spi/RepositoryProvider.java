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

import com.codenvy.flux.watcher.core.RepositoryListener;
import com.codenvy.flux.watcher.core.Resource;

import java.util.Set;

/**
 * Interface implemented by a repository.
 *
 * @author Kevin Pollet
 */
public interface RepositoryProvider {
    /**
     * Returns if the repository contains the given project.
     *
     * @param projectId
     *         the project id.
     * @return {@code true} if the repository contains the given project, {@code false} otherwise.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} parameter is {@code null}.
     */
    boolean isProject(String projectId);

    /**
     * Add a project to the repository implementation.
     *
     * @param projectId
     *         the project id.
     * @param projectPath
     *         the absolute project projectPath.
     * @return {@code true} if project was not already added and projectPath exists, {@code false} otherwise.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} or {@code projectPath} parameter is {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if the given {@code projectPath} is not a directory, doesn't exist or is not absolute.
     */
    boolean addProject(String projectId, String projectPath);

    /**
     * Remove a project from the repository implementation.
     *
     * @param projectId
     *         the project id.
     * @return {@code true} if project exists, {@code false} otherwise.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} parameter is {@code null}.
     */
    boolean removeProject(String projectId);

    /**
     * Returns all {@link com.codenvy.flux.watcher.core.Resource} of the given project.
     *
     * @param projectId
     *         the project id.
     * @return the {@link com.codenvy.flux.watcher.core.Resource} {@link java.util.Set}, never {@code null}.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} parameter is {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if project doesn't exist.
     */
    Set<Resource> getProjectResources(String projectId);

    /**
     * Returns the {@link com.codenvy.flux.watcher.core.Resource} in the given project with the given relative path.
     *
     * @param projectId
     *         the project id.
     * @param path
     *         the {@link com.codenvy.flux.watcher.core.Resource} relative path.
     * @return the {@link com.codenvy.flux.watcher.core.Resource} or {@code null} if not found.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} or {@code path} parameter is {@code null}.
     */
    Resource getResource(String projectId, String path);

    /**
     * Creates the given {@link com.codenvy.flux.watcher.core.Resource}.
     *
     * @param resource
     *         the {@link com.codenvy.flux.watcher.core.Resource} to be created.
     * @throws java.lang.NullPointerException
     *         if {@code resource} parameter is {@code null}.
     */
    void createResource(Resource resource);

    /**
     * Updates the given {@link com.codenvy.flux.watcher.core.Resource}.
     *
     * @param resource
     *         the {@link com.codenvy.flux.watcher.core.Resource} to be updated.
     * @throws java.lang.NullPointerException
     *         if {@code resource} parameter is {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if {@code resource} parameter is not a file.
     */
    void updateResource(Resource resource);

    /**
     * Deletes the given {@link com.codenvy.flux.watcher.core.Resource}.
     *
     * @param resource
     *         the {@link com.codenvy.flux.watcher.core.Resource} to be deleted.
     * @throws java.lang.NullPointerException
     *         if {@code resource} parameter is {@code null}.
     */
    void deleteResource(Resource resource);

    /**
     * Adds a {@link com.codenvy.flux.watcher.core.RepositoryListener}.
     *
     * @param listener
     *         the {@link com.codenvy.flux.watcher.core.RepositoryListener} to add.
     * @return {@code true} if the listener was not already added, {@code false} otherwise.
     * @throws java.lang.NullPointerException
     *         if {@code listener} parameter is {@code null}.
     */
    boolean addRepositoryListener(RepositoryListener listener);

    /**
     * Removes a {@link com.codenvy.flux.watcher.core.RepositoryListener}.
     *
     * @param listener
     *         the {@link com.codenvy.flux.watcher.core.RepositoryListener} to remove.
     * @return {@code true} if the listener exists, {@code false} otherwise.
     * @throws java.lang.NullPointerException
     *         if {@code listener} parameter is {@code null}.
     */
    boolean removeRepositoryListener(RepositoryListener listener);

    /**
     * Unwrap the {@link com.codenvy.flux.watcher.core.spi.RepositoryProvider} to access implementation specific methods.
     *
     * @param clazz
     *         the type to unwrap to.
     * @param <T>
     *         the type to unwrap to.
     * @return the unwrapped {@link com.codenvy.flux.watcher.core.spi.RepositoryProvider} implementation.
     * @throws java.lang.NullPointerException
     *         if {@code clazz} parameter is {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if the {@link com.codenvy.flux.watcher.core.spi.RepositoryProvider} cannot be unwrapped to the given type.
     */
    <T> T unwrap(Class<T> clazz);
}
