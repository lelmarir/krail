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
package uk.q3c.krail.core.user.notify;

import java.io.Serializable;

import com.google.inject.Inject;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

public class DefaultUserNotifier implements UserNotifier, Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
	protected DefaultUserNotifier() {
    	;
    }

    @Override
	public void show(NotificationType type, String description) {
		Notification n = new Notification(description, convertType(type));
		show(n);
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

	private void show(Notification n) {
		n.setPosition(Position.TOP_CENTER);
		n.setDelayMsec(1000);
		n.show(UI.getCurrent().getPage());
	}
}
