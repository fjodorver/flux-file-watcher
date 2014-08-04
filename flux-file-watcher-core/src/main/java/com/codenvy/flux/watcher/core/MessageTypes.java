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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used to indicate which {@link com.codenvy.flux.watcher.core.MessageType} the {@link
 * com.codenvy.flux.watcher.core.MessageHandler} can handle.
 *
 * @author Kevin Pollet
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface MessageTypes {
    /**
     * Returns the {@link com.codenvy.flux.watcher.core.MessageType}  the {@link
     * com.codenvy.flux.watcher.core.MessageHandler} can handle.
     *
     * @return the {@link com.codenvy.flux.watcher.core.MessageType}  the {@link
     * com.codenvy.flux.watcher.core.MessageHandler} can handle.
     */
    MessageType[] value();
}
