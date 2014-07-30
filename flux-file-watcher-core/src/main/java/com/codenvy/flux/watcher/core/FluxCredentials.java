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
 * Credentials used to connect to the Flux server.
 *
 * @author Kevin Pollet
 */
public final class FluxCredentials {
    private final String username;
    private final String token;

    /**
     * Constructs an instance of {@link FluxCredentials}.
     *
     * @param username
     *         the username.
     * @throws java.lang.NullPointerException
     *         if {@code username} parameter is {@code null}.
     */
    public FluxCredentials(String username) {
        this(username, null);
    }

    /**
     * Constructs an instance of {@link FluxCredentials}.
     *
     * @param username
     *         the username.
     * @param token
     *         the user token.
     * @throws java.lang.NullPointerException
     *         if {@code username} parameter is {@code null}.
     */
    public FluxCredentials(String username, String token) {
        this.username = checkNotNull(username);
        this.token = checkNotNull(token);
    }

    /**
     * Returns the username.
     *
     * @return the username.
     */
    public String username() {
        return username;
    }

    /**
     * Returns the user token.
     *
     * @return the user token.
     */
    public String token() {
        return token;
    }
}
