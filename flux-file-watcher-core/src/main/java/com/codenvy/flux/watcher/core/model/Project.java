package com.codenvy.flux.watcher.core.model;

import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Set;

public class Project implements Serializable {

    @SerializedName("project")
    private String name;

    @SerializedName("resource")
    private String path;

    @SerializedName("files")
    private Set<Resource> resources = Sets.newHashSet();

    public String getName() {
        return name;
    }

    public Project setName(String name) {
        this.name = name;
        return this;
    }

    public String getPath() {
        return path;
    }

    public Project setPath(String path) {
        this.path = path;
        return this;
    }

    public Set<Resource> getResources() {
        return resources;
    }

    public Project setResources(Set<Resource> resources) {
        this.resources = resources;
        return this;
    }
}