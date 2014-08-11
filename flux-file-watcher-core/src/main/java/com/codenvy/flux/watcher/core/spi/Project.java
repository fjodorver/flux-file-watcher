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

import com.codenvy.flux.watcher.core.Resource;

import java.util.Set;

/**
 * Project contract implemented by a provider.
 *
 * @author Kevin Pollet
 * @see com.codenvy.flux.watcher.core.spi.ProjectFactory
 */
public interface Project {
    /**
     * Returns the project unique id.
     *
     * @return the project unique id, never {@code null}.
     */
    String id();

    /**
     * Returns the project absolute path.
     *
     * @return the project absolute path, never {@code null}.
     */
    String path();

    /**
     * Watch project {@link com.codenvy.flux.watcher.core.Resource} modifications. The project {@link
     * com.codenvy.flux.watcher.core.Resource} modifications have to be sent with the {@link com.codenvy.flux.watcher.core.RepositoryEventBus}.
     */
    void watch();

    /**
     * Unwatch project {@link com.codenvy.flux.watcher.core.Resource} modifications.
     */
    void unwatch();

    /**
     * Returns all project {@link com.codenvy.flux.watcher.core.Resource}.
     *
     * @return the {@link com.codenvy.flux.watcher.core.Resource} {@link java.util.Set}, never {@code null}.
     */
    Set<Resource> getResources();

    /**
     * Returns the {@link com.codenvy.flux.watcher.core.Resource} in with the given relative resourcePath.
     *
     * @param resourcePath
     *         the {@link com.codenvy.flux.watcher.core.Resource} relative resourcePath.
     * @return the {@link com.codenvy.flux.watcher.core.Resource} or {@code null} if not found.
     * @throws java.lang.NullPointerException
     *         if {@code resourcePath} parameter is {@code null}.
     */
    Resource getResource(String resourcePath);

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
}
