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
package waffle.application;

import org.apache.catalina.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import waffle.apache.NegotiateAuthenticator;
import waffle.apache.WindowsRealm;
import waffle.components.CustomizationBean;
import waffle.controllers.IndexController;
import waffle.controllers.WaffleController;
import waffle.servlet.spi.BasicSecurityFilterProvider;
import waffle.servlet.spi.NegotiateSecurityFilterProvider;
import waffle.servlet.spi.SecurityFilterProvider;
import waffle.servlet.spi.SecurityFilterProviderCollection;
import waffle.spring.NegotiateSecurityFilter;
import waffle.spring.NegotiateSecurityFilterEntryPoint;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;

/**
 * The Class Application.
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
        return configureApplication(builder);
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(final String[] args) {
        Application.LOGGER.info("Initializing Waffle");
        Application.configureApplication(new SpringApplicationBuilder()).run(args);
    }

    /**
     * Configure application.
     *
     * @param builder the builder
     * @return the spring application builder
     */
    private static SpringApplicationBuilder configureApplication(final SpringApplicationBuilder builder) {
        return builder.sources(Application.class, CustomizationBean.class, IndexController.class, WaffleController.class).bannerMode(Mode.OFF);
    }

    /**
     * Tomcat embedded servlet container factory.
     *
     * @param negotiateAuthenticator the negotiate authenticator
     * @param windowsRealm the windows realm
     * @return the tomcat embedded servlet container factory
     */
    @Bean
    public TomcatEmbeddedServletContainerFactory tomcatEmbeddedServletContainerFactory(@Autowired NegotiateAuthenticator negotiateAuthenticator, @Autowired WindowsRealm windowsRealm) {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        factory.addContextValves(negotiateAuthenticator);
        factory.addContextCustomizers(new WindowsRealmContext(windowsRealm));
        return factory;
    }

    /**
     * The Class WindowsRealmContext.
     */
    protected static class WindowsRealmContext implements TomcatContextCustomizer {

        /** The windows realm. */
        WindowsRealm windowsRealm;

        /**
         * Instantiates a new windows realm context.
         *
         * @param windowsRealm the windows realm
         */
        public WindowsRealmContext(final WindowsRealm windowsRealm) {
            this.windowsRealm = windowsRealm;
        }

        @Override
        public void customize(Context context) {
            context.setRealm(this.windowsRealm);
        }
    }

    /**
     * The Class ApplicationSecurity.
     */
    @Configuration
    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
    protected static class ApplicationSecurity extends WebSecurityConfigurerAdapter {

        /** The negotiate security filter. */
        @Autowired
        NegotiateSecurityFilter negotiateSecurityFilter;

        /** The security filter provider collection. */
        @Autowired
        SecurityFilterProviderCollection securityFilterProviderCollection;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            //TODO This isn't working
//            http.addFilterBefore(negotiateSecurityFilter, BasicAuthenticationFilter.class).authorizeRequests().antMatchers("/**").fullyAuthenticated();
        }

        @Override
        public void configure(AuthenticationManagerBuilder auth) throws Exception {
            super.configure(auth);
        }

    }

    /**
     * Gets the negotiate authenticator.
     *
     * @return the negotiate authenticator
     */
    @Bean
    public NegotiateAuthenticator getNegotiateAuthenticator() {
        NegotiateAuthenticator valve = new NegotiateAuthenticator();
        valve.setPrincipalFormat("fqn");
        valve.setRoleFormat("both");
        valve.setProtocols("Negotiate,NTLM");
        return valve;
    }

    /**
     * Gets the windows realm.
     *
     * @return the windows realm
     */
    @Bean
    public WindowsRealm getWindowsRealm() {
        return new WindowsRealm();
    }

    /**
     * Gets the windows auth provider impl.
     *
     * @return the windows auth provider impl
     */
    @Bean
    public WindowsAuthProviderImpl getWindowsAuthProviderImpl() {
        return new WindowsAuthProviderImpl();
    }

    /**
     * Gets the negotiate security filter provider.
     *
     * @param windowsAuthProviderImpl the windows auth provider impl
     * @return the negotiate security filter provider
     */
    @Bean
    public NegotiateSecurityFilterProvider getNegotiateSecurityFilterProvider(@Autowired WindowsAuthProviderImpl windowsAuthProviderImpl) {
        return new NegotiateSecurityFilterProvider(windowsAuthProviderImpl);
    }

    /**
     * Gets the basic security filter provider.
     *
     * @param windowsAuthProviderImpl the windows auth provider impl
     * @return the basic security filter provider
     */
    @Bean
    public BasicSecurityFilterProvider getBasicSecurityFilterProvider(@Autowired WindowsAuthProviderImpl windowsAuthProviderImpl) {
        return new BasicSecurityFilterProvider(windowsAuthProviderImpl);
    }

    /**
     * Gets the security filter provider collection.
     *
     * @param negotiateSecurityFilterProvider the negotiate security filter provider
     * @param basicSecurityFilterProvider the basic security filter provider
     * @return the security filter provider collection
     */
    @Bean
    public SecurityFilterProviderCollection getSecurityFilterProviderCollection(
            @Autowired NegotiateSecurityFilterProvider negotiateSecurityFilterProvider,
            @Autowired BasicSecurityFilterProvider basicSecurityFilterProvider) {
        SecurityFilterProvider[] providerCollection = new SecurityFilterProvider[2];
        providerCollection[0] = negotiateSecurityFilterProvider;
        providerCollection[1] = basicSecurityFilterProvider;
        return new SecurityFilterProviderCollection(providerCollection);
    }

    /**
     * Gets the negotiate security filter.
     *
     * @param securityFilterProviderCollection the security filter provider collection
     * @return the negotiate security filter
     */
    @Bean
    public NegotiateSecurityFilter getNegotiateSecurityFilter(@Autowired SecurityFilterProviderCollection securityFilterProviderCollection ) {
        NegotiateSecurityFilter filter = new NegotiateSecurityFilter();
        filter.setProvider(securityFilterProviderCollection);
        return filter;
    }

    /**
     * Gets the negotiate security filter entry point.
     *
     * @param securityFilterProviderCollection the security filter provider collection
     * @return the negotiate security filter entry point
     */
    @Bean
    public NegotiateSecurityFilterEntryPoint getNegotiateSecurityFilterEntryPoint(@Autowired SecurityFilterProviderCollection securityFilterProviderCollection) {
        NegotiateSecurityFilterEntryPoint entryPoint = new NegotiateSecurityFilterEntryPoint();
        entryPoint.setProvider(securityFilterProviderCollection);
        return entryPoint;
    }

}