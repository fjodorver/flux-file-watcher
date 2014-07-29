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
package com.codenvy.flux;

/**
 * @author Kevin Pollet
 */
public final class MessageFields {
    public static final String USERNAME           = "username";
    public static final String CALLBACK_ID        = "callback_id";
    public static final String REQUEST_SENDER_ID  = "requestSenderID";
    public static final String PROJECT_NAME       = "project";
    public static final String RESOURCE_PATH      = "resource";
    public static final String RESOURCE_TYPE      = "type";
    public static final String RESOURCE_TIMESTAMP = "timestamp";
    public static final String RESOURCE_HASH      = "hash";
    public static final String RESOURCE_CONTENT   = "hash";

    /**
     * Disable instantiation.
     */
    private MessageFields() {
    }
}
