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

import org.apache.shiro.authz.UnauthenticatedException;

import uk.q3c.krail.core.navigate.Navigator;
import uk.q3c.krail.core.navigate.sitemap.NavigationState;
import uk.q3c.krail.core.navigate.sitemap.StandardPageKey;
import uk.q3c.krail.core.user.notify.UserNotifier;
import uk.q3c.krail.core.user.notify.UserNotifier.NotificationType;
import uk.q3c.krail.i18n.DescriptionKey;

import com.google.inject.Inject;

public class DefaultUnauthenticatedExceptionHandler implements UnauthenticatedExceptionHandler, Serializable {

	private static final long serialVersionUID = 7883073068352609958L;
	
	private final UserNotifier notifier;
	private final Navigator navigator;

	@Inject
	protected DefaultUnauthenticatedExceptionHandler(UserNotifier notifier, Navigator navigator) {
		super();
		this.notifier = notifier;
		this.navigator = navigator;
	}

	@Override
	public void onUnauthenticatedException(NavigationState targetNavigationState, UnauthenticatedException throwable) {
		navigator.navigateTo(StandardPageKey.Log_In);
		notifier.show(NotificationType.WARNING, DescriptionKey.You_have_not_logged_in);
		
	}

}
