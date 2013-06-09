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
package uk.co.q3c.util;

import static org.fest.assertions.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycila.testing.junit.MycilaJunitRunner;
import com.mycila.testing.plugin.guice.GuiceContext;

@RunWith(MycilaJunitRunner.class)
@GuiceContext({})
public class BasicForestTest {

	BasicForest<String> forest;
	BasicForest<TestNode> forest2;

	String s0 = "0";
	String s1 = "1";
	String s11 = "1.1";
	String s12 = "1.2";
	String s121 = "1.2.1";
	String s111 = "1.1.1";
	String s2 = "2";
	String s21 = "2.1";
	String s22 = "2.2";
	private TestNode tna;
	private TestNode tnc;
	private TestNode tnb;
	private TestNode tnb1;
	private TestNode tnb2;
	private TestNode tnb3;
	private TestNode tnb13;
	private TestNode tnb12;
	private TestNode tnb11;

	class TestNode {
		String s;
		int i;

		protected TestNode(String s, int i) {
			super();
			this.s = s;
			this.i = i;

		}

		@Override
		public String toString() {
			return s + "," + i;
		}

	}

	@Before
	public void setup() {
		forest = new BasicForest<>();
		forest2 = new BasicForest<>();
	}

	@Test
	public void addNode() {
		// given

		// when
		forest.addNode(s1);
		// then
		assertThat(forest.containsNode(s1)).isTrue();
	}

	@Test
	public void addChildHasChild() {
		// given

		// when
		forest.addNode(s1);
		forest.addChild(s1, s11);
		// then
		assertThat(forest.containsNode(s1)).isTrue();
		assertThat(forest.containsNode(s11)).isTrue();
		assertThat(forest.hasChild(s1, s11)).isTrue();
	}

	@Test
	public void addChildParentNotInTree() {
		// given

		// when
		forest.addChild(s1, s11);
		// then
		assertThat(forest.containsNode(s1)).isTrue();
		assertThat(forest.containsNode(s11)).isTrue();
		assertThat(forest.hasChild(s1, s11)).isTrue();
	}

	@Test
	public void getParent() {
		// given

		// when
		forest.addNode(s1);
		forest.addChild(s1, s11);
		// then
		assertThat(forest.getParent(s11)).isEqualTo(s1);
	}

	@Test
	public void addBranch() {
		// given
		List<String> branch = new ArrayList<>();
		branch.add(s1);
		branch.add(s11);
		branch.add(s111);
		// when
		forest.addBranch(branch);
		// then
		assertThat(forest.containsNode(s1)).isTrue();
		assertThat(forest.containsNode(s11)).isTrue();
		assertThat(forest.containsNode(s111)).isTrue();
		assertThat(forest.getParent(s11)).isEqualTo(s1);
		assertThat(forest.getParent(s111)).isEqualTo(s11);
	}

	@Test
	public void getNode() {
		// given
		List<String> branch = new ArrayList<>();
		branch.add(s1);
		branch.add(s11);
		branch.add(s111);
		// when
		forest.addBranch(branch);
		// then
		assertThat(forest.getNode(s11)).isEqualTo(s11);
	}

	@Test
	public void getChildren() {
		// given
		List<String> branch = new ArrayList<>();
		branch.add(s1);
		branch.add(s11);
		branch.add(s111);
		// when
		forest.addBranch(branch);
		forest.addChild(s1, s12);
		// then
		assertThat(forest.getChildCount(s1)).isEqualTo(2);
		assertThat(forest.getChildren(s1)).containsOnly(s11, s12);
	}

	@Test
	public void getSubtreeNodes() {
		// given

		// when
		addStringNodes();
		// then
		assertThat(forest.getSubtreeNodes(s1)).containsOnly(s1, s11, s111, s12, s121);
	}

	@Test
	public void findLeavesOf() {
		// given

		// when
		addStringNodes();
		// then
		assertThat(forest.findLeaves(s1)).containsOnly(s111, s121);
	}

	@Test
	public void findLeaves() {
		// given

		// when
		addStringNodes();
		// then
		assertThat(forest.findLeaves()).containsOnly(s111, s121, s21, s22);
	}

	@Test
	public void getEntries() {
		// given

		// when
		forest.addNode(s1);
		forest.addChild(s1, s11);
		forest.addChild(s1, s12);
		forest.addNode(s2);
		// then
		assertThat(forest.getEntries()).containsOnly(s1, s11, s12, s2);
	}

