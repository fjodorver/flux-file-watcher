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

import com.codenvy.flux.watcher.core.Credentials;
import com.codenvy.flux.watcher.core.Facade;
import com.codenvy.flux.watcher.core.RepositoryModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Kevin Pollet
 */
public class Main {
    public static void main(String[] args) throws URISyntaxException {
        Injector injector = Guice.createInjector(new RepositoryModule(), new JDKModule());
        Facade facade = injector.getInstance(Facade.class);

        URI uri = new URI("http://localhost:3000");
        facade.addRemote(uri, new Credentials("defaultuser"));
        facade.connectToChannel(uri, "defaultuser");
        facade.connectProject("flux", "/home/fjodor/Doccuments/");
    }
}