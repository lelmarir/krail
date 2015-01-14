package uk.q3c.krail.core.navigate.sitemap;

import java.util.HashMap;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import uk.q3c.krail.core.navigate.PageNotFoundException;
import uk.q3c.krail.core.navigate.sitemap.NavigationState.Parameters;
import uk.q3c.krail.core.navigate.sitemap.impl.AbstractNode;
import uk.q3c.krail.core.navigate.sitemap.impl.NavigationStateImpl;
import uk.q3c.krail.core.navigate.sitemap.impl.ParametersImpl;
import uk.q3c.krail.core.view.KrailView;

public class DefaultSitemap implements Sitemap {

	public class ViewNode extends AbstractNode {

		private AccesControl accesControlRule;
		private final Class<? extends KrailView> view;

		public ViewNode(String uri, Class<? extends KrailView> view) {
			super(uri);
			this.view = view;
		}

		@Override
		public Class<? extends KrailView> getViewClass() {
			return view;
		}

		@Override
		public AccesControl getAccesControlRule() {
			return accesControlRule;
		}

		public void setAccesControlRule(AccesControl accesControlRule) {
			this.accesControlRule = accesControlRule;
		}
	}

	public class RedirectNode extends AbstractNode {

		private SitemapNode target;

		private RedirectNode(String uri) {
			super(uri);
		}

		public RedirectNode(String fromUri, SitemapNode target) {
			this(fromUri);
			this.target = target;
		}

		@Override
		public Class<? extends KrailView> getViewClass() {
			return target.getViewClass();
		}

		@Override
		public AccesControl getAccesControlRule() {
			return target.getAccesControlRule();
		}

		@Override
		public String buildFragment(Parameters parameters) {
			return target.buildFragment(parameters);
		}
	}

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefaultSitemap.class);

	private LinkedList<AbstractNode> nodes = new LinkedList<>();
	private HashMap<StandardPageKey, AbstractNode> standardViews = new HashMap<>();
	private HashMap<Class<? extends KrailView>, AbstractNode> nodesByClass = new HashMap<>();

	public DefaultSitemap() {
		;
	}

	@Override
	public ViewNode addView(String uri, Class<? extends KrailView> view) {
		checkUniqueClass(view);

		ViewNode node = new ViewNode(uri, view);
		add(node);
		return node;
	}

	@Override
	public RedirectNode addRedirect(String uri, SitemapNode targetNode) {
		RedirectNode node = new RedirectNode(uri, targetNode);
		add(node);
		return node;
	}

	private void add(AbstractNode node) {
		checkUniqueUriPattern(node.getUriPattern());

		nodes.add(node);
		nodesByClass.put(node.getViewClass(), node);
	}

	private void checkUniqueUriPattern(String uri) {
		AbstractNode n = get(uri);
		if (n != null) {
			throw new IllegalStateException(
					"Unable to register again this uri (" + uri
							+ "), remove it first: \n" + "\t " + n);
		}
	}

	private void checkUniqueClass(Class<? extends KrailView> view) {
		AbstractNode n = get(view);
		if (n != null) {
			throw new IllegalStateException(
					"Unable to register the same ViewClass ("
							+ view
							+ ") to multiple ViewNodes, use Redirects Instead: \n "
							+ "\t " + n);
		}
	}

	private AbstractNode get(Class<? extends KrailView> viewClass) {
		return nodesByClass.get(viewClass);
	}

	private AbstractNode get(String uriPattern) {
		for (AbstractNode node : nodes) {
			// TODO: consider regex expressio equality intead of siple string
			// equal
			if (node.getUriPattern().equals(nodes)) {
				return node;
			}
		}
		return null;
	}

	@Override
	public SitemapNode getStandardView(StandardPageKey key) {
		return standardViews.get(key);
	}

	@Override
	public void setStandardView(StandardPageKey key, ViewNode view) {
		standardViews.put(key, view);
	}

	@Override
	public NavigationState buildNavigationState(
			Class<? extends KrailView> viewClass) throws PageNotFoundException {
		AbstractNode node = nodesByClass.get(viewClass);
		if (node == null) {
			throw new PageNotFoundException(
					"Unable to find the node for the class " + viewClass);
		}
		return buildNavigationStateFor(node, null);
	}

	@Override
	public NavigationState buildNavigationStateFor(String fragment)
			throws PageNotFoundException {
		LinkedList<NavigationState> matches = new LinkedList<>();
		for (AbstractNode n : nodes) {
			NavigationState state = n.match(fragment);
			if (state != null) {
				if (LOGGER.isDebugEnabled()) {
					// queque all possible match (should exist only one)
					matches.add(state);
				} else {
					// return the first match
					return state;
				}
			}
		}
		assert LOGGER.isDebugEnabled();
		if (matches.size() > 1) {
			StringBuilder sb = new StringBuilder(
					"Multiple URI match the current fragment, this implementation cant tell wich one is the right one, it will use the first one:\n");
			for (NavigationState n : matches) {
				sb.append("\t " + n.toString() + " \n");
			}
			LOGGER.warn(sb.toString());
		}
		if (matches.isEmpty()) {
			throw new PageNotFoundException(
					"Unable to find the node for the fragment '" + fragment
							+ "'\n "+ nodes.toString());
		}
		return matches.getFirst();
	}

	@Override
	public NavigationState buildNavigationStateFor(StandardPageKey pageKey)
			throws PageNotFoundException {
		AbstractNode node = standardViews.get(pageKey);
		if (node == null) {
			throw new PageNotFoundException(
					"Unable to find the node for the standard view '" + pageKey
							+ "'");
		}
		return buildNavigationStateFor(node);
	}

	@Override
	public NavigationState buildNavigationStateFor(SitemapNode node) {
		assert node != null;
		return buildNavigationStateFor(node, new ParametersImpl());
	}

	@Override
	public NavigationState buildNavigationStateFor(SitemapNode node,
			Parameters parameters) {
		return new NavigationStateImpl(node, parameters != null ? parameters
				: new ParametersImpl());
	}
}
