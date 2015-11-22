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

import javax.servlet.ServletException;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import waffle.servlet.WaffleInfoServlet;

/**
 * The Class Demo.
 */
public class Demo {

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(final String[] args) {
        try {
            DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(Demo.class.getClassLoader())
                .setContextPath("/waffle-wildfly")
                .setDeploymentName("waffle-wildfly.war")
                .addServlets(
                        Servlets.servlet("WaffleInfo", WaffleInfoServlet.class)
                                .addMapping("/waffle"));

            DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
            manager.deploy();

            HttpHandler servletHandler = manager.start();
            PathHandler path = Handlers.path(Handlers.redirect("/waffle-wildfly"))
                .addPrefixPath("/waffle-wildfly", servletHandler);

            Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(path)
                .build();
            server.start();
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

}
