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
package com.codenvy.flux.watcher.core.utils;

import com.codenvy.flux.watcher.core.spi.Resource;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Kevin Pollet
 */
public final class ResourceHelper {
    private static final MessageDigest messageDigest;

    static {
        try {

            messageDigest = MessageDigest.getInstance("SHA-1");

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param resource
     * @return
     * @throws java.lang.NullPointerException
     */
    public static String hash(Resource resource) {
        checkNotNull(resource);
        checkNotNull(resource.content());

        switch (resource.type()) {
            case FILE: {
                final byte[] digest = messageDigest.digest(resource.content());
                return DatatypeConverter.printHexBinary(digest);
            }

            default:
                return "0";
        }
    }

    /**
     * Disable instantiation.
     */
    private ResourceHelper() {
    }
}
