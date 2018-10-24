package uk.q3c.krail.core.view;

import uk.q3c.krail.core.navigate.NavigationTarget;

/**
 * Interface for ErrorViews. Binding to implementation can be changed in
 * {@link ViewModule}
 * 
 * @author david
 * 
 */
public interface ErrorView extends KrailView {

	String ERROR_PARAMETER = "error";
	String LOCALIZED_MESSAGE_PARAMETER = "message";

	static NavigationTarget buildNavigationTarget(Throwable error) {
		assert error != null;
		return buildNavigationTarget(error, null);
	}

	static NavigationTarget buildNavigationTarget(String message) {
		assert message != null;
		return buildNavigationTarget(null, message);
	}

	static NavigationTarget buildNavigationTarget(Throwable error, String message) {
		NavigationTarget navigationTarget = new NavigationTarget(ErrorView.class);
		assert error != null || message != null;
		if (error != null) {
			navigationTarget.putParameter(ERROR_PARAMETER, error);
		}
		if (message != null) {
			navigationTarget.putParameter(LOCALIZED_MESSAGE_PARAMETER, message);
		}
		return navigationTarget;
	}

}
