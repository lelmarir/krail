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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.subject.support.DelegatingSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import uk.q3c.krail.core.shiro.loginevent.AbstractAuthenticationEvent.SuccesfulLoginEventImpl;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.AuthenticationListener;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.AuthenticationNotifier;
import uk.q3c.krail.core.shiro.loginevent.AbstractAuthenticationEvent;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.FailedLoginEvent;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.LogoutEvent;

import com.vaadin.server.VaadinSession;

public class KrailSecurityManager extends DefaultSecurityManager implements AuthenticationNotifier {

	private static class SubjectSerializableWrapper implements Serializable {

		private String sessionId;
		private Subject subject;

		public SubjectSerializableWrapper(Subject subject) {
			this.subject = subject;
		}

		public Subject get() {
			if (subject == null && sessionId != null) {
				subject = new Subject.Builder().sessionId(sessionId).buildSubject();
			}
			return subject;
		}

		private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
			stream.writeObject(subject != null ? subject.getSession().getId() : null);
		}

		private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
			sessionId = (String) stream.readObject();
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(KrailSecurityManager.class);

	@Inject
	private VaadinSessionProvider sessionProvider;
	private LoadingCache<VaadinSession, Set<AuthenticationListener>> loginEventListeners;
	/**
	 * will be used if no sessions are present (background threads)
	 */
	private InheritableThreadLocal<Subject> threadLocalSubject = new InheritableThreadLocal<>();

	public KrailSecurityManager(Collection<Realm> realms) {
		super(realms);
		this.loginEventListeners = CacheBuilder.newBuilder().weakKeys()
				.build(new CacheLoader<VaadinSession, Set<AuthenticationListener>>() {

					@Override
					public Set<AuthenticationListener> load(VaadinSession key) throws Exception {
						return Collections.newSetFromMap(new WeakHashMap<AuthenticationListener, Boolean>());
					}

				});
	}

	private Set<AuthenticationListener> getCurrentSessionAuthenticationListeners() {
		try {
			return loginEventListeners.get(sessionProvider.get());
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onSuccessfulLogin(AuthenticationToken token, AuthenticationInfo info, Subject subject) {
		LOGGER.info("onSuccessfulLogin(user={})", token.getPrincipal());
		super.onSuccessfulLogin(token, info, subject);
		setSubject(subject);
		fireSuccessfulLoginEvent(token, info, subject);
	}

	private void fireSuccessfulLoginEvent(AuthenticationToken token, AuthenticationInfo info, Subject subject) {
		SuccesfulLoginEventImpl event = new AbstractAuthenticationEvent.SuccesfulLoginEventImpl(subject, token, info);
		ArrayList<AuthenticationListener> list = new ArrayList<>(getCurrentSessionAuthenticationListeners());
		for (AuthenticationListener l : list) {
			l.onSuccess(event);
		}
	}

	@Override
	protected void onFailedLogin(AuthenticationToken token, AuthenticationException ae, Subject subject) {
		LOGGER.info("onFailedLogin(user={}, exception={})", token.getPrincipal(), ae.toString());
		super.onFailedLogin(token, ae, subject);
		fireFailedLoginEvent(token, ae, subject);
	}

	private void fireFailedLoginEvent(AuthenticationToken token, AuthenticationException ae, Subject subject) {
		FailedLoginEvent event = new AbstractAuthenticationEvent.FailedLoginEventImpl(subject, token, ae);
		ArrayList<AuthenticationListener> list = new ArrayList<>(getCurrentSessionAuthenticationListeners());
		for (AuthenticationListener l : list) {
			l.onFailure(event);
		}
	}

	private void fireLogoutEvent(Subject subject) {
		LogoutEvent event = new AbstractAuthenticationEvent.LogoutEventImpl(subject);
		ArrayList<AuthenticationListener> list = new ArrayList<>(getCurrentSessionAuthenticationListeners());
		for (AuthenticationListener l : list) {
			l.onLogout(event);
		}
	}

	@Override
	public Subject createSubject(SubjectContext context) {
		SecurityManager securityManager = context.resolveSecurityManager();
		Session session = context.resolveSession();
		boolean sessionCreationEnabled = context.isSessionCreationEnabled();
		PrincipalCollection principals = context.resolvePrincipals();
		boolean authenticated = context.resolveAuthenticated();
		String host = context.resolveHost();

		return new DelegatingSubject(principals, authenticated, host, session, sessionCreationEnabled,
				securityManager) {
			@Override
			public void logout() {
				LOGGER.info("logout(user={})", principals.getPrimaryPrincipal());
				super.logout();
				fireLogoutEvent(this);
			}
		};
	}

	public Subject getSubject() {
		VaadinSession session = sessionProvider.get();
		if (session != null) {
			SubjectSerializableWrapper subjectWrapper = session.getAttribute(SubjectSerializableWrapper.class);
			if (subjectWrapper == null) {
				LOGGER.debug(
						"VaadinSession is valid, but does not have a stored Subject, creating a new Subject (will be stored now)");
				subjectWrapper = new SubjectSerializableWrapper(new Subject.Builder().buildSubject());
				session.setAttribute(SubjectSerializableWrapper.class, subjectWrapper);
			}
			Subject subject = subjectWrapper.get();

			if (threadLocalSubject.get() != null) {
				throw new IllegalStateException(
						"when is present a session, the threadLocalSubject should be null! (thread='"
								+ Thread.currentThread().getName() + "', session='" + session + "', session.subject='"
								+ subject + "', threadLocalSubject='" + threadLocalSubject.get() + "')");
			}
			return subject;
		} else {
			Subject subject = threadLocalSubject.get();
			if (subject != null) {
				LOGGER.debug("no session present, returning subject from ThreadLocalVariable: {}", subject);
				return subject;
			} else {
				throw new IllegalStateException("No session and no threadLocal subject!");
			}
		}

	}

	protected void setSubject(Subject subject) {
		VaadinSession session = sessionProvider.get();
		if (session != null) {
			LOGGER.debug("storing Subject instance in VaadinSession");
			session.setAttribute(SubjectSerializableWrapper.class, new SubjectSerializableWrapper(subject));
		} else {
			throw new IllegalStateException("no session present, if you are running a background thread use runForSubject() instead");
		}
	}

	//TODO:use @RunAs annotation instead
	public void runAsSubject(Subject subject, Runnable runnable) {
		this.threadLocalSubject.set(subject);
		MDC.put("subject", subject.toString());
		try {
			runnable.run();
		} finally {
			MDC.remove("subject");
			this.threadLocalSubject.set(null);
		}
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
		getCurrentSessionAuthenticationListeners().add(listener);
	}

	@Override
	public void removeListener(AuthenticationListener listener) {
		getCurrentSessionAuthenticationListeners().remove(listener);
	}
}
