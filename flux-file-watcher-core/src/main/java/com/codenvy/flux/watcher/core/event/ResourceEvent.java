package com.codenvy.flux.watcher.core.event;

import com.codenvy.flux.watcher.core.adapter.ResourceTypeAdapter;
import com.codenvy.flux.watcher.core.enums.EventType;
import com.codenvy.flux.watcher.core.model.Resource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class ResourceEvent implements IEvent {

    private EventType type;

    private Gson gson = new GsonBuilder().registerTypeAdapter(Resource.class, new ResourceTypeAdapter()).create();

    private Resource resource;

    public ResourceEvent(EventType type) {
        this.type = type;
    }

    public ResourceEvent(EventType type, Resource resource) {
        this.type = type;
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public EventType getType() {
        return type;
    }

    @Override
    public JsonElement serialize() {
        return gson.toJsonTree(resource);
    }

    @Override
    public void deserialize(JsonElement content) {
        resource = gson.fromJson(content, Resource.class);
    }
}