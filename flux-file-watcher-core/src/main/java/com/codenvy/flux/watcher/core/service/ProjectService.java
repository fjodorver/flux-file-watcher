package com.codenvy.flux.watcher.core.service;

import com.codenvy.flux.watcher.core.model.Project;

import java.util.Set;

public interface ProjectService {

    Set<Project> findAll();

    Project find(String name);

    void connect(Project project);

    void disconnect(Project project);
}