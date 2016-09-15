package com.codenvy.flux.watcher.core.handler;

import com.codenvy.flux.watcher.core.enums.EventType;
import com.codenvy.flux.watcher.core.enums.ResourceType;
import com.codenvy.flux.watcher.core.event.LocalResourceEvent;
import com.codenvy.flux.watcher.core.event.ResourceEvent;
import com.codenvy.flux.watcher.core.model.Resource;
import com.codenvy.flux.watcher.core.service.ConnectionService;
import com.codenvy.flux.watcher.core.service.ResourceService;
import com.google.common.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ResourceHandler {

    @Inject
    private ResourceService resourceService;

    @Inject
    private ConnectionService connectionService;

    @Subscribe
    public void onEvent(ResourceEvent resourceEvent){
        Resource resource = resourceEvent.getResource();
        Resource localResource = resourceService.find(resource);
        switch (resourceEvent.getType()) {
            case REQUEST:
                if(localResource != null)
                    connectionService.send(new ResourceEvent(EventType.RESPONSE, localResource));
                break;
            case RESPONSE:
                boolean isFile = resource.getType() == ResourceType.FILE;
                boolean isStore = resourceService.save(resource);
                if(isFile && isStore){
                    connectionService.send(new ResourceEvent(EventType.STORE, resource));
                }
                break;
            case CREATE:
                switch (resource.getType()) {
                    case FILE:
                        connectionService.send(new ResourceEvent(EventType.REQUEST, resource));
                        break;
                    case FOLDER:
                        resourceService.save(resource);
                        connectionService.send(new ResourceEvent(EventType.STORE, resource));
                        break;
                }
                break;
            case CHANGE:
                connectionService.send(new ResourceEvent(EventType.REQUEST, resource));
                break;
            case DELETE:
                resourceService.delete(resource);
                break;
        }
    }

    @Subscribe
    public void onLocalEvent(LocalResourceEvent event){
        Resource resource = event.getResource();
        switch (event.getType()) {
            case CREATE:
                connectionService.send(new ResourceEvent(EventType.CREATE, resource));
                connectionService.send(new ResourceEvent(EventType.STORE, resource));
                break;
            case CHANGE:
                connectionService.send(new ResourceEvent(EventType.CHANGE, resource));
                connectionService.send(new ResourceEvent(EventType.STORE, resource));
                break;
            case DELETE:
                connectionService.send(new ResourceEvent(EventType.DELETE, resource));
                break;
        }
    }
}