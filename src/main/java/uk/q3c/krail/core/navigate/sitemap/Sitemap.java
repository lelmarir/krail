package uk.q3c.krail.core.navigate.sitemap;

import org.apache.shiro.subject.Subject;

import com.vaadin.navigator.View;

import uk.q3c.krail.core.navigate.InvalidURIException;
import uk.q3c.krail.core.navigate.PageNotFoundException;
import uk.q3c.krail.core.navigate.parameters.Parameters;
import uk.q3c.krail.core.navigate.sitemap.AccesControl.Public;
import uk.q3c.krail.core.navigate.sitemap.DefaultSitemap.RedirectNode;
import uk.q3c.krail.core.navigate.sitemap.DefaultSitemap.ViewNode;
import uk.q3c.krail.core.view.DefaultLoginView;
import uk.q3c.krail.core.view.KrailView;

public interface Sitemap {
	
	NavigationState buildNavigationStateFor(String fragment) throws InvalidURIException;

	NavigationState buildNavigationStateFor(StandardPageKey pageKey) throws PageNotFoundException;
	
	NavigationState buildNavigationStateFor(SitemapNode node);

	NavigationState buildNavigationStateFor(SitemapNode node, Parameters parameters);

	NavigationState buildNavigationState(Class<? extends KrailView> viewClass) throws PageNotFoundException;
	
	ViewNode addView(String string, Class<DefaultLoginView> class1, AccesControl accesControl);
	
	NavigationState buildNavigationState(Class<? extends KrailView> viewClass, Parameters parameters) throws PageNotFoundException;
	
	SitemapNode getStandardView(StandardPageKey key);
	
	void setStandardView(StandardPageKey key, SitemapNode viewNode);
	
	void setStandardView(StandardPageKey key, StandardPageKey viewNode);

	ViewNode addView(String uri, Class<? extends KrailView> view);

	RedirectNode addRedirect(String uri, SitemapNode targetNode);

	boolean contains(Class<? extends View> viewClass);

	void checkAuthorization(Class<? extends KrailView> viewClass, Subject subject);


}
