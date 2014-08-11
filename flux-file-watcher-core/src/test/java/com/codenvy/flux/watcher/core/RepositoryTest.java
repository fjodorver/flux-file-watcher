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
package com.codenvy.flux.watcher.core;

import com.codenvy.flux.watcher.core.spi.Project;
import com.codenvy.flux.watcher.core.spi.ProjectFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link com.codenvy.flux.watcher.core.Repository} tests.
 *
 * @author Kevin Pollet
 */
public final class RepositoryTest {
    private final static String PROJECT_ID   = "project-id";
    private final static String PROJECT_PATH = "/project-id";

    private Repository     repository;
    private FluxMessageBus fluxMessageBusMock;

    @Before
    public void beforeTest() {
        final Project projectMock = mock(Project.class);
        when(projectMock.id()).thenReturn(PROJECT_ID);
        when(projectMock.path()).thenReturn(PROJECT_PATH);

        final ProjectFactory projectFactoryMock = mock(ProjectFactory.class);
        when(projectFactoryMock.newProject(anyString(), anyString())).thenReturn(projectMock);

        fluxMessageBusMock = mock(FluxMessageBus.class);

        repository = new Repository(fluxMessageBusMock, projectFactoryMock, mock(RepositoryEventBus.class));
    }

    @Test(expected = NullPointerException.class)
    public void testNewWithNullMessageBus() {
        new Repository(null, mock(ProjectFactory.class), mock(RepositoryEventBus.class));
    }

    @Test(expected = NullPointerException.class)
    public void testNewWithNullProjectFactory() {
        new Repository(mock(FluxMessageBus.class), null, mock(RepositoryEventBus.class));
    }

    @Test(expected = NullPointerException.class)
    public void testNewWithNullRepositoryEventBus() {
        new Repository(mock(FluxMessageBus.class), mock(ProjectFactory.class), null);
    }

    @Test(expected = NullPointerException.class)
    public void testAddRemoteWithNullRemoteURL() {
        repository.addRemote(null, new Credentials("defaultuser"));
    }

    @Test(expected = NullPointerException.class)
    public void testAddRemoteWithNullCredentials() throws MalformedURLException {
        repository.addRemote(new URL("http://localhost:8080"), null);
    }

    @Test
    public void testAddRemote() throws MalformedURLException {
        final URL remoteURL = new URL("http://localhost:8080");
        final Credentials defaultUser = new Credentials("defaultuser");

        repository.addRemote(remoteURL, defaultUser);

        verify(fluxMessageBusMock, times(1)).connect(remoteURL, defaultUser);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveRemoteWithNullRemoteURL() {
        repository.removeRemote(null);
    }

    @Test
    public void testRemoveRemote() throws MalformedURLException {
        final URL remoteURL = new URL("http://localhost:8080");

        repository.removeRemote(remoteURL);

        verify(fluxMessageBusMock, times(1)).disconnect(remoteURL);
    }

    @Test(expected = NullPointerException.class)
    public void testAddProjectWithNullProjectId() {
        repository.addProject(null, PROJECT_PATH);
    }

    @Test(expected = NullPointerException.class)
    public void testAddProjectWithNullProjectPath() {
        repository.addProject(PROJECT_ID, null);
    }

    @Test
    public void testRemoveProjectWithNonExistentProjectId() {
        final boolean isRemoved = repository.removeProject(PROJECT_ID);

        Assert.assertFalse(isRemoved);
    }

    @Test
    public void testRemoveProject() {
        final boolean isAdded = repository.addProject(PROJECT_ID, PROJECT_PATH);
        final boolean isRemoved = repository.removeProject(PROJECT_ID);

        Assert.assertTrue(isAdded);
        Assert.assertTrue(isRemoved);
    }

    @Test
    public void testAddProjectWithAlreadyAddedProject() {
        boolean isAdded = repository.addProject(PROJECT_ID, PROJECT_PATH);

        Assert.assertTrue(isAdded);

        isAdded = repository.addProject(PROJECT_ID, PROJECT_PATH);

        Assert.assertFalse(isAdded);
    }

    @Test
    public void testGetProjectWithNullProjectId() {
        final Project project = repository.getProject(null);

        Assert.assertNull(project);
    }

    @Test
    public void testGetProjectWithNonExistentProjectId() {
        final Project project = repository.getProject("foo");

        Assert.assertNull(project);
    }

    @Test
    public void testGetProject() {
        final boolean isAdded = repository.addProject(PROJECT_ID, PROJECT_PATH);

        Assert.assertTrue(isAdded);

        final Project project = repository.getProject(PROJECT_ID);

        Assert.assertNotNull(project);
        Assert.assertEquals(PROJECT_ID, project.id());
        Assert.assertEquals(PROJECT_PATH, project.path());
    }
}
