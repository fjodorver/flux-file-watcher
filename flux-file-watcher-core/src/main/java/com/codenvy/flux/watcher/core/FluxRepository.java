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

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.UUID;

import static com.codenvy.flux.watcher.core.Message.Fields.PROJECT;
import static com.codenvy.flux.watcher.core.MessageType.PROJECT_CONNECTED;
import static com.codenvy.flux.watcher.core.MessageType.PROJECT_DISCONNECTED;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a Flux repository which adds Flux connectivity capabilities to a regular repository.
 *
 * @author Kevin Pollet
 */
@Singleton
public class FluxRepository implements Repository {
    private final int                id;
    private final RepositoryProvider repository;
    private final FluxConnector      fluxConnector;

    /**
     * Constructs an instance of {@link com.codenvy.flux.watcher.core.FluxRepository}.
     *
     * @param fluxConnector
     *         the {@link com.codenvy.flux.watcher.core.FluxConnector} instance.
     * @param repository
     *         the {@link com.codenvy.flux.watcher.core.spi.RepositoryProvider} instance.
     * @throws java.lang.NullPointerException
     *         if {@code fluxConnector} or {@code repository} parameter is {@code null}.
     */
    @Inject
    FluxRepository(FluxConnector fluxConnector, RepositoryProvider repository) {
        this.id = new Long(UUID.randomUUID().getMostSignificantBits()).intValue();
        this.fluxConnector = checkNotNull(fluxConnector);
        this.repository = checkNotNull(repository);
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

    @Override
    public boolean hasProject(String projectId) {
        return repository.hasProject(projectId);
    }

    @Override
    public boolean addProject(String projectId, String projectPath) {
        final boolean isAdded = repository.addProject(projectId, projectPath);
        if (isAdded) {
            try {

                final JSONObject content = new JSONObject().put(PROJECT.value(), projectId);
                fluxConnector.broadcastMessage(new Message(PROJECT_CONNECTED, content));

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return isAdded;
    }

    @Override
    public boolean removeProject(String projectId) {
        final boolean isRemoved = repository.removeProject(projectId);
        if (isRemoved) {
            try {

                final JSONObject content = new JSONObject().put(PROJECT.value(), projectId);
                fluxConnector.broadcastMessage(new Message(PROJECT_DISCONNECTED, content));

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return isRemoved;
    }

    /**
     * Returns the underlying {@link com.codenvy.flux.watcher.core.spi.RepositoryProvider} instance.
     *
     * @return the underlying {@link com.codenvy.flux.watcher.core.spi.RepositoryProvider} instance, never {@code null}.
     */
    public RepositoryProvider underlyingRepository() {
        return repository;
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
