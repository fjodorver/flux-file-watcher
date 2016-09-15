package com.codenvy.flux.watcher.core.connection.events;

import com.codenvy.flux.watcher.core.enums.ChannelStatus;

public class ChannelEvent {

    private ChannelStatus status;

    private String channel;

    public ChannelEvent(ChannelStatus status, String channel) {
        this.status = status;
        this.channel = channel;
    }

    public ChannelStatus getStatus() {
        return status;
    }

    public ChannelEvent setStatus(ChannelStatus status) {
        this.status = status;
        return this;
    }

    public String getChannel() {
        return channel;
    }

    public ChannelEvent setChannel(String channel) {
        this.channel = channel;
        return this;
    }
}