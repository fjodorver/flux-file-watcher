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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;

import static com.google.common.jimfs.Jimfs.URI_SCHEME;

/**
 * @author Kevin Pollet
 */
public final class FileSystemRepositoryTestModule extends FileSystemRepositoryModule {
    public static final String FILE_SYSTEM_NAME = "in-memory";

    @Override
    protected FileSystem provideFileSystem() {
        try {

            return FileSystems.getFileSystem(new URI(URI_SCHEME, FILE_SYSTEM_NAME, null, null));

        } catch (FileSystemNotFoundException e) {
            return Jimfs.newFileSystem(FILE_SYSTEM_NAME);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
