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
import com.codenvy.flux.watcher.core.spi.RepositoryProvider;
import com.codenvy.flux.watcher.core.spi.Resource;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.file.Paths;

import static com.codenvy.flux.watcher.core.MessageFields.CALLBACK_ID;
import static com.codenvy.flux.watcher.core.MessageFields.PROJECT_NAME;
import static com.codenvy.flux.watcher.core.MessageFields.REQUEST_SENDER_ID;
import static com.codenvy.flux.watcher.core.MessageFields.RESOURCE_CONTENT;
import static com.codenvy.flux.watcher.core.MessageFields.RESOURCE_HASH;
import static com.codenvy.flux.watcher.core.MessageFields.RESOURCE_PATH;
import static com.codenvy.flux.watcher.core.MessageFields.RESOURCE_TIMESTAMP;
import static com.codenvy.flux.watcher.core.MessageFields.RESOURCE_TYPE;
import static com.codenvy.flux.watcher.core.MessageFields.USERNAME;
import static com.codenvy.flux.watcher.core.MessageType.GET_PROJECT_RESPONSE;
import static com.codenvy.flux.watcher.core.MessageType.GET_RESOURCE_REQUEST;
import static com.codenvy.flux.watcher.core.spi.Resource.ResourceType.FILE;

/**
 * @author Kevin Pollet
 */
//TODO username
//TODO byte must be sent instead of string
@MessageTypes(GET_RESOURCE_REQUEST)
public class SendResourceHandler implements MessageHandler {
    private final FluxRepository repository;

    public SendResourceHandler(FluxRepository repository) {
        this.repository = repository;
    }

    @Override
    public void onMessage(Message message) {
        try {
            final JSONObject request = message.content();
            final String username = request.getString(USERNAME);
            final int callbackID = request.getInt(CALLBACK_ID);
            final String requestSenderID = request.getString(REQUEST_SENDER_ID);
            final String projectName = request.getString(PROJECT_NAME);
            final String resourcePath = request.getString(RESOURCE_PATH);

            // we ask the repository to retrieve the resource
            final RepositoryProvider repository = this.repository.repositoryProvider();
            final Resource resource = repository.getResource(projectName, Paths.get(resourcePath));

            // we send the resource only if the timestamp are equals or no timestamp is specified
            if (!request.has(RESOURCE_TIMESTAMP) || request.getLong(RESOURCE_TIMESTAMP) == resource.timestamp()) {
                final JSONObject response = new JSONObject();
                response.put(CALLBACK_ID, callbackID);
                response.put(REQUEST_SENDER_ID, requestSenderID);
                response.put(USERNAME, username);
                response.put(PROJECT_NAME, projectName);
                response.put(RESOURCE_PATH, resourcePath);
                response.put(RESOURCE_TIMESTAMP, resource.timestamp());
                response.put(RESOURCE_HASH, resource.hash());
                response.put(RESOURCE_TYPE, resource.type().name().toLowerCase());
                if (resource.type() == FILE) {
                    response.put(RESOURCE_CONTENT, new String(resource.content()));
                }

                message.connection()
                       .sendMessage(new Message(GET_PROJECT_RESPONSE, response));
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
