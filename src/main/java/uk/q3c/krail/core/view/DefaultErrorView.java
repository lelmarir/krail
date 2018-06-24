/*
 * Copyright (c) 2014 David Sowerby
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package uk.q3c.krail.core.view;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import uk.q3c.krail.core.navigate.BeforeInboundNavigation;
import uk.q3c.krail.core.navigate.Navigator;
import uk.q3c.krail.core.navigate.Parameter;
import uk.q3c.krail.core.navigate.sitemap.NavigationState;
import uk.q3c.krail.core.navigate.sitemap.SitemapNode;
import uk.q3c.krail.core.navigate.sitemap.StandardPageKey;
import uk.q3c.krail.core.view.KrailViewChangeEvent.CancellableKrailViewChangeEvent;
import uk.q3c.util.StackTraceUtil;

/**
 * @author David Sowerby 4 Aug 2013
 */

public class DefaultErrorView extends ViewBase<Layout> implements ErrorView {

	private Throwable error;
	private TextArea textArea;
	private Label descriptionLabel;

	private NavigationState previousNavigationState;

	@Inject
	private Provider<Navigator> navigatorProvider;

	protected DefaultErrorView() {
		super();
		CssLayout outerLayout = new CssLayout();
		{
			VerticalLayout mainLayout = new VerticalLayout();
			{
				mainLayout.setSizeFull();

				VerticalLayout descriptionLayout = new VerticalLayout();
				{
					descriptionLayout.setDefaultComponentAlignment(
							Alignment.MIDDLE_CENTER);

					descriptionLabel = new Label("Something went wrong");
					{
						descriptionLabel.setSizeUndefined();
						descriptionLabel.addStyleName(ValoTheme.LABEL_H1);
						descriptionLabel.setContentMode(ContentMode.HTML);
					}
					descriptionLayout.addComponent(descriptionLabel);

					Button backButton = new Button("Back", new ClickListener() {
						@Override
						public void buttonClick(ClickEvent event) {
							if (previousNavigationState != null) {
								navigatorProvider.get().navigateTo(previousNavigationState);
							} else {
								navigatorProvider.get().navigateTo(
										StandardPageKey.Public_Home);
							}
						}
					});
					backButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
					descriptionLayout.addComponent(backButton);

					// FIXME: localizzare
					Button moreButton = new Button("show more");
					{
						moreButton.addStyleName(ValoTheme.BUTTON_LINK);
						moreButton.addClickListener(new ClickListener() {
							@Override
							public void buttonClick(ClickEvent event) {
								textArea.setVisible(true);
								descriptionLayout.setVisible(false);
							}
						});
					}
					descriptionLayout.addComponent(moreButton);
				}
				mainLayout.addComponent(descriptionLayout);
				mainLayout.setComponentAlignment(descriptionLayout,
						Alignment.MIDDLE_CENTER);

				textArea = new TextArea();
				{
					textArea.setSizeFull();
					textArea.setVisible(false);
					textArea.setValue(
							"Error view has been called but no error has been set.  This should not happen");
					textArea.setReadOnly(true);
				}
				mainLayout.addComponent(textArea);
				mainLayout.setExpandRatio(textArea, 1);
			}
			outerLayout.addComponent(mainLayout);
		}

		setRootComponent(outerLayout);
	}

	public TextArea getTextArea() {
		return textArea;
	}

	public Throwable getError() {
		return error;
	}

	@BeforeInboundNavigation
	protected void beforeInboundNavigation(
			CancellableKrailViewChangeEvent event) {
		event.cancel();
		event.getNavigator().navigateTo(StandardPageKey.Public_Home);
	}

	@BeforeInboundNavigation
	protected void beforeInboundNavigation(
			CancellableKrailViewChangeEvent event,
			@Parameter(value = ErrorView.ERROR_PARAMETER, optional = true) Throwable error,
			@Parameter(value = ErrorView.LOCALIZED_MESSAGE_PARAMETER, optional = true) String localizedMessage) {
		// try to close any opened windows
		int loopCount = 10;
		while (loopCount > 0
				&& UI.getCurrent().getWindows().iterator().hasNext()) {
			try {
				UI.getCurrent().getWindows().iterator().next().close();
			} catch (Exception e) {
				;
			}
			loopCount--;
		}
		this.error = error;
		if (error != null) {
			textArea.setReadOnly(false);
			textArea.setValue(StackTraceUtil.getStackTrace(error));
			textArea.setReadOnly(true);
		}

		if (localizedMessage != null && !localizedMessage.isEmpty()) {
			descriptionLabel.setValue(localizedMessage);
		}

		if (event.getSourceNavigationState() != null) {
			previousNavigationState = event.getSourceNavigationState();
		}
	}
}
