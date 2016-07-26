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

import com.codenvy.flux.watcher.core.Resource;
import com.codenvy.flux.watcher.core.spi.Project;
import com.google.common.jimfs.Jimfs;

import javax.security.auth.login.Configuration;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.file.FileSystem;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.nio.file.Files.createDirectory;
import static org.mockito.Mockito.mock;

/**
 * @author Kevin Pollet
 */
public class Main {
    public static void main(String[] args) throws IOException {
        /*final Injector injector = Guice.createInjector(new RepositoryModule(), new JDKProjectModule());

        final Repository repository = injector.getInstance(Repository.class);
        repository.addRemote(new URL("http://localhost:3000"), Credentials.DEFAULT_USER_CREDENTIALS);
        repository.addProject("flux", "/Users/kevin/Desktop/flux/flux");*/
        final String PROJECT_ID = "codenvy-project-id";
        final String PROJECT_PATH = "/codenvy-project";

        MemoryMXBean mxBean = ManagementFactory.getMemoryMXBean();
        FileSystem fileSystem = Jimfs.newFileSystem();
        createDirectory(fileSystem.getPath(PROJECT_PATH));
        createDirectory(fileSystem.getPath(PROJECT_PATH).resolve("src"));
        JDKProjectWatchService watchService = mock(JDKProjectWatchService.class);
        Project project = new JDKProject(fileSystem, watchService, PROJECT_ID, PROJECT_PATH);

        byte[] data = new byte[1000000];
        Random random = new Random();
        random.nextBytes(data);

        List<Long> results = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            String filename = MessageFormat.format("src/{0}.txt", i);
            Resource resource = Resource.newFile(filename, LocalDateTime.now().getNano(), data);
            project.createResource(resource);
            project.updateResource(resource);
            project.deleteResource(resource);
            results.add(mxBean.getHeapMemoryUsage().getUsed());
        }
        System.out.println(results.stream().mapToLong(x -> x).average().orElse(0));
    }
}
