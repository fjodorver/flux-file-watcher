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
import com.codenvy.flux.watcher.core.Repository;
import com.codenvy.flux.watcher.core.RepositoryModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Kevin Pollet
 */
public class Main {
    public static void main(String[] args) throws MalformedURLException {
        final Injector injector = Guice.createInjector(new RepositoryModule(), new JDKProjectModule());

        final Repository repository = injector.getInstance(Repository.class);
        repository.addRemote(new URL("http://localhost:3000"), Credentials.DEFAULT_USER_CREDENTIALS);
        repository.addProject("flux", "/Users/kevin/Desktop/flux/flux");
    }
}
