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

import com.codenvy.flux.watcher.core.repository.ProjectRepository;
import com.codenvy.flux.watcher.core.repository.impl.ProjectRepositoryImpl;
import com.codenvy.flux.watcher.core.service.ConnectionService;
import com.codenvy.flux.watcher.core.service.ProjectService;
import com.codenvy.flux.watcher.core.service.impl.ConnectionServiceImpl;
import com.codenvy.flux.watcher.core.service.impl.ProjectServiceImpl;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Singleton;

/**
 * Guice bindings for {@link RepositoryModule}.
 *
 * @author Kevin Pollet
 */
public class RepositoryModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ConnectionService.class).to(ConnectionServiceImpl.class);
        bind(ProjectService.class).to(ProjectServiceImpl.class);
        bind(ProjectRepository.class).to(ProjectRepositoryImpl.class);
    }

    @Singleton
    @Provides
    protected EventBus provideEventBus() {
        return new EventBus();
    }
}