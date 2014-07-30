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

import com.codenvy.flux.watcher.core.spi.RepositoryProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.UUID;

/**
 * @author Kevin Pollet
 */
@Singleton
public class FluxRepository {
    private final int                   id;
    private final FluxCredentials       credentials;
    private final RepositoryProvider    repositoryProvider;
    private final FluxConnectionManager connectionManager;

    @Inject
    public FluxRepository(FluxCredentials credentials, FluxConnectionManager connectionManager, RepositoryProvider repositoryProvider) {
        this.id = new Long(UUID.randomUUID().getMostSignificantBits()).intValue();
        this.credentials = credentials;
        this.connectionManager = connectionManager;
        this.repositoryProvider = repositoryProvider;
    }

    public FluxConnection connect(URL serverURL) {
        return connectionManager.newConnection(serverURL, credentials);
    }

    public void disconnect(URL serverURL) {
        final FluxConnection connection = connectionManager.connections().get(serverURL);
        if (connection != null) {
            connection.close();
        }
    }

    public void addProject(String projectId, String path) {
        repositoryProvider.addProject(projectId, path);
    }

    public void removeProject(String projectId) {
        repositoryProvider.removeProject(projectId);
    }

    public int id() {
        return id;
    }

    public FluxCredentials credentials() {
        return credentials;
    }

    public RepositoryProvider repositoryProvider() {
        return repositoryProvider;
    }

    public FluxConnectionManager connectionManager() {
        return connectionManager;
    }
}
