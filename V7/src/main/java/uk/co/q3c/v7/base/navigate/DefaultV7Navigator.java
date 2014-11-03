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
package uk.co.q3c.v7.base.navigate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.vaadin.server.Page;
import com.vaadin.server.Page.UriFragmentChangedEvent;
import com.vaadin.shared.Position;
import com.vaadin.ui.UI;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.q3c.v7.base.navigate.sitemap.*;
import uk.co.q3c.v7.base.navigate.sitemap.NavigationState.Parameters;
import uk.co.q3c.v7.base.shiro.SubjectProvider;
import uk.co.q3c.v7.base.shiro.loginevent.AuthenticationEvent.AuthenticationNotifier;
import uk.co.q3c.v7.base.shiro.loginevent.AuthenticationEvent.FailedLoginEvent;
import uk.co.q3c.v7.base.shiro.loginevent.AuthenticationEvent.LogoutEvent;
import uk.co.q3c.v7.base.shiro.loginevent.AuthenticationEvent.SuccesfulLoginEvent;
import uk.co.q3c.v7.base.ui.ScopedUI;
import uk.co.q3c.v7.base.ui.ScopedUIProvider;
import uk.co.q3c.v7.base.view.*;
import uk.co.q3c.v7.base.view.V7ViewChangeEvent.CancellableV7ViewChangeEvent;
import uk.co.q3c.v7.base.view.V7ViewChangeEventImpl.CancellableWrapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

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
public class DefaultV7Navigator implements V7Navigator {

