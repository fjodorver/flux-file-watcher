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

import com.codenvy.flux.watcher.core.RepositoryEventBus;

/**
 * Contract of a Repository.
 *
 * @author Kevin Pollet
 */
public interface Repository {
    /**
     * Returns whether the repository has the given project.
     *
     * @param projectId
     *         the project id.
     * @return {@code true} if the repository has the given project, {@code false} otherwise.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} parameter is {@code null}.
     */
    boolean hasProject(String projectId);

    /**
     * Add a project to the repository.
     *
     * @param projectId
     *         the project id.
     * @param projectPath
     *         the absolute project path.
     * @return {@code true} if project was not already added and {@code projectPath} exists, {@code false} otherwise.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} or {@code projectPath} parameter is {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if the given {@code projectPath} is not a directory, not absolute or doesn't exist.
     */
    boolean addProject(String projectId, String projectPath);

    /**
     * Remove a project from the repository.
     *
     * @param projectId
     *         the project id.
     * @return {@code true} if project has been removed, {@code false} otherwise.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} parameter is {@code null}.
     */
    boolean removeProject(String projectId);

    /**
     * Returns the {@link com.codenvy.flux.watcher.core.RepositoryEventBus}.
     *
     * @return the {@link com.codenvy.flux.watcher.core.RepositoryEventBus}, never {@code null}
     */
    RepositoryEventBus eventBus();

    /**
     * Returns the {@link com.codenvy.flux.watcher.core.spi.RepositoryResourceProvider}.
     *
     * @return the {@link com.codenvy.flux.watcher.core.spi.RepositoryResourceProvider}, never {@code null}.
     */
    RepositoryResourceProvider repositoryResourceProvider();
}
