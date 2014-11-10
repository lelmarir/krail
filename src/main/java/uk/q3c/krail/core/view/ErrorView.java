package uk.q3c.krail.core.view;

import uk.q3c.krail.core.navigate.BeforeInboundNavigation;

/**
 * Interface for ErrorViews. Binding to implementation can be changed in {@link ViewModule}
 * 
 * @author david
 * 
 */
public interface ErrorView extends KrailView {

	@BeforeInboundNavigation
	void beforeInboundNavigation(Throwable error);

}
