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
package com.codenvy.flux.watcher.fs;

import com.codenvy.flux.watcher.core.FluxModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.BeforeClass;

/**
 * @author Kevin Pollet
 */
public class AbstractTest {
    protected static Injector injector;

    @BeforeClass
    public static void initialize() {
        injector = Guice.createInjector(new FluxModule(), new FileSystemRepositoryTestModule());
    }
}
