/*
 * Copyright (C) 2013 David Sowerby
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.co.q3c.v7.base.navigate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import uk.co.q3c.util.BasicForest;
import uk.co.q3c.v7.base.view.component.UserNavigationTree;
import uk.co.q3c.v7.i18n.CurrentLocale;
import uk.co.q3c.v7.i18n.I18NKeys;
import uk.co.q3c.v7.i18n.I18NListener;
import uk.co.q3c.v7.i18n.I18NTranslator;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.SortedSparseMultigraph;

/**
 * Encapsulates the site layout. Individual "virtual pages" are represented by {@link SitemapNode} instances. This map
 * is usually built by an implementation of {@link SitemapProvider}, and is one of the fundamental building blocks of
 * the application, as it maps out pages, URLs and Views.
 * <p>
 * <p>
 * Because it has such a fundamental purpose, an instance of this class has to be created early in the application start
 * up process. It is better therefore not to introduce dependencies into this class, otherwise the design, and ordering
 * of construction, of Guice modules starts to get complicated.
 * <p>
 * <p>
 * A potential solution for dependencies can be seen in {@link SitemapURIConverter}, which acts as an intermediary
 * between this class and {@link URIFragmentHandler} implementations, thus avoiding the creation of dependencies here.
 * <p>
 * <p>
 * <b>Ordering</b><br>
 * <br>
 * {@link BasicForest} uses a Jung {@link Forest} implementation to make the structure acyclic, and directed. Being
 * acyclic of course prevents unintentional loops occurring. The {@link Sitemap} has two other requirements:
 * <ol>
 * <li>return the nodes in insertion order, so that the original structure defined in sitemap.properties can be returned
 * exactly as it was defined
 * <li>return the nodes in sorted order, for users who wish to view pages in alphabetical order. This also needs to be
 * I18N aware. <br>
 * <br>
 * <b>Insertion Order</b><br>
 * <br>
 * A second graph is used to maintain insertion order. This is uses a {@link DirectedOrderedSparseMultigraph}. This
 * method is preferred over using an index in the {@link SitemapNode}, because it allows this technique to be used with
 * any Vector type.<br>
 * <br>
 * <b>Sorted Order</b><br>
 * <br>
 * A user may prefer page labels (for example displayed in a {@link UserNavigationTree}) to be ordered alphabetically.
 * These labels need to be I18N aware. Two options were considered, a) to sort on retrieval or b) to store in sorted
 * order,using another Jung implementation, {@link SortedSparseMultigraph} in addition to insertion order.<br>
 * <br>
 * Either would work, but the latter is considered the better option overall, particularly as it utilises well
 * established, tested code. Sitemap has Singleton scope and the labels need to be stored somewhere. The I18N
 * translation would need to be done only once, unless the user changes Locale. It does mean that this class needs to
 * respond to I18N value changes, and therefore implement {@link I18NListener} <br>
 * <br>
 * 
 * @author David Sowerby 19 May 2013
 * 
 */
public class Sitemap extends BasicForest<SitemapNode> implements I18NListener {

	public enum Ordering {
		INSERTION,
		COMPARATOR,
		UNORDERED;
	}

	private Ordering ordering;

	private final SortedSparseMultigraph<SitemapNode, Integer> sortedGraph = new SortedSparseMultigraph<>();
	private final DirectedOrderedSparseMultigraph<SitemapNode, Integer> orderedGraph = new DirectedOrderedSparseMultigraph<>();

	private int nextNodeId = 0;
	private int errors = 0;
	private final Map<StandardPageKeys, String> standardPages = new HashMap<>();
	private String report;
	// Uses LinkedHashMap to retain insertion order
	private final Map<String, String> redirects = new LinkedHashMap<>();

	private final CurrentLocale currentLocale;

	public Sitemap(CurrentLocale currentLocale) {
		this.currentLocale = currentLocale;
		sortedGraph.setVertexComparator(new SitemapNodeLabelComparator());
	}

