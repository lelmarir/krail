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
package uk.q3c.krail.core.guice;

import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;

public abstract class BaseServletModule extends ServletModule {

	/**
	 * <b>don't configure servlets with</b>
	 * <code>serve("/*").with(Servlet.class);</code> use @WebServlet annotation
	 * instead and inject dependencies with
	 * {@link #requestStaticInjection(Class...)})
	 */
	@Override
	protected void configureServlets() {
		//in case there are no krailRequestHandler, to create an empty set
		Multibinder<KrailRequestHandler> krailRequestHandlerBinder = Multibinder
				.newSetBinder(binder(), KrailRequestHandler.class);
		bindRequestHandlers(krailRequestHandlerBinder);

		// BaseServlet will not be instantiated by guice
		requestStaticInjection(BaseServlet.class);
	}

	protected void bindRequestHandlers(
			Multibinder<KrailRequestHandler> multibinder) {
		;
	}
}
