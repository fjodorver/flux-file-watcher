package com.codenvy.flux.watcher.core.connection.impl;

import com.codenvy.flux.watcher.core.connection.IConnection;
import com.codenvy.flux.watcher.core.connection.IListener;
import com.codenvy.flux.watcher.core.event.IEvent;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonParser;

import java.util.Map;
import java.util.Set;

public abstract class AbstractConnection implements IConnection {

    IListener listener;

    JsonParser parser = new JsonParser();

    Set<String> channels = Sets.newHashSet();

    Map<String, IEvent> eventMap = Maps.newHashMap();

    @Override
    public void setListener(IListener listener) {
        this.listener = listener;
    }
}