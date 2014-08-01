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

/**
 * Constants fo the tests.
 *
 * @author Kevin Pollet
 */
public final class TestConstants {
    public static final String PROJECT_ID                        = "codenvy-project-id";
    public static final String PROJECT_PATH                      = "/codenvy-project";
    public static final String RELATIVE_PROJECT_SRC_FOLDER_PATH  = "src";
    public static final String RELATIVE_PROJECT_MAIN_FOLDER_PATH = "src/main";
    public static final String RELATIVE_PROJECT_HELLO_FILE_PATH  = "src/hello";
    public static final String RELATIVE_PROJECT_README_FILE_PATH = "readme";

    /**
     * Disable instantiation.
     */
    private TestConstants() {
    }
}
