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

import com.codenvy.flux.watcher.core.FluxConnector;
import com.codenvy.flux.watcher.core.Message;
import com.codenvy.flux.watcher.core.RepositoryEvent;
import com.codenvy.flux.watcher.core.RepositoryEventTypes;
import com.codenvy.flux.watcher.core.RepositoryListener;
import com.codenvy.flux.watcher.core.Resource;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.codenvy.flux.watcher.core.Message.Fields.HASH;
import static com.codenvy.flux.watcher.core.Message.Fields.PROJECT;
import static com.codenvy.flux.watcher.core.Message.Fields.RESOURCE;
import static com.codenvy.flux.watcher.core.Message.Fields.TIMESTAMP;
import static com.codenvy.flux.watcher.core.MessageType.RESOURCE_CHANGED;
import static com.codenvy.flux.watcher.core.MessageType.RESOURCE_STORED;
import static com.codenvy.flux.watcher.core.RepositoryEventType.ENTRY_MODIFIED;
import static com.codenvy.flux.watcher.core.Resource.ResourceType.FILE;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listener sending a message to flux connections when a resource is modified in the repository.
 *
 * @author Kevin Pollet
 */
//TODO check timestamp?
@Singleton
@RepositoryEventTypes(ENTRY_MODIFIED)
public class EntryModifiedListener implements RepositoryListener {
    private final FluxConnector fluxConnector;

    /**
     * Constructs an instance of {@code EntryModifiedListener}.
     *
     * @param fluxConnector
     *         the {@link com.codenvy.flux.watcher.core.FluxConnector}.
     * @throws NullPointerException
     *         if {@code fluxConnector} parameter is {@code null}.
     */
    @Inject
    EntryModifiedListener(FluxConnector fluxConnector) {
        this.fluxConnector = checkNotNull(fluxConnector);
    }

    @Override
    public void onEvent(RepositoryEvent event) {
        try {

            final Resource resource = event.resource();
            if (resource.type() == FILE) {
                final JSONObject content = new JSONObject()
                        .put(PROJECT.value(), resource.projectId())
                        .put(RESOURCE.value(), resource.path())
                        .put(TIMESTAMP.value(), resource.timestamp())
                        .put(HASH.value(), resource.hash());

                fluxConnector.broadcastMessage(new Message(RESOURCE_CHANGED, content));
                fluxConnector.broadcastMessage(new Message(RESOURCE_STORED, content));
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
