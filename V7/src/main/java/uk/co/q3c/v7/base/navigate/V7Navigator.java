package uk.co.q3c.v7.base.navigate;

import uk.co.q3c.v7.base.navigate.sitemap.NavigationState;
import uk.co.q3c.v7.base.navigate.sitemap.NavigationState.Parameters;
import uk.co.q3c.v7.base.navigate.sitemap.Sitemap;
import uk.co.q3c.v7.base.navigate.sitemap.SitemapNode;
import uk.co.q3c.v7.base.navigate.sitemap.StandardViewKey;
import uk.co.q3c.v7.base.shiro.loginevent.AuthenticationEvent.AuthenticationListener;
import uk.co.q3c.v7.base.ui.ScopedUI;
import uk.co.q3c.v7.base.view.V7View;
import uk.co.q3c.v7.base.view.V7ViewChangeNotifier;

import com.vaadin.server.Page.UriFragmentChangedListener;

/**
 * Uses the {@link UserSitemap} to control navigation from one 'page' to another, using a uri String, or a
 * {@link StandardViewKey} or a {@link UserSitemapNode} to identify a page.<br>
 * <br>
 * Even though {@link UserSitemapNode} should have already been verified for authorisation, all page navigation is
 * checked for authorisation. <br>
 * <br>
 * Looks up the view for the supplied URI, or {@link UserSitemapNode} and calls on {@link ScopedUI} to present that
 * view. Listeners are notified before and after a change of view occurs. The {@link #loginSuccessful()} method is
 * called after a successful user login - this allows the navigator to change views appropriately (according to the
 * implementation). Typically this would be to either return to the view where the user was before they went to the
 * login page, or perhaps to a specified landing page (Page here refers really to a V7View - a "virtual page"). <br>
 * <br>
 * The navigator must also respond to a change in user status (logged in or out) - logging out just navigates to the
 * logout page, while logging in applies some logic, see {@link #userStatusChanged()}
 *
 * @author David Sowerby 20 Jan 2013
 *
 */
public interface V7Navigator extends UriFragmentChangedListener, AuthenticationListener, V7ViewChangeNotifier {

	void navigateTo(String fragment) throws InvalidURIException;

	/**
	 * A convenience method to look up the URI fragment for the {@link StandardViewKey} and navigate to it
	 *
	 * @param pageKey
	 */
	void navigateTo(StandardViewKey pageKey);

	/**
	 * Navigates to the location represented by {@code navigationState}, which may include parameters
	 *
	 * @param navigationState
	 */
	void navigateTo(NavigationState navigationState);

	/**
	 * Returns the NavigationState representing the current position of the
	 * navigator
	 * 
	 * @return
	 */
	NavigationState getCurrentNavigationState();

	/**
	 * Returns the NavigationState representing the previous position of the
	 * navigator
	 * 
	 * @return
	 */
	NavigationState getPreviousNavigationState();

	void navigateToErrorView(Throwable throwable);

}
