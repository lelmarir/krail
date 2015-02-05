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

import uk.q3c.krail.core.navigate.sitemap.NavigationState;
import uk.q3c.krail.core.user.notify.UserNotifier;
import uk.q3c.krail.core.user.notify.UserNotifier.NotificationType;
import uk.q3c.krail.i18n.DescriptionKey;

import com.google.inject.Inject;

public class DefaultUnauthorizedExceptionHandler implements UnauthorizedExceptionHandler, Serializable {

	private final UserNotifier notifier;

	@Inject
	protected DefaultUnauthorizedExceptionHandler(UserNotifier notifier) {
		super();
		this.notifier = notifier;
	}

	@Override
	public void onUnauthorizedException(NavigationState targetNavigationState, UnauthorizedException throwable) {
		notifier.show(NotificationType.ERROR, DescriptionKey.No_Permission);
	}
}
