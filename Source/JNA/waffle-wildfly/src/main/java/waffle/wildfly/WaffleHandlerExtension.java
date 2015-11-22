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

import javax.servlet.ServletContext;

import io.undertow.Handlers;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;

import waffle.wildfly.WaffleAuthenticationMechanism.Factory;

/**
 * The Class WaffleHandlerExtension.
 */
public class WaffleHandlerExtension implements ServletExtension {

    /* (non-Javadoc)
     * @see io.undertow.servlet.ServletExtension#handleDeployment(io.undertow.servlet.api.DeploymentInfo, javax.servlet.ServletContext)
     */
    @Override
    public void handleDeployment(final DeploymentInfo deploymentInfo, final ServletContext servletContext) {
        deploymentInfo.addAuthenticationMechanism("waffle", new Factory());
        deploymentInfo.addInitialHandlerChainWrapper(new HandlerWrapper() {
            @Override
            public HttpHandler wrap(final HttpHandler handler) {
                return Handlers.path().addPrefixPath("/", handler).addPrefixPath("/waffle", new WaffleHandler());
            }
        });
    }

}
