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

import com.codenvy.flux.watcher.core.RepositoryEvent;
import com.codenvy.flux.watcher.core.RepositoryEventBus;
import com.codenvy.flux.watcher.core.RepositoryEventType;
import com.codenvy.flux.watcher.core.RepositoryEventTypes;
import com.codenvy.flux.watcher.core.RepositoryListener;
import com.codenvy.flux.watcher.core.Resource;
import com.google.common.base.Throwables;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.codenvy.flux.watcher.core.RepositoryEventType.PROJECT_RESOURCE_CREATED;
import static com.codenvy.flux.watcher.core.RepositoryEventType.PROJECT_RESOURCE_DELETED;
import static com.codenvy.flux.watcher.core.RepositoryEventType.PROJECT_RESOURCE_MODIFIED;
import static com.codenvy.flux.watcher.core.Resource.ResourceType.FILE;
import static com.codenvy.flux.watcher.core.Resource.ResourceType.FOLDER;
import static com.codenvy.flux.watcher.core.Resource.ResourceType.UNKNOWN;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.WatchEvent.Kind;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link com.codenvy.flux.watcher.fs.FileSystemWatchService} tests.
 *
 * @author Kevin Pollet
 */
public final class FileSystemWatchServiceTest extends AbstractTest {
    private FileSystemWatchService fileSystemWatchService;

    @Before
    public void beforeTest() throws NoSuchMethodException {
        final Map<String, Path> projects = new HashMap<>();
        projects.put(PROJECT_ID, fileSystem().getPath(PROJECT_PATH));

        final RepositoryEventBus repositoryEventBusMock = mock(RepositoryEventBus.class);
        final FileSystemRepository fileSystemRepositoryMock = mock(FileSystemRepository.class);
        when(fileSystemRepositoryMock.projects()).thenReturn(projects);

        fileSystemWatchService = new FileSystemWatchService(fileSystem(), fileSystemRepositoryMock, repositoryEventBusMock);
    }

