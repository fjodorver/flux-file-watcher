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
 * Interface implemented to be advise when a {@link com.codenvy.flux.watcher.core.Message} is received by a {@link
 * com.codenvy.flux.watcher.core.FluxConnection}.
 *
 * @author Kevin Pollet
 */
public interface MessageHandler {
    /**
     * Method called when a {@link com.codenvy.flux.watcher.core.Message} is received.
     *
     * @param message
     *         the {@link com.codenvy.flux.watcher.core.Message} instance, never {@code null}.
     */
    void onMessage(Message message);
}
