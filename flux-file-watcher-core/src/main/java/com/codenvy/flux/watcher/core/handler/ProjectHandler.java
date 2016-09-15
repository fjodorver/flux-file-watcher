package com.codenvy.flux.watcher.core.handler;

import com.codenvy.flux.watcher.core.enums.EventType;
import com.codenvy.flux.watcher.core.event.ProjectEvent;
import com.codenvy.flux.watcher.core.event.ResourceEvent;
import com.codenvy.flux.watcher.core.model.Project;
import com.codenvy.flux.watcher.core.model.Resource;
import com.codenvy.flux.watcher.core.service.ConnectionService;
import com.codenvy.flux.watcher.core.service.ProjectService;
import com.codenvy.flux.watcher.core.service.ResourceService;
import com.google.common.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ProjectHandler {

    @Inject
    private ProjectService projectService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ConnectionService connectionService;

    @Subscribe
    public void onEvent(ProjectEvent event){
        Project project = event.getProject();
        Project localProject = projectService.find(project.getName());
        if(localProject == null)
            return;
        switch (event.getType()) {
            case REQUEST:
                connectionService.send(new ProjectEvent(EventType.RESPONSE, localProject));
                break;
            case RESPONSE:
                for (Resource resource : project.getResources()) {
                    resource.setProjectName(project.getName());
                    switch (resource.getType()) {
                        case FILE:
                            connectionService.send(new ResourceEvent(EventType.REQUEST, resource));
                            break;
                        case FOLDER:
                            resourceService.save(resource);
                            break;
                    }
                }
                for (Resource resource : event.getResources()) {
                    resource.setProjectName(project.getName());
                    resourceService.delete(resource);
                }
                break;
        }
    }
}