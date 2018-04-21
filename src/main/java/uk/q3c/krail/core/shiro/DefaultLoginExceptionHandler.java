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

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ConcurrentAccessException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;

import com.google.inject.Inject;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

public class DefaultLoginExceptionHandler implements LoginExceptionHandler {

	@Inject
	protected DefaultLoginExceptionHandler() {
		;//FIXME: localizzare
	}

	@Override
	public void unknownAccount(AuthenticationToken token,
			UnknownAccountException uae) {
		showNotification("Login Error",
			"Unknown_Account", Type.WARNING_MESSAGE);
	}

	@Override
	public void incorrectCredentials(AuthenticationToken token,
			IncorrectCredentialsException ice) {
		showNotification("Login_Error",
				"Invalid_Login", Type.WARNING_MESSAGE);
	}

	@Override
	public void expiredCredentials(AuthenticationToken token,
			ExpiredCredentialsException ece) {
		showNotification("Login_Error",
				"Account_Expired", Type.WARNING_MESSAGE);
	}

	@Override
	public void accountLocked(AuthenticationToken token,
			LockedAccountException lae) {
		showNotification("Login_Error",
				"Account_Locked", Type.WARNING_MESSAGE);
	}

	@Override
	public void excessiveAttempts(AuthenticationToken token,
			ExcessiveAttemptsException excess) {
		showNotification("Login_Error",
				"Too_Many_Login_Attempts", Type.WARNING_MESSAGE);
	}

	@Override
	public void concurrentAccess(AuthenticationToken token,
			ConcurrentAccessException cae) {
		showNotification("Login_Error",
				"Account_Already_In_Use", Type.WARNING_MESSAGE);
	}

	@Override
	public void disabledAccount(AuthenticationToken token,
			DisabledAccountException dae) {
		showNotification("Login_Error",
				"Account_is_Disabled", Type.WARNING_MESSAGE);
	}

	@Override
	public void genericException(AuthenticationToken token,
			AuthenticationException ae) {
		showNotification("Login_Error",
				"Generic_Authentication_Exception", Type.WARNING_MESSAGE);
	}

	private void showNotification(String caption,
			String description, Type type) {
		Notification n = new Notification(caption,
				description, type);
		n.show(UI.getCurrent().getPage());
	}

}
