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

/**
 * Interface implemented by a repository.
 *
 * @author Kevin Pollet
 */
//TODO add project and path doesn't exist, behaviour?
public interface RepositoryProvider {
    /**
     * Add a project to the repository implementation.
     *
     * @param projectId
     *         the project id.
     * @param path
     *         the absolute project path.
     * @return {@code true} if project was not already added and path exists, {@code false} otherwise.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} or {@code path} parameter is {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if the given {@code path} is not a directory.
     */
    boolean addProject(String projectId, String path);

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
     * Returns the {@link com.codenvy.flux.watcher.core.spi.Resource} in the given project with the given relative path.
     *
     * @param projectId
     *         the project id.
     * @param path
     *         the {@link com.codenvy.flux.watcher.core.spi.Resource} relative path.
     * @return the {@link com.codenvy.flux.watcher.core.spi.Resource} or {@code null} if not found.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} or {@code path} parameter is {@code null}.
     */
    Resource getResource(String projectId, String path);

    /**
     * Creates the given {@link com.codenvy.flux.watcher.core.spi.Resource}.
     *
     * @param resource
     *         the {@link com.codenvy.flux.watcher.core.spi.Resource} to be created.
     * @throws java.lang.NullPointerException
     *         if {@code resource} parameter is {@code null}.
     */
    void createResource(Resource resource);

    /**
     * Deletes the given {@link com.codenvy.flux.watcher.core.spi.Resource}.
     *
     * @param resource
     *         the {@link com.codenvy.flux.watcher.core.spi.Resource} to be deleted.
     * @throws java.lang.NullPointerException
     *         if {@code resource} parameter is {@code null}.
     */
    void deleteResource(Resource resource);

    /**
     * Adds a {@link com.codenvy.flux.watcher.core.spi.RepositoryListener}.
     *
     * @param listener
     *         the {@link com.codenvy.flux.watcher.core.spi.RepositoryListener} to add.
     * @return {@code true} if the listener was not already added, {@code false} otherwise.
     * @throws java.lang.NullPointerException
     *         if {@code listener} parameter is {@code null}.
     */
    boolean addRepositoryListener(RepositoryListener listener);

    /**
     * Removes a {@link com.codenvy.flux.watcher.core.spi.RepositoryListener}.
     *
     * @param listener
     *         the {@link com.codenvy.flux.watcher.core.spi.RepositoryListener} to remove.
     * @return {@code true} if the listener exists, {@code false} otherwise.
     * @throws java.lang.NullPointerException
     *         if {@code listener} parameter is {@code null}.
     */
    boolean removeRepositoryListener(RepositoryListener listener);
}
