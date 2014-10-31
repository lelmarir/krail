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
package uk.co.q3c.v7.base.user.notify;

import java.io.Serializable;
import uk.co.q3c.v7.i18n.DescriptionKey;
import uk.co.q3c.v7.i18n.MessageKey;
import uk.co.q3c.v7.i18n.Translate;

import com.google.inject.Inject;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

public class DefaultUserNotifier implements UserNotifier, Serializable {
	private static final long serialVersionUID = 1L;

	private final Translate translate;

	@Inject
	protected DefaultUserNotifier(Translate translate) {
		this.translate = translate;

	}

	@Override
	public void show(NotificationType type, MessageKey description,
			Object... arguments) {
		Notification n = new Notification(translate.from(
				description, arguments), convertType(type));
		n.show(UI.getCurrent().getPage());
	}

	@Override
	public void show(NotificationType type, DescriptionKey description) {
		Notification n = new Notification(translate.from(
				description), convertType(type));
		n.show(UI.getCurrent().getPage());
	}

	private Type convertType(NotificationType type) {
		switch (type) {
		case ERROR:
			return Type.ERROR_MESSAGE;
		case WARNING:
			return Type.WARNING_MESSAGE;
		case INFO:
			return Type.HUMANIZED_MESSAGE;
		default:
			return Type.ERROR_MESSAGE;
		}
	}
}
