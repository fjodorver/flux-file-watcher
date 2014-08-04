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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Event sent when a modification is done on a repository.
 *
 * @author Kevin Pollet
 * @see RepositoryEventType
 * @see Resource
 */
public class RepositoryEvent {
    private final RepositoryEventType type;
    private final Resource            resource;

    /**
     * Constructs an instance of {@link RepositoryEvent}.
     *
     * @param type
     *         the {@link RepositoryEventType}.
     * @param resource
     *         the {@link Resource} source of the event.
     * @throws java.lang.NullPointerException
     *         if {@code type} or {@code resource} parameter is {@code null}.
     */
    public RepositoryEvent(RepositoryEventType type, Resource resource) {
        this.type = checkNotNull(type);
        this.resource = checkNotNull(resource);
    }

    /**
     * Returns the {@link RepositoryEventType} of this event.
     *
     * @return the {@link RepositoryEventType}, never {@code null}.
     */
    public RepositoryEventType type() {
        return type;
    }

    /**
     * Returns the {@link Resource} source this event.
     *
     * @return the {@link Resource}, never {@code null}.
     */
    public Resource resource() {
        return resource;
    }
}
