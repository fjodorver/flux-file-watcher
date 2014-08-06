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

import com.codenvy.flux.watcher.core.FluxMessageHandler;
import com.codenvy.flux.watcher.core.FluxMessageTypes;
import com.codenvy.flux.watcher.core.FluxRepository;
import com.codenvy.flux.watcher.core.FluxMessage;
import com.codenvy.flux.watcher.core.Resource;
import com.codenvy.flux.watcher.core.spi.RepositoryResourceProvider;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Singleton;

import static com.codenvy.flux.watcher.core.FluxMessage.Fields.PROJECT;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.RESOURCE;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.TIMESTAMP;
import static com.codenvy.flux.watcher.core.FluxMessageType.RESOURCE_DELETED;

/**
 * Handler replying to a {@link com.codenvy.flux.watcher.core.FluxMessageType#RESOURCE_DELETED}.
 *
 * @author Kevin Pollet
 */
@Singleton
@FluxMessageTypes(RESOURCE_DELETED)
public class ResourceDeletedHandler implements FluxMessageHandler {
    @Override
    public void onMessage(FluxMessage message, FluxRepository repository) {
        final RepositoryResourceProvider repositoryResourceProvider = repository.repositoryResourceProvider();

        try {

            final JSONObject request = message.content();
            final String projectName = request.getString(PROJECT.value());
            final String resourcePath = request.getString(RESOURCE.value());
            final long resourceTimestamp = request.getLong(TIMESTAMP.value());

            if (repository.hasProject(projectName)) {
                final Resource resource = Resource.newUnknown(projectName, resourcePath, resourceTimestamp);
                repositoryResourceProvider.deleteResource(resource);
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
