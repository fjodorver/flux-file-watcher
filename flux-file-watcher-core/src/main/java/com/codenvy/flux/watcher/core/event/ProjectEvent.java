package com.codenvy.flux.watcher.core.event;

import com.codenvy.flux.watcher.core.adapter.ResourceTypeAdapter;
import com.codenvy.flux.watcher.core.enums.EventType;
import com.codenvy.flux.watcher.core.model.Project;
import com.codenvy.flux.watcher.core.model.Resource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ProjectEvent implements IEvent {

    private EventType type;

    private Gson gson = new GsonBuilder().registerTypeAdapter(Resource.class, new ResourceTypeAdapter()).create();

    private Project project;

    private Resource[] resources;

    public ProjectEvent(EventType type) {
        this.type = type;
    }

    public ProjectEvent(EventType type, Project project) {
        this.type = type;
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public Resource[] getResources() {
        return resources;
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
        return gson.toJsonTree(project);
    }

    @Override
    public void deserialize(JsonElement content) {
        JsonObject message = content.getAsJsonObject();
        project = gson.fromJson(content, Project.class);
        if(message.has("deleted")){
            resources = gson.fromJson(message.get("deleted"), Resource[].class);
        }
    }
}