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
package com.codenvy.flux.watcher.core.internal;

import com.codenvy.flux.watcher.core.FluxConnectionManager;
import com.codenvy.flux.watcher.core.FluxCredentials;
import com.codenvy.flux.watcher.core.Message;
import com.codenvy.flux.watcher.core.spi.RepositoryEvent;
import com.codenvy.flux.watcher.core.spi.RepositoryEventTypes;
import com.codenvy.flux.watcher.core.spi.RepositoryListener;
import com.codenvy.flux.watcher.core.spi.Resource;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.codenvy.flux.watcher.core.MessageFields.PROJECT_NAME;
import static com.codenvy.flux.watcher.core.MessageFields.RESOURCE_HASH;
import static com.codenvy.flux.watcher.core.MessageFields.RESOURCE_PATH;
import static com.codenvy.flux.watcher.core.MessageFields.RESOURCE_TIMESTAMP;
import static com.codenvy.flux.watcher.core.MessageFields.RESOURCE_TYPE;
import static com.codenvy.flux.watcher.core.MessageFields.USERNAME;
import static com.codenvy.flux.watcher.core.MessageType.RESOURCE_CREATED;
import static com.codenvy.flux.watcher.core.spi.RepositoryEventType.ENTRY_CREATED;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listener sending a message to flux connections when a resource is created in the repository.
 *
 * @author Kevin Pollet
 */
@Singleton
@RepositoryEventTypes(ENTRY_CREATED)
public final class EntryCreatedListener implements RepositoryListener {
    private final FluxCredentials       credentials;
    private final FluxConnectionManager connectionManager;

    /**
     * Constructs an instance of {@code EntryCreatedListener}.
     *
     * @param credentials
     *         the {@link com.codenvy.flux.watcher.core.FluxCredentials}.
     * @param connectionManager
     *         the {@link com.codenvy.flux.watcher.core.FluxConnectionManager}.
     * @throws java.lang.NullPointerException
     *         if {@code credentials} or {@code connectionManager} parameter is {@code null}.
     */
    @Inject
    public EntryCreatedListener(FluxCredentials credentials, FluxConnectionManager connectionManager) {
        this.credentials = checkNotNull(credentials);
        this.connectionManager = checkNotNull(connectionManager);
    }

    @Override
    public void onEvent(RepositoryEvent event) {
        try {

            final Resource createdResource = event.resource();

            final JSONObject message = new JSONObject();
            message.put(USERNAME, credentials.username());
            message.put(PROJECT_NAME, createdResource.projectId());
            message.put(RESOURCE_PATH, createdResource.path());
            message.put(RESOURCE_TIMESTAMP, createdResource.timestamp());
            message.put(RESOURCE_HASH, createdResource.hash());
            message.put(RESOURCE_TYPE, createdResource.type().name().toLowerCase());

            // broadcast message to all connections
            connectionManager.broadcastMessage(new Message(RESOURCE_CREATED, message));

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
