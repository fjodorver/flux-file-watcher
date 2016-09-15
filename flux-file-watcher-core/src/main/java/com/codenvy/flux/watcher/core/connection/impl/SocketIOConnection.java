package com.codenvy.flux.watcher.core.connection.impl;

import com.codenvy.flux.watcher.core.Credentials;
import com.codenvy.flux.watcher.core.enums.ConnectionStatus;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.parser.Packet;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Singleton;
import java.net.URI;

@Singleton
public class SocketIOConnection extends AbstractConnection {

    private IO.Options options;

    private Socket socket;

    public SocketIOConnection(URI uri, Credentials credentials) {
        options = new IO.Options();
        options.transports = new String[]{"websocket"};
        socket = IO.socket(uri, options);
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... objects) {
                listener.onConnectionEvent(ConnectionStatus.CONNECTED);
                for (String channel : channels) {
                    connectToChannel(channel);
                }
            }
        });
        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... objects) {
                listener.onConnectionEvent(ConnectionStatus.DISCONNECTED);
                for (String channel : channels) {
                    disconnectFromChannel(channel);
                }
            }
        });
        socket.io().on(Manager.EVENT_PACKET, new Emitter.Listener() {
            @Override
            public void call(Object... objects) {
                Packet packet = (Packet) objects[0];
                if(packet.type == 2){
                    JsonArray array = parser.parse(packet.data.toString()).getAsJsonArray();
                    listener.onMessage(array.get(0).getAsString(), array.get(1));
                }
            }
        });
    }

    @Override
    public void open() {
        socket.open();
    }

    @Override
    public void close() {
        socket.close();
    }

    @Override
    public void connectToChannel(String channel) {
        Preconditions.checkNotNull(channel, "Channel name should not be null");
        Preconditions.checkArgument(socket.connected(), "Cannot connect to channel. Not connected to socket.io");
        socket.emit("connectToChannel", getMessage(channel), new Ack() {
            @Override
            public void call(Object... objects) {
                if(objects.length == 1){
                    JsonObject message = parser.parse(objects[0].toString()).getAsJsonObject();
                    if(message.get("connectedToChannel").getAsBoolean()){
                        listener.onChannelEvent(ConnectionStatus.CONNECTED, channel);
                    }
                }
            }
        });
    }

    @Override
    public void disconnectFromChannel(String channel) {
        socket.emit("disconnectFromChannel", getMessage(channel), new Ack() {
            @Override
            public void call(Object... objects) {
                if(objects.length == 1){
                    JsonObject message = parser.parse(objects[0].toString()).getAsJsonObject();
                    if(message.get("disconnectedFromChannel").getAsBoolean()){
                        listener.onChannelEvent(ConnectionStatus.DISCONNECTED, channel);
                    }
                }
            }
        });
    }

    @Override
    public void sendMessage(String type, JsonElement content) {
        try {
            socket.emit(type, new JSONObject(content.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject getMessage(String channel){
        JSONObject message = new JSONObject();
        try {
            message.put("channel", channel);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }
}