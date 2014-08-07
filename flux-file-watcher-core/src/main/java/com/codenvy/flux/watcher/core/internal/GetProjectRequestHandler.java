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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Singleton;

import static com.codenvy.flux.watcher.core.FluxMessage.Fields.CALLBACK_ID;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.FILES;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.HASH;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.PATH;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.PROJECT;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.REQUEST_SENDER_ID;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.TIMESTAMP;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.TYPE;
import static com.codenvy.flux.watcher.core.FluxMessageType.GET_PROJECT_REQUEST;
import static com.codenvy.flux.watcher.core.FluxMessageType.GET_PROJECT_RESPONSE;

/**
 * Handler replying to a {@link com.codenvy.flux.watcher.core.FluxMessageType#GET_PROJECT_REQUEST}.
 *
 * @author Kevin Pollet
 */
@Singleton
@FluxMessageTypes(GET_PROJECT_REQUEST)
public class GetProjectRequestHandler implements FluxMessageHandler {
    @Override
    public void onMessage(FluxMessage message, FluxRepository repository) throws JSONException {
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
               .sendMessage(new FluxMessage(GET_PROJECT_RESPONSE, content));
    }
}
