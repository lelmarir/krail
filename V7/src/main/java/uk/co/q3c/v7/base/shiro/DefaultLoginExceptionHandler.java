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
package uk.co.q3c.v7.base.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ConcurrentAccessException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;

import uk.co.q3c.v7.i18n.DescriptionKey;
import uk.co.q3c.v7.i18n.Translate;

import com.google.inject.Inject;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

public class DefaultLoginExceptionHandler implements LoginExceptionHandler {

	private final Translate translate;

	@Inject
	protected DefaultLoginExceptionHandler(Translate translate) {
		this.translate = translate;
	}

	@Override
	public void unknownAccount(AuthenticationToken token,
			UnknownAccountException uae) {
		showNotification(DescriptionKey.Login_Error_Caption,
				DescriptionKey.Unknown_Account, Type.WARNING_MESSAGE);
	}

	@Override
	public void incorrectCredentials(AuthenticationToken token,
			IncorrectCredentialsException ice) {
		showNotification(DescriptionKey.Login_Error_Caption,
				DescriptionKey.Invalid_Login, Type.WARNING_MESSAGE);
	}

	@Override
	public void expiredCredentials(AuthenticationToken token,
			ExpiredCredentialsException ece) {
		showNotification(DescriptionKey.Login_Error_Caption,
				DescriptionKey.Account_Expired, Type.WARNING_MESSAGE);
	}

	@Override
	public void accountLocked(AuthenticationToken token,
			LockedAccountException lae) {
		showNotification(DescriptionKey.Login_Error_Caption,
				DescriptionKey.Account_Locked, Type.WARNING_MESSAGE);
	}

	@Override
	public void excessiveAttempts(AuthenticationToken token,
			ExcessiveAttemptsException excess) {
		showNotification(DescriptionKey.Login_Error_Caption,
				DescriptionKey.Too_Many_Login_Attempts, Type.WARNING_MESSAGE);
	}

	@Override
	public void concurrentAccess(AuthenticationToken token,
			ConcurrentAccessException cae) {
		showNotification(DescriptionKey.Login_Error_Caption,
				DescriptionKey.Account_Already_In_Use, Type.WARNING_MESSAGE);
	}

	@Override
	public void disabledAccount(AuthenticationToken token,
			DisabledAccountException dae) {
		showNotification(DescriptionKey.Login_Error_Caption,
				DescriptionKey.Account_is_Disabled, Type.WARNING_MESSAGE);
	}

	@Override
	public void genericException(AuthenticationToken token,
			AuthenticationException ae) {
		showNotification(DescriptionKey.Login_Error_Caption,
				DescriptionKey.Generic_Authentication_Exception, Type.WARNING_MESSAGE);
	}

	private void showNotification(DescriptionKey caption,
			DescriptionKey description, Type type) {
		Notification n = new Notification(translate.from(caption),
				translate.from(description), type);
		n.show(UI.getCurrent().getPage());
	}

}
