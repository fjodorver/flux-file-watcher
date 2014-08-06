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
 * The types of Flux message.
 *
 * @author Kevin Pollet
 * @see com.codenvy.flux.watcher.core.Message
 */
public enum MessageType {
    CONNECT_TO_CHANNEL("connectToChannel"),
    GET_PROJECT_REQUEST("getProjectRequest"),
    GET_PROJECT_RESPONSE("getProjectResponse"),
    GET_RESOURCE_REQUEST("getResourceRequest"),
    GET_RESOURCE_RESPONSE("getResourceResponse"),
    PROJECT_CONNECTED("projectConnected"),
    PROJECT_DISCONNECTED("projectDisconnected"),
    RESOURCE_CHANGED("resourceChanged"),
    RESOURCE_CREATED("resourceCreated"),
    RESOURCE_DELETED("resourceDeleted"),
    RESOURCE_STORED("resourceStored");

    private final String value;

    /**
     * Constructs an instance of {@link com.codenvy.flux.watcher.core.MessageType}.
     *
     * @param value
     *         the {@link com.codenvy.flux.watcher.core.MessageType} value.
     */
    MessageType(String value) {
        this.value = value;
    }

    /**
     * Returns the {@link com.codenvy.flux.watcher.core.MessageType} corresponding to the given value.
     *
     * @param value
     *         the {@link com.codenvy.flux.watcher.core.MessageType} value.
     * @return the {@link com.codenvy.flux.watcher.core.MessageType} corresponding to the given value or {@code null} if none.
     */
    public static MessageType fromType(String value) {
        final MessageType[] messageTypes = MessageType.values();
        for (MessageType oneMessageType : messageTypes) {
            if (oneMessageType.value.equals(value)) {
                return oneMessageType;
            }
        }
        return null;
    }

    /**
     * Returns the {@link com.codenvy.flux.watcher.core.MessageType} value.
     *
     * @return the {@link com.codenvy.flux.watcher.core.MessageType} value, never {@code null}.
     */
    public String value() {
        return value;
    }
}
