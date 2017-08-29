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

import java.util.LinkedList;
import java.util.List;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.q3c.krail.core.navigate.sitemap.NavigationState;
import uk.q3c.krail.core.navigate.sitemap.NavigationState.Parameters;
import uk.q3c.krail.core.navigate.sitemap.Sitemap;
import uk.q3c.krail.core.navigate.sitemap.SitemapNode;
import uk.q3c.krail.core.navigate.sitemap.StandardPageKey;
import uk.q3c.krail.core.shiro.SubjectProvider;
import uk.q3c.krail.core.ui.ScopedUI;
import uk.q3c.krail.core.ui.ScopedUIProvider;
import uk.q3c.krail.core.view.DefaultViewFactory;
import uk.q3c.krail.core.view.ErrorView;
import uk.q3c.krail.core.view.AfterViewChangeListener;
import uk.q3c.krail.core.view.BeforeSecurityCheckListener;
import uk.q3c.krail.core.view.BeforeViewChangeListener;
import uk.q3c.krail.core.view.KrailView;
import uk.q3c.krail.core.view.KrailViewChangeEvent;
import uk.q3c.krail.core.view.KrailViewChangeEvent.CancellableKrailViewChangeEvent;
import uk.q3c.krail.core.view.KrailViewChangeEventImpl;
import uk.q3c.krail.core.view.KrailViewChangeEventImpl.CancellableWrapper;
import uk.q3c.krail.core.view.ViewBuildException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.vaadin.server.Page;
import com.vaadin.server.Page.UriFragmentChangedEvent;

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

	private static final long serialVersionUID = -1199874611306964538L;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefaultNavigator.class);

	private final NavigationCallbackHandler callbackHandler = new DefaultNavigationCallbackHandler();

	private final List<BeforeSecurityCheckListener> beforeSecurityCheckListener = new LinkedList<BeforeSecurityCheckListener>();
	private final List<BeforeViewChangeListener> beforeViewChangeListeners = new LinkedList<BeforeViewChangeListener>();
	private final List<AfterViewChangeListener> afterViewChangeListeners = new LinkedList<AfterViewChangeListener>();
	private final Provider<Subject> subjectProvider;
	private final Sitemap sitemap;
	private final ScopedUI ui;
	private final DefaultViewFactory viewFactory;
	private NavigationState currentNavigationState;
	private NavigationState previousNavigationState;

	@Inject
	public DefaultNavigator(Provider<Subject> subjectProvider, Sitemap sitemap,
			ScopedUIProvider uiProvider, DefaultViewFactory viewFactory) {
		super();
		this.ui = uiProvider.get();
		assert this.ui != null;
		this.subjectProvider = subjectProvider;
		this.sitemap = sitemap;
		this.viewFactory = viewFactory;
	}

	@Override
	public void uriFragmentChanged(UriFragmentChangedEvent event) {
		String fragment = event.getUriFragment();
		navigateTo(fragment != null ? fragment : "");
	}

	@Override
	public void navigateTo(String fragment) throws InvalidURIException {
		LOGGER.debug("Navigating to fragment: {}", fragment);

		// set up the navigation state
		NavigationState navigationState = sitemap
				.buildNavigationStateFor(fragment);
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
	public void navigateTo(NavigationTarget target) {
		navigateTo(target.getView(), target.getParaeters());
	}
	
	public void navigateTo(NavigationState navigationState)
			throws AuthorizationException {
		checkNotNull(navigationState);
		checkNotNull(callbackHandler);

		// stop unnecessary changes, but also to prevent navigation aware
		// components from causing a loop by responding to a change of URI (they
		// should suppress events when they do, but may not)
		if (navigationState.equals(currentNavigationState)) {
			LOGGER.debug("fragment unchanged, no navigation required");
			return;
		}

		SitemapNode node = navigationState.getSitemapNode();
		assert node != null;

		KrailViewChangeEvent event = new KrailViewChangeEventImpl(this,
				currentNavigationState, navigationState);

		CancellableWrapper cancellable = new CancellableWrapper(event);

		fireBeforeSecurityCheck(cancellable);
		if (cancellable.isCancelled()) {
			LOGGER.debug("navigation canceled by a KrailViewChangeListener beforeSecurityCheck");
			return;
		}

		Subject subject = subjectProvider.get();
		// throw an exception if not authorized
		assert node.getAccesControlRule() != null : node;
		try {
			node.getAccesControlRule().checkAuthorization(subject);
		} catch (AuthorizationException e) {
			throw new NavigationAuthorizationException(navigationState, e);
		}

		// if change is blocked revert to previous state
		fireBeforeViewChange(cancellable);
		if (cancellable.isCancelled()) {
			LOGGER.debug("navigation canceled by a KrailViewChangeListener beforeViewChange");
			return;
		}

		// notify Outbound navigation to current view
		if (getCurrentView() != null) {
			fireViewBeforeOutboundNavigationEvent(getCurrentView(),
					cancellable, callbackHandler);
			if (cancellable.isCancelled()) {
				LOGGER.debug(
						"navigation canceled by the view {} in NavigationAwareView#onOutboundNavigation beforeOutboundNavigation",
						getCurrentView().getClass().getSimpleName());
				return;
			}
		}

		LOGGER.debug("obtaining view instance for '{}'", node);
		KrailView view = viewFactory.get(node.getViewClass());

		// notify before Inbound navigation to target view

		fireViewBeforeInboundNavigationEvent(view, cancellable, callbackHandler);
		if (cancellable.isCancelled()) {
			LOGGER.debug(
					"navigation canceled by the view {} in NavigationAwareView#onInboundNavigation",
					view.getClass().getSimpleName());
			return;
		}

		checkViewRootComponentNotNull(view);

		setCurrentNavigationState(navigationState);

		// now change the view
		changeView(view);

		fireViewAfterInboundNavigationEvent(view, event);

		// and tell listeners its changed
		fireAfterViewChange(event);

		// make sure the page uri is updated if necessary, but do not fire any
		// change events as we have already responded to the change
		updateUriFragment(navigationState, false);

	}

	private void checkViewRootComponentNotNull(KrailView view) {
		try {
			if (view.getRootComponent() == null) {
				throw new RuntimeException(
						"getRootComponent() should have trowned a ViewBuildException instead of returning null");
			}
		} catch (ViewBuildException e) {
			throw new RuntimeException(
					"The rootComponent shuld not be null after BeforeInboundNavigation()",
					e);
		}
	}

	/**
	 * Fires an event before an imminent view change.
	 * <p/>
	 * Listeners are called in registration order. If any listener returns
	 * <code>false</code>, the rest of the listeners are not called and the view
	 * change is blocked.
	 * <p/>
	 * The view change listeners may also e.g. open a warning or question dialog
	 * and save the parameters to re-initiate the navigation operation upon user
	 * action.
	 * 
	 * @param event
	 *            view change event (not null, view change not yet performed)
	 * 
	 * @return true if the view change should be allowed, false to silently
	 *         block the navigation operation
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
	 * @param event
	 *            view change event (not null)
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
	 * @param event
	 *            view change event (not null)
	 * */
	private void fireBeforeSecurityCheck(KrailViewChangeEvent event) {
		for (BeforeSecurityCheckListener l : beforeSecurityCheckListener) {
			l.beforeSecurityCheck(event);
		}
	}

	private void fireViewBeforeOutboundNavigationEvent(KrailView view,
			CancellableWrapper cancellable,
			NavigationCallbackHandler callbackHandler) {
		callbackHandler.beforeOutboundNavigationEvent(view, cancellable);
	}

	private void fireViewBeforeInboundNavigationEvent(KrailView view,
			CancellableKrailViewChangeEvent cancellable,
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
		updateUriFragment(navigationState, false);
	}

	private void updateUriFragment(NavigationState navigationState,
			boolean fireEvents) {
		assert navigationState != null;
		Page page = ui.getPage();
		if (!navigationState.getFragment().equals(page.getUriFragment())) {
			page.setUriFragment(navigationState.getFragment(), fireEvents);
		}
	}

	protected void changeView(KrailView view) {
		ui.changeView(view);
	}

	private void fireViewAfterInboundNavigationEvent(KrailView view,
			KrailViewChangeEvent event) {
		callbackHandler.afterInbounNavigationEvent(view, event);
	}

	@Override
	public NavigationState getCurrentNavigationState() {
		return currentNavigationState;
	}

	private KrailView getCurrentView() {
		return ui.getView();
	}

	@Override
	public NavigationState getPreviousNavigationState() {
		return previousNavigationState;
	}

	@Override
	public void navigateToErrorView(final Throwable error) {
		LOGGER.debug(
				"A {} Error has been thrown, reporting via the Error View",
				error.getClass().getName(), error);

		NavigationTarget navigationTarget = new NavigationTarget(ErrorView.class);
		navigationTarget.putParameter("error", error);
		navigateTo(navigationTarget);
	}

	@Override
	public void addBeforeViewChangeListener(
			BeforeViewChangeListener listener) {
		beforeViewChangeListeners.add(listener);
	}

	@Override
	public void removeBeforeViewChangeListener(
			BeforeViewChangeListener listener) {
		beforeViewChangeListeners.remove(listener);
	}

	@Override
	public void addAfterViewChangeListener(
			AfterViewChangeListener listener) {
		afterViewChangeListeners.add(listener);
	}

	@Override
	public void removeAfterViewChangeListener(
			AfterViewChangeListener listener) {
		afterViewChangeListeners.remove(listener);
	}

	@Override
	public void addBeforeSecurityCheckListener(
			BeforeSecurityCheckListener listener) {
		beforeSecurityCheckListener.add(listener);
	}

	@Override
	public void removeBeforeSecurityCheckListener(
			BeforeSecurityCheckListener listener) {
		beforeSecurityCheckListener.remove(listener);
	}
}
