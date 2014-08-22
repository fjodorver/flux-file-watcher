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
package com.codenvy.flux.watcher.server;

import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Answers.*;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.FileEntry;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.api.vfs.server.ContentStream;
import com.codenvy.flux.watcher.core.Resource;


@RunWith(MockitoJUnitRunner.class)
public class TestVFSProject {

    private static final Logger LOG          = LoggerFactory.getLogger(TestVFSProject.class);

    public static final String  PROJECT_PATH = "/aProject";
    @Mock
    ProjectManager              projectManager;

    @Mock
    public Project              codenvyProject;

    @Mock(answer = RETURNS_DEEP_STUBS)
    public FolderEntry          baseFolder;

    @Mock(answer = RETURNS_DEEP_STUBS)
    public FileEntry            pomXml;

    @Mock(answer = RETURNS_DEEP_STUBS)
    public FolderEntry          src;

    @Mock(answer = RETURNS_DEEP_STUBS)
    public FolderEntry          srcMain;

    @Mock(answer = RETURNS_DEEP_STUBS)
    public FolderEntry          srcMainJava;

    @Mock(answer = RETURNS_DEEP_STUBS)
    public FileEntry            srcMainJavaAClassJava;


    protected InputStream       resourceAClassStream;
    protected InputStream       resourcePomStream;

    protected ContentStream     javaClassContent;
    protected ContentStream     pomContent;

    @Before
    public void initMocks() throws ForbiddenException, ServerException {
        ArrayList<FileEntry> noFiles = new ArrayList<FileEntry>();
        ArrayList<FolderEntry> noFolders = new ArrayList<FolderEntry>();

        when(projectManager.getProject("1q2w3e", PROJECT_PATH)).thenReturn(codenvyProject);
        when(codenvyProject.getBaseFolder()).thenReturn(baseFolder);

        when(baseFolder.getChildFolders()).thenReturn(Arrays.asList(src));
        when(baseFolder.getChildFiles()).thenReturn(Arrays.asList(pomXml));


        when(src.getChildFiles()).thenReturn(noFiles);
        when(src.getChildFolders()).thenReturn(Arrays.asList(srcMain));
        when(src.getVirtualFile().getLastModificationDate()).thenReturn(System.currentTimeMillis());
        when(src.getVirtualFile().getPath()).thenReturn("/aProject/src");


        when(srcMain.getChildFiles()).thenReturn(noFiles);
        when(srcMain.getChildFolders()).thenReturn(Arrays.asList(srcMainJava));
        when(srcMain.getVirtualFile().getLastModificationDate()).thenReturn(System.currentTimeMillis());
        when(srcMain.getVirtualFile().getPath()).thenReturn("/aProject/src/main");

        when(srcMainJava.getChildFiles()).thenReturn(Arrays.asList(srcMainJavaAClassJava));
        when(srcMainJava.getChildFolders()).thenReturn(noFolders);
        when(srcMainJava.getVirtualFile().getLastModificationDate()).thenReturn(System.currentTimeMillis());
        when(srcMainJava.getVirtualFile().getPath()).thenReturn("/aProject/src/main/java");

        when(srcMainJavaAClassJava.getVirtualFile().getLastModificationDate()).thenReturn(System.currentTimeMillis());
        when(srcMainJavaAClassJava.getVirtualFile().getPath()).thenReturn("/aProject/src/main/java/AClass.java");
        resourceAClassStream = TestVFSProject.class.getResourceAsStream("/AClass.java.resource");
        // ContentStream is final, not possible to mock it :/
        javaClassContent = new ContentStream("AClass.java", resourceAClassStream, "text/x-java-source,java");
        when(srcMainJavaAClassJava.getVirtualFile().getContent()).thenReturn(javaClassContent);

        when(pomXml.getVirtualFile().getLastModificationDate()).thenReturn(System.currentTimeMillis());
        when(pomXml.getVirtualFile().getPath()).thenReturn("/aProject/pom.xml");
        resourcePomStream = TestVFSProject.class.getResourceAsStream("/pom.xml.resource");
        pomContent = new ContentStream("pom.xml", resourcePomStream, "application/xml");
        when(pomXml.getVirtualFile().getContent()).thenReturn(pomContent);


    }

    @Test
    public void testGetResources() {


        VFSProject vfsProject = new VFSProject(projectManager, "id", PROJECT_PATH);
        Set<Resource> resources = vfsProject.getResources();
        for (Resource resource : resources) {
            LOG.info(resource.toString());
        }
        LOG.info("number of resources: " + resources.size());
        Assert.assertEquals("number of resources is", 5, resources.size());

    }
}
