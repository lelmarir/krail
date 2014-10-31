package uk.co.q3c.v7.base.navigate.sitemap;

import uk.co.q3c.v7.base.navigate.InvalidURIException;
import uk.co.q3c.v7.base.navigate.PageNotFoundException;
import uk.co.q3c.v7.base.navigate.sitemap.DefaultSitemap.RedirectNode;
import uk.co.q3c.v7.base.navigate.sitemap.DefaultSitemap.ViewNode;
import uk.co.q3c.v7.base.navigate.sitemap.NavigationState.Parameters;
import uk.co.q3c.v7.base.view.V7View;

public interface Sitemap {
	
	NavigationState buildNavigationStateFor(String fragment) throws InvalidURIException;

	NavigationState buildNavigationStateFor(StandardViewKey pageKey) throws PageNotFoundException;
	
	NavigationState buildNavigationStateFor(SitemapNode node);

	NavigationState buildNavigationStateFor(SitemapNode node, Parameters parameters);

	NavigationState buildNavigationState(Class<? extends V7View> viewClass) throws PageNotFoundException;
	
	SitemapNode getStandardView(StandardViewKey key);
	
	void setStandardView(StandardViewKey publicHome, ViewNode addView);

	ViewNode addView(String uri, Class<? extends V7View> view);

	RedirectNode addRedirect(String uri, SitemapNode targetNode);

}
