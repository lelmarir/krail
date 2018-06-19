/*
 * Copyright (C) 2014 David Sowerby
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
package uk.q3c.krail.core.ui;

import java.util.Locale;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.vaadin.annotations.Push;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;

import uk.q3c.krail.core.guice.uiscope.UIKey;
import uk.q3c.krail.core.guice.uiscope.UIScope;
import uk.q3c.krail.core.guice.uiscope.UIScoped;
import uk.q3c.krail.core.navigate.DefaultNavigator;
import uk.q3c.krail.core.navigate.Navigator;
import uk.q3c.krail.core.navigate.VaadinNavigatorWrapper;
import uk.q3c.krail.core.view.KrailView;
import uk.q3c.krail.core.view.KrailViewHolder;
import uk.q3c.krail.i18n.CurrentLocale;
import uk.q3c.krail.i18n.LocaleChangeListener;

/**
 * The base class for all Krail UIs, it provides an essential part of the
 * {@link UIScoped} mechanism. It also provides support for Vaadin Server Push
 * (but only if you annotate your sub-class with {@link Push}), by capturing
 * broadcast messages in {@link #processBroadcastMessage(String, String)} and
 * passing them to the {@link PushMessageRouter}. For a full description of the
 * Krail server push implementation see:
 * https://sites.google.com/site/q3cjava/server-push
 *
 * @author David Sowerby
 * @date modified 31 Mar 2014
 */

public abstract class ScopedUI extends UI
		implements KrailViewHolder, LocaleChangeListener {
	private static Logger log = LoggerFactory.getLogger(ScopedUI.class);

	@Inject
	private Provider<ErrorHandler> errorHandlerProvider;
	@Inject
	private Provider<Navigator> navigatorProvider;
	@Inject
	private Provider<CurrentLocale> currentLocaleProvider;

	private final Panel headerDisplayPanel;
	private Panel viewDisplayPanel;
	private boolean layoutDone = false;
	private UIKey instanceKey;
	private Component screenLayout;
	private UIScope uiScope;
	private KrailView view;

	protected ScopedUI() {
		super();
		headerDisplayPanel = new Panel();
		viewDisplayPanel = new Panel();
	}

	public UIKey getInstanceKey() {
		return instanceKey;
	}

	protected void setInstanceKey(UIKey instanceKey) {
		this.instanceKey = instanceKey;
	}

	protected void setScope(UIScope uiScope) {
		this.uiScope = uiScope;
	}

	@Override
	public void detach() {
		if (uiScope != null) {
			uiScope.releaseScope(instanceKey);
		}
		super.detach();
	}

	@Override
	public void setNavigator(com.vaadin.navigator.Navigator navigator) {
		throw new MethodReconfigured(
				"UI.setNavigator() not available, use injection instead");
	}

	@Override
	public void changeView(KrailView toView) {
		if (toView == null) {
			throw new IllegalArgumentException("toView should not be null");
		}
		if (log.isDebugEnabled()) {
			String to = (toView == null) ? "null"
					: toView.getClass().getSimpleName();
			log.debug("changing view to {}", to);
		}

		Component header = toView.getHeaderComponent();
		Component content = toView.getRootComponent();
		content.setSizeFull();
		headerDisplayPanel.setContent(header);
		viewDisplayPanel.setContent(content);
		this.view = toView;
	}

	/**
	 * Make sure you call this from sub-class overrides. The Vaadin Page is not
	 * available during the construction of this class, but is available when
	 * this method is invoked. As a result, this method sets the navigator a
	 * listener for URI changes and obtains the browser locale setting for
	 * initialising {@link CurrentLocale}. Both of these are provided by the
	 * Vaadin Page.
	 *
	 * @see com.vaadin.ui.UI#init(com.vaadin.server.VaadinRequest)
	 */
	@Override
	protected void init(VaadinRequest request) {

		VaadinSession session = getSession();

		ErrorHandler errorHandler = errorHandlerProvider.get();
		setErrorHandler(errorHandler);
		session.setErrorHandler(errorHandler);

		Navigator navigator = navigatorProvider.get();
		// page isn't available during injected construction, so we have to do
		// this here
		if (navigator instanceof com.vaadin.navigator.Navigator) {
			super.setNavigator((com.vaadin.navigator.Navigator) navigator);
		} else if (navigator instanceof DefaultNavigator) {
			log.debug(
					"The injected nagigator has been wrapped in VaadinNavigatorWrapper");
			super.setNavigator(
					new VaadinNavigatorWrapper((DefaultNavigator) navigator));
		} else {
			super.setNavigator(null);
			log.warn(
					"The injected navigator is not a sumblass of com.vaadin.navigator.Navigator");
		}
		Page page = getPage();

		page.setTitle(pageTitle());

		CurrentLocale currentLocale = this.currentLocaleProvider.get();
		// now that browser is active, and user sitemap loaded, set up
		// currentLocale
		currentLocale.readFromEnvironment();
		currentLocale.addListener(this);

		doLayout();
		
		//FIXME: non dovrei chiamare a mano init(), dovrebbe essere eseguito come @PostConstruct, a eventuali errori causano un loop
		navigator.init();
	}

	/**
	 * Provides a locale sensitive title for your application (which appears in
	 * the browser tab). The title is defined by the {@link #applicationTitle},
	 * which should be specified in your sub-class of {@link UIModule}
	 *
	 * @return
	 */
	protected String pageTitle() {
		return "";
	}

	/**
	 * Uses the {@link #screenLayout} defined by sub-class implementations of
	 * {@link #screenLayout()}, expands it to full size, and sets the View
	 * display panel to take up all spare space.
	 */
	protected void doLayout() {
		if (screenLayout == null) {
			screenLayout = screenLayout();
		}
		layoutDone = true;
		if (viewDisplayPanel.getParent() == null) {
			String msg = "Your implementation of ScopedUI.screenLayout() must include getViewDisplayPanel().  AS a "
					+ "minimum this could be 'return new VerticalLayout(getViewDisplayPanel())'";
			log.error(msg);
			throw new NotImplementedException(msg);
		}
		viewDisplayPanel.setSizeFull();
		setContent(screenLayout);
	}

	/**
	 * Override this to provide your screen layout. In order for Views to work
	 * one child component of this layout must be provided by
	 * {@link #getViewDisplayPanel()}. The simplest example would be
	 * {@code return new VerticalLayout(getViewDisplayPanel()}, which would set
	 * the View to take up all the available screen space. {@link BasicUI} is an
	 * example of a UI which contains a header and footer bar.
	 *
	 * @return
	 */
	protected abstract Component screenLayout();

	public Panel getHeaderDisplayPanel() {
		return headerDisplayPanel;
	}

	public Panel getViewDisplayPanel() {
		return viewDisplayPanel;
	}

	public void setViewDisplayPanel(Panel viewDisplayPanel) {
		if (layoutDone == true) {
			throw new UnsupportedOperationException(
					"You have the chance to replace teh default DisplayPanel only in or before #screenLayout() has been called.");
		}
		this.viewDisplayPanel = viewDisplayPanel;
	}

	/**
	 * Responds to a locale change from {@link CurrentLocale} and updates the
	 * translation for this UI and the current KrailView
	 */
	@Override
	public void localeChanged(Locale toLocale) {
		// during initial set up view has not been created but locale change
		// gets called for other components
		if (getView() != null) {

		}
	}

	public KrailView getView() {
		return view;
	}

}