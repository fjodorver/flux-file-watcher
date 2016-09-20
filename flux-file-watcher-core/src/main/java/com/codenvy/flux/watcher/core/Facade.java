package com.codenvy.flux.watcher.core;

import com.codenvy.flux.watcher.core.enums.EventType;
import com.codenvy.flux.watcher.core.event.ProjectEvent;
import com.codenvy.flux.watcher.core.event.ResourceEvent;
import com.codenvy.flux.watcher.core.handler.ProjectHandler;
import com.codenvy.flux.watcher.core.handler.ResourceHandler;
import com.codenvy.flux.watcher.core.model.Project;
import com.codenvy.flux.watcher.core.service.ConnectionService;
import com.codenvy.flux.watcher.core.service.ProjectService;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Inject;
import java.net.URI;

public class Facade {

    @Inject
    private ProjectService projectService;

    @Inject
    private ConnectionService connectionService;

    @Inject
    private EventBus eventBus;

    @Inject
    private ProjectHandler projectHandler;

    @Inject
    private ResourceHandler resourceHandler;

    @Inject
    public void init() {
        connectionService.registerEvent("getProjectRequest", new ProjectEvent(EventType.REQUEST));
        connectionService.registerEvent("getProjectResponse", new ProjectEvent(EventType.RESPONSE));
        connectionService.registerEvent("getResourceRequest", new ResourceEvent(EventType.REQUEST));
        connectionService.registerEvent("getResourceResponse", new ResourceEvent(EventType.RESPONSE));
        connectionService.registerEvent("resourceCreated", new ResourceEvent(EventType.CREATE));
        connectionService.registerEvent("resourceChanged", new ResourceEvent(EventType.CHANGE));
        connectionService.registerEvent("resourceDeleted", new ResourceEvent(EventType.DELETE));
        connectionService.registerEvent("resourceStored", new ResourceEvent(EventType.STORE));
        eventBus.register(projectHandler);
        eventBus.register(resourceHandler);
    }

    public void addRemote(URI uri, Credentials credentials){
        Preconditions.checkNotNull(uri);
        Preconditions.checkNotNull(credentials);
        connectionService.addRemote(uri, credentials);
    }

    public void removeRemote(URI uri){
        Preconditions.checkNotNull(uri);
        connectionService.removeRemote(uri);
    }

    public void connectToChannel(URI uri, String channel){
        Preconditions.checkNotNull(uri);
        Preconditions.checkNotNull(channel);
        connectionService.connectToChannel(uri, channel);
    }

    public void disconnectFromChannel(URI uri, String channel){
        throw new NotImplementedException();
    }

    public void connectProject(String name, String path){
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(path);
        projectService.connect(new Project().setName(name).setPath(path));
    }

    public void disconnectProject(String name){
        Preconditions.checkNotNull(name);
        Project project = projectService.find(name);
        Preconditions.checkNotNull(project);
        projectService.disconnect(project);
    }
}