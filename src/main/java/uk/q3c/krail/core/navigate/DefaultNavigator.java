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
package uk.q3c.krail.core.navigate;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.vaadin.navigator.NavigationStateManager;
import com.vaadin.navigator.Navigator.PushStateManager;
import com.vaadin.navigator.Navigator.UriFragmentManager;
import com.vaadin.navigator.PushStateNavigation;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;

import uk.q3c.krail.core.navigate.parameters.Parameters;
import uk.q3c.krail.core.navigate.sitemap.NavigationState;
import uk.q3c.krail.core.navigate.sitemap.Sitemap;
import uk.q3c.krail.core.navigate.sitemap.SitemapNode;
import uk.q3c.krail.core.navigate.sitemap.StandardPageKey;
import uk.q3c.krail.core.navigate.sitemap.annotations.View;
import uk.q3c.krail.core.ui.ScopedUI;
import uk.q3c.krail.core.ui.ScopedUIProvider;
import uk.q3c.krail.core.view.AfterViewChangeListener;
import uk.q3c.krail.core.view.BeforeSecurityCheckListener;
import uk.q3c.krail.core.view.BeforeViewChangeListener;
import uk.q3c.krail.core.view.ErrorView;
import uk.q3c.krail.core.view.KrailView;
import uk.q3c.krail.core.view.KrailViewChangeEvent;
import uk.q3c.krail.core.view.KrailViewChangeEvent.CancellableKrailViewChangeEvent;
import uk.q3c.krail.core.view.KrailViewChangeEventImpl;
import uk.q3c.krail.core.view.KrailViewChangeEventImpl.CancellableWrapper;
import uk.q3c.krail.core.view.LayoutFactory;
import uk.q3c.krail.core.view.ViewBuildException;

/**
 * The navigator is at the heart of navigation process, and provides navigation
 * form a number of data types (for example, String, {@link NavigationState} and
 * {@link UserSitemapNode}. Because the USerSitemap only holds pages authorised
 * for the current Subject, there is not need to check for authorisation before
 * navigating (there is still some old code in here which does, but that will be
 * removed)
 * <p/>
 * There is no need to register as a listener with {@link UserStatus}, the
 * navigator is always called after all other listeners - this is so that
 * navigation components are set up before the navigator moves to a page (which
 * might not be displayed in a navigation component if it is not up to date)
 * 
 * @author David Sowerby
 * @date 18 Apr 2014
 */
public class DefaultNavigator implements Navigator {

