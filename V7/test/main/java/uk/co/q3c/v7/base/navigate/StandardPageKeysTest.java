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

import java.util.Locale;

import org.junit.Test;

public class StandardPageKeysTest {

	@Test
	public void segmentAndLabel() {

		// given
		int i = 0;
		// when

		// then
		assertThat(StandardPageKeys.System_Account.segment()).isEqualTo("manage-account");
		assertThat(StandardPageKeys.System_Account.getValue(Locale.UK)).isEqualTo("manage account");
		i++;

		assertThat(StandardPageKeys.Login.segment()).isEqualTo("login");
		assertThat(StandardPageKeys.Login.getValue(Locale.UK)).isEqualTo("login");
		i++;

		assertThat(StandardPageKeys.Logout.segment()).isEqualTo("logout");
		assertThat(StandardPageKeys.Logout.getValue(Locale.UK)).isEqualTo("logout");
		i++;

		assertThat(StandardPageKeys.Enable_Account.segment()).isEqualTo("enable-account");
		assertThat(StandardPageKeys.Enable_Account.getValue(Locale.UK)).isEqualTo("enable account");
		i++;

		assertThat(StandardPageKeys.Request_Account.segment()).isEqualTo("request-account");
		assertThat(StandardPageKeys.Request_Account.getValue(Locale.UK)).isEqualTo("request account");
		i++;

		assertThat(StandardPageKeys.Reset_Account.segment()).isEqualTo("reset-account");
		assertThat(StandardPageKeys.Reset_Account.getValue(Locale.UK)).isEqualTo("reset account");
		i++;

		assertThat(StandardPageKeys.Refresh_Account.segment()).isEqualTo("refresh-account");
		assertThat(StandardPageKeys.Refresh_Account.getValue(Locale.UK)).isEqualTo("refresh account");
		i++;

		assertThat(StandardPageKeys.Unlock_Account.segment()).isEqualTo("unlock-account");
		assertThat(StandardPageKeys.Unlock_Account.getValue(Locale.UK)).isEqualTo("unlock account");
		i++;

		assertThat(StandardPageKeys.Public_Home.segment()).isEqualTo("public-home");
		assertThat(StandardPageKeys.Public_Home.getValue(Locale.UK)).isEqualTo("home");
		i++;

		assertThat(StandardPageKeys.Secure_Home.segment()).isEqualTo("secure-home");
		assertThat(StandardPageKeys.Secure_Home.getValue(Locale.UK)).isEqualTo("home");
		i++;

		assertThat(i).isEqualTo(StandardPageKeys.values().length);

		for (StandardPageKeys spk : StandardPageKeys.values()) {
			System.out.println(spk.segment());
		}
		System.out.println();
		for (StandardPageKeys spk : StandardPageKeys.values()) {
			System.out.println(spk.name());
		}
	}

}
