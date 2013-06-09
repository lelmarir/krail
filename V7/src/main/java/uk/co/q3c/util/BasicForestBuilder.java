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

import java.util.Deque;
import java.util.LinkedList;

/**
 * A utility class used in conjunction with {@link BasicForest} to build a forest structure form a series of inputs
 * based on the input object and the level of that object in a hierarchy. Typically the sequence may originally come
 * from a text representation of a structure, something like: <br>
 * <br>
 * -a<br>
 * --a1<br>
 * ---a11<br>
 * -b<br>
 * -b1<br>
 * <br>
 * <br>
 * This would be supplied as a sequence of calls with the appropriate object, and a level of: (the number of hyphens!)<br>
 * <br>
 * 
 * @author David Sowerby 4 Jun 2013
 * 
 */
public class BasicForestBuilder<T> {
	private final Deque<T> stack;
	private final BasicForest<T> forest;

	public BasicForestBuilder(BasicForest<T> forest) {
		stack = new LinkedList<>();
		this.forest = forest;
	}

	/**
	 * Attaches a {@code node} at the requested {@code level}. A level of 1 is equivalent to a root level (of which
	 * there can be more than one as this is a for a forest). Note that if the supplied structure is inconsistent the
	 * result of using this method will also be inconsistent. <br>
	 * <br>
	 * Specifically, for example, if a level 4 comes immediately after a level 2, it will still be attached to the
	 * preceding level 2, but has now become a level 3.
	 * 
	 * @param node
	 * @param level
	 */
	public void attach(T node, int level) {
		// a root node
		if (level == 1) {
			stack.clear();
			forest.addNode(node);
			stack.push(node);
			return;
		}
		if (level == currentLevel()) {
			stack.pop();
			attachToHead(node);
			return;
		}
		if (level > currentLevel()) {
			attachToHead(node);
		}
		if (level < currentLevel()) {
			while (currentLevel() >= level) {
				stack.pop();
			}
			attachToHead(node);
		}

	}

	private void attachToHead(T node) {
		T parentNode = stack.peek();
		forest.addChild(parentNode, node);
		stack.push(node);
	}

	private int currentLevel() {
		return stack.size();
	}
}
