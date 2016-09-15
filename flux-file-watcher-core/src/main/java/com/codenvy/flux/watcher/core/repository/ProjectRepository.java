package com.codenvy.flux.watcher.core.repository;

import com.codenvy.flux.watcher.core.model.Project;

import java.util.Set;

public interface ProjectRepository {

    Set<Project> findAll();

    Project findByName(String name);

    Project findByPath(String path);

    void save(Project project);

    void delete(Project project);
}