	public class DefaultNavigationCallbackHandler<T extends V7View> implements
			NavigationCallbackHandler<T> {

		@Override
		public void beforeOutboundNavigationEvent(T view,
				CancellableWrapper cancellable) {
			try {
				fireNavigationCallback(view, cancellable,
						BeforeOutboundNavigation.class);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void beforeInboundNavigationEvent(T view,
				CancellableV7ViewChangeEvent cancellable) {
			try {
				fireNavigationCallback(view, cancellable,
						BeforeInboundNavigation.class);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void afterInbounNavigationEvent(T view, V7ViewChangeEvent event) {
			try {
				fireNavigationCallback(view, event,
						AfterInboundNavigation.class);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		
	}

	private static final long serialVersionUID = -1199874611306964538L;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefaultV7Navigator.class);

	private final NavigationCallbackHandler DEFAULT_NAVIGATION_CALLBACK_HANDLER = new DefaultNavigationCallbackHandler();

	private static List<Method> getAnnotatedMethods(
			Class<? extends V7View> clazz,
			Class<? extends Annotation> annotation) {
		LinkedList<Method> list = new LinkedList<>();
		for (Method m : clazz.getMethods()) {
			if (m.isAnnotationPresent(annotation)) {
				list.add(m);
			}
		}
		return list;
	}

	private static void fireNavigationCallback(V7View view,
			V7ViewChangeEvent event, Class<? extends Annotation> annotation)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		List<Method> methods = getAnnotatedMethods(view.getClass(), annotation);
		if (methods.size() > 1) {
			throw new IllegalStateException(
					"Only one method can be annotated with "
							+ annotation.getClass() + ":" + methods);
		}

		if (!methods.isEmpty()) {
			Method method = methods.get(0);
			LOGGER.debug("found method annotated with " + annotation.getClass()
					+ ": ", method);

			if (!method.getReturnType().equals(Void.TYPE)) {
				throw new IllegalStateException("The method annotated with "
						+ annotation.getClass() + " should return void: "
						+ method);
			}

			Class<?>[] parametersTypes = method.getParameterTypes();
			Object[] args = new Object[parametersTypes.length];
			for (int i = 0; i < parametersTypes.length; i++) {
				if (parametersTypes[i].isAssignableFrom(event.getClass())) {
					args[i] = event;
					LOGGER.trace(
							"parameter {} of type {} -> CancellableV7ViewChangeEvent",
							i, parametersTypes[i]);
				} else {
					throw new IllegalStateException("Unable to bind parameter "
							+ i + " (of type " + parametersTypes[i]
							+ ") of the callback method " + method);
				}
			}

			method.invoke(view, args);
		}
	}

    private final List<V7ViewChangeListener> viewChangeListeners = new LinkedList<V7ViewChangeListener>();
    private final Provider<Subject> subjectProvider;
	private final Sitemap sitemap;
    private final ScopedUIProvider uiProvider;
    private final DefaultViewFactory viewFactory;
    private NavigationState currentNavigationState;
    private NavigationState previousNavigationState;

    @Inject
	public DefaultV7Navigator(SubjectProvider subjectProvider, Sitemap sitemap,
                              ScopedUIProvider uiProvider, DefaultViewFactory viewFactory,
			AuthenticationNotifier authenticationNotifier) {
        super();
        this.uiProvider = uiProvider;
        this.subjectProvider = subjectProvider;
		this.sitemap = sitemap;
        this.viewFactory = viewFactory;
		authenticationNotifier.addListener(this);
	}

	@Override
	public void addListener(V7ViewChangeListener listener) {
		viewChangeListeners.add(listener);
    }

    @Override
	public void removeListener(V7ViewChangeListener listener) {
		viewChangeListeners.remove(listener);
	}

	@Override
	public void uriFragmentChanged(UriFragmentChangedEvent event) {
		String fragment = event.getUriFragment();
		navigateTo(fragment != null ? fragment : "");
        }

    /**
	 * When a user has successfully logged in, they are routed back to the page
	 * they were on before going to the login page. If they have gone straight
	 * to the login page (maybe they bookmarked it), or they were on the logout
	 * page, they will be routed to the 'private home page' (the StandardPage
	 * for {@link StandardViewKey#PrivateHome})
     */
    @Override
	public void onSuccess(SuccesfulLoginEvent event) {
		assert event.getSubject().isAuthenticated();

		LOGGER.info("user logged in successfully, navigating to appropriate view");
		// they have logged in
		NavigationState previousNavigationState = getPreviousNavigationState();
		if (previousNavigationState != null
				&& !isLogOutPage(previousNavigationState)) {
			navigateTo(previousNavigationState);
		} else {
			navigateTo(StandardViewKey.PrivateHome);
		}
    }

	private boolean isLogOutPage(NavigationState navigationState) {
		return navigationState.getSitemapNode().equals(
				sitemap.getStandardView(StandardViewKey.Log_Out));
	}

    @Override
	public void onFailure(FailedLoginEvent event) {
		;
    }

    @Override
	public void onLogout(LogoutEvent event) {
		LOGGER.info("logging out");
		subjectProvider.get().logout();
		navigateTo(StandardViewKey.Log_Out);
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
	public void navigateTo(StandardViewKey pageKey) {
		navigateTo(sitemap.buildNavigationStateFor(pageKey));
	}

	public <T extends V7View> void navigateTo(Class<T> viewClass,
			NavigationCallbackHandler<T> callbackHandler) {
		NavigationState ns = sitemap.buildNavigationState(viewClass);
		navigateTo(ns, callbackHandler);
	}

	@Override
    public void navigateTo(NavigationState navigationState) {
		navigateTo(navigationState, DEFAULT_NAVIGATION_CALLBACK_HANDLER);
	}

	public void navigateTo(NavigationState navigationState,
			NavigationCallbackHandler callbackHandler) {
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

        Subject subject = subjectProvider.get();
		// throw an exception if not authorized
		assert node.getAccesControlRule() != null : node;
		node.getAccesControlRule().checkAuthorization(subject);

		V7ViewChangeEvent event = new V7ViewChangeEventImpl(this,
				currentNavigationState, navigationState);

		// notify V7ViewChangeListener
		{
			CancellableWrapper cancellable = new CancellableWrapper(event);
            // if change is blocked revert to previous state
			fireBeforeViewChange(cancellable);
			if (cancellable.isCancelled()) {
				LOGGER.debug("navigation canceled by a V7ViewChangeListener");
                return;
            }
		}

		// notify Outbound navigation to current view
		{
			if (getCurrentView() != null) {
				CancellableWrapper cancellable = new CancellableWrapper(event);
				fireViewBeforeOutboundNavigationEvent(getCurrentView(),
						cancellable, callbackHandler);
				if (cancellable.isCancelled()) {
					LOGGER.debug(
							"navigation canceled by the view {} in NavigationAwareView#onOutboundNavigation",
							getCurrentView().getClass().getSimpleName());
					return;
            }
        }
    }

		LOGGER.debug("obtaining view instance for '{}'", node);
		V7View view = viewFactory.get(node.getViewClass());

		// notify before Inbound navigation to target view
		{
			CancellableWrapper cancellable = new CancellableWrapper(event);
			fireViewBeforeInboundNavigationEvent(view, cancellable,
					callbackHandler);
			if (cancellable.isCancelled()) {
				LOGGER.debug(
						"navigation canceled by the view {} in NavigationAwareView#onInboundNavigation",
						view.getClass().getSimpleName());
				return;
        }
    }

		setCurrentNavigationState(navigationState);

		// make sure the page uri is updated if necessary, but do not fire any
		// change events as we have already responded to the change
		updateUriFragment(navigationState, false);
		// now change the view
		changeView(view);

		fireViewAfterInboundNavigationEvent(view, event, callbackHandler);

		// and tell listeners its changed
		fireAfterViewChange(event);

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
     *         view change event (not null, view change not yet performed)
     *
	 * @return true if the view change should be allowed, false to silently
	 *         block the navigation operation
     */
	protected void fireBeforeViewChange(CancellableV7ViewChangeEvent event) {
        for (V7ViewChangeListener l : viewChangeListeners) {
			l.beforeViewChange(event);
            }
        }

	private void fireViewBeforeOutboundNavigationEvent(V7View view,
			CancellableWrapper cancellable,
			NavigationCallbackHandler callbackHandler) {
		callbackHandler.beforeOutboundNavigationEvent(view, cancellable);
	}

	private void fireViewBeforeInboundNavigationEvent(V7View view,
			CancellableV7ViewChangeEvent cancellable,
			NavigationCallbackHandler callbackHandler) {
		callbackHandler.beforeInboundNavigationEvent(view, cancellable);
	}

	private void setCurrentNavigationState(NavigationState navigationState) {
		previousNavigationState = currentNavigationState;
		currentNavigationState = navigationState;
	}

	private void updateUriFragment(NavigationState navigationState,
			boolean fireEvents) {
		ScopedUI ui = uiProvider.get();
		Page page = ui.getPage();
		if (!navigationState.getFragment().equals(page.getUriFragment())) {
			page.setUriFragment(navigationState.getFragment(), fireEvents);
		}
	}

	protected void changeView(V7View view) {
		ScopedUI ui = uiProvider.get();
		ui.changeView(view);
    }

	private void fireViewAfterInboundNavigationEvent(V7View view,
			V7ViewChangeEvent event, NavigationCallbackHandler callbackHandler) {
		callbackHandler.afterInbounNavigationEvent(view, event);
	}

    /**
     * Fires an event after the current view has changed.
     * <p/>
     * Listeners are called in registration order.
     *
     * @param event
     *         view change event (not null)
     */
    protected void fireAfterViewChange(V7ViewChangeEvent event) {
        for (V7ViewChangeListener l : viewChangeListeners) {
            l.afterViewChange(event);
        }
    }

    @Override
    public NavigationState getCurrentNavigationState() {
        return currentNavigationState;
    }

	private V7View getCurrentView() {
		return uiProvider.get().getView();
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

		navigateTo(ErrorView.class,
				new DefaultNavigationCallbackHandler<ErrorView>() {
    @Override
					public void beforeInboundNavigationEvent(ErrorView view,
							CancellableV7ViewChangeEvent cancellable) {
						view.beforeInboundNavigation(error);
    }
				});
    }

}
