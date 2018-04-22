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

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.q3c.krail.core.navigate.parameters.Parameters;
import uk.q3c.krail.core.navigate.sitemap.NavigationState;
import uk.q3c.krail.core.navigate.sitemap.Sitemap;
import uk.q3c.krail.core.navigate.sitemap.SitemapNode;
import uk.q3c.krail.core.navigate.sitemap.StandardPageKey;
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
import com.vaadin.navigator.PushStateNavigation;
import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.server.Page.PopStateEvent;
import com.vaadin.shared.Registration;
import com.vaadin.ui.UI;

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

	/**
	 * A {@link NavigationStateManager} using hashbang fragments in the Page
	 * location URI to track views and enable listening to view changes.
	 * <p>
	 * A hashbang URI is one where the optional fragment or "hash" part - the
	 * part following a # sign - is used to encode navigation state in a web
	 * application. The advantage of this is that the fragment can be
	 * dynamically manipulated by javascript without causing page reloads.
	 * <p>
	 * This class is mostly for internal use by Navigator, and is only public
	 * and static to enable testing.
	 * <p>
	 * <strong>Note:</strong> Since 8.2 you can use {@link PushStateManager},
	 * which is based on HTML5 History API. To use it, add
	 * {@link PushStateNavigation} annotation to the UI.
	 */
	public static class UriFragmentManager implements NavigationStateManager {
		private final Page page;
		private Navigator navigator;
		private Registration uriFragmentRegistration;

		/**
		 * Creates a new URIFragmentManager and attach it to listen to URI
		 * fragment changes of a {@link Page}.
		 *
		 * @param page
		 *            page whose URI fragment to get and modify
		 */
		public UriFragmentManager(Page page) {
			this.page = page;
		}

		@SuppressWarnings("deprecation")
		@Override
		public void setNavigator(Navigator navigator) {
			if (this.navigator == null && navigator != null) {
				uriFragmentRegistration = page.addUriFragmentChangedListener(
						event -> navigator.navigateTo(getState()));
			} else if (this.navigator != null && navigator == null) {
				uriFragmentRegistration.remove();
			}
			this.navigator = navigator;
		}

		@Override
		public String getState() {
			String fragment = getFragment();
			if (fragment == null || !fragment.startsWith("!")) {
				return "";
			} else {
				return fragment.substring(1);
			}
		}

		@Override
		public void setState(String state) {
			setFragment("!" + state);
		}

		/**
		 * Returns the current URI fragment tracked by this UriFragentManager.
		 *
		 * @return The URI fragment.
		 */
		protected String getFragment() {
			return page.getUriFragment();
		}

		/**
		 * Sets the URI fragment to the given string.
		 *
		 * @param fragment
		 *            The new URI fragment.
		 */
		protected void setFragment(String fragment) {
			page.setUriFragment(fragment, false);
		}
	}

	/**
	 * A {@link NavigationStateManager} using path info, HTML5 push state and
	 * {@link PopStateEvent}s to track views and enable listening to view
	 * changes. This manager can be enabled with UI annotation
	 * {@link PushStateNavigation}.
	 * <p>
	 * The part of path after UI's "root path" (UI's path without view
	 * identifier) is used as {@link View}s identifier. The rest of the path
	 * after the view name can be used by the developer for extra parameters for
	 * the View.
	 * <p>
	 * This class is mostly for internal use by Navigator, and is only public
	 * and static to enable testing.
	 *
	 * @since 8.2
	 */
	public static class PushStateManager implements NavigationStateManager {
		private Registration popStateListenerRegistration;
		private UI ui;

		/**
		 * Creates a new PushStateManager.
		 *
		 * @param ui
		 *            the UI where the Navigator is attached to
		 */
		public PushStateManager(UI ui) {
			this.ui = ui;
		}

		@Override
		public void setNavigator(Navigator navigator) {
			if (popStateListenerRegistration != null) {
				popStateListenerRegistration.remove();
				popStateListenerRegistration = null;
			}
			if (navigator != null) {
				popStateListenerRegistration = ui.getPage().addPopStateListener(
						event -> navigator.navigateTo(getState()));
			}
		}

		@Override
		public String getState() {
			// Get the current URL
			URI location = ui.getPage().getLocation();
			String path = location.getPath();
			if (ui.getUiPathInfo() != null
					&& path.contains(ui.getUiPathInfo())) {
				// Split the path from after the UI PathInfo
				path = path.substring(path.indexOf(ui.getUiPathInfo())
						+ ui.getUiPathInfo().length());
			} else if (path.startsWith(ui.getUiRootPath())) {
				// Use the whole path after UI RootPath
				String uiRootPath = ui.getUiRootPath();
				path = path.substring(uiRootPath.length());
			} else {
				throw new IllegalStateException(getClass().getSimpleName()
						+ " is unable to determine the view path from the URL.");
			}

			if (path.startsWith("/")) {
				// Strip leading '/'
				path = path.substring(1);
			}
			return path;
		}

		@Override
		public void setState(String state) {
			StringBuilder sb = new StringBuilder(ui.getUiRootPath());
			if (!ui.getUiRootPath().endsWith("/")) {
				// make sure there is a '/' between the root path and the
				// navigation state.
				sb.append('/');
			}
			sb.append(state);
			URI location = ui.getPage().getLocation();
			if (location != null) {
				ui.getPage().pushState(location.resolve(sb.toString()));
			} else {
				throw new IllegalStateException(
						"The Page of the UI does not have a location.");
			}
		}
	}

	/**
	 * Creates a navigation state manager for given UI. This method should take
	 * into account any navigation related annotations.
	 *
	 * @param ui
	 *            the ui
	 * @return the navigation state manager
	 *
	 * @since 8.2
	 */
	protected static NavigationStateManager createNavigationStateManager(
			UI ui) {
		if (ui.getClass().getAnnotation(PushStateNavigation.class) != null) {
			return new PushStateManager(ui);
		}
		// Fall back to old default
		return new UriFragmentManager(ui.getPage());
	}

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefaultNavigator.class);

	private final NavigationCallbackHandler callbackHandler = new DefaultNavigationCallbackHandler();

	private NavigationStateManager stateManager;

	private final Provider<Subject> subjectProvider;
	private final Sitemap sitemap;
	private final ScopedUI ui;
	private final DefaultViewFactory viewFactory;
	private NavigationState currentNavigationState;
	private NavigationState previousNavigationState;
	private final List<BeforeSecurityCheckListener> beforeSecurityCheckListener = new LinkedList<BeforeSecurityCheckListener>();
	private final List<BeforeViewChangeListener> beforeViewChangeListeners = new LinkedList<BeforeViewChangeListener>();
	private final List<AfterViewChangeListener> afterViewChangeListeners = new LinkedList<AfterViewChangeListener>();

	@Inject
	public DefaultNavigator(Provider<Subject> subjectProvider, Sitemap sitemap,
			ScopedUIProvider uiProvider, DefaultViewFactory viewFactory) {
		super();
		this.ui = uiProvider.get();
		assert this.ui != null;
		this.subjectProvider = subjectProvider;
		this.sitemap = sitemap;
		this.viewFactory = viewFactory;

		setStateManager(createNavigationStateManager(ui));
		try {
		navigateTo(stateManager.getState());
		}catch(InvalidURIException e) {
			//TODO: gestire le url errate in maniera piu "gentile", con una pagina 404 o simili piuttosto che un generico errore
			navigateToErrorView(e);
		}
	}

	public void setStateManager(NavigationStateManager stateManager) {
		checkNotNull(stateManager);
		if (this.stateManager != null && stateManager != this.stateManager) {
			this.stateManager.setNavigator(null);
		}
		this.stateManager = stateManager;
		this.stateManager.setNavigator(this);
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
	public <T extends KrailView> void navigateTo(Class<T> viewClass,
			Parameters parameters) {
		NavigationState ns = sitemap.buildNavigationState(viewClass,
				parameters);
		navigateTo(ns);
	}

	@Override
	public void navigateTo(NavigationTarget target) {
		navigateTo(target.getViewClass(), target.getParaeters());
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
			LOGGER.debug(
					"navigation canceled by a KrailViewChangeListener beforeSecurityCheck");
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
			LOGGER.debug(
					"navigation canceled by a KrailViewChangeListener beforeViewChange");
			return;
		}

		// notify Outbound navigation to current view
		if (getCurrentView() != null) {
			fireViewBeforeOutboundNavigationEvent(getCurrentView(), cancellable,
					callbackHandler);
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

		fireViewBeforeInboundNavigationEvent(view, cancellable,
				callbackHandler);
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
		updateUriFragment(navigationState);

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
	 */
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
		updateUriFragment(navigationState);
	}

	private void updateUriFragment(NavigationState targetNavigationState) {
		assert targetNavigationState != null;
		Page page = ui.getPage();
		if (!targetNavigationState.getFragment()
				.equals(page.getUriFragment())) {
			stateManager.setState(targetNavigationState.getFragment());
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
		LOGGER.debug("A {} Error has been thrown, reporting via the Error View",
				error.getClass().getName(), error);

		NavigationTarget navigationTarget = new NavigationTarget(
				ErrorView.class);
		navigationTarget.putParameter("error", error);
		navigateTo(navigationTarget);
	}

	@Override
	public void addBeforeViewChangeListener(BeforeViewChangeListener listener) {
		beforeViewChangeListeners.add(listener);
	}

	@Override
	public void removeBeforeViewChangeListener(
			BeforeViewChangeListener listener) {
		beforeViewChangeListeners.remove(listener);
	}

	@Override
	public void addAfterViewChangeListener(AfterViewChangeListener listener) {
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
