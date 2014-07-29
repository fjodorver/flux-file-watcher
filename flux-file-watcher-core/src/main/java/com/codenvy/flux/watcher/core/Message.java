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

import org.json.JSONObject;

/**
 * @author Kevin Pollet
 */
public class Message {
    private final FluxConnection source;
    private final MessageType    type;
    private final JSONObject     content;

    public Message(MessageType type, JSONObject content) {
        this(null, type, content);
    }

    public Message(FluxConnection source, MessageType type, JSONObject content) {
        this.source = source;
        this.type = type;
        this.content = content;
    }

    public FluxConnection source() {
        return source;
    }

    public MessageType type() {
        return type;
    }

    public JSONObject content() {
        return content;
    }
}
