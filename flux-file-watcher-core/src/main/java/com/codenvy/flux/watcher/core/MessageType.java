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
 * @author Kevin Pollet
 */
//TODO naming
public enum MessageType {
    CONNECT_TO_CHANNEL("connectToChannel"),
    GET_PROJECT_REQUEST("getProjectRequest"),
    GET_PROJECT_RESPONSE("getProjectResponse"),
    GET_RESOURCE_REQUEST("getResourceRequest"),
    GET_RESOURCE_RESPONSE("getResourceResponse"),
    RESOURCE_CREATED("resourceCreated"),
    RESOURCE_CHANGED("resourceChanged"),
    RESOURCE_DELETED("resourceDeleted");

    private final String messageType;

    MessageType(String messageType) {
        this.messageType = messageType;
    }

    public static MessageType fromType(String type) {
        final MessageType[] messageTypes = MessageType.values();
        for (MessageType oneMessageType : messageTypes) {
            if (oneMessageType.messageType.equals(type)) {
                return oneMessageType;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return messageType;
    }
}
