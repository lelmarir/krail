package uk.q3c.krail.core.navigate.sitemap;

import uk.q3c.krail.core.navigate.InvalidURIException;
import uk.q3c.krail.core.navigate.PageNotFoundException;
import uk.q3c.krail.core.navigate.sitemap.DefaultSitemap.RedirectNode;
import uk.q3c.krail.core.navigate.sitemap.DefaultSitemap.ViewNode;
import uk.q3c.krail.core.navigate.sitemap.NavigationState.Parameters;
import uk.q3c.krail.core.view.KrailView;

public interface Sitemap {
	
	NavigationState buildNavigationStateFor(String fragment) throws InvalidURIException;

	NavigationState buildNavigationStateFor(StandardViewKey pageKey) throws PageNotFoundException;
	
	NavigationState buildNavigationStateFor(SitemapNode node);

	NavigationState buildNavigationStateFor(SitemapNode node, Parameters parameters);

	NavigationState buildNavigationState(Class<? extends KrailView> viewClass) throws PageNotFoundException;
	
	SitemapNode getStandardView(StandardViewKey key);
	
	void setStandardView(StandardViewKey publicHome, ViewNode addView);

	ViewNode addView(String uri, Class<? extends KrailView> view);

	RedirectNode addRedirect(String uri, SitemapNode targetNode);

}
