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

import com.codenvy.flux.watcher.core.FluxRepository;
import com.codenvy.flux.watcher.core.Message;
import com.codenvy.flux.watcher.core.MessageHandler;
import com.codenvy.flux.watcher.core.MessageTypes;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import static com.codenvy.flux.watcher.core.Message.Fields.CALLBACK_ID;
import static com.codenvy.flux.watcher.core.Message.Fields.HASH;
import static com.codenvy.flux.watcher.core.Message.Fields.PROJECT;
import static com.codenvy.flux.watcher.core.Message.Fields.RESOURCE;
import static com.codenvy.flux.watcher.core.Message.Fields.TIMESTAMP;
import static com.codenvy.flux.watcher.core.MessageType.GET_RESOURCE_REQUEST;
import static com.codenvy.flux.watcher.core.MessageType.RESOURCE_CHANGED;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handler replying to a {@link com.codenvy.flux.watcher.core.MessageType#RESOURCE_CHANGED}.
 *
 * @author Kevin Pollet
 */
@Singleton
@MessageTypes(RESOURCE_CHANGED)
public class ResourceChangedHandler implements MessageHandler {
    private final Provider<FluxRepository> repository;

    /**
     * Constructs an instance of {@link com.codenvy.flux.watcher.core.internal.ResourceChangedHandler}.
     *
     * @param repository
     *         the FluxRepository instance.
     * @throws NullPointerException
     *         if {@code repository} parameter is {@code null}.
     */
    @Inject
    ResourceChangedHandler(Provider<FluxRepository> repository) {
        this.repository = checkNotNull(repository);
    }

    @Override
    public void onMessage(Message message) {
        try {

            final JSONObject request = message.content();
            final String projectName = request.getString(PROJECT.value());
            final String resourcePath = request.getString(RESOURCE.value());
            final long resourceTimestamp = request.getLong(TIMESTAMP.value());
            final String resourceHash = request.getString(HASH.value());

            if (repository.get().hasProject(projectName)) {
                final JSONObject content = new JSONObject()
                        .put(CALLBACK_ID.value(), repository.get().id())
                        .put(PROJECT.value(), projectName)
                        .put(RESOURCE.value(), resourcePath)
                        .put(TIMESTAMP.value(), resourceTimestamp)
                        .put(HASH.value(), resourceHash);

                repository.get()
                          .fluxConnector()
                          .sendMessages(new Message(GET_RESOURCE_REQUEST, content));
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
