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

/**
 * {@link JDKProjectWatchService} tests.
 *
 * @author Kevin Pollet
 */
//TODO modify TESTS
public final class JDKProjectWatchServiceTest extends AbstractTest {
    /*private JDKProjectWatchService JDKProjectWatchService;

    @Before
    public void beforeTest() throws NoSuchMethodException {
        final RepositoryEventBus repositoryEventBusMock = mock(RepositoryEventBus.class);
        JDKProjectWatchService = new JDKProjectWatchService(fileSystem(), repositoryEventBusMock);
    }

    @Test(expected = NullPointerException.class)
    public void testWatchWithNullProject() {
        JDKProjectWatchService.watch(null);
    }

    @Test(expected = NullPointerException.class)
    public void testUnwatchWithNullPath() {
        JDKProjectWatchService.unwatch(null);
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
        final Method castMethod = JDKProjectWatchService.class.getDeclaredMethod("cast", WatchEvent.class);
        castMethod.setAccessible(true);

        try {

            castMethod.invoke(JDKProjectWatchService, (WatchEvent)null);

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
        final RepositoryEventBus repositoryEventBus = new RepositoryEventBus(Collections.<RepositoryListener>emptySet(), repository);
        final JDKProject fileSystemRepository = new JDKProject(fileSystem(), repositoryEventBus, id, path);

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
        final RepositoryEventBus repositoryEventBus = new RepositoryEventBus(Collections.<RepositoryListener>emptySet(), repository);
        final JDKProject fileSystemRepository = new JDKProject(fileSystem(), repositoryEventBus, id, path);

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
        final RepositoryEventBus repositoryEventBus = new RepositoryEventBus(Collections.<RepositoryListener>emptySet(), repository);
        final JDKProject fileSystemRepository = new JDKProject(fileSystem(), repositoryEventBus, id, path);

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
        final RepositoryEventBus repositoryEventBus = new RepositoryEventBus(Collections.<RepositoryListener>emptySet(), repository);
        final JDKProject fileSystemRepository = new JDKProject(fileSystem(), repositoryEventBus, id, path);

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
        final RepositoryEventBus repositoryEventBus = new RepositoryEventBus(Collections.<RepositoryListener>emptySet(), repository);
        final JDKProject fileSystemRepository = new JDKProject(fileSystem(), repositoryEventBus, id, path);

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
        final RepositoryEventBus repositoryEventBus = new RepositoryEventBus(Collections.<RepositoryListener>emptySet(), repository);
        final JDKProject fileSystemRepository = new JDKProject(fileSystem(), repositoryEventBus, id, path);

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
                JDKProjectWatchService.class.getDeclaredMethod("kindToRepositoryEventType", Kind.class);
        kindToRepositoryEventTypeMethod.setAccessible(true);

        try {

            return (RepositoryEventType)kindToRepositoryEventTypeMethod.invoke(JDKProjectWatchService, kind);

        } catch (InvocationTargetException e) {
            throw Throwables.propagate(e.getCause());
        }
    }

    private Resource pathToResource(Kind<Path> kind, Path resourcePath) throws Exception {
        final Method pathToResourceMethod = JDKProjectWatchService.class.getDeclaredMethod("pathToResource", Kind.class, Path.class);
        pathToResourceMethod.setAccessible(true);

        try {

            return (Resource)pathToResourceMethod.invoke(JDKProjectWatchService, kind, resourcePath);

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
    }   */
}
