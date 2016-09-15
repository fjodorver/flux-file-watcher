package com.codenvy.flux.watcher.core.connection.events;

import com.codenvy.flux.watcher.core.enums.ConnectionStatus;

public class ConnectionEvent {

    private ConnectionStatus status;

    public ConnectionEvent(ConnectionStatus status) {
        this.status = status;
    }

    public ConnectionStatus getStatus() {
        return status;
    }

    public ConnectionEvent setStatus(ConnectionStatus status) {
        this.status = status;
        return this;
    }
}