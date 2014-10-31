package uk.co.q3c.v7.base.view;

import uk.co.q3c.v7.base.navigate.BeforeInboundNavigation;

/**
 * Interface for ErrorViews. Binding to implementation can be changed in {@link ViewModule}
 * 
 * @author david
 * 
 */
public interface ErrorView extends V7View {

	@BeforeInboundNavigation
	void beforeInboundNavigation(Throwable error);

}