	public static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotationClass) {
		while (clazz != null) {
			A annotation = clazz.getAnnotation(annotationClass);
			if (annotation != null) {
				return annotation;
			}
			clazz = clazz.getSuperclass();
		}
		return null;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNavigator.class);

	@Inject
	private Provider<DefaultNavigationCallbackHandler> defaultCallbackHandlerProvider;

	@Inject
	private LayoutFactory layoutFactory;

	private NavigationCallbackHandler callbackHandler;

	protected NavigationStateManager stateManager;

	private final Provider<Subject> subjectProvider;
	private final Sitemap sitemap;
	private final ScopedUI ui;
	private NavigationState currentNavigationState;
	private NavigationState previousNavigationState;
	private final List<BeforeSecurityCheckListener> beforeSecurityCheckListener = new LinkedList<BeforeSecurityCheckListener>();
	private final List<BeforeViewChangeListener> beforeViewChangeListeners = new LinkedList<BeforeViewChangeListener>();
	private final List<AfterViewChangeListener> afterViewChangeListeners = new LinkedList<AfterViewChangeListener>();

	@Inject
	public DefaultNavigator(Provider<Subject> subjectProvider, Sitemap sitemap, ScopedUIProvider uiProvider) {
		super();
		this.ui = uiProvider.get();
		assert this.ui != null;
		this.subjectProvider = subjectProvider;
		this.sitemap = sitemap;

		setStateManager(createNavigationStateManager(ui));

	}

	@Override
	public void init() {
		// UI will kickstart the navigation
		;
	}

	protected NavigationCallbackHandler getCallbackHandler() {
		if (callbackHandler == null) {
			callbackHandler = defaultCallbackHandlerProvider.get();
		}
		return callbackHandler;
	}

	protected NavigationStateManager createNavigationStateManager(UI ui) {
		if (ui.getClass().getAnnotation(PushStateNavigation.class) != null) {
			return new PushStateManager(ui);
		}
		// Fall back to old default
		return new UriFragmentManager(ui.getPage());
	}

	public void setStateManager(NavigationStateManager stateManager) {
		checkNotNull(stateManager);
		if (this.stateManager != null && stateManager != this.stateManager) {
			this.stateManager.setNavigator(null);
		}
		this.stateManager = stateManager;
		this.stateManager.setNavigator(new VaadinNavigatorWrapper(this));
	}

	protected Sitemap getSitemap() {
		return sitemap;
	}

	@Override
	public void navigateBack() throws UnknownPreviousNavigationState {
		if (getPreviousNavigationState() != null) {
			navigateTo(getPreviousNavigationState());
		} else {
			throw new UnknownPreviousNavigationState();
		}
	}

	@Override
	public void navigateTo(String fragment) throws InvalidURIException {
		LOGGER.debug("Navigating to fragment: {}", fragment);

		// set up the navigation state
		NavigationState navigationState = sitemap.buildNavigationStateFor(fragment);
		navigateTo(navigationState);
	}

	@Override
	public void navigateTo(StandardPageKey pageKey) {
		navigateTo(sitemap.buildNavigationStateFor(pageKey));
	}

	@Override
	public <T extends KrailView> void navigateTo(Class<T> viewClass) {
		NavigationState ns = sitemap.buildNavigationState(viewClass);
		navigateTo(ns);
	}

	@Override
	public <T extends KrailView> void navigateTo(Class<T> viewClass, Parameters parameters) {
		NavigationState ns = sitemap.buildNavigationState(viewClass, parameters);
		navigateTo(ns);
	}

	@Override
	public void navigateTo(NavigationTarget target) throws AuthorizationException {
		navigateTo(target.getViewClass(), target.getParaeters());
	}

	@Override
	public void refresh() {
		navigateTo(getCurrentNavigationState(), true);
	}

	@Override
	public void navigateTo(NavigationState navigationState) throws AuthorizationException {
		navigateTo(navigationState, false);
	}

	public void navigateTo(NavigationState navigationState, boolean refresh) throws AuthorizationException {
		checkNotNull(navigationState);
		checkNotNull(getCallbackHandler());

		// stop unnecessary changes, but also to prevent navigation aware
		// components from causing a loop by responding to a change of URI (they
		// should suppress events when they do, but may not)
		if (refresh == false && !isDifferentState(navigationState)) {
			LOGGER.debug("fragment unchanged, no navigation required");
			return;
		}

		KrailViewChangeEvent event = new KrailViewChangeEventImpl(this, currentNavigationState, navigationState);

		CancellableWrapper cancellable = new CancellableWrapper(event);

		fireBeforeSecurityCheck(cancellable);
		if (cancellable.isCancelled()) {
			LOGGER.debug("navigation canceled by a KrailViewChangeListener beforeSecurityCheck");
			return;
		}

		Subject subject = subjectProvider.get();
		// will throw an exception if not authorized
		checkAuthorization(navigationState, subject);

		// if change is blocked revert to previous state
		fireBeforeViewChange(cancellable);
		if (cancellable.isCancelled()) {
			LOGGER.debug("navigation canceled by a KrailViewChangeListener beforeViewChange");
			return;
		}

		KrailView sourceView = getCurrentView();

		// notify befre Outbound navigation to current view
		if (sourceView != null) {
			fireViewBeforeOutboundNavigationEvent(sourceView, cancellable, getCallbackHandler());
			if (cancellable.isCancelled()) {
				LOGGER.debug("navigation canceled by the view {} in @BeforeOutboundNavigation",
						sourceView.getClass().getSimpleName());
				return;
			}

			// notify before Inbound navigation to target view
			fireViewBeforeInboundNavigationEvent(sourceView, cancellable, getCallbackHandler());
			if (cancellable.isCancelled()) {
				LOGGER.debug("navigation canceled by the view {} in @BeforeInboundNavigation",
						sourceView.getClass().getSimpleName());
				return;
			}
		}

		LOGGER.debug("obtaining view instance for '{}'", navigationState);
		KrailView targetView = navigationState.getView();

		checkViewRootComponentNotNull(targetView);

		setCurrentNavigationState(navigationState);
		// TODO: dovrei aggiungere un listener su detach() per notificare
		// @AfterOutboundNavigation alla vista corrente (ad esempio alla chiusura della
		// sessione)

		// now change the view
		changeView(targetView);

		if (sourceView != null) {
			fireViewAfterOutboundNavigationEvent(sourceView, event);
			sourceView = null;
		}

		fireViewAfterInboundNavigationEvent(targetView, event);

		// and tell listeners its changed
		fireAfterViewChange(event);

		// make sure the page uri is updated if necessary, but do not fire any
		// change events as we have already responded to the change
		updateUriFragment(navigationState);

	}

	@Override
	public void checkAuthorization(Class<? extends KrailView> viewClass) throws NavigationAuthorizationException {
		checkAuthorization(viewClass, subjectProvider.get());
	}

	public void checkAuthorization(Class<? extends KrailView> viewClass, Subject subject)
			throws NavigationAuthorizationException {
		sitemap.checkAuthorization(viewClass, subject);
	}

	private void checkAuthorization(NavigationState navigationState, Subject subject)
			throws NavigationAuthorizationException {
		SitemapNode node = navigationState.getSitemapNode();
		assert node.getAccesControlRule() != null : node;
		try {
			node.getAccesControlRule().checkAuthorization(subject);
		} catch (AuthorizationException e) {
			throw new NavigationAuthorizationException(navigationState, e);
		}
	}

	private boolean isDifferentState(NavigationState navigationState) {
		return !navigationState.equals(currentNavigationState);
	}

	private void checkViewRootComponentNotNull(KrailView view) {
		try {
			Component rootComponent = view.getRootComponent();
			if (rootComponent == null) {
				throw new RuntimeException(
						"getRootComponent() should have trowned a ViewBuildException instead of returning null");
			} else {
				if (LOGGER.isDebugEnabled()) {
					if (rootComponent instanceof Layout && ((Layout) rootComponent).getComponentCount() > 0) {
						LOGGER.warn(
								"It is advisable to creare the view layout components only after attach() or afterNavigationInbound()");
					}
				}
			}
		} catch (ViewBuildException e) {
			throw new RuntimeException("The rootComponent shuld not be null after BeforeInboundNavigation()", e);
		}
	}

	/**
	 * Fires an event before an imminent view change.
	 * <p/>
	 * Listeners are called in registration order. If any listener returns
	 * <code>false</code>, the rest of the listeners are not called and the view
	 * change is blocked.
	 * <p/>
	 * The view change listeners may also e.g. open a warning or question dialog and
	 * save the parameters to re-initiate the navigation operation upon user action.
	 * 
	 * @param event view change event (not null, view change not yet performed)
	 * 
	 * @return true if the view change should be allowed, false to silently block
	 *         the navigation operation
	 */
	protected void fireBeforeViewChange(CancellableKrailViewChangeEvent event) {
		for (BeforeViewChangeListener l : beforeViewChangeListeners) {
			l.beforeViewChange(event);
		}
	}

	/**
	 * Fires an event after the current view has changed.
	 * <p/>
	 * Listeners are called in registration order.
	 * 
	 * @param event view change event (not null)
	 */
	protected void fireAfterViewChange(KrailViewChangeEvent event) {
		for (AfterViewChangeListener l : afterViewChangeListeners) {
			l.afterViewChange(event);
		}
	}

	/**
	 * Fires an event before security check
	 * <p/>
	 * Listeners are called in registration order.
	 * 
	 * @param event view change event (not null)
	 */
	private void fireBeforeSecurityCheck(KrailViewChangeEvent event) {
		for (BeforeSecurityCheckListener l : beforeSecurityCheckListener) {
			l.beforeSecurityCheck(event);
		}
	}

	private void fireViewBeforeOutboundNavigationEvent(KrailView view, CancellableWrapper cancellable,
			NavigationCallbackHandler callbackHandler) {
		callbackHandler.beforeOutboundNavigationEvent(view, cancellable);
	}

	private void fireViewBeforeInboundNavigationEvent(KrailView view, CancellableKrailViewChangeEvent cancellable,
			NavigationCallbackHandler callbackHandler) {
		callbackHandler.beforeInboundNavigationEvent(view, cancellable);
	}

	private void setCurrentNavigationState(NavigationState navigationState) {
		previousNavigationState = currentNavigationState;
		currentNavigationState = navigationState;
	}

	@Override
	public void updateUriFragment() {
		NavigationState navigationState = getCurrentNavigationState();
		assert navigationState != null;
		updateUriFragment(navigationState);
	}

	private void updateUriFragment(NavigationState targetNavigationState) {
		assert targetNavigationState != null;
		Page page = getUI().getPage();
		if (!targetNavigationState.getFragment().equals(page.getUriFragment())) {
			stateManager.setState(targetNavigationState.getFragment());
		}
	}

	protected void changeView(KrailView view) {
		// set default view title from annotation
		if (view.getViewTitle() == null || view.getViewTitle().isEmpty()) {
			//solo se non è gia presente un titolo
			View viewAnnotation = DefaultNavigator.getAnnotation(view.getClass(), View.class);
			if (viewAnnotation != null) {
				if (viewAnnotation.title().length > 0) {
					// TODO: localizazione
					view.getViewTitleComponet().setTitel(viewAnnotation.title()[0]);
				}
			}
		}
		// display the view in the ui
		ui.changeView(view, layoutFactory.get(view));
	}

	private void fireViewAfterOutboundNavigationEvent(KrailView view, KrailViewChangeEvent event) {
		getCallbackHandler().afterOutboundNavigationEvent(view, event);
	}

	private void fireViewAfterInboundNavigationEvent(KrailView view, KrailViewChangeEvent event) {
		getCallbackHandler().afterInbounNavigationEvent(view, event);
	}

	@Override
	public NavigationState getCurrentNavigationState() {
		return currentNavigationState;
	}

	public ScopedUI getUI() {
		return ui;
	}

	public KrailView getCurrentView() {
		return getUI().getView();
	}

	@Override
	public NavigationState getPreviousNavigationState() {
		return previousNavigationState;
	}

	@Override
	public void navigateToErrorView(final Throwable error) {
		navigateToErrorView(error, null);
	}

	@Override
	public void navigateToErrorView(final Throwable error, String localizedMessage) {
		LOGGER.debug("A {} Error has been thrown, reporting via the Error View: {}", error.getClass().getName(),
				localizedMessage, error);

		navigateTo(ErrorView.buildNavigationTarget(error, localizedMessage));
	}

	@Override
	public void addBeforeViewChangeListener(BeforeViewChangeListener listener) {
		beforeViewChangeListeners.add(listener);
	}

	@Override
	public void removeBeforeViewChangeListener(BeforeViewChangeListener listener) {
		beforeViewChangeListeners.remove(listener);
	}

	@Override
	public void addAfterViewChangeListener(AfterViewChangeListener listener) {
		afterViewChangeListeners.add(listener);
	}

	@Override
	public void removeAfterViewChangeListener(AfterViewChangeListener listener) {
		afterViewChangeListeners.remove(listener);
	}

	@Override
	public void addBeforeSecurityCheckListener(BeforeSecurityCheckListener listener) {
		beforeSecurityCheckListener.add(listener);
	}

	@Override
	public void removeBeforeSecurityCheckListener(BeforeSecurityCheckListener listener) {
		beforeSecurityCheckListener.remove(listener);
	}

}
