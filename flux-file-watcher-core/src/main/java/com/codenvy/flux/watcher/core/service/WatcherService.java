package com.codenvy.flux.watcher.core.service;

import com.codenvy.flux.watcher.core.model.Project;

import java.io.IOException;

public interface WatcherService {

    void watch(Project project) throws IOException;

    void unwatch(Project project) throws IOException;
}