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

package uk.co.q3c.v7.base.view;

import com.google.inject.Inject;
import com.vaadin.ui.TextArea;

import uk.co.q3c.util.StackTraceUtil;

/**
 * @author David Sowerby 4 Aug 2013
 */

public class DefaultErrorView extends ViewBase<TextArea> implements ErrorView {

	private Throwable error;
	private TextArea textArea;

	@Inject
	protected DefaultErrorView() {
		super();

		textArea = new TextArea();
		textArea.setSizeFull();
		textArea.setValue("Error view has been called but no error has been set.  This should not happen");
		textArea.setReadOnly(true);

		setRootComponent(textArea);
	}

	public TextArea getTextArea() {
		return textArea;
	}

	public Throwable getError() {
		return error;
	}

	@Override
	public void beforeInboundNavigation(Throwable error) {
		this.error = error;
		textArea.setReadOnly(false);
		textArea.setValue(StackTraceUtil.getStackTrace(error));
		textArea.setReadOnly(true);
	}
}
