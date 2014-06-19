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
package uk.co.q3c.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Comparator;

import com.vaadin.ui.Tree;

public class TargetTreeWrapper_VaadinTree<S, T> extends TargetTreeWrapperBase<S, T> {

	private final Tree tree;

	public TargetTreeWrapper_VaadinTree(Tree tree) {
		super();
		this.tree = tree;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T createNode(T parentNode, S sourceChildNode) {
		checkNotNull(sourceChildNode);
		checkNotNull(getCaptionReader(), "a caption reader is needed, so that tree item captions are set");
		T newTargetNode = null;
		if (getNodeModifier() == null) {
			newTargetNode = (T) sourceChildNode;
		} else {
			newTargetNode = getNodeModifier().create(parentNode, sourceChildNode);
		}
		tree.setItemCaption(newTargetNode, getCaptionReader().getCaption(sourceChildNode));
		return newTargetNode;
	}

	/**
	 * Not used in this implementation
	 */
	@Override
	public void sort(T parentNode, Comparator<T> comparator) {

	}

	@Override
	public void addChild(T parentNode, T childNode) {
		tree.addItem(childNode);
		if (parentNode != null) {
			if (tree.getItem(parentNode) == null) {
				tree.addItem(parentNode);
			}
		}
		tree.setParent(childNode, parentNode);

	}

}
