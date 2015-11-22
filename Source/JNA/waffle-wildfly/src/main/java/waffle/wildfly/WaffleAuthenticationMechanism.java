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
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extension.undertow.security.AccountImpl;

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMechanismFactory;
import io.undertow.security.api.SecurityContext;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.IdentityManager;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.servlet.handlers.ServletRequestContext;
import waffle.windows.auth.impl.WindowsAccountImpl;

/**
 * The Class WaffleAuthenticationMechanism.
 */
public class WaffleAuthenticationMechanism implements AuthenticationMechanism {

    /** The Constant LOGGER. */
    private static final Logger          LOGGER = LoggerFactory.getLogger(WaffleAuthenticationMechanism.class);

    /** The authenticator. */
    private final NegotiateAuthenticator authenticator;
    
    /** The mechanism name. */
    private final String                 mechanismName;

    /** The Constant UPTIME. */
    static final long                    UPTIME = System.currentTimeMillis();

    /**
     * Instantiates a new waffle authentication mechanism.
     *
     * @param newMechanismName the new mechanism name
     */
    WaffleAuthenticationMechanism(final String newMechanismName) {
        try {
            this.authenticator = new NegotiateAuthenticator();
            WaffleAuthenticationMechanism.LOGGER.info("Waffle-Auth started");
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
        this.mechanismName = newMechanismName;
    }

    /**
     * Authenticate.
     *
     * @param exchange the exchange
     * @param securityContext the security context
     * @return the authentication mechanism outcome
     */
    @Override
    public AuthenticationMechanismOutcome authenticate(final HttpServerExchange exchange,
            final SecurityContext securityContext) {
        final ServletRequestContext servletRequestContext = exchange
                .getAttachment(ServletRequestContext.ATTACHMENT_KEY);
        final HttpServletRequest request = servletRequestContext.getOriginalRequest();
        final HttpServletResponse response = servletRequestContext.getOriginalResponse();

        /** Temp to see what's in context **/
//        WaffleHandler handler = new WaffleHandler();
//        try {
//            handler.handleRequest(exchange);
//        } catch (Exception e1) {
//            // Do nothing
//        }

        String accountName = WindowsAccountImpl.getCurrentUsername();
        try {
            // If accountName is null try using authenticator but that requires my hard-coded identity.
            if (accountName == null && this.authenticator.authenticate(request, response)) {
                final Principal principal = this.authenticator.doLogin(WindowsAccountImpl.getCurrentUsername(), "XXXXXXXX");
                accountName = principal == null ? null : principal.getName();
            }
        } catch (final Exception e) {
            WaffleAuthenticationMechanism.LOGGER.error("HTTP Authorization Header={}", request.getHeader("Authorization"));
            return AuthenticationMechanismOutcome.NOT_ATTEMPTED;
        }

        if (accountName == null) {
            return AuthenticationMechanismOutcome.NOT_AUTHENTICATED;
        }

        final SimplePrincipal principal = new SimplePrincipal(accountName, String.valueOf(UPTIME));

        final IdentityManager identityManager = securityContext.getIdentityManager();
        Account account = identityManager
                .verify(new AccountImpl(principal, Collections.<String> emptySet(), principal.getCredential()));
        if (account == null) {
            account = new AccountImpl(accountName);
        }

        securityContext.authenticationComplete(account, this.mechanismName, true);

        WaffleAuthenticationMechanism.LOGGER.debug("authentificated {}", principal);

        return AuthenticationMechanismOutcome.AUTHENTICATED;
    }

    /**
     * Send challenge.
     *
     * @param exchange the exchange
     * @param securityContext the security context
     * @return the challenge result
     */
    @Override
    public ChallengeResult sendChallenge(final HttpServerExchange exchange, final SecurityContext securityContext) {
        return new ChallengeResult(true, Integer.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
    }

    /**
     * The Class Factory.
     */
    public static final class Factory implements AuthenticationMechanismFactory {
        
        /* (non-Javadoc)
         * @see io.undertow.security.api.AuthenticationMechanismFactory#create(java.lang.String, io.undertow.server.handlers.form.FormParserFactory, java.util.Map)
         */
        @Override
        public AuthenticationMechanism create(final String mechanismName, final FormParserFactory formParserFactory,
                final Map<String, String> properties) {
            return new WaffleAuthenticationMechanism(mechanismName);
        }
    }

}
