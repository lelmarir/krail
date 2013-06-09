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

import org.junit.Test;

public class BasicForestBuilderTest {

	String[] input = new String[] { "a", "b", "b1", "b11", "b12", "b121", "b122", "b21", "c", "c11" };

	@Test
	public void attach() {

		// given
		BasicForest<String> forest = new BasicForest<>();
		BasicForestBuilder<String> builder = new BasicForestBuilder<>(forest);
		// when

		// then (using length of string as level)

		for (String s : input) {
			builder.attach(s, s.length());
		}
		System.out.println(forest);
	}

}
