package com.codenvy.flux.watcher.core.connection;

import com.google.gson.JsonElement;

public interface IConnection {

    void open();

    void close();

    void connectToChannel(String channel);

    void disconnectFromChannel(String channel);

    void sendMessage(String type, JsonElement content);

    void setListener(IListener listener);
}