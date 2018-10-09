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

import java.io.Serializable;

import org.apache.shiro.authz.UnauthorizedException;

import uk.q3c.krail.core.navigate.NavigationAuthorizationException;
import uk.q3c.krail.core.navigate.Navigator;
import uk.q3c.krail.core.navigate.sitemap.NavigationState;
import uk.q3c.krail.core.navigate.sitemap.StandardPageKey;
import uk.q3c.krail.core.user.notify.UserNotifier;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class DefaultUnauthorizedExceptionHandler implements UnauthorizedExceptionHandler, Serializable {

	private final UserNotifier notifier;

	@Inject
	private Provider<Navigator> navigatorProvider;

	@Inject
	protected DefaultUnauthorizedExceptionHandler(UserNotifier notifier) {
		super();
		this.notifier = notifier;
	}

	protected void onUnauthorizedException(NavigationState targetNavigationState, UnauthorizedException throwable) {
		notifier.notifyNoPermission(targetNavigationState, throwable);
		Navigator navigator = navigatorProvider.get();
		if (navigator.getCurrentNavigationState() == null
				|| navigator.getCurrentNavigationState().equals(targetNavigationState)) {
			navigator.navigateTo(StandardPageKey.Private_Home);
		}
	}

	@Override
	public boolean handle(ErrorEvent event) {
		Throwable throwable = event.getThrowable();
		if (throwable instanceof NavigationAuthorizationException) {
			Throwable cause = throwable.getCause();
			NavigationState targetNavigationState = ((NavigationAuthorizationException) throwable)
					.getTargetNavigationState();
			// handle an unauthorised access attempt
			if (cause instanceof UnauthorizedException) {
				onUnauthorizedException(targetNavigationState, (UnauthorizedException) cause);
				return true;
			}
		}
		return false;
	}
}
