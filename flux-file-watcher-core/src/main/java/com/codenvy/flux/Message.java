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
package com.codenvy.flux;

import org.json.JSONObject;

/**
 * @author Kevin Pollet
 */
public class Message {
    private final FluxConnection connection;
    private final MessageType    type;
    private final JSONObject     content;

    public Message(MessageType type, JSONObject content) {
        this(null, type, content);
    }

    public Message(FluxConnection connection, MessageType type, JSONObject content) {
        this.connection = connection;
        this.type = type;
        this.content = content;
    }

    public FluxConnection connection() {
        return connection;
    }

    public MessageType type() {
        return type;
    }

    public JSONObject content() {
        return content;
    }
}
