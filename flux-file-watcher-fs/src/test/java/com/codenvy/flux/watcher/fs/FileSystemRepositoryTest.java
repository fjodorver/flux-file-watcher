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


import com.codenvy.flux.watcher.core.RepositoryEventBus;
import com.codenvy.flux.watcher.core.Resource;
import com.codenvy.flux.watcher.core.spi.RepositoryProvider;
import com.google.common.collect.Sets;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Set;

import static com.codenvy.flux.watcher.core.Resource.ResourceType.FILE;
import static com.codenvy.flux.watcher.core.Resource.ResourceType.FOLDER;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.readAllBytes;
import static org.mockito.Mockito.mock;

/**
 * {@link com.codenvy.flux.watcher.fs.FileSystemRepository} tests.
 *
 * @author Kevin Pollet
 */
public final class FileSystemRepositoryTest extends AbstractTest {
    private FileSystemRepository fileSystemRepository;

    @Before
    public void beforeTest() throws IOException {
        final RepositoryEventBus repositoryEventBusMock = mock(RepositoryEventBus.class);
        fileSystemRepository = new FileSystemRepository(fileSystem(), repositoryEventBusMock);

        final int numberOfProjects = fileSystemRepository.projects().size();
        final boolean isAdded = fileSystemRepository.addProject(PROJECT_ID, PROJECT_PATH);

        Assert.assertTrue(isAdded);
        Assert.assertEquals(numberOfProjects + 1, fileSystemRepository.projects().size());
    }

    @After
    public void afterTest() throws IOException {
        final int numberOfProjects = fileSystemRepository.projects().size();
        final boolean isRemoved = fileSystemRepository.removeProject(PROJECT_ID);

        Assert.assertTrue(isRemoved);
        Assert.assertEquals(numberOfProjects - 1, fileSystemRepository.projects().size());
    }

    @Test(expected = NullPointerException.class)
    public void testHasProjectWithNullProjectId() {
        fileSystemRepository.hasProject(null);
    }

    @Test
    public void testHasProjectWithNonExistentProjectId() {
        final boolean isProject = fileSystemRepository.hasProject("foo");

        Assert.assertFalse(isProject);
    }

    @Test
    public void testHasProject() {
        final boolean isProject = fileSystemRepository.hasProject(PROJECT_ID);

        Assert.assertTrue(isProject);
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
        final int numberOfProjects = fileSystemRepository.projects().size();
        final boolean isRemoved = fileSystemRepository.removeProject("foo");

        Assert.assertFalse(isRemoved);
        Assert.assertEquals(numberOfProjects, fileSystemRepository.projects().size());
    }

    @Test
    public void testAddProjectWithAlreadyAddedProject() {
        final int numberOfProjects = fileSystemRepository.projects().size();
        final boolean isAdded = fileSystemRepository.addProject(PROJECT_ID, PROJECT_PATH);

        Assert.assertFalse(isAdded);
        Assert.assertEquals(numberOfProjects, fileSystemRepository.projects().size());
    }

    @Test(expected = NullPointerException.class)
    public void testGetProjectResourcesWithNullProjectId() {
        fileSystemRepository.getProjectResources(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetProjectResourcesWithNonExistentProjectId() {
        fileSystemRepository.getProjectResources("foo");
    }

    @Test
    public void testGetProjectResources() {
        final Set<Resource> resources = fileSystemRepository.getProjectResources(PROJECT_ID);
        final Set<String> paths = Sets.newHashSet(RELATIVE_PROJECT_SRC_FOLDER_PATH, RELATIVE_PROJECT_README_FILE_PATH);

        Assert.assertNotNull(resources);
        Assert.assertEquals(2, resources.size());

        for (Resource oneResource : resources) {
            if (paths.contains(oneResource.path())) {
                paths.remove(oneResource.path());
            }
        }

        Assert.assertTrue(paths.isEmpty());
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
        final Path absoluteResourcePath = fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_README_FILE_PATH);
        final Resource resource = fileSystemRepository.getResource(PROJECT_ID, RELATIVE_PROJECT_README_FILE_PATH);

        Assert.assertNotNull(resource);
        Assert.assertEquals(PROJECT_ID, resource.projectId());
        Assert.assertEquals(RELATIVE_PROJECT_README_FILE_PATH, resource.path());
        Assert.assertEquals(FILE, resource.type());
        Assert.assertEquals(getLastModifiedTime(absoluteResourcePath).toMillis(), resource.timestamp());
        Assert.assertTrue(Arrays.equals(readAllBytes(absoluteResourcePath), resource.content()));
        Assert.assertNotNull(resource.hash());
    }

    @Test
    public void testGetResourceWithFolderPath() throws IOException {
        final Path absoluteFolderPath = fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_SRC_FOLDER_PATH);
        final Resource resource = fileSystemRepository.getResource(PROJECT_ID, RELATIVE_PROJECT_SRC_FOLDER_PATH);

        Assert.assertNotNull(resource);
        Assert.assertEquals(PROJECT_ID, resource.projectId());
        Assert.assertEquals(RELATIVE_PROJECT_SRC_FOLDER_PATH, resource.path());
        Assert.assertEquals(FOLDER, resource.type());
        Assert.assertEquals(getLastModifiedTime(absoluteFolderPath).toMillis(), resource.timestamp());
        Assert.assertTrue(Arrays.equals(new byte[0], resource.content()));
        Assert.assertNotNull(resource.hash());
    }

