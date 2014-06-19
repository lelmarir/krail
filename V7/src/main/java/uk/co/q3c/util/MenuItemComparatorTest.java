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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Test;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;

public class MenuItemComparatorTest {

	@Test
	public void mi() {
		// given

		// when
		MenuBar bar = new MenuBar();
		MenuItem c = bar.addItem("c", null);
		MenuItem a = bar.addItem("a", null);
		MenuItem b = bar.addItem("b", null);

		Collections.sort(bar.getItems(), new MenuItemComparator());
		// then
		assertThat(bar.getItems()).containsExactly(a, b, c);

	}
}
