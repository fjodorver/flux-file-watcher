package com.codenvy.flux.watcher.core.service.impl;

import com.codenvy.flux.watcher.core.Credentials;
import com.codenvy.flux.watcher.core.connection.IConnection;
import com.codenvy.flux.watcher.core.connection.IListener;
import com.codenvy.flux.watcher.core.connection.impl.SocketIOConnection;
import com.codenvy.flux.watcher.core.enums.ConnectionStatus;
import com.codenvy.flux.watcher.core.event.IEvent;
import com.codenvy.flux.watcher.core.model.Project;
import com.codenvy.flux.watcher.core.service.ConnectionService;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

@Singleton
public class ConnectionServiceImpl implements ConnectionService {

    private int callbackID = (int) UUID.randomUUID().getMostSignificantBits();

    private String senderID;

    private Map<URI, IConnection> connectionMap = Maps.newConcurrentMap();

    private Map<String, IEvent> eventMap = Maps.newConcurrentMap();

    @Inject
    private EventBus eventBus;

    @Override
    public void addRemote(URI uri, Credentials credentials) {
        IConnection connection = connectionMap.get(uri);
        if(connection == null){
            connection = new SocketIOConnection(uri, credentials);
            connectionMap.put(uri, connection);
        }
        connection.setListener(new IListener() {
            @Override
            public void onConnectionEvent(ConnectionStatus status) {

            }

            @Override
            public void onChannelEvent(ConnectionStatus status, String channel) {

            }

            @Override
            public void onMessage(String type, JsonElement content) {
                JsonObject message = content.getAsJsonObject();
                if(message.has("requestSenderID")){
                    senderID = message.get("requestSenderID").getAsString();
                }
                if(message.has("responseSenderID")){
                    senderID = message.get("responseSenderID").getAsString();
                }
                IEvent event = eventMap.get(type);
                if(event != null){
                    event.deserialize(message);
                    eventBus.post(event);
                }
            }
        });
        connection.open();
    }

    @Override
    public void removeRemote(URI uri) {
        IConnection connection = connectionMap.get(uri);
        if(connection != null){
            connection.close();
            connectionMap.remove(uri);
        }
    }

    @Override
    public void connectToChannel(URI uri, String channel) {
        IConnection connection = connectionMap.get(uri);
        if(connection != null){
            connection.connectToChannel(channel);
        }
    }

    @Override
    public void disconnectFromChannel(URI uri, String channel) {
        IConnection connection = connectionMap.get(uri);
        if(connection != null){
            connection.disconnectFromChannel(channel);
        }
    }

    @Override
    public void connectProject(Project project) {
        JsonObject message = new JsonObject();
        message.addProperty("project", project.getName());
        sendMessage("projectConnected", message);
        message.addProperty("includeDeleted", true);
        sendMessage("getProjectRequest", message);
    }

    @Override
    public void disconnectProject(Project project) {
        JsonObject message = new JsonObject();
        message.addProperty("project", project.getName());
        sendMessage("projectDisconnected", message);
    }

    @Override
    public void sendMessage(String type, JsonElement content) {
        JsonObject message = content.getAsJsonObject();
        for (IConnection connection : connectionMap.values()) {
            message.addProperty("callback_id", callbackID);
            message.addProperty("requestSenderID", senderID);
            message.addProperty("responseSenderID", senderID);
            message.addProperty("username", "defaultuser");
            connection.sendMessage(type, message);
        }
    }

    @Override
    public void send(IEvent event) {
        for (Map.Entry<String, IEvent> entry : eventMap.entrySet()) {
            boolean nameEquals = Objects.equal(entry.getValue().getName(), event.getName());
            boolean typeEquals = entry.getValue().getType().equals(event.getType());
            if(nameEquals && typeEquals){
                sendMessage(entry.getKey(), event.serialize());
            }
        }
    }

    @Override
    public void registerEvent(String type, IEvent event) {
        eventMap.put(type, event);
    }
}