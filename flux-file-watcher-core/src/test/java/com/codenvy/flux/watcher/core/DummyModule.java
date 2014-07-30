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
import com.google.inject.AbstractModule;

/**
 * @author Kevin Pollet
 */
public class DummyModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(RepositoryProvider.class).to(DummyRepositoryProvider.class);
    }
}