	@Test
	public void clear() {
		// given
		addStringNodes();
		// when
		forest.clear();
		// then
		assertThat(forest.getEntries()).isEmpty();
	}

	@Test
	public void getBranchRoots() {
		// given

		// when
		addStringNodes();
		// then
		assertThat(forest.getRoots()).containsOnly(s0);
	}

	/**
	 * toString() puts a blank line at the start
	 */
	@Test
	public void tostring() {
		// given

		// when
		addStringNodes();
		// then
		assertThat(forest.toString()).isEqualTo(
				"\n-0\n--2\n---2.1\n---2.2\n--1\n---1.2\n----1.2.1\n---1.1\n----1.1.1\n");
	}

	@Test
	public void text() {
		// given
		StringBuilder buf = new StringBuilder();
		// when
		addStringNodes();
		forest.text(s0, buf, 0);
		String s = buf.toString();
		// then
		assertThat(s).isEqualTo("-0\n--2\n---2.1\n---2.2\n--1\n---1.2\n----1.2.1\n---1.1\n----1.1.1\n");
	}

	@Test
	public void getChildCount() {
		// given

		// when
		addStringNodes();
		// then
		assertThat(forest.getChildCount(s0)).isEqualTo(2);
		assertThat(forest.getChildCount(s111)).isEqualTo(0);
	}

	@Test
	public void hasChildren() {
		// given

		// when
		addStringNodes();
		// then
		assertThat(forest.hasChildren(s0)).isTrue();
		assertThat(forest.hasChildren(s111)).isFalse();
	}

	@Test
	public void getRoot() {
		// given

		// when
		addStringNodes();
		// then
		assertThat(forest.getRoot()).isEqualTo(s0);
	}

	@Test
	public void getNodeCount() {
		// given

		// when
		addStringNodes();
		// then
		assertThat(forest.getNodeCount()).isEqualTo(9);
	}

	// /**
	// * Quite a few methods to test
	// */
	// @Test
	// public void sortOrder() {
	//
	// // given
	// addTestNodes();
	// // when
	// forest2.setSortOrderInsertion();
	// // then
	// assertThat(forest2.getRoot()).isEqualTo(tnc);
	// assertThat(forest2.getRoots()).containsExactly(tnc, tna, tnb);
	// assertThat(forest2.getChildren(tnb)).containsExactly(tnb2, tnb3, tnb1);
	// assertThat(forest2.getSubtreeNodes(tnb)).containsExactly(tnb2, tnb3, tnb1, tnb12, tnb11, tnb13);
	// assertThat(forest2.findLeaves()).containsExactly(tnc, tna, tnb2, tnb3, tnb12, tnb11, tnb13);
	// assertThat(forest2.findLeaves(tnb)).containsExactly(tnb2, tnb3, tnb12, tnb11, tnb13);
	// assertThat(forest2.getAllNodes()).containsExactly(tnc, tna, tnb, tnb2, tnb3, tnb12, tnb11, tnb13);
	// assertThat(forest2.getEntries()).containsExactly(tnc, tna, tnb, tnb2, tnb3, tnb12, tnb11, tnb13);
	// assertThat(forest2.toString()).isEqualTo("\n-c\n-a\n-b\n--b2\n--b3\n--b1\n---b12\n---b11\n---b13");
	// }

	private void addStringNodes() {

		forest.addNode(s0);
		forest.addChild(s1, s12);
		forest.addChild(s0, s1);
		forest.addChild(s0, s2);
		forest.addChild(s1, s11);
		forest.addChild(s12, s121);
		forest.addChild(s11, s111);
		forest.addChild(s2, s21);
		forest.addChild(s2, s22);
	}

	private void addTestNodes() {
		createTestNodes();
		forest2.addNode(tnc);
		forest2.addNode(tna);
		forest2.addChild(tnb, tnb2);
		forest2.addChild(tnb, tnb3);
		forest2.addChild(tnb, tnb1);
		forest2.addChild(tnb1, tnb12);
		forest2.addChild(tnb1, tnb11);
		forest2.addChild(tnb1, tnb13);

	}

	private void createTestNodes() {
		tna = new TestNode("a", 1);
		tnb = new TestNode("b", 9);
		tnc = new TestNode("c", 3);
		tnb1 = new TestNode("b1", 4);
		tnb2 = new TestNode("b2", 3);
		tnb3 = new TestNode("b3", 2);
		tnb11 = new TestNode("b11", 8);
		tnb12 = new TestNode("b12", 16);
		tnb13 = new TestNode("b13", 6);
	}

}
