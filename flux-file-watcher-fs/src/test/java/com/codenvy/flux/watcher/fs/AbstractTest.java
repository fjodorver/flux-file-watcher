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

import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static com.codenvy.flux.watcher.fs.TestConstants.PROJECT_PATH;
import static com.codenvy.flux.watcher.fs.TestConstants.RELATIVE_PROJECT_README_FILE_PATH;
import static com.codenvy.flux.watcher.fs.TestConstants.RELATIVE_PROJECT_SRC_FOLDER_PATH;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.walkFileTree;

/**
 * Abstract test class bootstrapping a Guice injector.
 *
 * @author Kevin Pollet
 */
public class AbstractTest {
    private FileSystem fileSystem;

    public FileSystem fileSystem() {
        return fileSystem;
    }

    @Before
    public void initFileSystem() throws IOException {
        fileSystem = Jimfs.newFileSystem();

        createDirectory(fileSystem.getPath(PROJECT_PATH));
        createDirectory(fileSystem.getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_SRC_FOLDER_PATH));
        createFile(fileSystem.getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_README_FILE_PATH));
    }

    @After
    public void destroyFileSystem() throws IOException {
        walkFileTree(fileSystem.getPath(PROJECT_PATH), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                delete(dir);
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                delete(file);
                return CONTINUE;
            }
        });

        fileSystem.close();
    }
}
