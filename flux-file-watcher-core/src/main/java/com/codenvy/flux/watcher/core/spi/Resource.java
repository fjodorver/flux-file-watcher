/*******************************************************************************
 * Copyright (c) 2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.flux.watcher.core.spi;

import java.nio.file.Path;

/**
 * @author Kevin Pollet
 */
public class Resource {
    private final String       projectId;
    private final Path         path;
    private final long         timestamp;
    private final String       hash;
    private final ResourceType type;
    private final byte[]       content;

    public Resource(String projectId, Path path, long timestamp, String hash, ResourceType type, byte[] content) {
        this.projectId = projectId;
        this.path = path;
        this.timestamp = timestamp;
        this.hash = hash;
        this.type = type;
        this.content = content;
    }

    public String projectId() {
        return projectId;
    }

    public Path path() {
        return path;
    }

    public long timestamp() {
        return timestamp;
    }

    public String hash() {
        return hash;
    }

    public ResourceType type() {
        return type;
    }

    public byte[] content() {
        return content;
    }

    public enum ResourceType {
        FILE,
        FOLDER
    }
}
