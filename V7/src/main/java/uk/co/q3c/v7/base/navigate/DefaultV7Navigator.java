package uk.co.q3c.v7.base.navigate;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;

import uk.co.q3c.v7.base.guice.uiscope.UIScoped;
import uk.co.q3c.v7.base.ui.ScopedUI;
import uk.co.q3c.v7.base.view.ErrorView;
import uk.co.q3c.v7.base.view.LoginView;
import uk.co.q3c.v7.base.view.LogoutView;
import uk.co.q3c.v7.base.view.components.HeaderBar;

import com.google.inject.Provider;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page.UriFragmentChangedEvent;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;

@UIScoped
public class DefaultV7Navigator implements V7Navigator {

	private V7View currentView = null;
	private final List<V7ViewChangeListener> listeners = new LinkedList<V7ViewChangeListener>();
	private final Provider<ErrorView> errorViewPro;
	private final Provider<LoginView> loginViewPro;
	private final URIFragmentHandler uriHandler;
	private final Map<String, Provider<V7View>> viewProMap;
	private final HeaderBar headerBar;
	private final Provider<LogoutView> logoutViewPro;

	@Inject
	protected DefaultV7Navigator(Provider<ErrorView> errorViewPro, URIFragmentHandler uriHandler,
			Map<String, Provider<V7View>> viewProMap, Provider<LoginView> loginViewPro,
			Provider<LogoutView> logoutViewPro, HeaderBar headerBar) {
		super();
		this.errorViewPro = errorViewPro;
		this.viewProMap = viewProMap;
		this.uriHandler = uriHandler;
		this.loginViewPro = loginViewPro;
		this.headerBar = headerBar;
		this.logoutViewPro = logoutViewPro;
	}

	@Override
	public void navigateTo(String fragment) {
		String viewName = uriHandler.setFragment(fragment).virtualPage();
		Provider<V7View> provider = viewProMap.get(viewName);
		V7View view = null;
		if (provider == null) {
			view = errorViewPro.get();
		} else {
			view = provider.get();
		}

		navigateTo(view, viewName, fragment);
		getUI().getPage().setUriFragment(fragment, false);

	}

	/**
	 * Internal method activating a view, setting its parameters and calling
	 * listeners.
	 * 
	 * @param view
	 *            view to activate
	 * @param viewName
	 *            (optional) name of the view or null not to change the
	 *            navigation state
	 * @param parameters
	 *            parameters passed in the navigation state to the view
	 */
	protected void navigateTo(V7View view, String viewName, String fragment) {
		V7ViewChangeEvent event = new V7ViewChangeEvent(this, currentView, view, viewName, fragment);
		if (!fireBeforeViewChange(event)) {
			return;
		}
		getUI().changeView(currentView, view);
		view.enter(event);
		currentView = view;
		// ui.getPage().setUriFragment(newUriFragment, false);
		fireAfterViewChange(event);
	}

	/**
	 * Fires an event before an imminent view change.
	 * <p>
	 * Listeners are called in registration order. If any listener returns
	 * <code>false</code>, the rest of the listeners are not called and the view
	 * change is blocked.
	 * <p>
	 * The view change listeners may also e.g. open a warning or question dialog
	 * and save the parameters to re-initiate the navigation operation upon user
	 * action.
	 * 
	 * @param event
	 *            view change event (not null, view change not yet performed)
	 * @return true if the view change should be allowed, false to silently
	 *         block the navigation operation
	 */
	protected boolean fireBeforeViewChange(V7ViewChangeEvent event) {
		for (V7ViewChangeListener l : listeners) {
			if (!l.beforeViewChange(event)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Fires an event after the current view has changed.
	 * <p>
	 * Listeners are called in registration order.
	 * 
	 * @param event
	 *            view change event (not null)
	 */
	protected void fireAfterViewChange(V7ViewChangeEvent event) {
		for (V7ViewChangeListener l : listeners) {
			l.afterViewChange(event);
		}
	}

	/**
	 * Listen to changes of the active view.
	 * <p>
	 * Registered listeners are invoked in registration order before (
	 * {@link ViewChangeListener#beforeViewChange(ViewChangeEvent)
	 * beforeViewChange()}) and after (
	 * {@link ViewChangeListener#afterViewChange(ViewChangeEvent)
	 * afterViewChange()}) a view change occurs.
	 * 
	 * @param listener
	 *            Listener to invoke during a view change.
	 */
	@Override
	public void addViewChangeListener(V7ViewChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a view change listener.
	 * 
	 * @param listener
	 *            Listener to remove.
	 */
	@Override
	public void removeViewChangeListener(V7ViewChangeListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void uriFragmentChanged(UriFragmentChangedEvent event) {
		navigateTo(event.getPage().getUriFragment());
	}

	@Override
	public String getNavigationState() {
		return uriHandler.fragment();
	}

	@Override
	public List<String> geNavigationParams() {
		return uriHandler.parameterList();
	}

	public ScopedUI getUI() {
		/**
		 * TODO This should be injected, with a UIScoped UI!
		 */
		UI ui = CurrentInstance.get(UI.class);
		ScopedUI scopedUi = (ScopedUI) ui;
		return scopedUi;
	}

	/**
	 * If logged in, log out and vice versa
	 * 
	 * @see uk.co.q3c.v7.base.navigate.V7Navigator#navigateToLoginOut()
	 */
	@Override
	public void navigateToLoginOut() {
		boolean loggedIn = SecurityUtils.getSubject().isAuthenticated();
		if (loggedIn) {
			navigateToLogout();
		} else {
			navigateToLogin();
		}

	}

	@Override
	public void navigateToLogin() {
		getUI().changeView(currentView, loginViewPro.get());
	}

	@Override
	public void navigateToLogout() {
		// TODO why is cast needed?
		getUI().changeView(currentView, (V7View) logoutViewPro.get());
		headerBar.getLoginBtn().setCaption("log in");
		headerBar.getUserLabel().setValue("guest");
	}

	@Override
	public void returnAfterLogin() {
		getUI().changeView(loginViewPro.get(), currentView);
		// TODO this is too closely coupled
		// https://github.com/davidsowerby/v7/issues/63
		headerBar.getUserLabel().setValue(SecurityUtils.getSubject().getPrincipal().toString());
		headerBar.getLoginBtn().setCaption("log out");
	}

	@Override
	public void requestAccountReset(UsernamePasswordToken token) {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestAccountRefresh(UsernamePasswordToken token) {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestAccountUnlock(UsernamePasswordToken token) {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestAccountEnable(UsernamePasswordToken token) {
		// TODO Auto-generated method stub

	}

	public V7View getCurrentView() {
		return currentView;
	}
}
