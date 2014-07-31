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


import com.codenvy.flux.watcher.core.spi.Resource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Calendar;

import static com.codenvy.flux.watcher.core.spi.Resource.ResourceType.FILE;
import static com.codenvy.flux.watcher.core.spi.Resource.ResourceType.FOLDER;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.walkFileTree;

/**
 * @author Kevin Pollet
 */
public class FileSystemRepositoryTest extends AbstractTest {
    private static final String PROJECT_ID                        = "codenvy-project-id";
    private static final String PROJECT_PATH                      = "/codenvy-project";
    private static final String RELATIVE_PROJECT_SRC_FOLDER_PATH  = "src";
    private static final String RELATIVE_PROJECT_README_FILE_PATH = "readme";

    private static FileSystemRepository fileSystemRepository;
    private static FileSystem           fileSystem;

    @BeforeClass
    public static void beforeClass() throws IOException {
        fileSystemRepository = injector.getInstance(FileSystemRepository.class);
        fileSystem = injector.getInstance(FileSystem.class);
    }

    @Before
    public void beforeTest() throws IOException {
        createDirectory(fileSystem.getPath(PROJECT_PATH));
        createDirectory(fileSystem.getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_SRC_FOLDER_PATH));
        createFile(fileSystem.getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_README_FILE_PATH));
    }

    @After
    public void afterTest() throws IOException {
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
    }

    @Test(expected = NullPointerException.class)
    public void testAddProjectWithNullProjectId() {
        fileSystemRepository.addProject(null, "foo");
    }

    @Test(expected = NullPointerException.class)
    public void testAddProjectWithNullProjectPath() {
        fileSystemRepository.addProject("foo-id", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProjectWithNonExistentProjectPath() {
        fileSystemRepository.addProject("foo-id", "/foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProjectWithNonDirectoryPath() {
        fileSystemRepository.addProject("foo-id", RELATIVE_PROJECT_README_FILE_PATH);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveProjectWithNullProjectId() {
        fileSystemRepository.removeProject(null);
    }

    @Test
    public void testRemoveProjectWithNonExistentProjectId() {
        final boolean isRemoved = fileSystemRepository.removeProject("foo");

        Assert.assertFalse(isRemoved);
        Assert.assertEquals(0, fileSystemRepository.projects().size());
    }

    @Test
    public void testAddAndRemoveProject() {
        final boolean isAdded = fileSystemRepository.addProject(PROJECT_ID, PROJECT_PATH);

        Assert.assertTrue(isAdded);
        Assert.assertEquals(1, fileSystemRepository.projects().size());

        final boolean isRemoved = fileSystemRepository.removeProject(PROJECT_ID);

        Assert.assertTrue(isRemoved);
        Assert.assertEquals(0, fileSystemRepository.projects().size());
    }

    @Test
    public void testAddProjectWithAlreadyAddedProjectAndRemove() {
        boolean isAdded = fileSystemRepository.addProject(PROJECT_ID, PROJECT_PATH);

        Assert.assertTrue(isAdded);
        Assert.assertEquals(1, fileSystemRepository.projects().size());

        isAdded = fileSystemRepository.addProject(PROJECT_ID, PROJECT_PATH);
        Assert.assertFalse(isAdded);
        Assert.assertEquals(1, fileSystemRepository.projects().size());

        final boolean isRemoved = fileSystemRepository.removeProject(PROJECT_ID);

        Assert.assertTrue(isRemoved);
        Assert.assertEquals(0, fileSystemRepository.projects().size());
    }

    @Test(expected = NullPointerException.class)
    public void testGetResourceWithNullProjectId() {
        fileSystemRepository.getResource(null, "foo");
    }

    @Test(expected = NullPointerException.class)
    public void testGetResourceWithNullResourcePath() {
        fileSystemRepository.getResource("foo-id", null);
    }

    @Test
    public void testGetResourceWithNonExistentResourcePath() {
        final Resource resource = fileSystemRepository.getResource("foo-id", "foo");

        Assert.assertNull(resource);
    }

    @Test
    public void testGetResourceWithFilePath() throws IOException {
        fileSystemRepository.addProject(PROJECT_ID, PROJECT_PATH);

        final Path absoluteResourcePath = fileSystem.getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_README_FILE_PATH);
        final Resource resource = fileSystemRepository.getResource(PROJECT_ID, RELATIVE_PROJECT_README_FILE_PATH);

        Assert.assertNotNull(resource);
        Assert.assertEquals(PROJECT_ID, resource.projectId());
        Assert.assertEquals(RELATIVE_PROJECT_README_FILE_PATH, resource.path());
        Assert.assertEquals(FILE, resource.type());
        Assert.assertEquals(getLastModifiedTime(absoluteResourcePath).toMillis(), resource.timestamp());
        Assert.assertTrue(Arrays.equals(readAllBytes(absoluteResourcePath), resource.content()));
        Assert.assertNotNull(resource.hash());

        fileSystemRepository.removeProject(PROJECT_ID);
    }

    @Test
    public void testGetResourceWithFolderPath() throws IOException {
        fileSystemRepository.addProject(PROJECT_ID, PROJECT_PATH);

        final Path absoluteFolderPath = fileSystem.getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_SRC_FOLDER_PATH);
        final Resource resource = fileSystemRepository.getResource(PROJECT_ID, RELATIVE_PROJECT_SRC_FOLDER_PATH);

        Assert.assertNotNull(resource);
        Assert.assertEquals(PROJECT_ID, resource.projectId());
        Assert.assertEquals(RELATIVE_PROJECT_SRC_FOLDER_PATH, resource.path());
        Assert.assertEquals(FOLDER, resource.type());
        Assert.assertEquals(getLastModifiedTime(absoluteFolderPath).toMillis(), resource.timestamp());
        Assert.assertTrue(Arrays.equals(new byte[0], resource.content()));
        Assert.assertNotNull(resource.hash());

        fileSystemRepository.removeProject(PROJECT_ID);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateResourceWithNullResource() {
        fileSystemRepository.createResource(null);
    }

    @Test
    public void testCreateResourceWithFolderResource() throws IOException {
        fileSystemRepository.addProject(PROJECT_ID, PROJECT_PATH);

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 26);
        calendar.set(Calendar.MONTH, 8);
        calendar.set(Calendar.YEAR, 1984);

        final Path absoluteFolderPath = fileSystem.getPath(PROJECT_PATH).resolve("src/main");
        fileSystemRepository.createResource(Resource.newFolder(PROJECT_ID, "src/main", calendar.getTimeInMillis()));

        Assert.assertTrue(exists(absoluteFolderPath));
        Assert.assertTrue(isDirectory(absoluteFolderPath));
        Assert.assertEquals(calendar.getTimeInMillis(), getLastModifiedTime(absoluteFolderPath).toMillis());

        fileSystemRepository.removeProject(PROJECT_ID);
    }

    @Test
    public void testCreateResourceWithFileResource() throws IOException {
        fileSystemRepository.addProject(PROJECT_ID, PROJECT_PATH);

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 26);
        calendar.set(Calendar.MONTH, 8);
        calendar.set(Calendar.YEAR, 1984);

        final Path absoluteFilePath = fileSystem.getPath(PROJECT_PATH).resolve("src/hello");
        fileSystemRepository.createResource(Resource.newFile(PROJECT_ID, "src/hello", calendar.getTimeInMillis(), "helloWorld".getBytes()));

        Assert.assertTrue(exists(absoluteFilePath));
        Assert.assertFalse(isDirectory(absoluteFilePath));
        Assert.assertTrue(Arrays.equals(readAllBytes(absoluteFilePath), "helloWorld".getBytes()));
        Assert.assertEquals(calendar.getTimeInMillis(), getLastModifiedTime(absoluteFilePath).toMillis());

        fileSystemRepository.removeProject(PROJECT_ID);
    }

    //TODO test delete resource


    @Test(expected = NullPointerException.class)
    public void testAddRepositoryListenerWithNullListener() {
        fileSystemRepository.addRepositoryListener(null);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveRepositoryListenerWithNullListener() {
        fileSystemRepository.removeRepositoryListener(null);
    }
}

