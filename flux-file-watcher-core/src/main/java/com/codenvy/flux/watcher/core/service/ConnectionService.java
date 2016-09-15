package com.codenvy.flux.watcher.core.service;

import com.codenvy.flux.watcher.core.Credentials;
import com.codenvy.flux.watcher.core.event.IEvent;
import com.codenvy.flux.watcher.core.model.Project;
import com.google.gson.JsonElement;

import java.net.URI;

public interface ConnectionService {

    void addRemote(URI uri, Credentials credentials);

    void removeRemote(URI uri);

    void connectToChannel(URI uri, String channel);

    void disconnectFromChannel(URI uri, String channel);

    void connectProject(Project project);

    void disconnectProject(Project project);

    void sendMessage(String type, JsonElement content);

    void send(IEvent event);

    void registerEvent(String type, IEvent event);
}