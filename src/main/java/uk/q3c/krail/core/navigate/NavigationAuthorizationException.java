package uk.q3c.krail.core.navigate;

import org.apache.shiro.authz.AuthorizationException;

import uk.q3c.krail.core.navigate.sitemap.NavigationState;

public class NavigationAuthorizationException extends AuthorizationException {

	private static final long serialVersionUID = -5518508806556371091L;
	
	private NavigationState navigationState;

	public NavigationAuthorizationException(NavigationState navigationState,
			AuthorizationException e) {
		super(e);
		this.navigationState = navigationState;
	}

	public NavigationState getTargetNavigationState() {
		return navigationState;
	}	
}
