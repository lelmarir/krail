/*
 * Copyright (C) 2013 David Sowerby
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.q3c.krail.core.guice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;

import org.apache.shiro.guice.ShiroModule;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import uk.q3c.krail.core.guice.errors.ErrorModule;
import uk.q3c.krail.core.guice.threadscope.ThreadScopeModule;
import uk.q3c.krail.core.guice.uiscope.UIScopeModule;
import uk.q3c.krail.core.guice.vsscope.VaadinSessionScopeModule;
import uk.q3c.krail.core.navigate.sitemap.SitemapModule;
import uk.q3c.krail.core.services.ServiceManagerModule;
import uk.q3c.krail.core.shiro.ShiroVaadinModule;
import uk.q3c.krail.core.shiro.StandardShiroModule;
import uk.q3c.krail.core.user.UserModule;
import uk.q3c.krail.core.user.opt.UserOptionModule;
import uk.q3c.krail.core.view.ViewModule;
import uk.q3c.krail.i18n.I18NModule;

import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.guice.LifecycleInjectorMode;

public abstract class DefaultBindingManager
		extends GuiceServletContextListener {
	protected static Injector injector;
	private static Logger log = LoggerFactory
			.getLogger(DefaultBindingManager.class);

	private String basePackage = "";
	private Reflections basePackageReflections;
	
	private ServiceManagerModule servicesModule;
	private I18NModule i18NModule;
	private UserOptionModule userOptionModule;
	private ServletModule servletModule;
	private ShiroVaadinModule shiroVaadinModule;
	private SitemapModule sitemapModule;
	private ViewModule viewModule;
	private ShiroModule shiroModule;
	private UserModule userModule;
	private ErrorModule errorModule;
	private boolean automaticStaticInjection = true;

	private Collection<Module> appModules = new ArrayList<>();

	protected DefaultBindingManager() {
		super();
		configure();
	}

	protected abstract void configure();

	public Injector getInjector(boolean create) {
		if (injector == null && create) {
			injector = createInjector();
			log.debug("injector created");
		}
		return injector;
	}

	/**
	 * Module instances for the core should be added in {@link #getModules()}.
	 * Module instances for the app using Krail should be added to
	 * {@link AppModules#appModules()}
	 *
	 * @see com.google.inject.servlet.GuiceServletContextListener#getInjector()
	 */
	@Override
	protected Injector getInjector() {
		return getInjector(true);
	}

	protected Injector createInjector() {
		bridgeJULToSLF4J();

		return LifecycleInjector.builder().usingBasePackages(getBasePackage())
				.withMode(LifecycleInjectorMode.SIMULATED_CHILD_INJECTORS)
				.withModules(getModules()).build().createInjector();
	}

	private void bridgeJULToSLF4J() {
		// Optionally remove existing handlers attached to j.u.l root logger
		SLF4JBridgeHandler.removeHandlersForRootLogger(); // (since SLF4J 1.6.5)

		// add SLF4JBridgeHandler to j.u.l's root logger, should be done once
		// during
		// the initialization phase of your application
		SLF4JBridgeHandler.install();
	}

	protected List<Module> getModules() {
		List<Module> coreModules = new ArrayList<>();

		coreModules.add(getI18NModule());
		coreModules.add(getSitemapModule());

		coreModules.add(new ThreadScopeModule());
		coreModules.add(new UIScopeModule());
		coreModules.add(new VaadinSessionScopeModule());

		coreModules.add(getServicesModule());

		coreModules.add(getErrorModule());

		coreModules.add(getShiroModule());
		coreModules.add(getShiroVaadinModule());
		coreModules.add(new ShiroAopModule());

		coreModules.add(getViewModule());

		coreModules.add(getUserModule());

		coreModules.add(getUserOptionModule());

		coreModules.addAll(appModules);
		
		if(automaticStaticInjection) {
			coreModules.add(new StaticInjectionModule(getBasePackageReflections()));
		}

		// bind after appModules to allow other servlets
		coreModules.add(getServletModule());
		return coreModules;
	}

	private void checkIfConfigurationStillPossible() {
		if (injector != null) {
			throw new UnsupportedOperationException(
					"The injector has already been created, it's no more possible to change the configuration");
		}
	}
	
	public String getBasePackage() {
		if(log.isDebugEnabled()) {
			if(basePackage == null || basePackage.isEmpty()) {
				log.warn("No basePackadge provided for reflection.");
			}
		}
		return basePackage;
	}
	
	public Reflections getBasePackageReflections() {
		if(basePackageReflections == null) {
			basePackageReflections = new Reflections(getBasePackage());
		}
		return basePackageReflections;
	}
	
	public void setBasePackage(String basePackage) {
		checkIfConfigurationStillPossible();
		this.basePackage = basePackage;
	}

	protected UserOptionModule getUserOptionModule() {
		if (this.userOptionModule == null) {
			this.userOptionModule = new UserOptionModule();
		}
		return this.userOptionModule;
	}

	public void setUserOptionModule(UserOptionModule userOptionModule) {
		checkIfConfigurationStillPossible();
		this.userOptionModule = userOptionModule;
	}

	protected I18NModule getI18NModule() {
		if (this.i18NModule == null) {
			this.i18NModule = new I18NModule();
		}
		return this.i18NModule;
	}

	public void setI18NModule(I18NModule i18nModule) {
		checkIfConfigurationStillPossible();
		this.i18NModule = i18nModule;
	}

	protected ServletModule getServletModule() {
		if (this.servletModule == null) {
			throw new IllegalStateException("You must provide a BaseServletModule implementation");
		}
		return this.servletModule;
	}

	public void setServletModule(ServletModule servletModule) {
		checkIfConfigurationStillPossible();
		this.servletModule = servletModule;
	}

	public ServiceManagerModule getServicesModule() {
		if (this.servicesModule == null) {
			this.servicesModule = new ServiceManagerModule();
		}
		return this.servicesModule;
	}

	public void setServicesModule(ServiceManagerModule servicesModule) {
		checkIfConfigurationStillPossible();
		this.servicesModule = servicesModule;
	}

	protected ShiroVaadinModule getShiroVaadinModule() {
		if (this.shiroVaadinModule == null) {
			this.shiroVaadinModule = new ShiroVaadinModule();
		}
		return this.shiroVaadinModule;
	}

	public void setShiroVaadinModule(ShiroVaadinModule shiroVaadinModule) {
		checkIfConfigurationStillPossible();
		this.shiroVaadinModule = shiroVaadinModule;
	}

	public SitemapModule getSitemapModule() {
		if (this.sitemapModule == null) {
			this.sitemapModule = new SitemapModule(getBasePackageReflections());
		}
		return this.sitemapModule;
	}

	public void setSitemapModule(SitemapModule sitemapModule) {
		checkIfConfigurationStillPossible();
		this.sitemapModule = sitemapModule;
	}

	protected ViewModule getViewModule() {
		if (this.viewModule == null) {
			this.viewModule = new ViewModule();
		}
		return this.viewModule;
	}

	public void setViewModule(ViewModule viewModule) {
		checkIfConfigurationStillPossible();
		this.viewModule = viewModule;
	}

	protected ShiroModule getShiroModule() {
		if (this.shiroModule == null) {
			this.shiroModule = new StandardShiroModule();
		}
		return this.shiroModule;
	}

	public void setShiroModule(StandardShiroModule shiroModule) {
		checkIfConfigurationStillPossible();
		this.shiroModule = shiroModule;
	}

	private UserModule getUserModule() {
		if (this.userModule == null) {
			this.userModule = new UserModule();
		}
		return this.userModule;
	}

	public void setUserModule(UserModule userModule) {
		checkIfConfigurationStillPossible();
		this.userModule = userModule;
	}

	protected ErrorModule getErrorModule() {
		if (this.errorModule == null) {
			this.errorModule = new ErrorModule(getBasePackageReflections());
		}
		return this.errorModule;
	}

	public void setErrorModule(ErrorModule errorModule) {
		checkIfConfigurationStillPossible();
		this.errorModule = errorModule;
	}

	protected void addModule(Module module) {
		this.appModules.add(module);
	}
	
	public boolean isAutomaticStaticInjection() {
		return automaticStaticInjection;
	}
	
	public void setAutomaticStaticInjection(boolean automaticStaticInjection) {
		this.automaticStaticInjection = automaticStaticInjection;
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		super.contextInitialized(servletContextEvent);
		ServiceManager servicesManager = getInjector()
				.getInstance(ServiceManager.class);
		if (servicesManager != null) {
			servicesManager.startAsync();
			servicesManager.awaitHealthy();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		log.info("Stopping services");
		try {
			Injector injector = getInjector(false);
			if (injector != null) {
				ServiceManager serviceManager = injector
						.getInstance(ServiceManager.class);
				serviceManager.stopAsync();
				log.debug("Waiting for services to stop...");
				serviceManager.awaitStopped(60, TimeUnit.SECONDS);
				log.debug("all service stopped.");
			}
		} catch (Exception e) {
			log.error("Exception while stopping services", e);
		}

		// context may not have been crated, and super does not check for it
		if (servletContextEvent.getServletContext() != null) {
			super.contextDestroyed(servletContextEvent);
		}
	}
}