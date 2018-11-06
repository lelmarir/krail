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

import uk.q3c.krail.core.ui.ScopedUIProvider;

import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.vaadin.server.CustomizedSystemMessages;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.SystemMessages;
import com.vaadin.server.SystemMessagesInfo;
import com.vaadin.server.SystemMessagesProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;

@Singleton
public class BaseServlet extends VaadinServlet implements SessionInitListener {

	private static final long serialVersionUID = 4490881052475037408L;

	// Static injection, becouse this class will not be instantiated by guice
	@Inject
	private static ScopedUIProvider uiProvider;
	@Inject
	private static Set<KrailRequestInterceptor> requestInterceptors;

	// will not be instantiated by guice
	public BaseServlet() {
		;
	}

	@Override
	protected void servletInitialized() throws ServletException {
		super.servletInitialized();
		getService().addSessionInitListener(this);
		getService().setSystemMessagesProvider(new SystemMessagesProvider() {

			@Override
			public SystemMessages getSystemMessages(SystemMessagesInfo systemMessagesInfo) {
				// disabilita il messaggio di errore e ricarica immediatamente
				// la pagina
				CustomizedSystemMessages messages = new CustomizedSystemMessages();
				messages.setSessionExpiredNotificationEnabled(false);
				return messages;
			}
		});
	}

	@Override
	public void sessionInit(SessionInitEvent event) throws ServiceException {
		event.getSession().addUIProvider(uiProvider);
	}

	@Override
	protected VaadinServletService createServletService(DeploymentConfiguration deploymentConfiguration)
			throws ServiceException {

		VaadinServletService service = new VaadinServletService(this, deploymentConfiguration) {

			@Override
			protected List<RequestHandler> createRequestHandlers() throws ServiceException {
				for (KrailRequestInterceptor interceptor : requestInterceptors) {
					interceptor.init();
				}
				return super.createRequestHandlers();
			}

			@Override
			public void requestStart(VaadinRequest request, VaadinResponse response) {
				super.requestStart(request, response);
				for (KrailRequestInterceptor handler : requestInterceptors) {
					handler.requestStart(request, response);
				}
			}

			@Override
			public void requestEnd(VaadinRequest request, VaadinResponse response, VaadinSession session) {
				for (KrailRequestInterceptor handler : requestInterceptors) {
					handler.requestEnd(request, response, session);
				}
				super.requestEnd(request, response, session);
			}

			@Override
			public void destroy() {
				for (KrailRequestInterceptor interceptor : requestInterceptors) {
					interceptor.destroy();
				}
				super.destroy();
			}
		};
		service.init();
		return service;
	}
}
