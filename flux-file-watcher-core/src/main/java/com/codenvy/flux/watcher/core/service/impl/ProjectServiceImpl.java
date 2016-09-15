package com.codenvy.flux.watcher.core.service.impl;

import com.codenvy.flux.watcher.core.model.Project;
import com.codenvy.flux.watcher.core.repository.ProjectRepository;
import com.codenvy.flux.watcher.core.service.ConnectionService;
import com.codenvy.flux.watcher.core.service.ProjectService;
import com.codenvy.flux.watcher.core.service.ResourceService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class ProjectServiceImpl implements ProjectService {

    @Inject
    private ProjectRepository projectRepository;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ConnectionService connectionService;

    @Override
    public Set<Project> findAll() {
        return projectRepository.findAll();
    }

    @Override
    public Project find(String name) {
        Project project = projectRepository.findByName(name);
        project.setResources(resourceService.findAll(project));
        return project;
    }

    @Override
    public void connect(Project project) {
        projectRepository.save(project);
        connectionService.connectProject(project);
    }

    @Override
    public void disconnect(Project project) {
        projectRepository.delete(project);
        connectionService.disconnectProject(project);
    }
}