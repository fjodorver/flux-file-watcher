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
import com.codenvy.flux.watcher.core.spi.RepositoryResourceProvider;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Singleton;

import static com.codenvy.flux.watcher.core.Message.Fields.CALLBACK_ID;
import static com.codenvy.flux.watcher.core.Message.Fields.HASH;
import static com.codenvy.flux.watcher.core.Message.Fields.PROJECT;
import static com.codenvy.flux.watcher.core.Message.Fields.RESOURCE;
import static com.codenvy.flux.watcher.core.Message.Fields.TIMESTAMP;
import static com.codenvy.flux.watcher.core.Message.Fields.TYPE;
import static com.codenvy.flux.watcher.core.MessageType.GET_RESOURCE_REQUEST;
import static com.codenvy.flux.watcher.core.MessageType.RESOURCE_CREATED;
import static com.codenvy.flux.watcher.core.MessageType.RESOURCE_STORED;
import static com.codenvy.flux.watcher.core.Resource.ResourceType;
import static com.codenvy.flux.watcher.core.Resource.ResourceType.FILE;
import static com.codenvy.flux.watcher.core.Resource.ResourceType.FOLDER;

/**
 * Handler replying to a {@link com.codenvy.flux.watcher.core.MessageType#RESOURCE_CREATED}.
 *
 * @author Kevin Pollet
 */
@Singleton
@MessageTypes(RESOURCE_CREATED)
public class ResourceCreatedHandler implements MessageHandler {
    @Override
    public void onMessage(Message message, FluxRepository repository) {
        final RepositoryResourceProvider repositoryResourceProvider = repository.repositoryResourceProvider();

        try {

            final JSONObject request = message.content();
            final String projectName = request.getString(PROJECT.value());
            final String resourcePath = request.getString(RESOURCE.value());
            final long resourceTimestamp = request.getLong(TIMESTAMP.value());
            final String resourceHash = request.getString(HASH.value());

            if (repository.hasProject(projectName)) {

                if (repositoryResourceProvider.getResource(projectName, resourcePath) == null) {

                    final ResourceType resourceType = ResourceType.valueOf(request.getString(TYPE.value()).toUpperCase());
                    if (resourceType == FOLDER) {
                        final Resource folder = Resource.newFolder(projectName, resourcePath, resourceTimestamp);

                        repositoryResourceProvider.createResource(folder);

                        final JSONObject content = new JSONObject()
                                .put(PROJECT.value(), projectName)
                                .put(RESOURCE.value(), resourcePath)
                                .put(TIMESTAMP.value(), resourceTimestamp)
                                .put(HASH.value(), resourceHash)
                                .put(TYPE.value(), resourceType.name().toLowerCase());

                        message.source()
                               .sendMessage(new Message(RESOURCE_STORED, content));

                    } else if (resourceType == FILE) {
                        final JSONObject content = new JSONObject()
                                .put(CALLBACK_ID.value(), repository.id())
                                .put(PROJECT.value(), projectName)
                                .put(RESOURCE.value(), resourcePath)
                                .put(TIMESTAMP.value(), resourceTimestamp)
                                .put(HASH.value(), resourceHash)
                                .put(TYPE.value(), resourceType.name().toLowerCase());

                        message.source()
                               .sendMessage(new Message(GET_RESOURCE_REQUEST, content));
                    }
                }
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
