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

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static com.codenvy.flux.watcher.core.RepositoryEventType.PROJECT_RESOURCE_CREATED;
import static com.codenvy.flux.watcher.core.RepositoryEventType.PROJECT_RESOURCE_DELETED;
import static com.codenvy.flux.watcher.core.RepositoryEventType.PROJECT_RESOURCE_MODIFIED;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * {@link com.codenvy.flux.watcher.core.RepositoryEventBus} tests
 *
 * @author Kevin Pollet
 */
public final class RepositoryEventBusTest {
    private RepositoryEventBus repositoryEventBus;

    @Before
    public void beforeTest() {
        this.repositoryEventBus = new RepositoryEventBus(Collections.<RepositoryListener>emptySet());
    }

    @Test(expected = NullPointerException.class)
    public void testAddRepositoryListenerWithNullListener() {
        repositoryEventBus.addRepositoryListener(null);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveRepositoryListenerWithNullListener() {
        repositoryEventBus.removeRepositoryListener(null);
    }

    @Test(expected = NullPointerException.class)
    public void testFireRepositoryEventWithNullEvent() {
        repositoryEventBus.fireRepositoryEvent(null);
    }

    @Test
    public void testFireRepositoryEvent() throws Exception {
        final EntryCreatedListener entryCreatedListener = new EntryCreatedListener();
        repositoryEventBus.addRepositoryListener(entryCreatedListener);

        final EntryModifiedListener entryModifiedListener = new EntryModifiedListener();
        repositoryEventBus.addRepositoryListener(entryModifiedListener);

        final EntryDeletedListener entryDeletedListener = new EntryDeletedListener();
        repositoryEventBus.addRepositoryListener(entryDeletedListener);

        final EntryCreatedAndModifiedListener entryCreatedAndModifiedListener = new EntryCreatedAndModifiedListener();
        repositoryEventBus.addRepositoryListener(entryCreatedAndModifiedListener);

        fireAllEventTypes();

        verify(entryCreatedListener.mock, times(1)).onEvent(any(RepositoryEvent.class));
        verify(entryModifiedListener.mock, times(1)).onEvent(any(RepositoryEvent.class));
        verify(entryDeletedListener.mock, times(1)).onEvent(any(RepositoryEvent.class));
        verify(entryCreatedAndModifiedListener.mock, times(2)).onEvent(any(RepositoryEvent.class));
    }

    private void fireAllEventTypes() {
        final RepositoryEvent entryCreatedEvent = new RepositoryEvent(PROJECT_RESOURCE_CREATED, mock(Resource.class), mock(Project.class));
        repositoryEventBus.fireRepositoryEvent(entryCreatedEvent);

        final RepositoryEvent entryDeletedEvent = new RepositoryEvent(PROJECT_RESOURCE_DELETED, mock(Resource.class), mock(Project.class));
        repositoryEventBus.fireRepositoryEvent(entryDeletedEvent);

        final RepositoryEvent entryModifiedEvent =
                new RepositoryEvent(PROJECT_RESOURCE_MODIFIED, mock(Resource.class), mock(Project.class));
        repositoryEventBus.fireRepositoryEvent(entryModifiedEvent);
    }

    public static class AbstractRepositoryListener implements RepositoryListener {
        public final RepositoryListener mock;

        public AbstractRepositoryListener() {
            this.mock = mock(RepositoryListener.class);
        }

        @Override
        public void onEvent(RepositoryEvent event) throws Exception {
            mock.onEvent(event);
        }
    }

    @RepositoryEventTypes(PROJECT_RESOURCE_CREATED)
    public static class EntryCreatedListener extends AbstractRepositoryListener {
    }

    @RepositoryEventTypes(PROJECT_RESOURCE_DELETED)
    public static class EntryDeletedListener extends AbstractRepositoryListener {
    }

    @RepositoryEventTypes(PROJECT_RESOURCE_MODIFIED)
    public static class EntryModifiedListener extends AbstractRepositoryListener {
    }

    @RepositoryEventTypes({PROJECT_RESOURCE_CREATED, PROJECT_RESOURCE_MODIFIED})
    public static class EntryCreatedAndModifiedListener extends AbstractRepositoryListener {
    }
}
