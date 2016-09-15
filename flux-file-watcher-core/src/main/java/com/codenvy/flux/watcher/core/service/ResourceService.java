package com.codenvy.flux.watcher.core.service;

import com.codenvy.flux.watcher.core.model.Project;
import com.codenvy.flux.watcher.core.model.Resource;

import java.util.Set;

public interface ResourceService {

    Set<Resource> findAll(Project project);

    Resource find(Resource resource);

    boolean save(Resource resource);

    boolean delete(Resource resource);
}