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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Predicates.notNull;
import static java.util.Collections.emptySet;

/**
 * @author Kevin Pollet
 */
// TODO implement authentication
public class FluxConnection {
    private final static Logger logger = LoggerFactory.getLogger(FluxConnection.class);

    private final SocketIO            socket;
    private final Set<MessageHandler> messageHandlers;
    private final Object              messageHandlersLock;

    public FluxConnection(URL serverURL) {
        this.socket = new SocketIO(serverURL);
        this.messageHandlers = new HashSet<>();
        this.messageHandlersLock = new Object();
    }

    void open() {
        if (!socket.isConnected()) {
            socket.connect(new IOCallback() {
                @Override
                public void onDisconnect() {

                }

                @Override
                public void onConnect() {
                    try {

                        final JSONObject message = new JSONObject();
                        message.put("channel", "defaultuser");

                        sendMessage(new Message(MessageType.CONNECT_TO_CHANNEL, message));

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
                    synchronized (messageHandlersLock) {
                        final Set<MessageHandler> messageHandlers = getHandlersFor(messageType);
                        for (MessageHandler oneMessageHandler : messageHandlers) {
                            final Message message =
                                    new Message(FluxConnection.this, MessageType.fromType(messageType), (JSONObject)objects[0]);
                            oneMessageHandler.onMessage(message);
                        }
                    }
                }

                @Override
                public void onError(SocketIOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    void close() {
        if (socket.isConnected()) {
            socket.disconnect();
        }
    }

    public boolean addMessageHandler(MessageHandler messageHandler) {
        synchronized (messageHandlersLock) {
            return messageHandlers.add(messageHandler);
        }
    }

    public boolean removeMessageHandler(MessageHandler messageHandler) {
        synchronized (messageHandlersLock) {
            return messageHandlers.remove(messageHandler);
        }
    }

    public void sendMessage(Message message) {
        socket.emit(message.type().toString(), message);
    }

    private Set<MessageHandler> getHandlersFor(final String messageType) {
        return FluentIterable.from(messageHandlers)
                             .filter(notNull())
                             .filter(new Predicate<MessageHandler>() {
                                 @Override
                                 public boolean apply(MessageHandler messageHandler) {
                                     final Set<String> supportedTypes = getSupportedMessageTypesFor(messageHandler);
                                     return supportedTypes.contains(messageType);
                                 }
                             })
                             .toImmutableSet();
    }

    private Set<String> getSupportedMessageTypesFor(MessageHandler messageHandler) {
        final MessageTypes types = messageHandler.getClass().getAnnotation(MessageTypes.class);
        if (types == null) {
            return emptySet();
        }

        return FluentIterable.from(Arrays.asList(types.value()))
                             .filter(Predicates.notNull())
                             .transform(new Function<MessageType, String>() {
                                 @Override
                                 public String apply(MessageType messageType) {
                                     return messageType.toString();
                                 }
                             })
                             .toImmutableSet();
    }
}
