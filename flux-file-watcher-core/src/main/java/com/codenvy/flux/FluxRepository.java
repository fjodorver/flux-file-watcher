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
package com.codenvy.flux;

import com.codenvy.flux.internal.EntryCreatedListener;
import com.codenvy.flux.internal.SendResourceHandler;
import com.codenvy.flux.spi.RepositoryProvider;
import com.codenvy.flux.spi.RepositoryWatchingService;

import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Kevin Pollet
 */
public class FluxRepository {
    private final int                                id;
    private final String                             username;
    private final ConcurrentMap<URL, FluxConnection> connections;
    private final RepositoryProvider                 repositoryProvider;

    public FluxRepository(String username) {
        this.username = username;
        this.id = new Long(UUID.randomUUID().getMostSignificantBits()).intValue();
        this.connections = new ConcurrentHashMap<>();
        this.repositoryProvider = ServiceLoader.load(RepositoryProvider.class).iterator().next();

        // initialize the repository provider
        final RepositoryWatchingService watchingService = this.repositoryProvider.getWatchingService();
        watchingService.addRepositoryListener(new EntryCreatedListener(this));
    }

    public FluxConnection connectTo(URL serverURL) {
        FluxConnection connection = connections.get(serverURL);
        if (connection == null) {
            FluxConnection newConnection = new FluxConnection(serverURL);
            connection = connections.putIfAbsent(serverURL, newConnection);
            if (connection == null) {
                connection = initializeConnection(newConnection);
            }
        }
        return connection;
    }

    public void disconnectFrom(URL serverURL) {
        final FluxConnection connection = connections.get(serverURL);
        if (connection != null) {
            connection.disconnect();
        }
    }

    public int id() {
        return id;
    }

    public String username() {
        return username;
    }

    public Map<URL, FluxConnection> connections() {
        return Collections.unmodifiableMap(connections);
    }

    public RepositoryProvider repositoryProvider() {
        return repositoryProvider;
    }

    private FluxConnection initializeConnection(FluxConnection connection) {
        connection.addMessageHandler(new SendResourceHandler(this));
        connection.connect();

        return connection;
    }
}