    @Test(expected = NullPointerException.class)
    public void testCreateResourceWithNullResource() {
        fileSystemRepository.createResource(null);
    }

    @Test
    public void testCreateResourceWithFolderResource() throws IOException {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 26);
        calendar.set(Calendar.MONTH, 8);
        calendar.set(Calendar.YEAR, 1984);

        final Path absoluteFolderPath = fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_MAIN_FOLDER_PATH);
        fileSystemRepository.createResource(
                Resource.newFolder(PROJECT_ID, RELATIVE_PROJECT_MAIN_FOLDER_PATH, calendar.getTimeInMillis()));

        Assert.assertTrue(exists(absoluteFolderPath));
        Assert.assertTrue(isDirectory(absoluteFolderPath));
        Assert.assertEquals(calendar.getTimeInMillis(), getLastModifiedTime(absoluteFolderPath).toMillis());
    }

    @Test
    public void testCreateResourceWithFileResource() throws IOException {
        final byte[] helloFileContent = "helloWorld".getBytes();
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 26);
        calendar.set(Calendar.MONTH, 8);
        calendar.set(Calendar.YEAR, 1984);

        final Path absoluteFilePath = fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_HELLO_FILE_PATH);
        fileSystemRepository.createResource(
                Resource.newFile(PROJECT_ID, RELATIVE_PROJECT_HELLO_FILE_PATH, calendar.getTimeInMillis(), helloFileContent));

        Assert.assertTrue(exists(absoluteFilePath));
        Assert.assertFalse(isDirectory(absoluteFilePath));
        Assert.assertTrue(Arrays.equals(readAllBytes(absoluteFilePath), helloFileContent));
        Assert.assertEquals(calendar.getTimeInMillis(), getLastModifiedTime(absoluteFilePath).toMillis());

    }

    @Test(expected = NullPointerException.class)
    public void testDeleteResourceWithNullResource() {
        fileSystemRepository.deleteResource(null);
    }

    @Test
    public void testDeleteResourceWithEmptyFolderResource() throws IOException {
        final Path absoluteFolderPath = fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_SRC_FOLDER_PATH);
        fileSystemRepository.deleteResource(Resource.newFolder(PROJECT_ID, RELATIVE_PROJECT_SRC_FOLDER_PATH, System.currentTimeMillis()));

        Assert.assertFalse(exists(absoluteFolderPath));
    }

    @Test
    public void testDeleteResourceWithNonEmptyFolderResource() throws IOException {
        final Path absoluteFilePath = fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_HELLO_FILE_PATH);
        createFile(absoluteFilePath);

        final Path absoluteFolderPath = fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_SRC_FOLDER_PATH);
        fileSystemRepository.deleteResource(Resource.newFolder(PROJECT_ID, RELATIVE_PROJECT_SRC_FOLDER_PATH, System.currentTimeMillis()));

        Assert.assertFalse(exists(absoluteFolderPath));
    }

    @Test
    public void testDeleteResourceWithFileResource() throws IOException {
        final Path absoluteFilePath = fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_README_FILE_PATH);
        fileSystemRepository
                .deleteResource(Resource.newFile(PROJECT_ID, RELATIVE_PROJECT_README_FILE_PATH, System.currentTimeMillis(), new byte[0]));

        Assert.assertFalse(exists(absoluteFilePath));
    }

    @Test
    public void testEventBus() {
        Assert.assertNotNull(fileSystemRepository.eventBus());
    }

    @Test(expected = NullPointerException.class)
    public void testUnwrapWithNullClass() {
        fileSystemRepository.unwrap(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnwrapWithNonAssignableClass() {
        fileSystemRepository.unwrap(String.class);
    }

    @Test
    public void testUnwrap() {
        final RepositoryProvider repositoryProvider = fileSystemRepository.unwrap(RepositoryProvider.class);

        Assert.assertNotNull(repositoryProvider);
        Assert.assertSame(fileSystemRepository, repositoryProvider);
    }
}