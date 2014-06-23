/*
 * Copyright (C) 2014 David Sowerby
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
package uk.co.q3c.v7.base.navigate.sitemap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import uk.co.q3c.util.BasicForest;
import uk.co.q3c.v7.base.navigate.NavigationState;
import uk.co.q3c.v7.base.navigate.URIFragmentHandler;
import uk.co.q3c.v7.base.shiro.PageAccessControl;
import uk.co.q3c.v7.base.shiro.PagePermission;
import uk.co.q3c.v7.base.view.V7View;
import uk.co.q3c.v7.i18n.I18NKey;
import uk.co.q3c.v7.i18n.LabelKey;
import uk.co.q3c.v7.i18n.TestLabelKey;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import fixture.testviews2.OptionsView;
import fixture.testviews2.TestLoginView;
import fixture.testviews2.TestLogoutView;
import fixture.testviews2.TestPrivateHomeView;
import fixture.testviews2.TestPublicHomeView;
import fixture.testviews2.View1;
import fixture.testviews2.View2;

public class MockUserSitemap implements UserSitemap {

	public final UserSitemap userSitemap;

	public UserSitemapNode loginNode;
	public UserSitemapNode logoutNode;
	public UserSitemapNode privateHomeNode;
	public UserSitemapNode publicHomeNode;
	public UserSitemapNode publicNode;
	public UserSitemapNode privateNode;

	public String loginURI = "public/login";
	public String logoutURI = "public/logout";
	public String privateURI = "private";
	public String publicURI = "public";
	public String privateHomeURI = "private/home";
	public String publicHomeURI = "public/home";

	public UserSitemapNode public1Node;
	public String public1URI = "public/1";

	public UserSitemapNode private1Node;
	public String private1URI = "private/1";

	public UserSitemapNode public2Node;
	public String public2URI = "public/2";

	public UserSitemapNode private2Node;
	public String private2URI = "private/2";

	public Class<? extends V7View> loginViewClass = TestLoginView.class;
	public Class<? extends V7View> logoutViewClass = TestLogoutView.class;
	public Class<? extends V7View> privateHomeViewClass = TestPrivateHomeView.class;
	public Class<? extends V7View> publicHomeViewClass = TestPublicHomeView.class;

	public Class<? extends V7View> public1ViewClass = View1.class;
	public Class<? extends V7View> public2ViewClass = View2.class;
	public Class<? extends V7View> private1ViewClass = View1.class;
	public Class<? extends V7View> private2ViewClass = View2.class;

	public UserSitemapNode aNode;
	public String aURI = "public/a";
	public Class<? extends V7View> aViewClass = View1.class;
	public UserSitemapNode a1Node;
	public String a1URI = "public/a/a1";
	public Class<? extends V7View> a1ViewClass = View2.class;
	public UserSitemapNode a11Node;
	public String a11URI = "public/a/a1/a11";
	public Class<? extends V7View> a11ViewClass = OptionsView.class;

	public UserSitemapNode bNode;
	public String bURI = "private/b";
	public Class<? extends V7View> bViewClass = View1.class;
	public UserSitemapNode b1Node;
	public String b1URI = "private/b/b1";
	public Class<? extends V7View> b1ViewClass = View2.class;
	public UserSitemapNode b11Node;
	public String b11URI = "private/b/b1/b11";
	public Class<? extends V7View> b11ViewClass = OptionsView.class;

	private final URIFragmentHandler uriHandler;

	@Inject
	protected MockUserSitemap(URIFragmentHandler uriHandler) {
		userSitemap = mock(DefaultUserSitemap.class);
		this.uriHandler = uriHandler;
	}

	/**
	 * Creates the nodes and pages for standard pages, including intermediate (public and private) pages.
	 */
	private void createStandardPages() {
		loginNode = createNode(loginURI, "login", loginViewClass, StandardPageKey.Login, "Login",
				PageAccessControl.PUBLIC);
		logoutNode = createNode(logoutURI, "logout", logoutViewClass, StandardPageKey.Logout, "Login",
				PageAccessControl.PUBLIC);
		privateHomeNode = createNode(privateHomeURI, "home", privateHomeViewClass, StandardPageKey.Private_Home,
				"Private Home", PageAccessControl.PUBLIC);
		publicHomeNode = createNode(publicHomeURI, "home", publicHomeViewClass, StandardPageKey.Public_Home,
				"Public Home", PageAccessControl.PUBLIC);

		publicNode = createNode(publicURI, "public", null, LabelKey.Public, "Public", PageAccessControl.PUBLIC);
		privateNode = createNode(privateURI, "private", null, LabelKey.Private, "Private", PageAccessControl.PERMISSION);
		createStandardPageLookups();
	}

	private void createStandardPageLookups() {
		when(userSitemap.standardPageURI(StandardPageKey.Login)).thenReturn(loginURI);
		when(userSitemap.standardPageURI(StandardPageKey.Logout)).thenReturn(logoutURI);
		when(userSitemap.standardPageURI(StandardPageKey.Private_Home)).thenReturn(privateHomeURI);
		when(userSitemap.standardPageURI(StandardPageKey.Public_Home)).thenReturn(publicHomeURI);
	}

	private void createPage(String URI, UserSitemapNode node) {
		when(userSitemap.nodeFor(URI)).thenReturn(node);
		when(userSitemap.getRedirectPageFor(URI)).thenReturn(URI);
		when(userSitemap.nodeFor(uriHandler.navigationState(URI))).thenReturn(node);
		when(userSitemap.uri(node)).thenReturn(URI);

	}

	public UserSitemapNode createNode(String fullURI, String uriSegment, Class<? extends V7View> viewClass,
			I18NKey<?> labelKey, String label, PageAccessControl pageAccessControl, String... roles) {

		// not used yet, but may be needed
		Collator collator = Collator.getInstance();
		CollationKey collationKey = collator.getCollationKey(label);

		MasterSitemapNode masterNode = new MasterSitemapNode(uriSegment, viewClass, labelKey);
		UserSitemapNode node = new UserSitemapNode(masterNode);
		masterNode.setPageAccessControl(pageAccessControl);
		masterNode.setRoles(Arrays.asList(roles));

		node.setCollationKey(collationKey);
		node.setLabel(label);

		createPage(fullURI, node);

		return node;
	}

	public void createNodeSet(int index) {

		switch (index) {
		case 1:
			public1Node = createNode(public1URI, "1", public1ViewClass, TestLabelKey.View1, "View 1",
					PageAccessControl.PUBLIC);
			private1Node = createNode(private1URI, "1", private1ViewClass, TestLabelKey.View1, "View 1",
					PageAccessControl.PERMISSION);
			public2Node = createNode(public2URI, "2", public2ViewClass, TestLabelKey.View2, "View 2",
					PageAccessControl.PUBLIC);
			private2Node = createNode(private2URI, "2", private2ViewClass, TestLabelKey.View2, "View 2",
					PageAccessControl.PERMISSION);
			break;
		case 2:
			aNode = createNode(aURI, "a", aViewClass, TestLabelKey.View1, "View 1", PageAccessControl.PUBLIC);
			a1Node = createNode(a1URI, "a1", a1ViewClass, TestLabelKey.View2, "View 2", PageAccessControl.PUBLIC);
			a11Node = createNode(a11URI, "a11", a11ViewClass, TestLabelKey.Opt, "Opt", PageAccessControl.PUBLIC);

			addChild(aNode, a1Node);
			addChild(a1Node, a11Node);

			List<UserSitemapNode> aChain = Arrays.asList(new UserSitemapNode[] { aNode });
			List<UserSitemapNode> a1Chain = Arrays.asList(new UserSitemapNode[] { aNode, a1Node });
			List<UserSitemapNode> a11Chain = Arrays.asList(new UserSitemapNode[] { aNode, a1Node, a11Node });

			when(userSitemap.nodeChainFor(aNode)).thenReturn(aChain);
			when(userSitemap.nodeChainFor(a1Node)).thenReturn(a1Chain);
			when(userSitemap.nodeChainFor(a11Node)).thenReturn(a11Chain);

			bNode = createNode(bURI, "b", bViewClass, TestLabelKey.View1, "View 1", PageAccessControl.PUBLIC);
			b1Node = createNode(b1URI, "b1", b1ViewClass, TestLabelKey.View2, "View 2", PageAccessControl.PUBLIC);
			b11Node = createNode(b11URI, "b11", b1ViewClass, TestLabelKey.Opt, "OPt", PageAccessControl.PUBLIC);

			addChild(bNode, b1Node);
			addChild(b1Node, b11Node);

			List<UserSitemapNode> bChain = Arrays.asList(new UserSitemapNode[] { bNode });
			List<UserSitemapNode> b1Chain = Arrays.asList(new UserSitemapNode[] { bNode, b1Node });
			List<UserSitemapNode> b11Chain = Arrays.asList(new UserSitemapNode[] { bNode, b1Node, b11Node });

			when(userSitemap.nodeChainFor(bNode)).thenReturn(bChain);
			when(userSitemap.nodeChainFor(b1Node)).thenReturn(b1Chain);
			when(userSitemap.nodeChainFor(b11Node)).thenReturn(b11Chain);
		}
		createStandardPages();
		addRedirect("");
	}

	@Override
	public void addChild(UserSitemapNode parentNode, UserSitemapNode childNode) {
		when(userSitemap.getParent(childNode)).thenReturn(parentNode);

	}

	/**
	 * Use this method to tell the mock which node to return for a specific URI (you'll need to do this when the URI
	 * contains parameters)
	 *
	 * @param fullFragment
	 * @param node
	 */
	public void setNodeFor(String fullFragment, UserSitemapNode node) {
		NavigationState navState = new NavigationState();
		navState.setFragment(fullFragment);
		when(userSitemap.nodeFor(navState)).thenReturn(node);

	}

	/**
	 * If a page is not fully defined, you will still need to mock the redirect check - usually just by setting 'from'
	 * and 'to' to be the same (the short-hand version for that is {@link #addRedirect(String)}
	 * 
	 * @return
	 */
	@Override
	public Sitemap<UserSitemapNode> addRedirect(String fromPage, String toPage) {
		when(userSitemap.getRedirectPageFor(fromPage)).thenReturn(toPage);
		return this;
	}

	public void addRedirect(String fromPage) {
		addRedirect(fromPage, fromPage);
	}

	@Override
	public void setLoaded(boolean loaded) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLoaded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addStandardPage(StandardPageKey pageKey, UserSitemapNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public BasicForest<UserSitemapNode> getForest() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRedirectPageFor(NavigationState navigationState) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserSitemapNode> nodeChainFor(UserSitemapNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserSitemapNode> nodeChainForUri(String uri, boolean allowPartialPath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNodeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ImmutableList<String> uris() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImmutableMap<String, String> getRedirects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRedirectPageFor(String page) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserSitemapNode getRedirectNodeFor(UserSitemapNode sourceNode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserSitemapNode> nodeChainFor(NavigationState navigationState, boolean allowPartialPath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserSitemapNode nodeFor(NavigationState navigationState) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserSitemapNode nodeFor(String uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserSitemapNode> getChildren(UserSitemapNode parentNode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserSitemapNode> nodeChainForSegments(List<String> segments, boolean allowPartialPath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImmutableMap<StandardPageKey, UserSitemapNode> getStandardPages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserSitemapNode standardPageNode(StandardPageKey pageKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String standardPageURI(StandardPageKey pageKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserSitemapNode nodeNearestFor(String uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserSitemapNode nodeNearestFor(NavigationState navigationState) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeNode(UserSitemapNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addNode(UserSitemapNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public PagePermission pagePermission(UserSitemapNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigationState navigationState(UserSitemapNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasUri(NavigationState navigationState) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasUri(String uri) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public UserSitemapNode getParent(UserSitemapNode childNode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getChildCount(UserSitemapNode node) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<UserSitemapNode> getRoots() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserSitemapNode> getAllNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String uri(UserSitemapNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsNode(UserSitemapNode node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public UserSitemapNode getRootFor(UserSitemapNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, UserSitemapNode> getUriMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserSitemapNode userNodeFor(SitemapNode masterNode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void buildUriMap() {
		// TODO Auto-generated method stub

	}
}