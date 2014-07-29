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

import com.codenvy.flux.FluxRepository;
import com.codenvy.flux.Message;
import com.codenvy.flux.MessageHandler;
import com.codenvy.flux.MessageTypes;
import com.codenvy.flux.spi.RepositoryProvider;
import com.codenvy.flux.spi.Resource;
import com.codenvy.flux.utils.ResourceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.file.Paths;

import static com.codenvy.flux.MessageFields.CALLBACK_ID;
import static com.codenvy.flux.MessageFields.PROJECT_NAME;
import static com.codenvy.flux.MessageFields.REQUEST_SENDER_ID;
import static com.codenvy.flux.MessageFields.RESOURCE_CONTENT;
import static com.codenvy.flux.MessageFields.RESOURCE_HASH;
import static com.codenvy.flux.MessageFields.RESOURCE_PATH;
import static com.codenvy.flux.MessageFields.RESOURCE_TIMESTAMP;
import static com.codenvy.flux.MessageFields.RESOURCE_TYPE;
import static com.codenvy.flux.MessageFields.USERNAME;
import static com.codenvy.flux.MessageType.GET_PROJECT_RESPONSE;
import static com.codenvy.flux.MessageType.GET_RESOURCE_REQUEST;
import static com.codenvy.flux.spi.Resource.ResourceType.FILE;

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
            final RepositoryProvider repositoryProvider = repository.repositoryProvider();
            final Resource resource = repositoryProvider.getResource(projectName, Paths.get(resourcePath));

            // we send the resource only if the timestamp are equals or no timestamp is specified
            if (!request.has(RESOURCE_TIMESTAMP) || request.getLong(RESOURCE_TIMESTAMP) == resource.timestamp()) {
                final JSONObject response = new JSONObject();
                response.put(CALLBACK_ID, callbackID);
                response.put(REQUEST_SENDER_ID, requestSenderID);
                response.put(USERNAME, username);
                response.put(PROJECT_NAME, projectName);
                response.put(RESOURCE_PATH, resourcePath);
                response.put(RESOURCE_TIMESTAMP, resource.timestamp());
                response.put(RESOURCE_HASH, ResourceHelper.hash(resource));
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