    @Test(expected = NullPointerException.class)
    public void testWatchWithNullPath() {
        fileSystemWatchService.watch(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWatchWithNonExistentPath() {
        fileSystemWatchService.watch(fileSystem().getPath("/foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWatchWithFilePath() {
        fileSystemWatchService.watch(fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_README_FILE_PATH));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWatchWithNonAbsolutePath() {
        fileSystemWatchService.watch(fileSystem().getPath(RELATIVE_PROJECT_README_FILE_PATH));
    }

    @Test(expected = NullPointerException.class)
    public void testUnwatchWithNullPath() {
        fileSystemWatchService.unwatch(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnwatchWithNonExistentPath() {
        fileSystemWatchService.unwatch(fileSystem().getPath("/foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnwatchWithFilePath() {
        fileSystemWatchService.unwatch(fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_README_FILE_PATH));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnwatchWithNonAbsolutePath() {
        fileSystemWatchService.unwatch(fileSystem().getPath(RELATIVE_PROJECT_README_FILE_PATH));
    }

    @Test
    public void testKindToRepositoryEventTypeWithNullKind() throws Exception {
        final RepositoryEventType repositoryEventType = kindToRepositoryEventType(null);

        Assert.assertNull(repositoryEventType);
    }

    @Test
    public void testKindToRepositoryEventTypeWithEntryCreateKind() throws Exception {
        final RepositoryEventType repositoryEventType = kindToRepositoryEventType(ENTRY_CREATE);

        Assert.assertNotNull(repositoryEventType);
        Assert.assertEquals(PROJECT_RESOURCE_CREATED, repositoryEventType);
    }

    @Test
    public void testKindToRepositoryEventTypeWithEntryDeleteKind() throws Exception {
        final RepositoryEventType repositoryEventType = kindToRepositoryEventType(ENTRY_DELETE);

        Assert.assertNotNull(repositoryEventType);
        Assert.assertEquals(PROJECT_RESOURCE_DELETED, repositoryEventType);
    }

    @Test
    public void testKindToRepositoryEventTypeWithEntryModifyKind() throws Exception {
        final RepositoryEventType repositoryEventType = kindToRepositoryEventType(ENTRY_MODIFY);

        Assert.assertNotNull(repositoryEventType);
        Assert.assertEquals(PROJECT_RESOURCE_MODIFIED, repositoryEventType);
    }

    @Test(expected = NullPointerException.class)
    public void testCastWithNullEvent() throws Throwable {
        final Method castMethod = FileSystemWatchService.class.getDeclaredMethod("cast", WatchEvent.class);
        castMethod.setAccessible(true);

        try {

            castMethod.invoke(fileSystemWatchService, (WatchEvent)null);

        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testPathToResourceWithNullKind() throws Exception {
        pathToResource(null, fileSystem().getPath("foo"));
    }

    @Test(expected = NullPointerException.class)
    public void testPathToResourceWithNullResourcePath() throws Exception {
        pathToResource(ENTRY_CREATE, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPathToResourceWithCreateKindAndNonExistentResourcePath() throws Exception {
        pathToResource(ENTRY_CREATE, fileSystem().getPath("foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPathToResourceWithRelativeResourcePath() throws Exception {
        pathToResource(ENTRY_CREATE, fileSystem().getPath("foo"));
    }

    @Test
    public void testPathToResourceWithFolderPath() throws Exception {
        final Path absoluteFolderPath = fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_SRC_FOLDER_PATH);
        final Resource resource =
                pathToResource(ENTRY_CREATE, fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_SRC_FOLDER_PATH));

        Assert.assertNotNull(resource);
        Assert.assertEquals(PROJECT_ID, resource.projectId());
        Assert.assertEquals(RELATIVE_PROJECT_SRC_FOLDER_PATH, resource.path());
        Assert.assertEquals(FOLDER, resource.type());
        Assert.assertEquals(getLastModifiedTime(absoluteFolderPath).toMillis(), resource.timestamp());
        Assert.assertTrue(Arrays.equals(new byte[0], resource.content()));
        Assert.assertNotNull(resource.hash());
    }

    @Test
    public void testPathToResourceWithFilePath() throws Exception {
        final Path absoluteFilePath = fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_README_FILE_PATH);
        final Resource resource =
                pathToResource(ENTRY_CREATE, fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_README_FILE_PATH));

        Assert.assertNotNull(resource);
        Assert.assertEquals(PROJECT_ID, resource.projectId());
        Assert.assertEquals(RELATIVE_PROJECT_README_FILE_PATH, resource.path());
        Assert.assertEquals(FILE, resource.type());
        Assert.assertEquals(getLastModifiedTime(absoluteFilePath).toMillis(), resource.timestamp());
        Assert.assertTrue(Arrays.equals(readAllBytes(absoluteFilePath), resource.content()));
        Assert.assertNotNull(resource.hash());
    }

    @Test
    public void testWatchEntryCreateFile() throws InterruptedException, IOException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final ProjectResourceCreatedListener projectResourceCreatedListener = new ProjectResourceCreatedListener(countDownLatch);
        final RepositoryEventBus repositoryEventBus = new RepositoryEventBus(Collections.<RepositoryListener>emptySet());
        final FileSystemRepository fileSystemRepository = new FileSystemRepository(fileSystem(), repositoryEventBus);

        fileSystemRepository.addProject(PROJECT_ID, PROJECT_PATH);
        repositoryEventBus.addRepositoryListener(projectResourceCreatedListener);

        final Path absoluteFilePath = fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_HELLO_FILE_PATH);
        createFile(absoluteFilePath);

        countDownLatch.await(1, MINUTES);

        Assert.assertNotNull(projectResourceCreatedListener.repositoryEvent);
        Assert.assertEquals(PROJECT_RESOURCE_CREATED, projectResourceCreatedListener.repositoryEvent.type());
        Assert.assertEquals(PROJECT_ID, projectResourceCreatedListener.repositoryEvent.resource().projectId());
        Assert.assertEquals(RELATIVE_PROJECT_HELLO_FILE_PATH, projectResourceCreatedListener.repositoryEvent.resource().path());
        Assert.assertEquals(FILE, projectResourceCreatedListener.repositoryEvent.resource().type());
        Assert.assertEquals(getLastModifiedTime(absoluteFilePath).toMillis(),
                            projectResourceCreatedListener.repositoryEvent.resource().timestamp());
        Assert.assertTrue(
                Arrays.equals(readAllBytes(absoluteFilePath), projectResourceCreatedListener.repositoryEvent.resource().content()));
        Assert.assertNotNull(projectResourceCreatedListener.repositoryEvent.resource().hash());
    }

    @Test
    public void testWatchEntryCreateFolder() throws InterruptedException, IOException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final ProjectResourceCreatedListener projectResourceCreatedListener = new ProjectResourceCreatedListener(countDownLatch);
        final RepositoryEventBus repositoryEventBus = new RepositoryEventBus(Collections.<RepositoryListener>emptySet());
        final FileSystemRepository fileSystemRepository = new FileSystemRepository(fileSystem(), repositoryEventBus);

        fileSystemRepository.addProject(PROJECT_ID, PROJECT_PATH);
        repositoryEventBus.addRepositoryListener(projectResourceCreatedListener);

        final Path absoluteFilePath = fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_MAIN_FOLDER_PATH);
        createDirectory(absoluteFilePath);

        countDownLatch.await(1, MINUTES);

        Assert.assertNotNull(projectResourceCreatedListener.repositoryEvent);
        Assert.assertEquals(PROJECT_RESOURCE_CREATED, projectResourceCreatedListener.repositoryEvent.type());
        Assert.assertEquals(PROJECT_ID, projectResourceCreatedListener.repositoryEvent.resource().projectId());
        Assert.assertEquals(RELATIVE_PROJECT_MAIN_FOLDER_PATH, projectResourceCreatedListener.repositoryEvent.resource().path());
        Assert.assertEquals(FOLDER, projectResourceCreatedListener.repositoryEvent.resource().type());
        Assert.assertEquals(getLastModifiedTime(absoluteFilePath).toMillis(),
                            projectResourceCreatedListener.repositoryEvent.resource().timestamp());
        Assert.assertTrue(Arrays.equals(new byte[0], projectResourceCreatedListener.repositoryEvent.resource().content()));
        Assert.assertNotNull(projectResourceCreatedListener.repositoryEvent.resource().hash());
    }

    @Test
    public void testWatchEntryModifyFile() throws InterruptedException, IOException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final ProjectResourceModifiedListener projectResourceModifiedListener = new ProjectResourceModifiedListener(countDownLatch);
        final RepositoryEventBus repositoryEventBus = new RepositoryEventBus(Collections.<RepositoryListener>emptySet());
        final FileSystemRepository fileSystemRepository = new FileSystemRepository(fileSystem(), repositoryEventBus);

        fileSystemRepository.addProject(PROJECT_ID, PROJECT_PATH);
        repositoryEventBus.addRepositoryListener(projectResourceModifiedListener);

        final String content = "README";
        final Path absoluteFilePath = fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_README_FILE_PATH);
        write(absoluteFilePath, content.getBytes());

        countDownLatch.await(1, MINUTES);

        Assert.assertNotNull(projectResourceModifiedListener.repositoryEvent);
        Assert.assertEquals(PROJECT_RESOURCE_MODIFIED, projectResourceModifiedListener.repositoryEvent.type());
        Assert.assertEquals(PROJECT_ID, projectResourceModifiedListener.repositoryEvent.resource().projectId());
        Assert.assertEquals(RELATIVE_PROJECT_README_FILE_PATH, projectResourceModifiedListener.repositoryEvent.resource().path());
        Assert.assertEquals(FILE, projectResourceModifiedListener.repositoryEvent.resource().type());
        Assert.assertEquals(getLastModifiedTime(absoluteFilePath).toMillis(),
                            projectResourceModifiedListener.repositoryEvent.resource().timestamp());
        Assert.assertTrue(Arrays.equals(content.getBytes(), projectResourceModifiedListener.repositoryEvent.resource().content()));
        Assert.assertNotNull(projectResourceModifiedListener.repositoryEvent.resource().hash());
    }

    @Test
    public void testWatchEntryDeleteFile() throws InterruptedException, IOException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final ProjectResourceDeletedListener projectResourceDeletedListener = new ProjectResourceDeletedListener(countDownLatch);
        final RepositoryEventBus repositoryEventBus = new RepositoryEventBus(Collections.<RepositoryListener>emptySet());
        final FileSystemRepository fileSystemRepository = new FileSystemRepository(fileSystem(), repositoryEventBus);

        fileSystemRepository.addProject(PROJECT_ID, PROJECT_PATH);
        repositoryEventBus.addRepositoryListener(projectResourceDeletedListener);

        final Path absoluteFilePath = fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_README_FILE_PATH);
        delete(absoluteFilePath);

        countDownLatch.await(1, MINUTES);

        Assert.assertNotNull(projectResourceDeletedListener.repositoryEvent);
        Assert.assertEquals(PROJECT_RESOURCE_DELETED, projectResourceDeletedListener.repositoryEvent.type());
        Assert.assertEquals(PROJECT_ID, projectResourceDeletedListener.repositoryEvent.resource().projectId());
        Assert.assertEquals(RELATIVE_PROJECT_README_FILE_PATH, projectResourceDeletedListener.repositoryEvent.resource().path());
        Assert.assertEquals(UNKNOWN, projectResourceDeletedListener.repositoryEvent.resource().type());
        Assert.assertTrue(Arrays.equals(new byte[0], projectResourceDeletedListener.repositoryEvent.resource().content()));
        Assert.assertNotNull(projectResourceDeletedListener.repositoryEvent.resource().hash());
    }

    @Test
    public void testWatchEntryDeleteFolder() throws InterruptedException, IOException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final ProjectResourceDeletedListener projectResourceDeletedListener = new ProjectResourceDeletedListener(countDownLatch);
        final RepositoryEventBus repositoryEventBus = new RepositoryEventBus(Collections.<RepositoryListener>emptySet());
        final FileSystemRepository fileSystemRepository = new FileSystemRepository(fileSystem(), repositoryEventBus);

        fileSystemRepository.addProject(PROJECT_ID, PROJECT_PATH);
        repositoryEventBus.addRepositoryListener(projectResourceDeletedListener);

        final Path absoluteFolderPath = fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_SRC_FOLDER_PATH);
        delete(absoluteFolderPath);

        countDownLatch.await(1, MINUTES);

        Assert.assertNotNull(projectResourceDeletedListener.repositoryEvent);
        Assert.assertEquals(PROJECT_RESOURCE_DELETED, projectResourceDeletedListener.repositoryEvent.type());
        Assert.assertEquals(PROJECT_ID, projectResourceDeletedListener.repositoryEvent.resource().projectId());
        Assert.assertEquals(RELATIVE_PROJECT_SRC_FOLDER_PATH, projectResourceDeletedListener.repositoryEvent.resource().path());
        Assert.assertEquals(UNKNOWN, projectResourceDeletedListener.repositoryEvent.resource().type());
        Assert.assertTrue(Arrays.equals(new byte[0], projectResourceDeletedListener.repositoryEvent.resource().content()));
        Assert.assertNotNull(projectResourceDeletedListener.repositoryEvent.resource().hash());
    }

