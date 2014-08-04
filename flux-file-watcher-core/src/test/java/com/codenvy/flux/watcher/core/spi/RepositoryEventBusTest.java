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
package com.codenvy.flux.watcher.core.spi;

import com.codenvy.flux.watcher.core.RepositoryEvent;
import com.codenvy.flux.watcher.core.RepositoryEventBus;
import com.codenvy.flux.watcher.core.RepositoryEventTypes;
import com.codenvy.flux.watcher.core.RepositoryListener;
import com.codenvy.flux.watcher.core.Resource;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static com.codenvy.flux.watcher.core.RepositoryEventType.ENTRY_CREATED;
import static com.codenvy.flux.watcher.core.RepositoryEventType.ENTRY_DELETED;
import static com.codenvy.flux.watcher.core.RepositoryEventType.ENTRY_MODIFIED;
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
    public void testFireRepositoryEvent() {
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
        final Resource resource = Resource.newFolder("codenvy-project-id", "src", System.currentTimeMillis());

        final RepositoryEvent entryCreatedEvent = new RepositoryEvent(ENTRY_CREATED, resource);
        repositoryEventBus.fireRepositoryEvent(entryCreatedEvent);

        final RepositoryEvent entryDeletedEvent = new RepositoryEvent(ENTRY_DELETED, resource);
        repositoryEventBus.fireRepositoryEvent(entryDeletedEvent);

        final RepositoryEvent entryModifiedEvent = new RepositoryEvent(ENTRY_MODIFIED, resource);
        repositoryEventBus.fireRepositoryEvent(entryModifiedEvent);
    }

    public static class AbstractRepositoryListener implements RepositoryListener {
        public final RepositoryListener mock;

        public AbstractRepositoryListener() {
            this.mock = mock(RepositoryListener.class);
        }

        @Override
        public void onEvent(RepositoryEvent event) {
            mock.onEvent(event);
        }
    }


    @RepositoryEventTypes(ENTRY_CREATED)
    public static class EntryCreatedListener extends AbstractRepositoryListener {
    }

    @RepositoryEventTypes(ENTRY_DELETED)
    public static class EntryDeletedListener extends AbstractRepositoryListener {
    }

    @RepositoryEventTypes(ENTRY_MODIFIED)
    public static class EntryModifiedListener extends AbstractRepositoryListener {
    }

    @RepositoryEventTypes({ENTRY_CREATED, ENTRY_MODIFIED})
    public static class EntryCreatedAndModifiedListener extends AbstractRepositoryListener {
    }
}
