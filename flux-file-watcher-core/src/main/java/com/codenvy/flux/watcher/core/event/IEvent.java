package com.codenvy.flux.watcher.core.event;

import com.codenvy.flux.watcher.core.enums.EventType;
import com.google.gson.JsonElement;

public interface IEvent {

    String getName();

    EventType getType();

    JsonElement serialize();

    void deserialize(JsonElement content);
}