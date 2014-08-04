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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.codenvy.flux.watcher.core.Message.Fields.CALLBACK_ID;
import static com.codenvy.flux.watcher.core.Message.Fields.FILES;
import static com.codenvy.flux.watcher.core.Message.Fields.PROJECT;
import static com.codenvy.flux.watcher.core.Message.Fields.REQUEST_SENDER_ID;
import static com.codenvy.flux.watcher.core.Message.Fields.HASH;
import static com.codenvy.flux.watcher.core.Message.Fields.PATH;
import static com.codenvy.flux.watcher.core.Message.Fields.TIMESTAMP;
import static com.codenvy.flux.watcher.core.Message.Fields.TYPE;
import static com.codenvy.flux.watcher.core.Message.Fields.USERNAME;
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
    private final RepositoryProvider repositoryProvider;

    /**
     * Constructs an instance of {@link com.codenvy.flux.watcher.core.internal.GetProjectRequestHandler}.
     *
     * @param repositoryProvider
     *         the {@link com.codenvy.flux.watcher.core.spi.RepositoryProvider} instance.
     * @throws java.lang.NullPointerException
     *         if {@code repositoryProvider} parameter is {@code null}.
     */
    @Inject
    GetProjectRequestHandler(RepositoryProvider repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    @Override
    public void onMessage(Message message) {
        try {

            final JSONObject request = message.content();
            final int callbackId = request.getInt(CALLBACK_ID.value());
            final String requestSenderId = request.getString(REQUEST_SENDER_ID.value());
            final String projectName = request.getString(PROJECT.value());
            final String username = request.getString(USERNAME.value());


            final JSONArray files = new JSONArray();
            for (Resource oneResource : repositoryProvider.getProjectResources(projectName)) {
                files.put(new JSONObject()
                                  .put(PATH.value(), oneResource.path())
                                  .put(TIMESTAMP.value(), oneResource.timestamp())
                                  .put(HASH.value(), oneResource.hash())
                                  .put(TYPE.value(), oneResource.type().name().toLowerCase()));
            }

            final JSONObject content = new JSONObject()
                    .put(CALLBACK_ID.value(), callbackId)
                    .put(REQUEST_SENDER_ID.value(), requestSenderId)
                    .put(USERNAME.value(), username)
                    .put(PROJECT.value(), projectName)
                    .put(FILES.value(), files);

            message.source()
                   .sendMessage(new Message(GET_PROJECT_RESPONSE, content));

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
