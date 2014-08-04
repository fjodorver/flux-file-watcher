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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class representing a Flux message.
 *
 * @author Kevin Pollet
 */
public class Message {
    private final FluxConnection source;
    private final MessageType    type;
    private final JSONObject     content;

    /**
     * Constructs an instance of {@link com.codenvy.flux.watcher.core.Message}.
     *
     * @param type
     *         the {@link com.codenvy.flux.watcher.core.MessageType}.
     * @param content
     *         the message {@link org.json.JSONObject} content.
     * @throws java.lang.NullPointerException
     *         if {@code type} or {@code content} parameter is {@code null}.
     */
    public Message(MessageType type, JSONObject content) {
        this(null, type, content);
    }

    /**
     * Constructs an instance of {@link com.codenvy.flux.watcher.core.Message}.
     *
     * @param source
     *         the {@code FluxConnection} where the {@link com.codenvy.flux.watcher.core.Message} comes from.
     * @param type
     *         the {@link com.codenvy.flux.watcher.core.MessageType}.
     * @param content
     *         the message {@link org.json.JSONObject} content.
     * @throws java.lang.NullPointerException
     *         if {@code type} or {@code content} parameter is {@code null}.
     */
    public Message(FluxConnection source, MessageType type, JSONObject content) {
        this.source = source;
        this.type = checkNotNull(type);
        this.content = checkNotNull(content);
    }

    /**
     * Returns the {@code FluxConnection} where the {@link com.codenvy.flux.watcher.core.Message} comes from.
     *
     * @return the {@code FluxConnection} where the {@link com.codenvy.flux.watcher.core.Message} comes from or {@code null} if none.
     */
    public FluxConnection source() {
        return source;
    }

    /**
     * Returns the {@link com.codenvy.flux.watcher.core.MessageType}.
     *
     * @return the {@link com.codenvy.flux.watcher.core.MessageType}, never {@code null}.
     */
    public MessageType type() {
        return type;
    }

    /**
     * Returns the {@link com.codenvy.flux.watcher.core.Message} content.
     *
     * @return the {@link com.codenvy.flux.watcher.core.Message} content, never {@code null}.
     */
    public JSONObject content() {
        return content;
    }

    /**
     * Fields used in {@link org.json.JSONObject} of Flux messages.
     *
     * @author Kevin Pollet
     */
    public enum Fields {
        USERNAME("username"),
        CALLBACK_ID("callback_id"),
        REQUEST_SENDER_ID("requestSenderID"),
        PROJECT_NAME("project"),
        RESOURCE_PATH("resource"),
        RESOURCE_TYPE("type"),
        RESOURCE_TIMESTAMP("timestamp"),
        RESOURCE_HASH("hash"),
        RESOURCE_CONTENT("content");

        private final String value;

        Fields(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }
}
