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
import com.codenvy.flux.watcher.core.Resource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Singleton;

import static com.codenvy.flux.watcher.core.Message.Fields.CALLBACK_ID;
import static com.codenvy.flux.watcher.core.Message.Fields.FILES;
import static com.codenvy.flux.watcher.core.Message.Fields.HASH;
import static com.codenvy.flux.watcher.core.Message.Fields.PATH;
import static com.codenvy.flux.watcher.core.Message.Fields.PROJECT;
import static com.codenvy.flux.watcher.core.Message.Fields.REQUEST_SENDER_ID;
import static com.codenvy.flux.watcher.core.Message.Fields.TIMESTAMP;
import static com.codenvy.flux.watcher.core.Message.Fields.TYPE;
import static com.codenvy.flux.watcher.core.MessageType.GET_PROJECT_REQUEST;
import static com.codenvy.flux.watcher.core.MessageType.GET_PROJECT_RESPONSE;

/**
 * Handler replying to a {@link com.codenvy.flux.watcher.core.MessageType#GET_PROJECT_REQUEST}.
 *
 * @author Kevin Pollet
 */
@Singleton
@MessageTypes(GET_PROJECT_REQUEST)
public class GetProjectRequestHandler implements MessageHandler {
    @Override
    public void onMessage(Message message, FluxRepository repository) {
        try {

            final JSONObject request = message.content();
            final int callbackId = request.getInt(CALLBACK_ID.value());
            final String requestSenderId = request.getString(REQUEST_SENDER_ID.value());
            final String projectName = request.getString(PROJECT.value());

            final JSONArray files = new JSONArray();
            for (Resource oneResource : repository.repositoryResourceProvider().getProjectResources(projectName)) {
                files.put(new JSONObject()
                                  .put(PATH.value(), oneResource.path())
                                  .put(TIMESTAMP.value(), oneResource.timestamp())
                                  .put(HASH.value(), oneResource.hash())
                                  .put(TYPE.value(), oneResource.type().name().toLowerCase()));
            }

            final JSONObject content = new JSONObject()
                    .put(CALLBACK_ID.value(), callbackId)
                    .put(REQUEST_SENDER_ID.value(), requestSenderId)
                    .put(PROJECT.value(), projectName)
                    .put(FILES.value(), files);

            message.source()
                   .sendMessage(new Message(GET_PROJECT_RESPONSE, content));

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
