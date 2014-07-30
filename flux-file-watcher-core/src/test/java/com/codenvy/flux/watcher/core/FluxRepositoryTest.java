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


import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link com.codenvy.flux.watcher.core.FluxRepository} tests.
 *
 * @author Kevin Pollet
 */
public class FluxRepositoryTest {
    @Test
    public void testBootstrap() {
        final Injector injector = Guice.createInjector(new FluxModule(), new DummyModule());
        final FluxRepository repository = injector.getInstance(FluxRepository.class);

        Assert.assertNotNull(repository);
    }
}
