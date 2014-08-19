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
package com.codenvy.flux.watcher.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.flux.watcher.core.Credentials;
import com.codenvy.flux.watcher.core.Repository;

@Singleton
public class FluxSyncInitService {

    private static final Logger  LOG      = LoggerFactory.getLogger(FluxSyncInitService.class);
    private static final String  FLUX_URL = "http://localhost:3000";

    private final ProjectManager projectManager;
    private final Repository     repository;

    @Inject
    public FluxSyncInitService(ProjectManager projectManager, Repository repository) {
        this.projectManager = projectManager;
        this.repository = repository;
    }

    @PostConstruct
    void start() {
        // connect to flux server
        try {
            repository.addRemote(new URL(FLUX_URL), Credentials.DEFAULT_USER_CREDENTIALS);
        } catch (MalformedURLException e) {
            LOG.error("Couldn't connect to Flux server", e);
        }

        // start sync for all projects in current workspace
        List<Project> projects;
        // TODO workspace should not be hardcoded
        try {
            projects = projectManager.getProjects("1q2w3e");
        } catch (ServerException e) {
            LOG.error("Couldn't get projects", e);
            throw new RuntimeException("Couldn't get projects", e);
        }

        if (projects != null) {
            for (Project project : projects) {
                repository.addProject(project.getName(), project.getPath());
            }
        }
    }

    @PreDestroy
    void stop() {
        // disconnect from flux server
        try {
            repository.removeRemote(new URL(FLUX_URL));
        } catch (MalformedURLException e) {
            LOG.error("Couldn't connect to Flux server", e);
        }
    }
}
