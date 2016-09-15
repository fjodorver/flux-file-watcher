package com.codenvy.flux.watcher.core.connection;

import com.codenvy.flux.watcher.core.enums.ConnectionStatus;
import com.google.gson.JsonElement;

public interface IListener {

    void onConnectionEvent(ConnectionStatus status);

    void onChannelEvent(ConnectionStatus status, String channel);

    void onMessage(String type, JsonElement content);
}