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

import static org.fest.assertions.Assertions.*;

import org.junit.Test;

public class SitemapUtilsTest {

	@Test
	public void segmentFromKeyName() {

		// given

		// when

		// then
		assertThat(SitemapUtils.segmentFromKeyName("Home")).isEqualTo("home");
		assertThat(SitemapUtils.segmentFromKeyName("System_account_Map")).isEqualTo("system-account-map");

	}

	@Test
	public void viewNameFromSegment() {

		// given

		// when

		// then
		assertThat(SitemapUtils.viewNameFromSegment("home", false)).isEqualTo("Home");
		assertThat(SitemapUtils.viewNameFromSegment("home", true)).isEqualTo("HomeView");
		assertThat(SitemapUtils.viewNameFromSegment("system-account-page", false)).isEqualTo("SystemAccountPage");
		assertThat(SitemapUtils.viewNameFromSegment("system-account-page", true)).isEqualTo("SystemAccountPageView");
		assertThat(SitemapUtils.viewNameFromSegment("system_acCount_page", false)).isEqualTo("SystemAccountPage");
		assertThat(SitemapUtils.viewNameFromSegment("system_account_page", true)).isEqualTo("SystemAccountPageView");
		assertThat(SitemapUtils.viewNameFromSegment("system account page", false)).isEqualTo("SystemAccountPage");
		assertThat(SitemapUtils.viewNameFromSegment("system account page", true)).isEqualTo("SystemAccountPageView");

	}

	@Test
	public void keyNameFromSegment() {

		// given

		// when

		// then
		assertThat(SitemapUtils.keyNameFromSegment("home")).isEqualTo("Home");
		assertThat(SitemapUtils.keyNameFromSegment("system-account-page")).isEqualTo("System_Account_Page");
		assertThat(SitemapUtils.keyNameFromSegment("system_acCount_page")).isEqualTo("System_Account_Page");
		assertThat(SitemapUtils.keyNameFromSegment("system account page")).isEqualTo("System_Account_Page");

	}
}
