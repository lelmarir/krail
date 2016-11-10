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

import org.apache.shiro.authz.UnauthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.q3c.krail.core.navigate.DefaultNavigator;
import uk.q3c.krail.core.navigate.NavigationAuthorizationException;
import uk.q3c.krail.core.navigate.Navigator;
import uk.q3c.krail.core.navigate.sitemap.NavigationState;
import uk.q3c.krail.core.navigate.sitemap.StandardPageKey;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.AuthenticationListener;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.AuthenticationNotifier;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.FailedLoginEvent;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.LogoutEvent;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.SuccesfulLoginEvent;
import uk.q3c.krail.core.user.notify.UserNotifier;
import uk.q3c.krail.core.user.notify.UserNotifier.NotificationType;
import uk.q3c.krail.i18n.DescriptionKey;

import com.google.inject.Inject;

public class AutenticationHandler implements UnauthenticatedExceptionHandler, AuthenticationListener {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefaultNavigator.class);

	private final UserNotifier notifier;
	private final Navigator navigator;

	private NavigationState targetNavigationState = null;

	@Inject
	protected AutenticationHandler(UserNotifier notifier, Navigator navigator,
			AuthenticationNotifier authenticationNotifier) {
		super();
		this.notifier = notifier;
		this.navigator = navigator;
		authenticationNotifier.addListener(this);
	}

	protected void onUnauthenticatedException(
			NavigationState targetNavigationState,
			UnauthenticatedException throwable) {
		LOGGER.info("onUnauthenticatedException()");
		this.targetNavigationState = targetNavigationState;

		navigator.navigateTo(StandardPageKey.Log_In);

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
				onUnauthenticatedException(targetNavigationState,
						(UnauthenticatedException) cause);
				return true;
			}
		}
		return false;
	}

	/**
	 * When a user has successfully logged in, they are routed back to the page
	 * they were on before going to the login page. If they have gone straight
	 * to the login page (maybe they bookmarked it), or they were on the logout
	 * page, they will be routed to the 'private home page' (the StandardPage
	 * for {@link StandardPageKey#Private_Home})
	 */
	@Override
	public void onSuccess(SuccesfulLoginEvent event) {
		assert event.getSubject().isAuthenticated();
		LOGGER.info("onSuccess() user logged in successfully, navigating to appropriate view");

		// they have logged in
		if (targetNavigationState != null) {
			navigator.navigateTo(targetNavigationState);
		} else {
			navigator.navigateTo(StandardPageKey.Private_Home);
		}

	}

	@Override
	public void onFailure(FailedLoginEvent event) {
		LOGGER.info("onFailure() ");
		notifier.show(NotificationType.WARNING,
				DescriptionKey.You_have_not_logged_in);
	}

	@Override
	public void onLogout(LogoutEvent event) {
		LOGGER.info("onLogout() logging out");
		navigator.navigateTo(StandardPageKey.Log_Out);
	}
}