	public String url(SitemapNode node) {
		StringBuilder buf = new StringBuilder(node.getUrlSegment());
		prependParent(node, buf);
		return buf.toString();
	}

	private void prependParent(SitemapNode node, StringBuilder buf) {
		SitemapNode parentNode = getParent(node);
		if (parentNode != null) {
			buf.insert(0, "/");
			buf.insert(0, parentNode.getUrlSegment());
			prependParent(parentNode, buf);
		}
	}

	/**
	 * creates a SiteMapNode and appends it to the map according to the {@code url} given, then returns it. If a node
	 * already exists at that location it is returned. If there are gaps in the structure, nodes are created to fill
	 * them (the same idea as forcing directory creation on a file path). An empty (not null) url is allowed. This
	 * represents the site base url without any further qualification.
	 * 
	 * @param toUrl
	 * @return
	 */
	public SitemapNode append(String url) {

		if (url.equals("")) {
			SitemapNode node = new SitemapNode();
			node.setUrlSegment(url);
			addNode(node);
			return node;
		}
		SitemapNode node = null;
		String[] segments = StringUtils.split(url, "/");
		List<SitemapNode> nodes = getRoots();
		SitemapNode parentNode = null;
		for (int i = 0; i < segments.length; i++) {
			node = findNodeBySegment(nodes, segments[i], true);
			addChild(parentNode, node);
			nodes = getChildren(node);
			parentNode = node;
		}

		return node;
	}

	private SitemapNode findNodeBySegment(List<SitemapNode> nodes, String segment, boolean createIfAbsent) {
		SitemapNode foundNode = null;
		for (SitemapNode node : nodes) {
			if (node.getUrlSegment().equals(segment)) {
				foundNode = node;
				break;
			}
		}

		if ((foundNode == null) && (createIfAbsent)) {
			foundNode = new SitemapNode();
			foundNode.setUrlSegment(segment);

		}
		return foundNode;
	}

	@Override
	public void addNode(SitemapNode node) {
		// if (node.getId() == 0) {
		// node.setId(nextNodeId());
		// }
		super.addNode(node);
		sortedGraph.addVertex(node);
		orderedGraph.addVertex(node);
	}

	/**
	 * use only during the initial build through a {@link SitemapProvider}. It adds a node only to the base graph (the
	 * {@link Forest}. This enables errors to be collected before copying the nodes to the {@link #sortedGraph} and
	 * {@link #orderedGraph} (which may generate their own errors if there are problems with the sitemap definition)
	 */
	public void addNodeInBuild(SitemapNode node) {
		super.addNode(node);
	}

	/**
	 * use only during the initial build through a {@link SitemapProvider}. It adds nodes only to the base graph (the
	 * {@link Forest}. This enables errors to be collected before copying the nodes to the {@link #sortedGraph} and
	 * {@link #orderedGraph} (which may generate their own errors if there are problems with the sitemap definition)
	 */
	public void addChildInBuild(SitemapNode parentNode, SitemapNode childNode) {
		super.addChild(parentNode, childNode);
	}

	@Override
	public void addChild(SitemapNode parentNode, SitemapNode childNode) {
		// super allows null parent
		// if (parentNode != null) {
		// if (parentNode.getId() == 0) {
		// parentNode.setId(nextNodeId());
		// }
		// }
		// if (childNode.getId() == 0) {
		// childNode.setId(nextNodeId());
		// }
		super.addChild(parentNode, childNode);
		sortedGraph.addEdge(getEdgeCount(), parentNode, childNode);
		orderedGraph.addEdge(getEdgeCount(), parentNode, childNode);
	}

	public String standardPageURI(StandardPageKeys pageKey) {
		return standardPages.get(pageKey);
	}

	private int nextNodeId() {
		nextNodeId++;
		return nextNodeId;
	}

	public Map<StandardPageKeys, String> getStandardPages() {
		return standardPages;
	}

	public boolean hasErrors() {
		return errors > 0;
	}

	public int getErrors() {
		return errors;
	}

