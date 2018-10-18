/*
 * Copyright (c) 2014 David Sowerby
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package uk.q3c.krail.core.shiro;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.q3c.krail.core.guice.uiscope.UIScoped;
import uk.q3c.krail.core.navigate.DefaultNavigator;
import uk.q3c.krail.core.navigate.NavigationAuthorizationException;
import uk.q3c.krail.core.navigate.Navigator;
import uk.q3c.krail.core.navigate.sitemap.NavigationState;
import uk.q3c.krail.core.navigate.sitemap.StandardPageKey;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.AuthenticationListener;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.FailedLoginEvent;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.LogoutEvent;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.SuccesfulLoginEvent;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.vaadin.ui.UI;

@UIScoped
public class AuthenticationHandler implements UnauthenticatedExceptionHandler, AuthenticationListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNavigator.class);

	@Inject
	private Provider<Navigator> navigatorProvider;
	private UI ui;

	private NavigationState targetNavigationStateBeforeUnathenticatedException = null;
	private NavigationState previousNavigationStateBeforeUnathenticatedException = null;

	@Inject
	protected AuthenticationHandler() {
		super();
		this.ui = UI.getCurrent();
	}

	protected void onUnauthenticatedException(NavigationState targetNavigationState,
			UnauthenticatedException throwable) {
		LOGGER.debug("onUnauthenticatedException(targetNavigationState={})", targetNavigationState);
		this.targetNavigationStateBeforeUnathenticatedException = targetNavigationState;
		this.previousNavigationStateBeforeUnathenticatedException = navigatorProvider.get().getCurrentNavigationState();

		navigatorProvider.get().navigateTo(StandardPageKey.Log_In);
	}

	@Override
	public boolean handle(ErrorEvent event) {
		Throwable throwable = event.getThrowable();
		if (throwable instanceof NavigationAuthorizationException) {
			NavigationState targetNavigationState = ((NavigationAuthorizationException) throwable)
					.getTargetNavigationState();
			Throwable cause = throwable.getCause();
			// handle an unauthenticated access attempt
			if (cause instanceof UnauthenticatedException) {
				onUnauthenticatedException(targetNavigationState, (UnauthenticatedException) cause);
				return true;
			}
		}
		return false;
	}

	/**
	 * When a user has successfully logged in, they are routed back to the page they
	 * were on before going to the login page. If they have gone straight to the
	 * login page (maybe they bookmarked it), or they were on the logout page, they
	 * will be routed to the 'private home page' (the StandardPage for
	 * {@link StandardPageKey#Private_Home})
	 */
	@Override
	public void onSuccess(SuccesfulLoginEvent event) {
		assert event.getSubject().isAuthenticated();

		LOGGER.info("onSuccessfulLogin(user={})", event.getSubject());
		ui.accessSynchronously(() -> {
			// they have logged in
			if (targetNavigationStateBeforeUnathenticatedException != null) {
				LOGGER.debug("onSuccessfulLogin(), navigating to previous navigation state '{}'",
						targetNavigationStateBeforeUnathenticatedException);
				try {
					navigatorProvider.get().navigateTo(targetNavigationStateBeforeUnathenticatedException);
				} catch (AuthorizationException e) {
					// the user does not have the permission for the required page
					event.getSubject().logout();
					throw e;
				} finally {
					targetNavigationStateBeforeUnathenticatedException = null;
					previousNavigationStateBeforeUnathenticatedException = null;
				}
			} else {
				// navigazione diretta alla pagina di login?
				navigatorProvider.get().navigateTo(StandardPageKey.Private_Home);
			}
		});

	}

	@Override
	public void onFailure(FailedLoginEvent event) {
		LOGGER.info("onFailedLogin(user={}, exception={})", event.getSubject(), event.getException().getMessage());
	}

	@Override
	public void onLogout(LogoutEvent event) {
		LOGGER.info("logout(user={})", event.getSubject());
		navigatorProvider.get().navigateTo(StandardPageKey.Log_Out);
	}
}
