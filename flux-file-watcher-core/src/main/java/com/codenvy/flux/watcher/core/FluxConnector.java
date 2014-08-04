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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages Flux connections to Flux instance.
 *
 * @author Kevin Pollet
 */
@Singleton
public class FluxConnector {
    private final ConcurrentMap<URL, FluxConnection> connections;
    private final Provider<Set<MessageHandler>>      messageHandlersProvider;

    /**
     * Constructs an instance of {@link FluxConnector}.
     *
     * @param messageHandlersProvider
     *         the {@link com.codenvy.flux.watcher.core.MessageHandler} to register.
     * @throws java.lang.NullPointerException
     *         if {@code messageHandlersProvider} is {@code null}.
     */
    @Inject
    public FluxConnector(Provider<Set<MessageHandler>> messageHandlersProvider) {
        this.messageHandlersProvider = checkNotNull(messageHandlersProvider);
        this.connections = new ConcurrentHashMap<>();
    }

    /**
     * Open a connection to the given server {@link java.net.URL}.
     *
     * @param serverURL
     *         the server {@link java.net.URL} to connect to.
     * @param credentials
     *         the {@link Credentials} to use for the connection.
     * @return the opened {@link com.codenvy.flux.watcher.core.FluxConnection} or the existing {@link
     * com.codenvy.flux.watcher.core.FluxConnection} if already opened.
     * @throws java.lang.NullPointerException
     *         if {@code serverURL} or {@code credentials} parameter is {@code null}.
     */
    public FluxConnection connect(URL serverURL, Credentials credentials) {
        checkNotNull(serverURL);
        checkNotNull(credentials);

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

    /**
     * Close the connection to the given server {@link java.net.URL}.
     *
     * @param serverURL
     *         the server {@link java.net.URL} to disconnect from.
     * @throws java.lang.NullPointerException
     *         if {@code serverURL} parameter is {@code null}.
     */
    public void disconnect(URL serverURL) {
        final FluxConnection connection = connections.remove(checkNotNull(serverURL));
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * Broadcast a message to all opened {@link com.codenvy.flux.watcher.core.FluxConnection}.
     *
     * @param message
     *         the {@link com.codenvy.flux.watcher.core.Message} instance to broadcast.
     * @throws java.lang.NullPointerException
     *         if {@code message} parameter is {@code null}.
     */
    public void broadcastMessage(Message message) {
        checkNotNull(message);

        for (FluxConnection oneConnection : connections.values()) {
            oneConnection.sendMessage(message);
        }
    }
}
