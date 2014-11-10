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
import java.util.LinkedList;
import java.util.List;

import com.google.inject.Inject;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.q3c.krail.core.shiro.loginevent.AbstractAuthenticationEvent.SuccesfulLoginEventImpl;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.AuthenticationListener;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.AuthenticationNotifier;
import uk.q3c.krail.core.shiro.loginevent.AbstractAuthenticationEvent;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.FailedLoginEvent;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.LogoutEvent;

import com.vaadin.server.VaadinSession;

public class KrailSecurityManager extends DefaultSecurityManager implements AuthenticationNotifier {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(KrailSecurityManager.class);
	
	@Inject
	private VaadinSessionProvider sessionProvider;
	private List<AuthenticationListener> loginEventListeners;
	
	public KrailSecurityManager(Collection<Realm> realms) {
		super(realms);
		this.loginEventListeners = new LinkedList<>();
	}

	@Override
	protected void onSuccessfulLogin(AuthenticationToken token,
			AuthenticationInfo info, Subject subject) {
		super.onSuccessfulLogin(token, info, subject);
		setSubject(subject);
		fireSuccessfulLoginEvent(token, info, subject);
	}

	private void fireSuccessfulLoginEvent(AuthenticationToken token,
			AuthenticationInfo info, Subject subject) {
		SuccesfulLoginEventImpl event = new AbstractAuthenticationEvent.SuccesfulLoginEventImpl(subject, token, info);
		for(AuthenticationListener l : loginEventListeners) {
			l.onSuccess(event);
		}
	}

	@Override
	protected void onFailedLogin(AuthenticationToken token,
			AuthenticationException ae, Subject subject) {
		super.onFailedLogin(token, ae, subject);
		fireFailedLoginEvent(token, ae, subject);
	}
	
	private void fireFailedLoginEvent(AuthenticationToken token,
			AuthenticationException ae, Subject subject) {
		FailedLoginEvent event = new AbstractAuthenticationEvent.FailedLoginEventImpl(subject, token, ae);
		for(AuthenticationListener l : loginEventListeners) {
			l.onFailure(event);
		}
	}

	@Override
	protected void beforeLogout(Subject subject) {
		super.beforeLogout(subject);
		fireLogoutEvent(subject);
	}

	private void fireLogoutEvent(Subject subject) {
		LogoutEvent event = new AbstractAuthenticationEvent.LogoutEventImpl(subject);
		for(AuthenticationListener l : loginEventListeners) {
			l.onLogout(event);
		}
	}

	protected void setSubject(Subject subject) {
		VaadinSession session = sessionProvider.get();
		LOGGER.debug("storing Subject instance in VaadinSession");
		session.setAttribute(Subject.class, subject);
	}

	/**
	 * Method injection is needed because the constructor has to complete
	 * 
	 * @see org.apache.shiro.mgt.SessionsSecurityManager#setSessionManager(org.apache.shiro.session.mgt.SessionManager)
	 */
	@Inject
	@Override
	public void setSessionManager(SessionManager sessionManager) {
		super.setSessionManager(sessionManager);
	}

	public void setSessionProvider(VaadinSessionProvider sessionProvider) {
		this.sessionProvider = sessionProvider;
	}

	@Override
	public void addListener(AuthenticationListener listener) {
		loginEventListeners.add(listener);
	}

	@Override
	public void removeListener(AuthenticationListener listener) {
		loginEventListeners.remove(listener);
	}
}
