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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.notNull;
import static java.util.Collections.emptySet;

/**
 * Message bus connected to Flux instance.
 *
 * @author Kevin Pollet
 */
@Singleton
public class FluxMessageBus {
    private final ConcurrentMap<URL, FluxConnection> connections;
    private final Set<MessageHandler>                messageHandlers;

    /**
     * Constructs an instance of {@link FluxMessageBus}.
     *
     * @param messageHandlers
     *         the {@link com.codenvy.flux.watcher.core.MessageHandler} to register.
     * @throws java.lang.NullPointerException
     *         if {@code messageHandlers} is {@code null}.
     */
    @Inject
    FluxMessageBus(Set<MessageHandler> messageHandlers) {
        this.messageHandlers = new CopyOnWriteArraySet<>(checkNotNull(messageHandlers));
        this.connections = new ConcurrentHashMap<>();
    }

    /**
     * Open a connection to the given server {@link java.net.URL}.
     *
     * @param serverURL
     *         the server {@link java.net.URL} to connect to.
     * @param credentials
     *         the {@link Credentials} to use for the connection.
     * @return the opened {@link com.codenvy.flux.watcher.core.FluxConnection} or the existing {@link
     * com.codenvy.flux.watcher.core.FluxConnection} if already opened.
     * @throws java.lang.NullPointerException
     *         if {@code serverURL} or {@code credentials} parameter is {@code null}.
     */
    public FluxConnection connect(URL serverURL, Credentials credentials) {
        checkNotNull(serverURL);
        checkNotNull(credentials);

        FluxConnection connection = connections.get(serverURL);
        if (connection == null) {
            FluxConnection newConnection = new FluxConnection(serverURL, credentials, this);
            connection = connections.putIfAbsent(serverURL, newConnection);
            if (connection == null) {
                connection = newConnection.open();
            }
        }

        return connection;
    }

    /**
     * Close the connection to the given server {@link java.net.URL}.
     *
     * @param serverURL
     *         the server {@link java.net.URL} to disconnect from.
     * @throws java.lang.NullPointerException
     *         if {@code serverURL} parameter is {@code null}.
     */
    public void disconnect(URL serverURL) {
        final FluxConnection connection = connections.remove(checkNotNull(serverURL));
        if (connection != null) {
            connection.close();
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
     * Broadcast messages to all opened {@link com.codenvy.flux.watcher.core.FluxConnection}.
     *
     * @param messages
     *         the {@link com.codenvy.flux.watcher.core.Message} to broadcast.
     * @throws java.lang.NullPointerException
     *         if {@code messages} parameter is {@code null}.
     */
    public void sendMessages(Message... messages) {
        checkNotNull(messages);

        for (Message oneMessage : messages) {
            checkNotNull(oneMessage);

            for (FluxConnection oneConnection : connections.values()) {
                oneConnection.sendMessage(oneMessage);
            }
        }
    }

    /**
     * Fires the given {@link com.codenvy.flux.watcher.core.Message} to all {@link com.codenvy.flux.watcher.core.MessageHandler}
     * registered.
     *
     * @param message
     *         the message.
     * @throws java.lang.NullPointerException
     *         if {@code message} parameter is {@code null}.
     */
    public void messageReceived(Message message) {
        checkNotNull(message);

        final Set<MessageHandler> messageHandlers = getMessageHandlersFor(message.type().value());
        for (MessageHandler oneMessageHandler : messageHandlers) {
            oneMessageHandler.onMessage(message);
        }
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
