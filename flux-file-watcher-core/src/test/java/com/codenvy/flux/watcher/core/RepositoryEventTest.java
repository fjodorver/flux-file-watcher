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

import org.junit.Test;

import static com.codenvy.flux.watcher.core.RepositoryEventType.PROJECT_RESOURCE_CREATED;
import static org.mockito.Mockito.mock;

/**
 * {@link com.codenvy.flux.watcher.core.RepositoryEvent} tests.
 *
 * @author Kevin Pollet
 */
public final class RepositoryEventTest {
    @Test(expected = NullPointerException.class)
    public void testNewRepositoryEventWithNullRepositoryEventType() {
        new RepositoryEvent(null, mock(Resource.class), mock(Project.class));
    }

    @Test(expected = NullPointerException.class)
    public void testNewRepositoryEventWithNullResource() {
        new RepositoryEvent(PROJECT_RESOURCE_CREATED, null, mock(Project.class));
    }

    @Test(expected = NullPointerException.class)
    public void testNewRepositoryEventWithNullProject() {
        new RepositoryEvent(PROJECT_RESOURCE_CREATED, mock(Resource.class), null);
    }
}
