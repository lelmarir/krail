package uk.q3c.krail.core.navigate;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.navigator.NavigationStateManager;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.navigator.ViewLeaveAction;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.Registration;
import com.vaadin.ui.UI;

import uk.q3c.krail.core.view.AfterViewChangeListener;
import uk.q3c.krail.core.view.BeforeViewChangeListener;
import uk.q3c.krail.core.view.KrailViewChangeEvent;

public class VaadinNavigatorWrapper extends Navigator{

	private static final Logger LOGGER = LoggerFactory
			.getLogger(VaadinNavigatorWrapper.class);
	
	private final DefaultNavigator navigator;
	private Map<ViewChangeListener, Registration> viewChangeListenersRegistrations = new HashMap<>();

	public VaadinNavigatorWrapper(DefaultNavigator navigator) {
		super();
		this.navigator = navigator;
	}

	public void navigateTo(String navigationState) {
		navigator.navigateTo(navigationState);
	}

	@Override
	public void runAfterLeaveConfirmation(ViewLeaveAction action) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected NavigationStateManager getStateManager() {
		return navigator.stateManager;
	}

	@Override
	public String getState() {
		return getStateManager().getState();
	}

	@Override
	public Map<String, String> getStateParameterMap() {
		throw new UnsupportedOperationException(
				"use #getCurrentNavigationState().parameters() instead");
	}

	@Override
	public Map<String, String> getStateParameterMap(String separator) {
		throw new UnsupportedOperationException(
				"use #getCurrentNavigationState().parameters() instead");
	}

	@Override
	public ViewDisplay getDisplay() {
		return new ViewDisplay() {
			@Override
			public void showView(View view) {
				navigator.getUI().getViewDisplayPanel().setContent(view.getViewComponent());
			}
		};
	}

	@Override
	public UI getUI() {
		return navigator.getUI();
	}

	@Override
	public void addView(String viewName, View view) {
		throw new UnsupportedOperationException();
	}

	public void addView(String viewName, Class<? extends View> viewClass) {
		if (navigator.getSitemap().contains(viewClass)) {
			// view already registered
			;
		} else {
			throw new UnsupportedOperationException(
					"Views should be registered trough Sitemap.");
		}
	}

	public void removeView(String viewName) {
		throw new UnsupportedOperationException();
	}

	public void addProvider(ViewProvider provider) {
		throw new UnsupportedOperationException();
	}

	public void removeProvider(ViewProvider provider) {
		throw new UnsupportedOperationException();
	}

	public void setErrorView(Class<? extends View> viewClass) {
		throw new UnsupportedOperationException();
	}

	public void setErrorView(View view) {
		throw new UnsupportedOperationException();
	}

	public void setErrorProvider(ViewProvider provider) {
		throw new UnsupportedOperationException();
	}

	public Registration addViewChangeListener(ViewChangeListener listener) {

		if (viewChangeListenersRegistrations.containsKey(listener)) {
			LOGGER.warn("The listener '{}' is already registered.");
			return null;
		}

		BeforeViewChangeListener beforeViewChangeListener = new BeforeViewChangeListener() {

			@Override
			public void beforeViewChange(KrailViewChangeEvent event) {
				ViewChangeEvent e = new ViewChangeEvent(VaadinNavigatorWrapper.this,
						event.getSourceNavigationState().getView(),
						event.getTargetNavigationState().getView(),
						event.getTargetNavigationState().getView()
								.getViewName(),
						event.getTargetNavigationState().parameters()
								.toString());
				listener.beforeViewChange(e);
			}
		};
		AfterViewChangeListener afterViewChangeListener = new AfterViewChangeListener() {

			@Override
			public void afterViewChange(KrailViewChangeEvent event) {
				ViewChangeEvent e = new ViewChangeEvent(VaadinNavigatorWrapper.this,
						event.getSourceNavigationState().getView(),
						event.getTargetNavigationState().getView(),
						event.getTargetNavigationState().getView()
								.getViewName(),
						event.getTargetNavigationState().parameters()
								.toString());
				listener.afterViewChange(e);
			}
		};
		Registration reg = new Registration() {

			@Override
			public void remove() {
				navigator.removeBeforeViewChangeListener(beforeViewChangeListener);
				navigator.removeAfterViewChangeListener(afterViewChangeListener);
			}
		};

		navigator.addBeforeViewChangeListener(beforeViewChangeListener);
		navigator.addAfterViewChangeListener(afterViewChangeListener);
		viewChangeListenersRegistrations.put(listener, reg);
		return reg;
	}

	public void removeViewChangeListener(ViewChangeListener listener) {
		Registration reg = viewChangeListenersRegistrations.remove(listener);
		if (reg != null) {
			reg.remove();
		}
	}

	public void destroy() {
		throw new UnsupportedOperationException();
	}
}
