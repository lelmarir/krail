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

import javax.security.auth.login.AccountLockedException;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ConcurrentAccessException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * Implementations should handle all Shiro exceptions captured during the login process.
 *
 * @author David Sowerby 14 Jan 2013
 */
public interface LoginExceptionHandler {
    /**
     * Response to an {@link UnknownAccountException}. This would be the response to a "normal" login failure - that
     * is,
     * before it becomes an {@link #excessiveAttempts(LoginView, UsernamePasswordToken)} event
     *
     * @param loginView
     * @param token
     */
	void unknownAccount(AuthenticationToken token,
			UnknownAccountException uae);

    /**
     * Response to {@link IncorrectCredentialsException}. See the javadoc of the exception
     *
     * @param loginView
     * @param token
     */
	void incorrectCredentials(AuthenticationToken token,
			IncorrectCredentialsException ice);

    /**
     * Response to {@link ExpiredCredentialsException}. See the javadoc of the exception. Typically, the implementation
     * of this method will navigate to a KrailView which allows the user to update their password.
     *
     * @param loginView
     * @param token
     */
	void expiredCredentials(AuthenticationToken token,
			ExpiredCredentialsException ece);

    /**
     * Response to {@link AccountLockedException}. See the javadoc of the exception. Typically, the implementation of
     * this method will navigate to a KrailView which allows the user to request that their account is unlocked,
     * although it perhaps just inform the user and do nothing else.
     *
     * @param loginView
     * @param token
     */
	void accountLocked(AuthenticationToken token,
			LockedAccountException lae);

    /**
     * Response to an {@link ExcessiveAttemptsException}, which occurs when a system is configured to raise an
     * exception when there is a specified limit to the number of times a user can try and login. A login failure
     * before that threshold is reached is handled by {@link #unknownAccount(LoginView, UsernamePasswordToken)}.
     * Typically, the implementation of this method will navigate to a KrailView which allows the user to request a
     * reset after filling in appropriate security answers.
     *
     * @param loginView
     * @param token
     */
	void excessiveAttempts(AuthenticationToken token,
			ExcessiveAttemptsException excess);

    /**
     * Response to {@link ConcurrentAccessException}. See the javadoc of the exception
     *
     * @param loginView
     * @param token
     */
	void concurrentAccess(AuthenticationToken token,
			ConcurrentAccessException cae);

    /**
     * Response to {@link DisabledAcoountException}. See the javadoc of the exception. Typically, the implementation of
     * this method will navigate to a KrailView which allows the user to request that their account is re-enabled,
     * although
     * exact behaviour is up to the implementation.
     *
     * @param loginView
     * @param token
     */
	void disabledAccount(AuthenticationToken token,
			DisabledAccountException dae);

	public void genericException(AuthenticationToken token, AuthenticationException ae);

}
