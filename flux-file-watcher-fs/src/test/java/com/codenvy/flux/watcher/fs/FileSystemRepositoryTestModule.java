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

import com.google.common.jimfs.Jimfs;

import java.nio.file.FileSystem;

/**
 * {@link com.google.inject.Module} providing an in-memory file system implementation.
 *
 * @author Kevin Pollet
 */
public final class FileSystemRepositoryTestModule extends FileSystemRepositoryModule {
    @Override
    protected FileSystem provideFileSystem() {
        return Jimfs.newFileSystem();
    }
}
