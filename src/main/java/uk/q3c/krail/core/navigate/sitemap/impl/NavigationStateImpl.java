package uk.q3c.krail.core.navigate.sitemap.impl;

import java.util.Objects;

import uk.q3c.krail.core.navigate.parameters.Parameters;
import uk.q3c.krail.core.navigate.sitemap.NavigationState;
import uk.q3c.krail.core.navigate.sitemap.SitemapNode;
import uk.q3c.krail.core.view.DefaultViewFactory;
import uk.q3c.krail.core.view.KrailView;

public class NavigationStateImpl implements NavigationState {

	private static final long serialVersionUID = 3214737916637205162L;

	private final DefaultViewFactory viewFactory;
	private SitemapNode node;
	private KrailView viewInstance;
	private Parameters parameters;

	public NavigationStateImpl(DefaultViewFactory viewFactory, SitemapNode node,
			Parameters parameters) {
		this.viewFactory = viewFactory;
		this.node = node;
		this.parameters = parameters;
	}

	@Override
	public String getFragment() {
		return node.buildFragment(getView(), parameters);
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
		return this.getClass().getSimpleName() + "@" + "{node=" + node
				+ ",parameters=" + parameters + "}";
	}

	protected KrailView buildViewInstance() {
		return viewFactory.get(getSitemapNode().getViewClass());
	}

	@Override
	public KrailView getView() {
		if (viewInstance == null) {
			viewInstance = buildViewInstance();
		}
		return viewInstance;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof NavigationState) {
			NavigationState state = (NavigationState)obj;
			return Objects.equals(state.getFragment(), getFragment());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getFragment());
	}

}
