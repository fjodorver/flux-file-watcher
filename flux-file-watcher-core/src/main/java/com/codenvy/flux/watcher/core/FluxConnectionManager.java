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

import com.google.inject.Provider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Kevin Pollet
 */
//TODO concurrency
//TODO remove connection wen needed, pool ?
@Singleton
public final class FluxConnectionManager {
    private final ConcurrentMap<URL, FluxConnection> connections;
    private final Provider<Set<MessageHandler>>      messageHandlersProvider;

    @Inject
    public FluxConnectionManager(Provider<Set<MessageHandler>> messageHandlersProvider) {
        this.messageHandlersProvider = messageHandlersProvider;
        this.connections = new ConcurrentHashMap<>();
    }

    public FluxConnection openConnection(URL serverURL, FluxCredentials credentials) {
        FluxConnection connection = connections.get(serverURL);
        if (connection == null) {
            FluxConnection newConnection = new FluxConnection(serverURL, credentials, messageHandlersProvider.get());
            connection = connections.putIfAbsent(serverURL, newConnection);
            if (connection == null) {
                connection = newConnection.open();
            }
        }

        return connection;
    }

    public void closeConnection(URL serverURL) {
        final FluxConnection connection = connections.remove(serverURL);
        if (connection != null) {
            connection.close();
        }
    }

    public void broadcastMessage(Message message) {
        for (FluxConnection oneConnection : connections.values()) {
            oneConnection.sendMessage(message);
        }
    }
}
