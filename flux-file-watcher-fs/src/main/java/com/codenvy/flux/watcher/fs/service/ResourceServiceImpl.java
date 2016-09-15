package com.codenvy.flux.watcher.fs.service;

import com.codenvy.flux.watcher.core.model.Project;
import com.codenvy.flux.watcher.core.model.Resource;
import com.codenvy.flux.watcher.core.repository.ProjectRepository;
import com.codenvy.flux.watcher.core.service.ResourceService;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Set;

import static com.codenvy.flux.watcher.core.enums.ResourceType.FILE;
import static com.codenvy.flux.watcher.core.enums.ResourceType.FOLDER;

@Singleton
public class ResourceServiceImpl implements ResourceService {

    @Inject
    private FileSystem fileSystem;

    @Inject
    private ProjectRepository projectRepository;

    @Override
    public Set<Resource> findAll(Project project) {
        Project localProject = projectRepository.findByName(project.getName());
        Path projectPath = fileSystem.getPath(localProject.getPath());
        Set<Resource> resources = Sets.newHashSet();
        try {
            Files.walkFileTree(projectPath, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    resources.add(getResource(projectPath, dir, attrs).setProjectName(project.getName()));
                    return super.preVisitDirectory(dir, attrs);
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    resources.add(getResource(projectPath, file, attrs).setProjectName(project.getName()));
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resources;
    }

    @Override
    public Resource find(Resource resource) {
        Project project = projectRepository.findByName(resource.getProjectName());
        Path projectPath = fileSystem.getPath(project.getPath());
        Path resourcePath = projectPath.resolve(resource.getPath());
        try {
            BasicFileAttributes attrs = Files.readAttributes(resourcePath, BasicFileAttributes.class);
            return getResource(projectPath, resourcePath, attrs).setProjectName(project.getName());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean save(Resource resource) {
        Path path = fileSystem.getPath(projectRepository.findByName(resource.getProjectName()).getPath());
        Path resourcePath = path.resolve(resource.getPath());
        try {
            switch (resource.getType()) {
                case FILE:
                    if (Files.exists(resourcePath)) {
                        if (!Arrays.equals(Files.readAllBytes(resourcePath), resource.getContent())) {
                            Files.write(resourcePath, resource.getContent());
                        }
                    } else {
                        Files.createFile(resourcePath);
                    }
                    break;
                case FOLDER:
                    Files.createDirectory(resourcePath);
                    break;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean delete(Resource resource) {
        Path path = fileSystem.getPath(projectRepository.findByName(resource.getProjectName()).getPath());
        try {
            Files.walkFileTree(path.resolve(resource.getPath()), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return super.postVisitDirectory(dir, exc);
                }
            });
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private Resource getResource(Path projectPath, Path resourcePath, BasicFileAttributes attrs) throws IOException {
        String path = projectPath.relativize(resourcePath).toString();
        Long timestamp = attrs.lastAccessTime().toMillis();
        Resource resource = new Resource().setPath(path).setTimestamp(timestamp);
        if (attrs.isDirectory())
            return resource.setHash("0").setType(FOLDER);
        else {
            byte[] content = Files.readAllBytes(resourcePath);
            return resource.setContent(content).setHash(Hashing.sha1().hashBytes(content).toString()).setType(FILE);
        }
    }
}