/*******************************************************************************
 * Copyright (c) 2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.flux.watcher.fs;

import com.codenvy.flux.watcher.core.Credentials;
import com.codenvy.flux.watcher.core.RepositoryModule;
import com.codenvy.flux.watcher.core.enums.EventType;
import com.codenvy.flux.watcher.core.event.ProjectEvent;
import com.codenvy.flux.watcher.core.event.ResourceEvent;
import com.codenvy.flux.watcher.core.handler.ProjectHandler;
import com.codenvy.flux.watcher.core.handler.ResourceHandler;
import com.codenvy.flux.watcher.core.model.Project;
import com.codenvy.flux.watcher.core.service.ConnectionService;
import com.codenvy.flux.watcher.core.service.ProjectService;
import com.codenvy.flux.watcher.fs.service.WatcherServiceImpl;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * @author Kevin Pollet
 */
public class Main {
    public static void main(String[] args) throws URISyntaxException {
        Injector injector = Guice.createInjector(new RepositoryModule(), new JDKModule());

        ProjectService projectService = injector.getInstance(ProjectService.class);
        ConnectionService connectionService = injector.getInstance(ConnectionService.class);
        ProjectHandler projectHandler = injector.getInstance(ProjectHandler.class);
        ResourceHandler resourceHandler = injector.getInstance(ResourceHandler.class);
        WatcherServiceImpl watcherService = injector.getInstance(WatcherServiceImpl.class);
        EventBus eventBus = injector.getInstance(EventBus.class);
        eventBus.register(projectHandler);
        eventBus.register(resourceHandler);

        Set<Service> services = Sets.newHashSet();
        services.add(watcherService);
        ServiceManager serviceManager = new ServiceManager(services);
        serviceManager.startAsync().awaitHealthy();

        URI uri = new URI("http://localhost:3000");

        connectionService.addRemote(uri, new Credentials("defaultuser"));
        connectionService.registerEvent("getProjectRequest", new ProjectEvent(EventType.REQUEST));
        connectionService.registerEvent("getProjectResponse", new ProjectEvent(EventType.RESPONSE));
        connectionService.registerEvent("getResourceRequest", new ResourceEvent(EventType.REQUEST));
        connectionService.registerEvent("getResourceResponse", new ResourceEvent(EventType.RESPONSE));
        connectionService.registerEvent("resourceCreated", new ResourceEvent(EventType.CREATE));
        connectionService.registerEvent("resourceChanged", new ResourceEvent(EventType.CHANGE));
        connectionService.registerEvent("resourceDeleted", new ResourceEvent(EventType.DELETE));
        connectionService.registerEvent("resourceStored", new ResourceEvent(EventType.STORE));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connectionService.connectToChannel(uri, "defaultuser");
        projectService.connect(new Project().setName("flux").setPath("/home/fjodor/Documents/"));
    }
}