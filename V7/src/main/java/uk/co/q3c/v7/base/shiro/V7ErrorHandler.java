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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import uk.co.q3c.v7.base.navigate.InvalidURIException;
import uk.co.q3c.v7.base.navigate.InvalidURIExceptionHandler;
import uk.co.q3c.v7.base.navigate.V7Navigator;

import com.google.inject.Inject;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.ErrorEvent;

/**
 * Extends the {@link DefaultErrorHandler} to intercept known V& exceptions,
 * including Shiro related exceptions - {@link UnauthorizedException} and
 * {@link UnauthenticatedException}. Uses pluggable handlers for all caught
 * exceptions.
 * 
 * @author David Sowerby 4 Jan 2013
 * 
 */
public class V7ErrorHandler extends DefaultErrorHandler {

	private static final long serialVersionUID = -8722893607740326862L;

	private final UnauthenticatedExceptionHandler authenticationHandler;
	private final UnauthorizedExceptionHandler authorisationHandler;
	private final InvalidURIExceptionHandler invalidUriHandler;
	private final V7Navigator navigator;

	@Inject
	protected V7ErrorHandler(
			UnauthenticatedExceptionHandler authenticationHandler,
			UnauthorizedExceptionHandler authorisationHandler,
			InvalidURIExceptionHandler invalidUriHandler, V7Navigator navigator) {
		super();
		this.authenticationHandler = authenticationHandler;
		this.authorisationHandler = authorisationHandler;
		this.invalidUriHandler = invalidUriHandler;
		this.navigator = navigator;
	}

	@Override
	public void error(ErrorEvent event) {
		Throwable throwable = event.getThrowable();

		List<Throwable> list = new ArrayList<Throwable>();
		while (throwable != null && list.contains(throwable) == false) {
			list.add(throwable);

			boolean handled = handle(throwable);
			if (handled == true) {
				break;
			}

			throwable = ExceptionUtils.getCause(throwable);
		}

		//default
		navigator.navigateToErrorView(event.getThrowable());

	}

	private boolean handle(Throwable throwable) {
		// handle an attempt to navigate to an invalid page
		if (throwable instanceof InvalidURIException) {
			invalidUriHandler.onInvalidUri((InvalidURIException) throwable);
			return true;
		}

		// handle an unauthenticated access attempt
		if (throwable instanceof UnauthenticatedException) {
			authenticationHandler.onUnauthenticatedException((UnauthenticatedException)throwable);
			return true;
		}

		// handle an unauthorised access attempt
		if (throwable instanceof UnauthorizedException) {
			authorisationHandler.onUnauthorizedException((UnauthorizedException)throwable);
			return true;
		}

		return false;
	}

}
