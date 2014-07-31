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
import com.codenvy.flux.watcher.core.spi.RepositoryListener;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Kevin Pollet
 */
//TODO how integrate flux authentication?
public class FluxModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(FluxRepository.class);
        bind(FluxConnectionManager.class);


        // message handlers binding
        final Multibinder<MessageHandler> messageHandlerBinder = Multibinder.newSetBinder(binder(), MessageHandler.class);
        messageHandlerBinder.addBinding().to(SendResourceHandler.class);

        // repository listeners binding
        final Multibinder<RepositoryListener> repositoryListenerBinder = Multibinder.newSetBinder(binder(), RepositoryListener.class);
        repositoryListenerBinder.addBinding().to(EntryCreatedListener.class);
    }
}
