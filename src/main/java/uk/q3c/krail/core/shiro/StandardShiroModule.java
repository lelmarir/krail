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
package uk.q3c.krail.core.shiro;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.guice.ShiroModule;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;

import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.AuthenticationNotifier;

import com.google.inject.binder.AnnotatedBindingBuilder;

/**
 * Bindings for Shiro and user related implementations
 * 
 * @author David Sowerby 15 Jul 2013
 * 
 */
public class StandardShiroModule extends ShiroModule {

	@Inject
	public static void setSecurityManager(SecurityManager securityManager) {
		SecurityUtils.setSecurityManager(securityManager);
	}

	public StandardShiroModule() {
		super();
	}

	@Override
	protected void configureShiro() {

		bindCredentialsMatcher();
		bindAuthenticationNotifier();
		bindLoginAttemptLog();
		bindRealms();
		bindSubjectIdentifier();
		expose(SubjectIdentifier.class);
		bindSubjectProvider();
		
		//to inject the method setSecurityManager()
		requestStaticInjection(StandardShiroModule.class);
	}

	/**
	 * Override this to provide your own {@link SubjectIdentifier} implementation
	 */
	protected void bindSubjectIdentifier() {
		bind(SubjectIdentifier.class).to(DefaultSubjectIdentifier.class);
	}

	/**
	 * Override this to bind your own Realm implementation(s). Multiple calls can be made to bindRealm();
	 */
	protected void bindRealms() {
		bindRealm().to(DefaultRealm.class);
	}

	/**
	 * Override this to bind your own implementation of {@link LoginAttemptLog}
	 */
	protected void bindLoginAttemptLog() {
		bind(LoginAttemptLog.class).to(DefaultLoginAttemptLog.class);
	}

	/**
	 * Override this method to bind your own {@link CredentialsMatcher} implementation
	 */
	protected void bindCredentialsMatcher() {
		bind(CredentialsMatcher.class).to(AlwaysPasswordCredentialsMatcher.class);
	}

	@Override
	protected void bindSecurityManager(AnnotatedBindingBuilder<? super SecurityManager> bind) {
		try {
			bind.toConstructor(KrailSecurityManager.class.getConstructor(Collection.class)).asEagerSingleton();
		} catch (NoSuchMethodException e) {
			throw new ConfigurationException(
					"This really shouldn't happen.  Either something has changed in Shiro, or there's a bug in ShiroModule.",
					e);
		}
	}
	
	private void bindAuthenticationNotifier() {
		bind(AuthenticationNotifier.class).to(KrailSecurityManager.class);
		expose(AuthenticationNotifier.class);
	}

	@Override
	protected void bindSessionManager(AnnotatedBindingBuilder<SessionManager> bind) {
		bind.to(VaadinSessionManager.class).asEagerSingleton();
	}

	protected void bindSubjectProvider() {
		bind(Subject.class).toProvider(SubjectProvider.class);
		expose(Subject.class);
	}

}
