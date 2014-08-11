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

import com.codenvy.flux.watcher.core.internal.GetProjectRequestHandler;
import com.codenvy.flux.watcher.core.internal.GetResourceRequestHandler;
import com.codenvy.flux.watcher.core.internal.GetResourceResponseHandler;
import com.codenvy.flux.watcher.core.internal.ProjectResourceCreatedListener;
import com.codenvy.flux.watcher.core.internal.ProjectResourceDeletedListener;
import com.codenvy.flux.watcher.core.internal.ProjectResourceModifiedListener;
import com.codenvy.flux.watcher.core.internal.ResourceChangedHandler;
import com.codenvy.flux.watcher.core.internal.ResourceCreatedHandler;
import com.codenvy.flux.watcher.core.internal.ResourceDeletedHandler;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * Guice bindings for {@link RepositoryModule}.
 *
 * @author Kevin Pollet
 */
public class RepositoryModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Repository.class);
        bind(FluxMessageBus.class);
        bind(RepositoryEventBus.class);

        // message handler bindings
        final Multibinder<FluxMessageHandler> messageHandlers = Multibinder.newSetBinder(binder(), FluxMessageHandler.class);
        messageHandlers.addBinding().to(GetResourceRequestHandler.class);
        messageHandlers.addBinding().to(GetResourceResponseHandler.class);
        messageHandlers.addBinding().to(GetProjectRequestHandler.class);
        messageHandlers.addBinding().to(ResourceCreatedHandler.class);
        messageHandlers.addBinding().to(ResourceDeletedHandler.class);
        messageHandlers.addBinding().to(ResourceChangedHandler.class);

        // repository listener bindings
        final Multibinder<RepositoryListener> repositoryListeners = Multibinder.newSetBinder(binder(), RepositoryListener.class);
        repositoryListeners.addBinding().to(ProjectResourceCreatedListener.class);
        repositoryListeners.addBinding().to(ProjectResourceDeletedListener.class);
        repositoryListeners.addBinding().to(ProjectResourceModifiedListener.class);
    }
}
