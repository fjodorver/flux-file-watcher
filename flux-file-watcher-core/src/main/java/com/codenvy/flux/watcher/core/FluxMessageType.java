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
 * @see FluxMessage
 */
public enum FluxMessageType {
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
     * Constructs an instance of {@link FluxMessageType}.
     *
     * @param value
     *         the {@link FluxMessageType} value.
     */
    FluxMessageType(String value) {
        this.value = value;
    }

    /**
     * Returns the {@link FluxMessageType} corresponding to the given value.
     *
     * @param value
     *         the {@link FluxMessageType} value.
     * @return the {@link FluxMessageType} corresponding to the given value or {@code null} if none.
     */
    public static FluxMessageType fromType(String value) {
        final FluxMessageType[] messageTypes = FluxMessageType.values();
        for (FluxMessageType oneMessageType : messageTypes) {
            if (oneMessageType.value.equals(value)) {
                return oneMessageType;
            }
        }
        return null;
    }

    /**
     * Returns the {@link FluxMessageType} value.
     *
     * @return the {@link FluxMessageType} value, never {@code null}.
     */
    public String value() {
        return value;
    }
}
