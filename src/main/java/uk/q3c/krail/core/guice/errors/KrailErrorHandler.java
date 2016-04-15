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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.inject.Provider;

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class KrailErrorHandler extends DefaultErrorHandler implements Serializable {

	private static final long serialVersionUID = -8722893607740326862L;
	private static final Logger LOGGER = LoggerFactory.getLogger(KrailErrorHandler.class);

	//used after the serialization
	@Inject
	private static Provider<KrailErrorHandler> instanceProvider;
	
	transient private LinkedList<ErrorHandler> errorHandlers;
	transient private final Navigator navigator;

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

			Throwable cause = throwable.getCause();
			if(cause == null && throwable instanceof InvocationTargetException){
				cause = ((InvocationTargetException)throwable).getTargetException();
			}
			throwable = cause;
		}

		if (handled == false) {
			LOGGER.error("Unable to handle the error: navigating to the error page", event.getThrowable());
			navigator.navigateToErrorView(event.getThrowable());
		}
	}

	private boolean handleError(ErrorEvent e) {
		ListIterator<ErrorHandler> it = errorHandlers.listIterator(errorHandlers.size());
		while(it.hasPrevious()) {
			ErrorHandler handler = it.previous();
			
			boolean handled = handler.handle(e);
			if (handled == true) {
				LOGGER.debug("Error handled by {} : {}", handler, e.getThrowable());
				return true;
			}
		}
		return false;
	}
	
	/**
	 * When deserializing this object, use Guice to create an instance rather than
	 * using the default behaviour of invoking Class.newInstance() which in turn
	 * invokes the no-args constructor. This ensures that any Guice configuration
	 * occurs, including both constructor and member injection.
	 */
	private Object readResolve() throws ObjectStreamException {
	  return instanceProvider.get();
	}	
	
}
