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
 * Interface implemented by a {@link com.codenvy.flux.watcher.core.spi.RepositoryResourceProvider}.
 *
 * @author Kevin Pollet
 */
public interface RepositoryResourceProvider {
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
     * Returns the {@link com.codenvy.flux.watcher.core.Resource} in the given project with the given relative resourcePath.
     *
     * @param projectId
     *         the project id.
     * @param resourcePath
     *         the {@link com.codenvy.flux.watcher.core.Resource} relative resourcePath.
     * @return the {@link com.codenvy.flux.watcher.core.Resource} or {@code null} if not found.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} or {@code resourcePath} parameter is {@code null}.
     */
    Resource getResource(String projectId, String resourcePath);

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
