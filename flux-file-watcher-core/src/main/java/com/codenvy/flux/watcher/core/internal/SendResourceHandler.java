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

import com.codenvy.flux.watcher.core.Message;
import com.codenvy.flux.watcher.core.MessageHandler;
import com.codenvy.flux.watcher.core.MessageTypes;
import com.codenvy.flux.watcher.core.Resource;
import com.codenvy.flux.watcher.core.spi.RepositoryProvider;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.codenvy.flux.watcher.core.Message.Fields.CALLBACK_ID;
import static com.codenvy.flux.watcher.core.Message.Fields.PROJECT_NAME;
import static com.codenvy.flux.watcher.core.Message.Fields.REQUEST_SENDER_ID;
import static com.codenvy.flux.watcher.core.Message.Fields.RESOURCE_CONTENT;
import static com.codenvy.flux.watcher.core.Message.Fields.RESOURCE_HASH;
import static com.codenvy.flux.watcher.core.Message.Fields.RESOURCE_PATH;
import static com.codenvy.flux.watcher.core.Message.Fields.RESOURCE_TIMESTAMP;
import static com.codenvy.flux.watcher.core.Message.Fields.RESOURCE_TYPE;
import static com.codenvy.flux.watcher.core.MessageType.GET_PROJECT_RESPONSE;
import static com.codenvy.flux.watcher.core.MessageType.GET_RESOURCE_REQUEST;
import static com.codenvy.flux.watcher.core.Resource.ResourceType.FILE;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handler replying to a {@link com.codenvy.flux.watcher.core.MessageType#GET_RESOURCE_REQUEST}.
 *
 * @author Kevin Pollet
 */
@Singleton
@MessageTypes(GET_RESOURCE_REQUEST)
public class SendResourceHandler implements MessageHandler {
    private final RepositoryProvider repositoryProvider;

    /**
     * Constructs an instance of {@link com.codenvy.flux.watcher.core.internal.SendResourceHandler}.
     *
     * @param repositoryProvider
     *         the {@link com.codenvy.flux.watcher.core.spi.RepositoryProvider}.
     * @throws java.lang.NullPointerException
     *         if {@code repositoryProvider} parameter is {@code null}.
     */
    @Inject
    public SendResourceHandler(RepositoryProvider repositoryProvider) {
        this.repositoryProvider = checkNotNull(repositoryProvider);
    }

    @Override
    public void onMessage(Message message) {
        try {

            final JSONObject request = message.content();
            final int callbackID = request.getInt(CALLBACK_ID.value());
            final String requestSenderID = request.getString(REQUEST_SENDER_ID.value());
            final String projectName = request.getString(PROJECT_NAME.value());
            final String resourcePath = request.getString(RESOURCE_PATH.value());

            // we ask the repository to retrieve the resource
            final Resource resource = repositoryProvider.getResource(projectName, resourcePath);

            // we send the resource only if the timestamp are equals or no timestamp is specified
            if (!request.has(RESOURCE_TIMESTAMP.value()) || request.getLong(RESOURCE_TIMESTAMP.value()) == resource.timestamp()) {
                final JSONObject response = new JSONObject();
                response.put(CALLBACK_ID.value(), callbackID);
                response.put(REQUEST_SENDER_ID.value(), requestSenderID);
                response.put(PROJECT_NAME.value(), projectName);
                response.put(RESOURCE_PATH.value(), resourcePath);
                response.put(RESOURCE_TIMESTAMP.value(), resource.timestamp());
                response.put(RESOURCE_HASH.value(), resource.hash());
                response.put(RESOURCE_TYPE.value(), resource.type().name().toLowerCase());
                if (resource.type() == FILE) {
                    response.put(RESOURCE_CONTENT.value(), new String(resource.content()));
                }

                message.source()
                       .sendMessage(new Message(GET_PROJECT_RESPONSE, response));
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
