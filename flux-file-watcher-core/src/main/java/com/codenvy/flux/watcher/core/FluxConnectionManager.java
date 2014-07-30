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
package com.codenvy.flux.watcher.core;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Kevin Pollet
 */
@Singleton
//TODO remove connection wen needed, pool ?
public final class FluxConnectionManager {
    private final ConcurrentMap<URL, FluxConnection> connections;
    private final Set<MessageHandler>                messageHandlers;

    @Inject
    public FluxConnectionManager(Set<MessageHandler> messageHandlers) {
        this.messageHandlers = messageHandlers;
        this.connections = new ConcurrentHashMap<>();
    }

    public FluxConnection newConnection(URL serverURL, FluxCredentials credentials) {
        FluxConnection connection = connections.get(serverURL);
        if (connection == null) {
            FluxConnection newConnection = new FluxConnection(serverURL, credentials);
            connection = connections.putIfAbsent(serverURL, newConnection);
            if (connection == null) {
                connection = newConnection;
                for (MessageHandler oneMessageHandler : messageHandlers) {
                    connection.addMessageHandler(oneMessageHandler);
                }
                newConnection.open();
            }
        }

        return connection;
    }

    public Map<URL, FluxConnection> connections() {
        return Collections.unmodifiableMap(connections);
    }
}
