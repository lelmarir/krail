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

import com.vaadin.ui.Component;

import uk.q3c.krail.core.navigate.KrailNavigator;
import uk.q3c.krail.core.navigate.sitemap.NavigationState;
import uk.q3c.krail.core.view.KrailViewChangeEvent.CancellableKrailViewChangeEvent;

//TODO: rewrite javadoc
/**
 * A view is constructed by the {@link ViewFactory} from a Provider defined in
 * the sitemap building process. When the view is selected for use, calls are
 * made against {@link KrailViewChangeListener}s added to {@link KrailNavigator}, and
 * {@link NavigationAwareView} inteface, in the following order:
 * <ol>
 * <li>
 * {@link KrailViewChangeListener#beforeViewChange(CancellableKrailViewChangeEvent)}</li>
 * <li>{@link NavigationAwareView#onOutboundNavigation(CancellableKrailViewChangeEvent)} (on current
 * view)</li>
 * <li>{@link NavigationAwareView#beforeInboundNavigation(CancellableKrailViewChangeEvent)} (on target
 * view)</li>
 * <li>{@link NavigationAwareView#afterInboundNavigation(KrailViewChangeEvent)} (on target view)</li>
 * <li>{@link KrailViewChangeListener#afterViewChange(KrailViewChangeEvent)}</li>
 * </ol>
 * Each method, is passed a {@link KrailViewChangeEvent}, which contains the
 * current {@link NavigationState} so that, for example, parameter information
 * can be used to determine how the View is to be built or respond in some other
 * way to URL parameters. Up to {@link #beforeInboundNavigation} is passed a
 * {@link CancellableKrailViewChangeEvent} that could be used to cancel the
 * navigation using {@link CancellableKrailViewChangeEvent#cancel()}
 */
public interface KrailView {

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
