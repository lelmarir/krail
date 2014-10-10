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
import org.apache.shiro.authc.ConcurrentAccessException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;

import uk.co.q3c.v7.base.view.LoginView;
import uk.co.q3c.v7.i18n.DescriptionKey;

import com.google.inject.Inject;

public class DefaultLoginExceptionHandler implements LoginExceptionHandler {

	@Inject
	protected DefaultLoginExceptionHandler() {
	}

	@Override
	public void unknownAccount(LoginView loginView, UsernamePasswordToken token, UnknownAccountException uae) {
		loginView.setStatusMessage(DescriptionKey.Unknown_Account);
	}

	@Override
	public void incorrectCredentials(LoginView loginView,
			UsernamePasswordToken token, IncorrectCredentialsException ice) {
		loginView.setStatusMessage(DescriptionKey.Invalid_Login);
	}

	@Override
	public void expiredCredentials(LoginView loginView,
			UsernamePasswordToken token, ExpiredCredentialsException ece) {
		loginView.setStatusMessage(DescriptionKey.Account_Expired);
	}

	@Override
	public void accountLocked(LoginView loginView, UsernamePasswordToken token,
			LockedAccountException lae) {
		loginView.setStatusMessage(DescriptionKey.Account_Locked);
	}

	@Override
	public void excessiveAttempts(LoginView loginView,
			UsernamePasswordToken token, ExcessiveAttemptsException excess) {
		loginView.setStatusMessage(DescriptionKey.Too_Many_Login_Attempts);
	}

	@Override
	public void concurrentAccess(LoginView loginView,
			UsernamePasswordToken token, ConcurrentAccessException cae) {
		loginView.setStatusMessage(DescriptionKey.Account_Already_In_Use);
	}

	@Override
	public void disabledAccount(LoginView loginView, UsernamePasswordToken token, DisabledAccountException dae) {
		loginView.setStatusMessage(DescriptionKey.Account_is_Disabled);
	}

	@Override
	public void genericException(LoginView loginView,
			UsernamePasswordToken token, AuthenticationException ae) {
		loginView.setStatusMessage(DescriptionKey.Generic_Authentication_Exception);
	}

}
