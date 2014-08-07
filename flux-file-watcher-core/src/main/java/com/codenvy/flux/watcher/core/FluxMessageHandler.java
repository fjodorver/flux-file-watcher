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

/**
 * Interface implemented to be advise when a {@link FluxMessage} is received by a {@link
 * com.codenvy.flux.watcher.core.FluxConnection}.
 *
 * @author Kevin Pollet
 */
public interface FluxMessageHandler {
    /**
     * Method called when a {@link FluxMessage} is received.
     *
     * @param message
     *         the {@link FluxMessage} instance, never {@code null}.
     * @param repository
     *         the {@link com.codenvy.flux.watcher.core.FluxRepository} instance, never {@code null}.
     * @throws java.lang.Exception
     *         if something goes wrong.
     */
    void onMessage(FluxMessage message, FluxRepository repository) throws Exception;
}
