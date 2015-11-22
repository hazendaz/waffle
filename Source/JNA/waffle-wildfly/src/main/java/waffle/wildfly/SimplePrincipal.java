/**
 * Waffle (https://github.com/dblock/waffle)
 *
 * Copyright (c) 2010 - 2015 Application Security, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Application Security, Inc.
 */
package waffle.wildfly;

import java.security.Principal;

/**
 * The Class SimplePrincipal.
 */
public class SimplePrincipal implements Principal {

    /** The name. */
    private final String name;
    
    /** The credential. */
    private final String credential;

    /**
     * Instantiates a new simple principal.
     *
     * @param newName the new name
     * @param newCredential the new credential
     */
    public SimplePrincipal(final String newName, final String newCredential) {
        this.credential = newCredential;
        final int idx = newName.indexOf('@');
        this.name = 0 < idx ? newName.substring(0, idx) : newName;
    }

    /* (non-Javadoc)
     * @see java.security.Principal#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Gets the credential.
     *
     * @return the credential
     */
    public String getCredential() {
        return this.credential;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.name;
    }

}
