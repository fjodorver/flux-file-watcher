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

import com.codenvy.flux.watcher.core.spi.RepositoryListener;
import com.google.common.jimfs.Jimfs;
import com.google.inject.multibindings.Multibinder;

import java.nio.file.FileSystem;

/**
 * @author Kevin Pollet
 */
public final class FileSystemRepositoryTestModule extends FileSystemRepositoryModule {
    @Override
    protected void configure() {
        super.configure();

        // register repository listeners
        final Multibinder<RepositoryListener> repositoryListeners = Multibinder.newSetBinder(binder(), RepositoryListener.class);
        repositoryListeners.addBinding().to(FileSystemRepositoryTest.EntryCreatedListener.class);
        repositoryListeners.addBinding().to(FileSystemRepositoryTest.EntryModifiedListener.class);
        repositoryListeners.addBinding().to(FileSystemRepositoryTest.EntryDeletedListener.class);
        repositoryListeners.addBinding().to(FileSystemRepositoryTest.EntryCreatedAndModifiedListener.class);
    }

    @Override
    protected FileSystem provideFileSystem() {
        return Jimfs.newFileSystem();
    }
}
