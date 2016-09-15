package com.codenvy.flux.watcher.core.enums;

import com.google.gson.annotations.SerializedName;

public enum ResourceType {
    @SerializedName("file")
    FILE,
    @SerializedName("folder")
    FOLDER,
    @SerializedName("unknown")
    UNKNOWN
}