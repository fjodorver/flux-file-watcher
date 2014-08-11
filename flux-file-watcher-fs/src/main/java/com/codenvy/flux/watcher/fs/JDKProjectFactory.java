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
package com.codenvy.flux.watcher.fs;

import com.codenvy.flux.watcher.core.RepositoryEventBus;
import com.codenvy.flux.watcher.core.spi.Project;
import com.codenvy.flux.watcher.core.spi.ProjectFactory;
import com.google.inject.Singleton;

import javax.inject.Inject;
import java.nio.file.FileSystem;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link com.codenvy.flux.watcher.core.spi.ProjectFactory} implementation.
 *
 * @author Kevin Pollet
 */
@Singleton
public class JDKProjectFactory implements ProjectFactory {
    private final FileSystem             fileSystem;
    private final JDKProjectWatchService watchService;

    /**
     * Constructs an instance of {@link com.codenvy.flux.watcher.fs.JDKProjectFactory}.
     *
     * @param fileSystem
     *         the {@link java.nio.file.FileSystem} instance.
     * @param repositoryEventBus
     *         the {@link com.codenvy.flux.watcher.core.RepositoryEventBus}.
     * @throws java.lang.NullPointerException
     *         if {@code fileSystem} or {@code repositoryEventBus} is {@code null}.
     */
    @Inject
    public JDKProjectFactory(FileSystem fileSystem, RepositoryEventBus repositoryEventBus) {
        this.fileSystem = checkNotNull(fileSystem);
        this.watchService = new JDKProjectWatchService(fileSystem, checkNotNull(repositoryEventBus));
    }

    @Override
    public Project newProject(String projectId, String projectPath) {
        return new JDKProject(fileSystem, watchService, projectId, projectPath);
    }
}
