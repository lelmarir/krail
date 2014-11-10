package uk.q3c.krail.core.navigate.sitemap.impl;

import uk.q3c.krail.core.navigate.sitemap.NavigationState;
import uk.q3c.krail.core.navigate.sitemap.SitemapNode;

public class NavigationStateImpl implements NavigationState {

	private static final long serialVersionUID = 3214737916637205162L;
	
	private SitemapNode node;
	private Parameters parameters;

	public NavigationStateImpl(SitemapNode node, Parameters parameters) {
		this.node = node;
		this.parameters = parameters;
	}

	@Override
	public String getFragment() {
		return node.buildFragment(parameters);
	}

	@Override
	public SitemapNode getSitemapNode() {
		return node;
	}
	
	@Override
	public Parameters parameters() {
		return parameters;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "@" + "{node="+node+",parameters="+parameters+"}";
	}

}
