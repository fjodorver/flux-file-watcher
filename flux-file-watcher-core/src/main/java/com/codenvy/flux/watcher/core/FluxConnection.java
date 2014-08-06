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

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import static com.codenvy.flux.watcher.core.Message.Fields.CHANNEL;
import static com.codenvy.flux.watcher.core.Message.Fields.CONNECTED_TO_CHANNEL;
import static com.codenvy.flux.watcher.core.Message.Fields.USERNAME;
import static com.codenvy.flux.watcher.core.MessageType.CONNECT_TO_CHANNEL;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a connection to a Flux remote.
 *
 * @author Kevin Pollet
 */
public class FluxConnection {
    private final SocketIO       socket;
    private final FluxMessageBus messageBus;
    private final Credentials    credentials;

    /**
     * Constructs an instance of {@code FluxConnection}.
     *
     * @param serverURL
     *         the server {@link java.net.URL} to connect to.
     * @param credentials
     *         the {@link Credentials} used to connect.
     * @param messageBus
     *         the {@link com.codenvy.flux.watcher.core.FluxMessageBus} instance.
     * @throws java.lang.NullPointerException
     *         if {@code serverURL}, {@code credentials} or {@code messageBus} parameter is {@code null}.
     */
    FluxConnection(URL serverURL, Credentials credentials, FluxMessageBus messageBus) {
        this.messageBus = checkNotNull(messageBus);
        this.socket = new SocketIO(checkNotNull(serverURL));
        this.credentials = checkNotNull(credentials);
    }

    /**
     * Open the connection.
     *
     * @return the opened {@link com.codenvy.flux.watcher.core.FluxConnection} instance.
     */
    FluxConnection open() {
        if (!socket.isConnected()) {
            socket.connect(new IOCallback() {
                @Override
                public void onDisconnect() {

                }

                @Override
                public void onConnect() {
                    try {

                        final JSONObject content = new JSONObject().put(CHANNEL.value(), credentials.username());
                        socket.emit(CONNECT_TO_CHANNEL.value(), new IOAcknowledge() {
                            @Override
                            public void ack(Object... objects) {
                                if (objects.length == 1 && objects[0] instanceof JSONObject) {
                                    final JSONObject ack = (JSONObject)objects[0];
                                    try {

                                        if (ack.has(CONNECTED_TO_CHANNEL.value()) && ack.getBoolean(CONNECTED_TO_CHANNEL.value())) {
                                            return;
                                        }

                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                socket.disconnect();
                            }
                        }, content);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onMessage(String s, IOAcknowledge ioAcknowledge) {

                }

                @Override
                public void onMessage(JSONObject jsonObject, IOAcknowledge ioAcknowledge) {

                }

                //TODO in flux implementation the username is checked, what to do?
                @Override
                public void on(String name, IOAcknowledge ioAcknowledge, Object... objects) {
                    final MessageType messageType = MessageType.fromType(name);
                    if (messageType != null && objects.length > 0 && objects[0] instanceof JSONObject) {
                        final Message message = new Message(FluxConnection.this, messageType, (JSONObject)objects[0]);
                        messageBus.messageReceived(message);
                    }
                }

                @Override
                public void onError(SocketIOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return this;
    }

    /**
     * Close the connection.
     */
    void close() {
        if (socket.isConnected()) {
            socket.disconnect();
        }
    }

    /**
     * Sends a {@link com.codenvy.flux.watcher.core.Message} on this connection
     *
     * @param message
     *         the {@link com.codenvy.flux.watcher.core.Message} instance to send.
     * @throws java.lang.NullPointerException
     *         if {@code message} parameter is {@code null}.
     */
    public void sendMessage(Message message) {
        checkNotNull(message);

        final JSONObject content = message.content();
        if (!content.has(USERNAME.value())) {
            try {
                content.put(USERNAME.value(), credentials.username());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        socket.emit(message.type().value(), message.content());
    }
}
