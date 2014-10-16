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

import com.vaadin.ui.Component;

import uk.co.q3c.v7.base.navigate.NavigationState;
import uk.co.q3c.v7.base.navigate.V7Navigator;
import uk.co.q3c.v7.base.view.V7ViewChangeEvent.CancellableV7ViewChangeEvent;

//TODO: rewrite javadoc
/**
 * A view is constructed by the {@link ViewFactory} from a Provider defined in
 * the sitemap building process. When the view is selected for use, calls are
 * made against {@link V7ViewChangeListener}s added to {@link V7Navigator}, and
 * {@link NavigationAwareView} inteface, in the following order:
 * <ol>
 * <li>
 * {@link V7ViewChangeListener#beforeViewChange(CancellableV7ViewChangeEvent)}</li>
 * <li>{@link NavigationAwareView#onOutboundNavigation(CancellableV7ViewChangeEvent)} (on current
 * view)</li>
 * <li>{@link NavigationAwareView#beforeInboundNavigation(CancellableV7ViewChangeEvent)} (on target
 * view)</li>
 * <li>{@link NavigationAwareView#afterInboundNavigation(V7ViewChangeEvent)} (on target view)</li>
 * <li>{@link V7ViewChangeListener#afterViewChange(V7ViewChangeEvent)}</li>
 * </ol>
 * Each method, is passed a {@link V7ViewChangeEvent}, which contains the
 * current {@link NavigationState} so that, for example, parameter information
 * can be used to determine how the View is to be built or respond in some other
 * way to URL parameters. Up to {@link #beforeInboundNavigation} is passed a
 * {@link CancellableV7ViewChangeEvent} that could be used to cancel the
 * navigation using {@link CancellableV7ViewChangeEvent#cancel()}
 */
public interface V7View {

	/**
	 * Implementing this class the view will be notified of inbound and outbound
	 * navigations
	 */
	public static interface NavigationAwareView extends V7View {

		/**
		 * Will be notified before navigating into this view and can cancel the
		 * naavigation using event.cancel()
		 * 
		 * <b>The view is not yet attached to the UI</b>
		 */
		void beforeInboundNavigation(CancellableV7ViewChangeEvent event);

		/**
		 * Will ne notified after the viev has navigated to and attached to the
		 * UI
		 */
		void afterInboundNavigation(V7ViewChangeEvent event);

		/**
		 * Will be notified before navigating away from this view and can cancel
		 * the naavigation using event.cancel()
		 */
		void onOutboundNavigation(CancellableV7ViewChangeEvent event);

	}

	/**
	 * To enable implementations to implement this interface without descending
	 * from Component. If the implementation does descend from Component, just
	 * return 'this'. Throws a ViewBuildException if the root component has not
	 * been set
	 * 
	 * @return
	 */
	public Component getRootComponent();

	/**
	 * A name for the view, typically displayed in a title bar
	 * 
	 * @return
	 */
	public String viewName();
}
