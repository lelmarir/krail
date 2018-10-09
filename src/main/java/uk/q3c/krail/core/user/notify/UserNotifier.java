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

import org.apache.shiro.authz.UnauthorizedException;

import uk.q3c.krail.core.navigate.InvalidURIException;
import uk.q3c.krail.core.navigate.sitemap.NavigationState;
import uk.q3c.krail.core.user.UserModule;

/**
 * Provides a common entry point for all notifications to users.
 * <p/>
 * The developer can map which notifications are actually available using the
 * {@link UserModule} (for example, the Vaadin supplied notifications, a
 * MessageBar, popup dialogs etc). These all implement the
 * {@link UserNotification} interface.
 * <p/>
 * User options are supplied to enable users to determine which notifications
 * they prefer - assuming that the developer makes the selection of those
 * options available to the user.
 *
 * @author David Sowerby
 */
public interface UserNotifier {

	void notifyNoPermission(NavigationState targetNavigationState, UnauthorizedException throwable);

	void notifyInvalidURI(InvalidURIException error);
}
