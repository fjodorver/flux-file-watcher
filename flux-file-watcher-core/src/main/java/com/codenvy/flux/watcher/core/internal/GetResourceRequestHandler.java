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

import com.codenvy.flux.watcher.core.FluxMessage;
import com.codenvy.flux.watcher.core.FluxMessageHandler;
import com.codenvy.flux.watcher.core.FluxMessageTypes;
import com.codenvy.flux.watcher.core.FluxRepository;
import com.codenvy.flux.watcher.core.Resource;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Singleton;

import static com.codenvy.flux.watcher.core.FluxMessage.Fields.CALLBACK_ID;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.CONTENT;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.HASH;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.PROJECT;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.REQUEST_SENDER_ID;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.RESOURCE;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.TIMESTAMP;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.TYPE;
import static com.codenvy.flux.watcher.core.FluxMessageType.GET_RESOURCE_REQUEST;
import static com.codenvy.flux.watcher.core.FluxMessageType.GET_RESOURCE_RESPONSE;
import static com.codenvy.flux.watcher.core.Resource.ResourceType.FILE;

/**
 * Handler replying to a {@link com.codenvy.flux.watcher.core.FluxMessageType#GET_RESOURCE_REQUEST}.
 *
 * @author Kevin Pollet
 */
@Singleton
@FluxMessageTypes(GET_RESOURCE_REQUEST)
public class GetResourceRequestHandler implements FluxMessageHandler {
    @Override
    public void onMessage(FluxMessage message, FluxRepository repository) {
        try {

            final JSONObject request = message.content();
            final int callbackId = request.getInt(CALLBACK_ID.value());
            final String requestSenderId = request.getString(REQUEST_SENDER_ID.value());
            final String projectName = request.getString(PROJECT.value());
            final String resourcePath = request.getString(RESOURCE.value());

            // we ask the repository to retrieve the resource
            final Resource resource = repository.repositoryResourceProvider().getResource(projectName, resourcePath);

            // we send the resource only if the timestamp are equals or no timestamp is specified
            if (!request.has(TIMESTAMP.value()) || request.getLong(TIMESTAMP.value()) == resource.timestamp()) {
                final JSONObject content = new JSONObject()
                        .put(CALLBACK_ID.value(), callbackId)
                        .put(REQUEST_SENDER_ID.value(), requestSenderId)
                        .put(PROJECT.value(), projectName)
                        .put(RESOURCE.value(), resourcePath)
                        .put(TIMESTAMP.value(), resource.timestamp())
                        .put(HASH.value(), resource.hash())
                        .put(TYPE.value(), resource.type().name().toLowerCase());

                if (resource.type() == FILE) {
                    content.put(CONTENT.value(), new String(resource.content()));
                }

                message.source()
                       .sendMessage(new FluxMessage(GET_RESOURCE_RESPONSE, content));
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
