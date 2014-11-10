package uk.q3c.krail.core.navigate.sitemap;

import uk.q3c.krail.core.navigate.sitemap.NavigationState.Parameters;
import uk.q3c.krail.core.view.KrailView;

public interface SitemapNode {
	
	String getUriPattern();

	Class<? extends KrailView> getViewClass();

	AccesControl getAccesControlRule();

	/**
	 * Construct the fragment, using the uriPattern of this node and replacing parameters
	 */
	String buildFragment(Parameters parameters) throws IllegalArgumentException;

}
