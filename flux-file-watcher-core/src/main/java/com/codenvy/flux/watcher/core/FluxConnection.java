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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.codenvy.flux.watcher.core.Message.Fields.USERNAME;
import static com.codenvy.flux.watcher.core.MessageType.CONNECT_TO_CHANNEL;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.notNull;
import static java.util.Collections.emptySet;

/**
 * Represents a connection to a Flux remote.
 *
 * @author Kevin Pollet
 */
public class FluxConnection {
    private final SocketIO            socket;
    private final Credentials         credentials;
    private final Set<MessageHandler> messageHandlers;

    /**
     * Constructs an instance of {@code FluxConnection}.
     *
     * @param serverURL
     *         the server {@link java.net.URL} to connect to.
     * @param credentials
     *         the {@link com.codenvy.flux.watcher.core.Credentials} used to connect.
     * @param messageHandlers
     *         the {@link com.codenvy.flux.watcher.core.MessageHandler} to add before opening the connection.
     * @throws java.lang.NullPointerException
     *         if {@code serverURL}, {@code credentials} or {@code messageHandlers} parameter is {@code null}.
     */
    FluxConnection(URL serverURL, Credentials credentials, Set<MessageHandler> messageHandlers) {
        this.socket = new SocketIO(checkNotNull(serverURL));
        this.credentials = checkNotNull(credentials);
        this.messageHandlers = new CopyOnWriteArraySet<>(checkNotNull(messageHandlers));
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

                        final JSONObject message = new JSONObject();
                        message.put("channel", credentials.username());

                        sendMessage(new Message(CONNECT_TO_CHANNEL, message));

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

                @Override
                public void on(String messageType, IOAcknowledge ioAcknowledge, Object... objects) {
                    final Set<MessageHandler> messageHandlers = getMessageHandlersFor(messageType);
                    for (MessageHandler oneMessageHandler : messageHandlers) {
                        final Message message = new Message(FluxConnection.this, MessageType.fromType(messageType), (JSONObject)objects[0]);
                        oneMessageHandler.onMessage(message);
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
     * Adds a {@link com.codenvy.flux.watcher.core.MessageHandler}.
     *
     * @param handler
     *         the {@link com.codenvy.flux.watcher.core.MessageHandler} to add.
     * @return {@code true} if the {@code handler} is not already added, {@code false} otherwise.
     * @throws java.lang.NullPointerException
     *         if {@code handler} parameter is {@code null}.
     */
    public boolean addMessageHandler(MessageHandler handler) {
        return messageHandlers.add(checkNotNull(handler));
    }

    /**
     * Removes a {@link com.codenvy.flux.watcher.core.MessageHandler}.
     *
     * @param handler
     *         the {@link com.codenvy.flux.watcher.core.MessageHandler} to remove.
     * @return {@code true} if the {@code handler} was already added, {@code false} otherwise.
     * @throws java.lang.NullPointerException
     *         if {@code handler} parameter is {@code null}.
     */
    public boolean removeMessageHandler(MessageHandler handler) {
        return messageHandlers.remove(checkNotNull(handler));
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

        socket.emit(message.type().value(), message);
    }

    private Set<MessageHandler> getMessageHandlersFor(final String messageType) {
        return FluentIterable.from(messageHandlers)
                             .filter(notNull())
                             .filter(new Predicate<MessageHandler>() {
                                 @Override
                                 public boolean apply(MessageHandler messageHandler) {
                                     final Set<String> supportedTypes = getMessageTypesFor(messageHandler);
                                     return supportedTypes.contains(messageType);
                                 }
                             })
                             .toSet();
    }

    private Set<String> getMessageTypesFor(MessageHandler messageHandler) {
        final MessageTypes types = messageHandler.getClass().getAnnotation(MessageTypes.class);
        if (types == null) {
            return emptySet();
        }

        return FluentIterable.from(Arrays.asList(types.value()))
                             .filter(Predicates.notNull())
                             .transform(new Function<MessageType, String>() {
                                 @Override
                                 public String apply(MessageType messageType) {
                                     return messageType.value();
                                 }
                             })
                             .toSet();
    }
}
