/**
 * Waffle (https://github.com/Waffle/waffle)
 *
 * Copyright (c) 2010-2016 Application Security, Inc.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors: Application Security, Inc.
 */
package waffle.controllers;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/waffle")
public class WaffleController {

    /** The Constant Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(WaffleController.class);

    @RequestMapping(value="/check")
    public String check(final HttpServletRequest request) {
        WaffleController.LOGGER.info("Remote User: {}", request.getRemoteUser());
        return "success";
    }

}