    @Test
    public void testUnwatch() throws IOException, InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final ProjectResourceDeletedListener projectResourceDeletedListener = new ProjectResourceDeletedListener(countDownLatch);
        final RepositoryEventBus repositoryEventBus = new RepositoryEventBus(Collections.<RepositoryListener>emptySet());
        final FileSystemRepository fileSystemRepository = new FileSystemRepository(fileSystem(), repositoryEventBus);

        fileSystemRepository.addProject(PROJECT_ID, PROJECT_PATH);
        repositoryEventBus.addRepositoryListener(projectResourceDeletedListener);
        fileSystemRepository.removeProject(PROJECT_ID);

        final Path absoluteFolderPath = fileSystem().getPath(PROJECT_PATH).resolve(RELATIVE_PROJECT_SRC_FOLDER_PATH);
        delete(absoluteFolderPath);

        countDownLatch.await(30, SECONDS);

        Assert.assertNull(projectResourceDeletedListener.repositoryEvent);
    }

    private RepositoryEventType kindToRepositoryEventType(Kind<?> kind) throws Exception {
        final Method kindToRepositoryEventTypeMethod =
                FileSystemWatchService.class.getDeclaredMethod("kindToRepositoryEventType", Kind.class);
        kindToRepositoryEventTypeMethod.setAccessible(true);

        try {

            return (RepositoryEventType)kindToRepositoryEventTypeMethod.invoke(fileSystemWatchService, kind);

        } catch (InvocationTargetException e) {
            throw Throwables.propagate(e.getCause());
        }
    }

    private Resource pathToResource(Kind<Path> kind, Path resourcePath) throws Exception {
        final Method pathToResourceMethod = FileSystemWatchService.class.getDeclaredMethod("pathToResource", Kind.class, Path.class);
        pathToResourceMethod.setAccessible(true);

        try {

            return (Resource)pathToResourceMethod.invoke(fileSystemWatchService, kind, resourcePath);

        } catch (InvocationTargetException e) {
            throw Throwables.propagate(e.getCause());
        }
    }

    private static abstract class AbstractRepositoryListener implements RepositoryListener {
        private final CountDownLatch  countDownLatch;
        public        RepositoryEvent repositoryEvent;

        public AbstractRepositoryListener(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
            this.repositoryEvent = null;
        }

        @Override
        public void onEvent(RepositoryEvent event) {
            try {

                if (repositoryEvent != null) {
                    throw new IllegalStateException();
                }
                repositoryEvent = event;

            } finally {
                countDownLatch.countDown();
            }
        }
    }

    @RepositoryEventTypes(PROJECT_RESOURCE_CREATED)
    private static class ProjectResourceCreatedListener extends AbstractRepositoryListener {
        public ProjectResourceCreatedListener(CountDownLatch countDownLatch) {
            super(countDownLatch);
        }
    }

    @RepositoryEventTypes(PROJECT_RESOURCE_MODIFIED)
    private static class ProjectResourceModifiedListener extends AbstractRepositoryListener {
        public ProjectResourceModifiedListener(CountDownLatch countDownLatch) {
            super(countDownLatch);
        }
    }

    @RepositoryEventTypes(PROJECT_RESOURCE_DELETED)
    private static class ProjectResourceDeletedListener extends AbstractRepositoryListener {
        public ProjectResourceDeletedListener(CountDownLatch countDownLatch) {
            super(countDownLatch);
        }
    }
}
