package com.codenvy.flux.watcher.core.model;

import com.codenvy.flux.watcher.core.enums.ResourceType;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Resource implements Serializable {

    @SerializedName("resource")
    private String path;

    @SerializedName("project")
    private String projectName;

    private Long timestamp;

    private ResourceType type;

    private String hash;

    private byte[] content;

    public Resource() {
    }

    public Resource(String path, String projectName) {
        this.path = path;
        this.projectName = projectName;
    }

    public String getPath() {
        return path;
    }

    public Resource setPath(String path) {
        this.path = path;
        return this;
    }

    public String getProjectName() {
        return projectName;
    }

    public Resource setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Resource setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ResourceType getType() {
        return type;
    }

    public Resource setType(ResourceType type) {
        this.type = type;
        return this;
    }

    public String getHash() {
        return hash;
    }

    public Resource setHash(String hash) {
        this.hash = hash;
        return this;
    }

    public byte[] getContent() {
        return content;
    }

    public Resource setContent(byte[] content) {
        this.content = content;
        return this;
    }
}
