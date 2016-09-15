package com.codenvy.flux.watcher.fs.service;

import com.codenvy.flux.watcher.core.enums.EventType;
import com.codenvy.flux.watcher.core.enums.ResourceType;
import com.codenvy.flux.watcher.core.event.LocalResourceEvent;
import com.codenvy.flux.watcher.core.model.Project;
import com.codenvy.flux.watcher.core.model.Resource;
import com.codenvy.flux.watcher.core.repository.ProjectRepository;
import com.codenvy.flux.watcher.core.service.WatcherService;
import com.google.common.eventbus.EventBus;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static com.codenvy.flux.watcher.core.enums.ResourceType.FILE;
import static com.codenvy.flux.watcher.core.enums.ResourceType.FOLDER;
import static java.nio.file.StandardWatchEventKinds.*;

@Singleton
public class WatcherServiceImpl extends AbstractExecutionThreadService implements WatcherService {

    private final Object object = new Object();

    private WatchService watchService;

    @Inject
    private FileSystem fileSystem;

    @Inject
    private ProjectRepository projectRepository;

    @Inject
    private EventBus eventBus;

    @Inject
    public void init() {
        try {
            watchService = fileSystem.newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void watch(Project project) throws IOException {
        Path projectPath = fileSystem.getPath(project.getPath());
        registerPath(projectPath);
    }

    @Override
    public void unwatch(Project project) throws IOException {
        Path projectPath = fileSystem.getPath(project.getPath());
        unregisterPath(projectPath);
    }

    @Override
    protected void run() throws Exception {
        while (isRunning()) {
            WatchKey key = watchService.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                Path watchablePath = (Path) key.watchable();
                if (event.kind() == OVERFLOW)
                    return;
                Project project = projectRepository.findByPath(watchablePath.toString());
                Resource resource = find(project.getName(), event.context().toString());

                EventType type = kindToMessageType(event.kind());
                if (type == EventType.CREATE && resource.getType() == ResourceType.FOLDER) {
                    registerPath(watchablePath.resolve(event.context().toString()));
                }
                eventBus.post(new LocalResourceEvent(type, resource));
            }
            key.reset();
        }
    }

    private Resource find(String projectName, String path) {
        Project project = projectRepository.findByName(projectName);
        Path projectPath = fileSystem.getPath(project.getPath());
        Path resourcePath = projectPath.resolve(path);
        try {
            BasicFileAttributes attrs = Files.readAttributes(resourcePath, BasicFileAttributes.class);
            return getResource(projectPath, resourcePath, attrs);
        } catch (IOException e) {
            return null;
        }
    }

    private Resource getResource(Path projectPath, Path resourcePath, BasicFileAttributes attrs) throws IOException {
        String path = projectPath.relativize(resourcePath).toString();
        Long timestamp = attrs.lastAccessTime().toMillis();
        Resource resource = new Resource().setPath(path).setTimestamp(timestamp);
        if (attrs.isDirectory())
            return new Resource().setHash("0").setType(FOLDER);
        else {
            byte[] content = Files.readAllBytes(resourcePath);
            return resource.setContent(content).setHash(Hashing.sha1().hashBytes(content).toString()).setType(FILE);
        }
    }

    private void registerPath(Path projectPath) throws IOException {
        synchronized (object) {
            Files.walkFileTree(projectPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    dir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                    return super.preVisitDirectory(dir, attrs);
                }
            });
        }
    }

    private void unregisterPath(Path projectPath) throws IOException {
        synchronized (object) {
            Files.walkFileTree(projectPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    dir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE).cancel();
                    return super.preVisitDirectory(dir, attrs);
                }
            });
        }
    }

    private EventType kindToMessageType(WatchEvent.Kind<?> kind) {
        if (kind == ENTRY_CREATE) {
            return EventType.CREATE;
        }
        if (kind == ENTRY_MODIFY) {
            return EventType.CHANGE;
        }
        if (kind == ENTRY_DELETE) {
            return EventType.DELETE;
        }
        return null;
    }
}