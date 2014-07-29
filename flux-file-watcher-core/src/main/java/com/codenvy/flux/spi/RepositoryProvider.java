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
package com.codenvy.flux.spi;

import java.nio.file.Path;

/**
 * @author Kevin Pollet
 */
public interface RepositoryProvider {
    Resource getResource(String projectId, Path resourcePath);

    void createResource(Resource resource);

    void deleteResource(Resource resource);

    RepositoryWatchingService getWatchingService();
}