	public void error() {
		errors++;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public String getReport() {
		return report;
	}

	/**
	 * If the {@code page} has been redirected, return the page it has been redirected to, otherwise, just return
	 * {@code page}
	 * 
	 * @param page
	 * @return
	 */
	public String getRedirectFor(String page) {
		return redirects.get(page);
	}

	public Map<String, String> getRedirects() {
		return redirects;
	}

	/**
	 * Returns a list of {@link SitemapNode} matching the {@code segments} provided. If there is an incomplete match (a
	 * segment cannot be found) then:
	 * <ol>
	 * <li>if {@code allowPartialPath} is true a list of nodes is returned correct to the longest path possible.
	 * <li>if {@code allowPartialPath} is false an empty list is returned
	 * 
	 * @param segments
	 * @return
	 */

	public List<SitemapNode> nodeChainForSegments(List<String> segments, boolean allowPartialPath) {
		List<SitemapNode> nodeChain = new ArrayList<>();
		int i = 0;
		String currentSegment = null;
		List<SitemapNode> nodes = getRoots();
		boolean segmentNotFound = false;
		SitemapNode node = null;
		while ((i < segments.size()) && (!segmentNotFound)) {
			currentSegment = segments.get(i);
			node = findNodeBySegment(nodes, currentSegment, false);
			if (node != null) {
				nodeChain.add(node);
				nodes = getChildren(node);
				i++;
			} else {
				segmentNotFound = true;
			}

		}
		if (segmentNotFound && !allowPartialPath) {
			nodeChain.clear();
		}
		return nodeChain;
	}

	public List<String> urls() {
		List<String> list = new ArrayList<>();
		for (SitemapNode node : getAllNodes()) {
			list.add(url(node));
		}
		return list;
	}

	@Override
	public void localeChange(I18NTranslator translator) {
		translateLabels();
	}

	private void translateLabels() {
		for (SitemapNode node : getAllNodes()) {
			I18NKeys<?> key = (I18NKeys<?>) node.getLabelKey();
			node.setLabel(key.getValue(currentLocale.getLocale()));
		}
	}

	/**
	 * returns the unordered graph used by ancestor class {@link BasicForest}
	 * 
	 * @return
	 */
	public Forest<SitemapNode, Integer> getGraphUnordered() {
		return super.getGraph();
	}

	/**
	 * Returns the sorted graph, which utilises {@link #comparator} to determine sort order
	 * 
	 * @return
	 */
	public SortedSparseMultigraph<SitemapNode, Integer> getGraphSorted() {
		return sortedGraph;
	}

	/**
	 * returns the graph used to retain the insertion order
	 * 
	 * @return
	 */
	public DirectedOrderedSparseMultigraph<SitemapNode, Integer> getGraphOrdered() {
		return orderedGraph;
	}

	public void setComparator(Comparator<SitemapNode> comparator) {
		sortedGraph.setVertexComparator(comparator);
	}

	/**
	 * Returns children ordered by {@code orderBy}
	 * 
	 * @param parentNode
	 *            parent node of the required children
	 * @param orderBy
	 *            the ordering required
	 * @return
	 */
	public List<SitemapNode> getChildren(SitemapNode parentNode, Ordering orderBy) {
		switch (orderBy) {
		case UNORDERED:
			return super.getChildren(parentNode);
		case COMPARATOR:
			return new ArrayList<SitemapNode>(getGraphSorted().getSuccessors(parentNode));
		case INSERTION:
			return new ArrayList<SitemapNode>(orderedGraph.getSuccessors(parentNode));
		}
		return null;
	}

	/**
	 * Returns children ordered by {@link #getOrdering()}
	 * 
	 * @see uk.co.q3c.util.BasicForest#getChildren(java.lang.Object)
	 * 
	 * @param parentNode
	 *            parent node of the required children
	 */
	@Override
	public List<SitemapNode> getChildren(SitemapNode parentNode) {
		return getChildren(parentNode, getOrdering());
	}

	public Ordering getOrdering() {
		return ordering;
	}

	public void setOrdering(Ordering ordering) {
		this.ordering = ordering;
	}

}
