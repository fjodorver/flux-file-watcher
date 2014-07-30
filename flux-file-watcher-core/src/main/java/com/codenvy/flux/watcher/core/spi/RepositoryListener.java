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
 * Listener used to be notified for {@link com.codenvy.flux.watcher.core.spi.RepositoryProvider} events.
 *
 * @author Kevin Pollet
 */
public interface RepositoryListener {
    /**
     * Called when an events is fired by the {@link com.codenvy.flux.watcher.core.spi.RepositoryProvider}.
     *
     * @param event
     *         the {@link com.codenvy.flux.watcher.core.spi.RepositoryEvent} instance, never {@code null}.
     */
    void onEvent(RepositoryEvent event);
}
