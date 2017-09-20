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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public abstract class DefaultBindingManager extends GuiceServletContextListener {
	protected static Injector injector;
	private static Logger log = LoggerFactory.getLogger(DefaultBindingManager.class);

	private ServiceManagerModule serviceManagerModule;
	private Module i18NModule;
	private Module userOptionModule;
	private Module servletModule;
	private Module shiroVaadinModule;
	private Module sitemapModule;
	private Module viewModule;
	private Module shiroModule;
	private Module userModule;
	private Module errorModule;

	private Collection<Module> appModules = new ArrayList<>();

	protected DefaultBindingManager() {
		super();
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
		return Guice.createInjector(getModules());
	}

	protected List<Module> getModules() {
		List<Module> coreModules = new ArrayList<>();

		coreModules.add(getI18NModule());
		coreModules.add(getSitemapModule());

		coreModules.add(new ThreadScopeModule());
		coreModules.add(new UIScopeModule());
		coreModules.add(new VaadinSessionScopeModule());

		serviceManagerModule = new ServiceManagerModule();
		coreModules.add(serviceManagerModule);

		coreModules.add(getErrorModule());

		coreModules.add(getShiroModule());
		coreModules.add(getShiroVaadinModule());
		coreModules.add(new ShiroAopModule());

		coreModules.add(getServletModule());

		coreModules.add(getViewModule());

		coreModules.add(getUserModule());

		coreModules.add(getUserOptionModule());

		coreModules.addAll(appModules);
		return coreModules;
	}

	private void checkIfConfigurationStillPossible() {
		if (injector != null) {
			throw new UnsupportedOperationException(
					"The injector has already been created, it's no more possible to change the configuration");
		}
	}

	protected Module getUserOptionModule() {
		if (this.userOptionModule == null) {
			this.userOptionModule = new UserOptionModule();
		}
		return this.userOptionModule;
	}

	public void setUserOptionModule(UserOptionModule userOptionModule) {
		checkIfConfigurationStillPossible();
		this.userOptionModule = userOptionModule;
	}

	protected Module getI18NModule() {
		if (this.i18NModule == null) {
			this.i18NModule = new I18NModule();
		}
		return this.i18NModule;
	}

	public void setI18NModule(I18NModule i18nModule) {
		checkIfConfigurationStillPossible();
		this.i18NModule = i18nModule;
	}

	protected Module getServletModule() {
		if (this.servletModule == null) {
			this.servletModule = new BaseServletModule();
		}
		return this.servletModule;
	}

	public void setServletModule(ServletModule servletModule) {
		checkIfConfigurationStillPossible();
		this.servletModule = servletModule;
	}

	protected Module getShiroVaadinModule() {
		if (this.shiroVaadinModule == null) {
			this.shiroVaadinModule = new ShiroVaadinModule();
		}
		return this.shiroVaadinModule;
	}

	public void setShiroVaadinModule(ShiroVaadinModule shiroVaadinModule) {
		checkIfConfigurationStillPossible();
		this.shiroVaadinModule = shiroVaadinModule;
	}

	public Module getSitemapModule() {
		if (this.sitemapModule == null) {
			this.sitemapModule = new SitemapModule();
		}
		return this.sitemapModule;
	}

	public void setSitemapModule(Module sitemapModule) {
		checkIfConfigurationStillPossible();
		this.sitemapModule = sitemapModule;
	}

	protected Module getViewModule() {
		if (this.viewModule == null) {
			this.viewModule = new ViewModule();
		}
		return this.viewModule;
	}

	public void setViewModule(Module viewModule) {
		checkIfConfigurationStillPossible();
		this.viewModule = viewModule;
	}

	protected Module getShiroModule() {
		if (this.shiroModule == null) {
			this.shiroModule = new StandardShiroModule();
		}
		return this.shiroModule;
	}

	public void setShiroModule(StandardShiroModule shiroModule) {
		checkIfConfigurationStillPossible();
		this.shiroModule = shiroModule;
	}

	private Module getUserModule() {
		if (this.userModule == null) {
			this.userModule = new UserModule();
		}
		return this.userModule;
	}

	public void setUserModule(UserModule userModule) {
		checkIfConfigurationStillPossible();
		this.userModule = userModule;
	}

	protected Module getErrorModule() {
		if(this.errorModule == null) {
			this.errorModule = new ErrorModule();
		}
		return this.errorModule;
	}
	
	public void setErrorModule(Module errorModule) {
		checkIfConfigurationStillPossible();
		this.errorModule = errorModule;
	}

	protected void addModule(Module module) {
		this.appModules.add(module);
	}

	public final ScopedBindingBuilder addService(Class<? extends Service> service) {
		return serviceManagerModule.addService(service);
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		log.info("Stopping services");
		try {
			Injector injector = getInjector(false);
			if (injector != null) {
				ServiceManager serviceManager = injector.getInstance(ServiceManager.class);
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