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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.handlers.ServletRequestContext;
import io.undertow.util.Headers;
import waffle.servlet.WaffleInfoServlet;

/**
 * The Class WaffleHandler.
 */
public class WaffleHandler implements HttpHandler {

    /* (non-Javadoc)
     * @see io.undertow.server.HttpHandler#handleRequest(io.undertow.server.HttpServerExchange)
     */
    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        final ServletRequestContext context = exchange.getAttachment(ServletRequestContext.ATTACHMENT_KEY);
        if (context != null) {
            final WaffleInfoServlet info = new WaffleInfoServlet();
            info.getWaffleInfoResponse((HttpServletRequest) context.getServletRequest(),
                    (HttpServletResponse) context.getServletResponse());
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            // Results have been written out at this point
        } else {
            exchange.getResponseSender().send("Couldn't find Servlet Request");
        }
    }

}
