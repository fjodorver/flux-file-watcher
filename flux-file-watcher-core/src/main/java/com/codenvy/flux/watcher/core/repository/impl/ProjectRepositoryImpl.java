package com.codenvy.flux.watcher.core.repository.impl;

import com.codenvy.flux.watcher.core.model.Project;
import com.codenvy.flux.watcher.core.repository.ProjectRepository;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;

@Singleton
public class ProjectRepositoryImpl implements ProjectRepository {

    private Map<String, Project> nameProjectMap = Maps.newHashMap();

    private Map<String, Project> pathProjectMap = Maps.newHashMap();

    @Override
    public Set<Project> findAll() {
        return Sets.newHashSet(nameProjectMap.values());
    }

    @Override
    public Project findByName(String name) {
        return nameProjectMap.get(name);
    }

    @Override
    public Project findByPath(String path) {
        return pathProjectMap.get(path);
    }

    @Override
    public void save(Project project) {
        nameProjectMap.put(project.getName(), project);
        pathProjectMap.put(project.getPath(), project);
    }

    @Override
    public void delete(Project project) {
        nameProjectMap.remove(project.getName());
        pathProjectMap.remove(project.getPath());
    }
}