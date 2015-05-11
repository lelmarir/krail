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
package uk.q3c.krail.core.guice.errors;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;

import uk.q3c.krail.core.guice.errors.ErrorHandler.ErrorEvent;
import uk.q3c.krail.core.navigate.Navigator;

import com.google.inject.Inject;
import com.vaadin.server.DefaultErrorHandler;

/**
 * Extends the {@link DefaultErrorHandler} to intercept known V& exceptions,
 * including Shiro related exceptions - {@link UnauthorizedException} and
 * {@link UnauthenticatedException}. Uses pluggable handlers for all caught
 * exceptions.
 * 
 * @author David Sowerby 4 Jan 2013
 * 
 */
public class KrailErrorHandler extends DefaultErrorHandler {

	private static final long serialVersionUID = -8722893607740326862L;

	private LinkedList<ErrorHandler> errorHandlers;
	private final Navigator navigator;

	@Inject
	protected KrailErrorHandler(Set<ErrorHandler> errorHandlers,
			Navigator navigator) {
		super();
		this.errorHandlers = new LinkedList<>(errorHandlers);
		this.navigator = navigator;
	}

	@Override
	public void error(com.vaadin.server.ErrorEvent event) {
		Throwable throwable = event.getThrowable();

		boolean handled = false;

		List<Throwable> list = new ArrayList<Throwable>();
		while (handled == false && throwable != null) {
			if (list.contains(throwable)) {
				// Loop detected
				break;
			}
			list.add(throwable);

			handled = handleError(new ErrorEvent(throwable));
			if (handled == true) {
				break;
			}

			throwable = throwable.getCause();
		}

		if (handled == false) {
			navigator.navigateToErrorView(event.getThrowable());
		}
	}

	private boolean handleError(ErrorEvent e) {
		ListIterator<ErrorHandler> it = errorHandlers.listIterator(errorHandlers.size());
		while(it.hasPrevious()) {
			ErrorHandler handler = it.previous();
			
			boolean handled = handler.handle(e);
			if (handled == true) {
				return true;
			}
		}
		return false;
	}
}