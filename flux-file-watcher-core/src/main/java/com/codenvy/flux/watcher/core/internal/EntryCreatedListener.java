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

import com.codenvy.flux.watcher.core.FluxConnector;
import com.codenvy.flux.watcher.core.Message;
import com.codenvy.flux.watcher.core.RepositoryEvent;
import com.codenvy.flux.watcher.core.RepositoryEventTypes;
import com.codenvy.flux.watcher.core.RepositoryListener;
import com.codenvy.flux.watcher.core.Resource;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.codenvy.flux.watcher.core.Message.Fields.PROJECT_NAME;
import static com.codenvy.flux.watcher.core.Message.Fields.RESOURCE_HASH;
import static com.codenvy.flux.watcher.core.Message.Fields.RESOURCE_PATH;
import static com.codenvy.flux.watcher.core.Message.Fields.RESOURCE_TIMESTAMP;
import static com.codenvy.flux.watcher.core.Message.Fields.RESOURCE_TYPE;
import static com.codenvy.flux.watcher.core.MessageType.RESOURCE_CREATED;
import static com.codenvy.flux.watcher.core.RepositoryEventType.ENTRY_CREATED;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listener sending a message to flux connections when a resource is created in the repository.
 *
 * @author Kevin Pollet
 */
@Singleton
@RepositoryEventTypes(ENTRY_CREATED)
public class EntryCreatedListener implements RepositoryListener {
    private final FluxConnector fluxConnector;

    /**
     * Constructs an instance of {@code EntryCreatedListener}.
     *
     * @param fluxConnector
     *         the {@link com.codenvy.flux.watcher.core.FluxConnector}.
     * @throws java.lang.NullPointerException
     *         if {@code fluxConnector} parameter is {@code null}.
     */
    @Inject
    public EntryCreatedListener(FluxConnector fluxConnector) {
        this.fluxConnector = checkNotNull(fluxConnector);
    }

    @Override
    public void onEvent(RepositoryEvent event) {
        try {

            final Resource createdResource = event.resource();

            final JSONObject message = new JSONObject();
            message.put(PROJECT_NAME.value(), createdResource.projectId());
            message.put(RESOURCE_PATH.value(), createdResource.path());
            message.put(RESOURCE_TIMESTAMP.value(), createdResource.timestamp());
            message.put(RESOURCE_HASH.value(), createdResource.hash());
            message.put(RESOURCE_TYPE.value(), createdResource.type().name().toLowerCase());

            // broadcast message to all connections
            fluxConnector.broadcastMessage(new Message(RESOURCE_CREATED, message));

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
