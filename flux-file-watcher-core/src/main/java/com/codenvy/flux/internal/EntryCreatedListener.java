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
package com.codenvy.flux.internal;

import com.codenvy.flux.FluxConnection;
import com.codenvy.flux.FluxRepository;
import com.codenvy.flux.Message;
import com.codenvy.flux.MessageType;
import com.codenvy.flux.spi.RepositoryEvent;
import com.codenvy.flux.spi.RepositoryEventTypes;
import com.codenvy.flux.spi.RepositoryListener;
import com.codenvy.flux.spi.Resource;

import org.json.JSONException;
import org.json.JSONObject;

import static com.codenvy.flux.MessageFields.PROJECT_NAME;
import static com.codenvy.flux.MessageFields.RESOURCE_HASH;
import static com.codenvy.flux.MessageFields.RESOURCE_PATH;
import static com.codenvy.flux.MessageFields.RESOURCE_TIMESTAMP;
import static com.codenvy.flux.MessageFields.RESOURCE_TYPE;
import static com.codenvy.flux.MessageFields.USERNAME;
import static com.codenvy.flux.MessageType.RESOURCE_CREATED;
import static com.codenvy.flux.spi.RepositoryEventType.ENTRY_CREATED;

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
            message.put(RESOURCE_PATH, createdResource.path().toString());
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
