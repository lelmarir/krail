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
import uk.q3c.krail.core.navigate.Parameter;
import uk.q3c.krail.i18n.MessageKey;
import uk.q3c.krail.i18n.Translate;
import uk.q3c.util.StackTraceUtil;

/**
 * @author David Sowerby 4 Aug 2013
 */

public class DefaultErrorView extends ViewBase<Layout> implements ErrorView {

	private Throwable error;
	private TextArea textArea;

	@Inject
	protected DefaultErrorView(Translate translate) {
		super();
		CssLayout outerLayout = new CssLayout();
		{
			VerticalLayout mainLayout = new VerticalLayout();
			{
				mainLayout.setSizeFull();

				VerticalLayout desriptionLayout = new VerticalLayout();
				{					
					desriptionLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
					
					Label description = new Label(translate.from(MessageKey.Something_went_wrong));
					{
						description.setSizeUndefined();
						description.addStyleName(ValoTheme.LABEL_H1);
					}					
					desriptionLayout.addComponent(description);

					Button more = new Button(translate.from(MessageKey.show_more));
					{
						more.addStyleName(ValoTheme.BUTTON_LINK);
						more.addClickListener(new ClickListener() {
							@Override
							public void buttonClick(ClickEvent event) {
								textArea.setVisible(true);
								desriptionLayout.setVisible(false);
							}
						});
					}
					desriptionLayout.addComponent(more);
				}
				mainLayout.addComponent(desriptionLayout);
				mainLayout.setComponentAlignment(desriptionLayout, Alignment.MIDDLE_CENTER);

				textArea = new TextArea();
				{
					textArea.setSizeFull();
					textArea.setVisible(false);
					textArea.setValue("Error view has been called but no error has been set.  This should not happen");
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
	protected void beforeInboundNavigation(@Parameter("error") Throwable error) {
		for (Window w : UI.getCurrent().getWindows()) {
			w.close();
		}
		this.error = error;
		textArea.setReadOnly(false);
		textArea.setValue(StackTraceUtil.getStackTrace(error));
		textArea.setReadOnly(true);
	}
}
