package uk.q3c.krail.core.navigate.sitemap;

import uk.q3c.krail.core.navigate.parameters.Parameters;
import uk.q3c.krail.core.view.KrailView;

public interface SitemapNode {

	String getUriPattern();

	Class<? extends KrailView> getViewClass();

	AccesControl getAccesControlRule();

	/**
	 * Construct the fragment, using the uriPattern of this node and replacing parameters
	 * @param viewInstance 
	 */
	String buildFragment(KrailView viewInstance, Parameters parameters) throws IllegalArgumentException;

	NavigationState buildNavigationState(String fragment);

	NavigationState buildNavigationState(Parameters params);

}