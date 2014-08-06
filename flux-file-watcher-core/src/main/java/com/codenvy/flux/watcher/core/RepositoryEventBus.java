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

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.notNull;
import static java.util.Arrays.asList;

/**
 * Event bus to listen and fire {@link com.codenvy.flux.watcher.core.spi.Repository} events.
 *
 * @author Kevin Pollet
 * @see com.codenvy.flux.watcher.core.RepositoryEvent
 */
@Singleton
public class RepositoryEventBus {
    private final Set<RepositoryListener> repositoryListeners;

    /**
     * Constructs an instance of {@link com.codenvy.flux.watcher.core.RepositoryEventBus}.
     *
     * @param repositoryListeners
     *         the repository listeners to register.
     * @throws java.lang.NullPointerException
     *         if {@code repositoryListeners} parameter is {@code null}.
     */
    @Inject
    public RepositoryEventBus(Set<RepositoryListener> repositoryListeners) {
        this.repositoryListeners = new CopyOnWriteArraySet<>(checkNotNull(repositoryListeners));
    }

    /**
     * Adds a {@link com.codenvy.flux.watcher.core.RepositoryListener}.
     *
     * @param listener
     *         the {@link com.codenvy.flux.watcher.core.RepositoryListener} to add.
     * @return {@code true} if the listener was not already added, {@code false} otherwise.
     * @throws java.lang.NullPointerException
     *         if {@code listener} parameter is {@code null}.
     */
    public boolean addRepositoryListener(RepositoryListener listener) {
        return repositoryListeners.add(checkNotNull(listener));
    }

    /**
     * Removes a {@link com.codenvy.flux.watcher.core.RepositoryListener}.
     *
     * @param listener
     *         the {@link com.codenvy.flux.watcher.core.RepositoryListener} to remove.
     * @return {@code true} if the listener has been removed, {@code false} otherwise.
     * @throws java.lang.NullPointerException
     *         if {@code listener} parameter is {@code null}.
     */
    public boolean removeRepositoryListener(RepositoryListener listener) {
        return repositoryListeners.remove(checkNotNull(listener));
    }

    /**
     * Fires a {@link com.codenvy.flux.watcher.core.RepositoryEvent} to all {@link com.codenvy.flux.watcher.core.RepositoryListener}
     * registered.
     *
     * @param event
     *         the {@link com.codenvy.flux.watcher.core.RepositoryEvent} to fire.
     * @throws java.lang.NullPointerException
     *         if {@code event} parameter is {@code null}.
     */
    public void fireRepositoryEvent(final RepositoryEvent event) {
        checkNotNull(event);

        final Set<RepositoryListener> filteredRepositoryListeners = FluentIterable
                .from(repositoryListeners)
                .filter(notNull())
                .filter(new Predicate<RepositoryListener>() {
                    @Override
                    public boolean apply(RepositoryListener listener) {
                        final RepositoryEventTypes repositoryEventTypes = listener.getClass().getAnnotation(RepositoryEventTypes.class);
                        return asList(repositoryEventTypes.value()).contains(event.type());
                    }
                })
                .toSet();

        for (RepositoryListener oneRepositoryListener : filteredRepositoryListeners) {
            oneRepositoryListener.onEvent(event);
        }
    }
}
