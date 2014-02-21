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
package fixture.testviews2;

import uk.co.q3c.v7.base.guice.uiscope.UIScoped;
import uk.co.q3c.v7.base.view.LoginView;
import uk.co.q3c.v7.base.view.V7ViewChangeEvent;
import uk.co.q3c.v7.i18n.I18NKey;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

@UIScoped
public class TestLoginView implements LoginView {

	@Override
	public void enter(V7ViewChangeEvent event) {

	}

	@Override
	public Component getRootComponent() {
		return new Label("not used");
	}

	@Override
	public void setUsername(String username) {

	}

	@Override
	public void setPassword(String password) {

	}

	@Override
	public Button getSubmitButton() {
		return null;
	}

	@Override
	public String getStatusMessage() {
		return null;
	}

	@Override
	public void setStatusMessage(String invalidLogin) {

	}

	@Override
	public String viewName() {

		return getClass().getSimpleName();
	}

	@Override
	public void setIds() {
	}

	@Override
	public void setStatusMessage(I18NKey<?> messageKey) {
	}
}
