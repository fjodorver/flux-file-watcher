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

import com.codenvy.flux.watcher.core.spi.RepositoryProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a Flux repository.
 *
 * @author Kevin Pollet
 */
@Singleton
public class FluxRepository {
    private final int                id;
    private final RepositoryProvider repositoryProvider;
    private final FluxConnector      fluxConnector;

    /**
     * Constructs an instance of {@link com.codenvy.flux.watcher.core.FluxRepository}.
     *
     * @param fluxConnector
     *         the {@link com.codenvy.flux.watcher.core.FluxConnector} instance.
     * @param repositoryProvider
     *         the {@link com.codenvy.flux.watcher.core.spi.RepositoryProvider} instance.
     * @throws java.lang.NullPointerException
     *         if {@code fluxConnector} or {@code repositoryProvider} parameter is {@code null}.
     */
    @Inject
    FluxRepository(FluxConnector fluxConnector, RepositoryProvider repositoryProvider) {
        this.id = new Long(UUID.randomUUID().getMostSignificantBits()).intValue();
        this.fluxConnector = checkNotNull(fluxConnector);
        this.repositoryProvider = checkNotNull(repositoryProvider);
    }

    /**
     * Returns the unique id of this {@link com.codenvy.flux.watcher.core.FluxRepository} instance.
     *
     * @return the unique id of this {@link com.codenvy.flux.watcher.core.FluxRepository} instance.
     */
    public int id() {
        return id;
    }

    /**
     * Connects this {@link com.codenvy.flux.watcher.core.FluxRepository} to a Flux server remote.
     *
     * @param serverURL
     *         the server {@link java.net.URL}.
     * @param credentials
     *         the {@link com.codenvy.flux.watcher.core.Credentials} used to connect.
     * @return the opened {@link com.codenvy.flux.watcher.core.FluxConnection}.
     * @throws java.lang.NullPointerException
     *         if {@code serverURL} or {@code credentials} parameter is {@code null}.
     */
    public FluxConnection connect(URL serverURL, Credentials credentials) {
        return fluxConnector.connect(checkNotNull(serverURL), checkNotNull(credentials));
    }

    /**
     * Disconnects this {@link com.codenvy.flux.watcher.core.FluxRepository} from a Flux server remote.
     *
     * @param serverURL
     *         the server {@link java.net.URL}.
     * @throws java.lang.NullPointerException
     *         if {@code serverURL} parameter is {@code null}.
     */
    public void disconnect(URL serverURL) {
        fluxConnector.disconnect(checkNotNull(serverURL));
    }

    /**
     * Adds a project to this {@link com.codenvy.flux.watcher.core.FluxRepository} instance.
     *
     * @param projectId
     *         the project unique id.
     * @param path
     *         the project path.
     * @return {@code true} if project was not already added and path exists, {@code false} otherwise.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} or {@code path} parameter is {@code null}.
     */
    public boolean addProject(String projectId, String path) {
        return repositoryProvider.addProject(checkNotNull(projectId), checkNotNull(path));
    }

    /**
     * Removes a project from this {@link com.codenvy.flux.watcher.core.FluxRepository} instance.
     *
     * @param projectId
     *         the project unique id.
     * @return {@code true} if project was already added, {@code false} otherwise.
     * @throws java.lang.NullPointerException
     *         if {@code projectId} or {@code path} parameter is {@code null}.
     */
    public boolean removeProject(String projectId) {
        return repositoryProvider.removeProject(checkNotNull(projectId));
    }

    /**
     * Returns the underlying {@link com.codenvy.flux.watcher.core.spi.RepositoryProvider} instance.
     *
     * @return the underlying {@link com.codenvy.flux.watcher.core.spi.RepositoryProvider} instance, never {@code null}.
     */
    public RepositoryProvider underlyingRepository() {
        return repositoryProvider;
    }

    /**
     * Returns the {@link com.codenvy.flux.watcher.core.FluxConnector} instance.
     *
     * @return the {@link com.codenvy.flux.watcher.core.FluxConnector} instance, never {@code null}.
     */
    public FluxConnector fluxConnector() {
        return fluxConnector;
    }
}
