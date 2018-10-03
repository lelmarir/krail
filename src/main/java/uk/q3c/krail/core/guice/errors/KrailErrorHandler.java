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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.q3c.krail.core.guice.errors.ErrorHandler.ErrorEvent;
import uk.q3c.krail.core.navigate.Navigator;

import com.google.inject.Inject;
import com.google.inject.Provider;
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
	private static final Logger LOGGER = LoggerFactory.getLogger(KrailErrorHandler.class);

	@Inject
	private Provider<Set<ErrorHandler>> errorHandlersProvider;
	@Inject
	private Provider<Navigator> navigatorProvider;

	@Inject
	protected KrailErrorHandler() {
		super();
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

			Throwable cause = throwable.getCause();
			if (cause == null && throwable instanceof InvocationTargetException) {
				cause = ((InvocationTargetException) throwable).getTargetException();
			}
			throwable = cause;
		}

		if (handled == false) {
			LOGGER.error("Unable to handle the error: navigating to the error page, \n"
					+ "handlers: {}", errorHandlersProvider.get(), event.getThrowable());
			
			navigatorProvider.get().navigateToErrorView(event.getThrowable());
		}
	}

	private boolean handleError(ErrorEvent e) {
		LinkedList<ErrorHandler> handlers = new LinkedList<>();
		handlers.addAll(errorHandlersProvider.get());

		// li eseguo nell'ordine inverso
		// FIXME: errorHandlers è un set, perche lo converto in lista e lo eseguo in
		// ordine inverso? tanto saranno in un ordine casuale...
		ListIterator<ErrorHandler> it = handlers.listIterator(handlers.size());
		while (it.hasPrevious()) {
			ErrorHandler handler = it.previous();

			boolean handled = handler.handle(e);
			if (handled == true) {
				LOGGER.debug("Error handled by {} : {}", handler, e.getThrowable());
				return true;
			}
		}
		return false;
	}

}
