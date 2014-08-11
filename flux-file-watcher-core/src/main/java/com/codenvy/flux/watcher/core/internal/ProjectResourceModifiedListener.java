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
import com.codenvy.flux.watcher.core.FluxMessageBus;
import com.codenvy.flux.watcher.core.RepositoryEvent;
import com.codenvy.flux.watcher.core.RepositoryEventTypes;
import com.codenvy.flux.watcher.core.RepositoryListener;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.codenvy.flux.watcher.core.FluxMessage.Fields.HASH;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.PROJECT;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.RESOURCE;
import static com.codenvy.flux.watcher.core.FluxMessage.Fields.TIMESTAMP;
import static com.codenvy.flux.watcher.core.FluxMessageType.RESOURCE_CHANGED;
import static com.codenvy.flux.watcher.core.FluxMessageType.RESOURCE_STORED;
import static com.codenvy.flux.watcher.core.RepositoryEventType.PROJECT_RESOURCE_MODIFIED;
import static com.codenvy.flux.watcher.core.Resource.ResourceType.FILE;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listener sending a message to flux connections when a project resource is modified in the repository.
 *
 * @author Kevin Pollet
 */
@Singleton
@RepositoryEventTypes(PROJECT_RESOURCE_MODIFIED)
public final class ProjectResourceModifiedListener implements RepositoryListener {
    private final FluxMessageBus messageBus;

    /**
     * Constructs an instance of {@code ProjectResourceModifiedListener}.
     *
     * @param messageBus
     *         the {@link com.codenvy.flux.watcher.core.FluxMessageBus}.
     * @throws NullPointerException
     *         if {@code messageBus} parameter is {@code null}.
     */
    @Inject
    ProjectResourceModifiedListener(FluxMessageBus messageBus) {
        this.messageBus = checkNotNull(messageBus);
    }

    @Override
    public void onEvent(RepositoryEvent event) throws JSONException {
        if (event.resource().type() == FILE) {
            final JSONObject content = new JSONObject()
                    .put(PROJECT.value(), event.project().id())
                    .put(RESOURCE.value(), event.resource().path())
                    .put(TIMESTAMP.value(), event.resource().timestamp())
                    .put(HASH.value(), event.resource().hash());

            messageBus.sendMessages(new FluxMessage(RESOURCE_CHANGED, content), new FluxMessage(RESOURCE_STORED, content));
        }
    }
}
