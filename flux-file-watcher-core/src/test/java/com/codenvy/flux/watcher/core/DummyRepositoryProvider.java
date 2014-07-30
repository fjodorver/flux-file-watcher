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

import com.codenvy.flux.watcher.core.spi.RepositoryListener;
import com.codenvy.flux.watcher.core.spi.RepositoryProvider;
import com.codenvy.flux.watcher.core.spi.Resource;

/**
 * Dummy {@link com.codenvy.flux.watcher.core.spi.RepositoryProvider} instance.
 *
 * @author Kevin Pollet
 */
public class DummyRepositoryProvider implements RepositoryProvider {
    @Override
    public boolean addProject(String projectId, String path) {
        return false;
    }

    @Override
    public boolean removeProject(String projectId) {
        return false;
    }

    @Override
    public Resource getResource(String projectId, String path) {
        return null;
    }

    @Override
    public void createResource(Resource resource) {

    }

    @Override
    public void deleteResource(Resource resource) {

    }

    @Override
    public boolean addRepositoryListener(RepositoryListener listener) {
        return false;
    }

    @Override
    public boolean removeRepositoryListener(RepositoryListener listener) {
        return false;
    }
}
