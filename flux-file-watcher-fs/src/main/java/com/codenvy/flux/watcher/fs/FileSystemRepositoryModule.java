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

import com.codenvy.flux.watcher.core.spi.RepositoryProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Singleton;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

/**
 * The file system repository provider implementation Guice bindings.
 *
 * @author Kevin Pollet
 */
public class FileSystemRepositoryModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(RepositoryProvider.class).to(FileSystemRepository.class);
    }

    @Singleton
    @Provides
    protected FileSystem provideFileSystem() {
        return FileSystems.getDefault();
    }
}
