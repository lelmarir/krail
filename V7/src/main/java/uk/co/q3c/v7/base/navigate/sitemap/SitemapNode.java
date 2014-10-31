package uk.co.q3c.v7.base.navigate.sitemap;

import uk.co.q3c.v7.base.navigate.sitemap.NavigationState.Parameters;
import uk.co.q3c.v7.base.view.V7View;

public interface SitemapNode {
	
	String getUriPattern();

	Class<? extends V7View> getViewClass();

	AccesControl getAccesControlRule();

	/**
	 * Construct the fragment, using the uriPattern of this node and replacing parameters
	 */
	String buildFragment(Parameters parameters) throws IllegalArgumentException;

}
