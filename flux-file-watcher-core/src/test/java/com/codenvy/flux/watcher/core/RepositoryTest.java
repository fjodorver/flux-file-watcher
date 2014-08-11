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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link Repository} tests.
 *
 * @author Kevin Pollet
 */
//TODO finish the repository method tests
public final class RepositoryTest {
    private final static String PROJECT_ID   = "project-id";
    private final static String PROJECT_PATH = "/project-id";

    private Repository repository;

    @Before
    public void beforeTest() {
        final Project projectMock = mock(Project.class);
        when(projectMock.id()).thenReturn(PROJECT_ID);

        final ProjectFactory projectFactoryMock = mock(ProjectFactory.class);
        when(projectFactoryMock.newProject(anyString(), anyString())).thenReturn(projectMock);

        repository = new Repository(mock(FluxMessageBus.class), projectFactoryMock, mock(RepositoryEventBus.class));
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
}
