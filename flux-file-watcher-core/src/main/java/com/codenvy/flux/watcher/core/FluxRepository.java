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

import com.codenvy.flux.watcher.core.internal.EntryCreatedListener;
import com.codenvy.flux.watcher.core.internal.SendResourceHandler;
import com.codenvy.flux.watcher.core.spi.RepositoryProvider;

import java.net.URL;
import java.nio.file.Path;
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

        // initialize repository listeners
        this.repositoryProvider.addRepositoryListener(new EntryCreatedListener(this));
    }

    public FluxConnection connectTo(URL serverURL) {
        FluxConnection connection = connections.get(serverURL);
        if (connection == null) {
            FluxConnection newConnection = new FluxConnection(serverURL);
            connection = connections.putIfAbsent(serverURL, newConnection);
            if (connection == null) {
                connection = initializeAndOpenConnection(newConnection);
            }
        }
        return connection;
    }

    public void disconnectFrom(URL serverURL) {
        final FluxConnection connection = connections.get(serverURL);
        if (connection != null) {
            connection.close();
        }
    }

    public void addProject(String projectId, Path path) {
        repositoryProvider.addProject(projectId, path);
    }

    public void removeProject(String projectId) {
        repositoryProvider.removeProject(projectId);
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

    private FluxConnection initializeAndOpenConnection(FluxConnection connection) {
        connection.addMessageHandler(new SendResourceHandler(this));
        connection.open();

        return connection;
    }
}
