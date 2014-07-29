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

/**
 * @author Kevin Pollet
 */
public class RepositoryEvent {
    private final RepositoryEventType type;
    private final Resource            resource;

    public RepositoryEvent(RepositoryEventType type, Resource resource) {
        this.type = type;
        this.resource = resource;
    }

    public RepositoryEventType type() {
        return type;
    }

    public Resource resource() {
        return resource;
    }
}
