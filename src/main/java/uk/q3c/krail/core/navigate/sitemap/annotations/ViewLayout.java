/*
 * Copyright 2000-2018 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package uk.q3c.krail.core.navigate.sitemap.annotations;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.SingleComponentContainer;

import uk.q3c.krail.core.view.KrailView;

/**
 * Implementations of this interface represent a parent for a navigation target
 * components via the {@link Route#layout()} parameter.
 *
 * @author Vaadin Ltd
 * @since 1.0
 */
public interface ViewLayout extends Component {

	/**
	 * Shows the content of the layout which is the router target component
	 * annotated with a {@link Route @Route}.
	 * <p>
	 * <strong>Note</strong> implementors should not care about old {@code @Route}
	 * content, because {@link Router} automatically removes it before calling the
	 * method.
	 * </p>
	 *
	 * @param content
	 *            the content component or {@code null} if the layout content is to
	 *            be cleared.
	 */
	default void setViewContent(Component content) {
		if (content != null) {
			if(this instanceof SingleComponentContainer)
			{
				((SingleComponentContainer)this).setContent(content);
			}else if(this instanceof ComponentContainer) {
				((ComponentContainer)this).removeAllComponents();
				((ComponentContainer)this).addComponent(content);
			}else {
				throw new IllegalStateException("you must implement setView()");
			}
			
		}
	}

}