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
    private final int                id;
    private final RepositoryProvider repositoryProvider;
    private final FluxConnector      fluxConnector;

    @Inject
    public FluxRepository(FluxConnector fluxConnector, RepositoryProvider repositoryProvider) {
        this.id = new Long(UUID.randomUUID().getMostSignificantBits()).intValue();
        this.fluxConnector = fluxConnector;
        this.repositoryProvider = repositoryProvider;
    }

    public int id() {
        return id;
    }

    public FluxConnection connect(URL serverURL, FluxCredentials credentials) {
        return fluxConnector.connect(serverURL, credentials);
    }

    public void disconnect(URL serverURL) {
        fluxConnector.disconnect(serverURL);
    }

    public void addProject(String projectId, String path) {
        repositoryProvider.addProject(projectId, path);
    }

    public void removeProject(String projectId) {
        repositoryProvider.removeProject(projectId);
    }

    public RepositoryProvider underlyingRepository() {
        return repositoryProvider;
    }

    public FluxConnector fluxConnector() {
        return fluxConnector;
    }
}
