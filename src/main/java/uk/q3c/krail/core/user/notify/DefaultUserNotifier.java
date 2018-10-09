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
package uk.q3c.krail.core.user.notify;

import java.io.Serializable;

import org.apache.shiro.authz.UnauthorizedException;

import com.google.inject.Inject;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import uk.q3c.krail.core.navigate.InvalidURIException;
import uk.q3c.krail.core.navigate.sitemap.NavigationState;

public class DefaultUserNotifier implements UserNotifier, Serializable {
	private static final long serialVersionUID = 1L;

	@Inject
	protected DefaultUserNotifier() {
		;
	}

	@Override
	public void notifyNoPermission(NavigationState targetNavigationState, UnauthorizedException throwable) {
		Notification.show("Access denied. No permission.", Type.ERROR_MESSAGE);
	}

	@Override
	public void notifyInvalidURI(InvalidURIException error) {
		Notification.show("Invalid URI: " + error.getMessage(), Type.ERROR_MESSAGE);
	}
}
