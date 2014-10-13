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


//TODO: rewrite javadoc
/**
 * A view is constructed by the {@link ViewFactory} from a Provider defined in the sitemap building process.  When
 * the view is selected for use, calls are made against {@link V7ViewChangeListener}s added to {@link V7Navigator}, and
 * this interface, in the following order:
 * <ol>
 * <li>{@link V7ViewChangeListener#beforeViewChange(V7ViewChangeEvent)}</li>
 * <li>{@link #init()}</li>
 * <li>{@link #beforeBuild}</li>
 * <li>{@link #buildView}</li>
 * <li>{@link #afterBuild}</li>
 * <li>{@link V7ViewChangeListener#afterViewChange(V7ViewChangeEvent)}</li>
 * </ol>
 * where build refers to the creation of UI fields and components which populate the view.  Each method, except init(),
 * is passed a
 * {@link V7ViewChangeEvent}, which contains the current {@link NavigationState} so that, for example, parameter
 * information can be used to determine how the View is to be built or respond in some other way to URL parameters.
 */
public interface V7View {
    /**
     * To enable implementations to implement this interface without descending from Component. If the implementation
     * does descend from Component, just return 'this'.  Throws a ViewBuildException if the root component has not been
     * set
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
