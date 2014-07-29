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

import com.codenvy.flux.watcher.core.FluxConnection;
import com.codenvy.flux.watcher.core.FluxRepository;
import com.codenvy.flux.watcher.core.Message;
import com.codenvy.flux.watcher.core.spi.RepositoryEvent;
import com.codenvy.flux.watcher.core.spi.RepositoryEventTypes;
import com.codenvy.flux.watcher.core.spi.RepositoryListener;
import com.codenvy.flux.watcher.core.spi.Resource;

import org.json.JSONException;
import org.json.JSONObject;

import static com.codenvy.flux.watcher.core.MessageFields.PROJECT_NAME;
import static com.codenvy.flux.watcher.core.MessageFields.RESOURCE_HASH;
import static com.codenvy.flux.watcher.core.MessageFields.RESOURCE_PATH;
import static com.codenvy.flux.watcher.core.MessageFields.RESOURCE_TIMESTAMP;
import static com.codenvy.flux.watcher.core.MessageFields.RESOURCE_TYPE;
import static com.codenvy.flux.watcher.core.MessageFields.USERNAME;
import static com.codenvy.flux.watcher.core.MessageType.RESOURCE_CREATED;
import static com.codenvy.flux.watcher.core.spi.RepositoryEventType.ENTRY_CREATED;

/**
 * @author Kevin Pollet
 */
@RepositoryEventTypes(ENTRY_CREATED)
public class EntryCreatedListener implements RepositoryListener {
    private final FluxRepository repository;

    public EntryCreatedListener(FluxRepository repository) {
        this.repository = repository;
    }

    @Override
    public void onEvent(RepositoryEvent event) {
        try {
            final Resource createdResource = event.resource();

            final JSONObject message = new JSONObject();
            message.put(USERNAME, repository.username()); //TODO
            message.put(PROJECT_NAME, createdResource.projectId());
            message.put(RESOURCE_PATH, createdResource.path());
            message.put(RESOURCE_TIMESTAMP, createdResource.timestamp());
            message.put(RESOURCE_HASH, createdResource.hash());
            message.put(RESOURCE_TYPE, createdResource.type().name().toLowerCase());

            // broadcast message to all connections
            for (FluxConnection oneConnection : repository.connections().values()) {
                oneConnection.sendMessage(new Message(RESOURCE_CREATED, message));
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
