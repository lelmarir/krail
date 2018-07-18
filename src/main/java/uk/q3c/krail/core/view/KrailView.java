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

import com.vaadin.navigator.View;
import com.vaadin.ui.Component;

import uk.q3c.krail.core.navigate.Navigator;
import uk.q3c.krail.core.navigate.sitemap.NavigationState;


/**
 * A view is constructed by the {@link ViewFactory} from a Provider defined in the sitemap building process.  When
 * the view is selected for use, calls are made against {@link BeforeViewChangeListener}s added to {@link Navigator},
 * and
 * this interface, in the following order:
 * <ol>
 * <li>{@link BeforeViewChangeListener#beforeViewChange(KrailViewChangeEvent)}</li>
 * <li>{@link #init()}</li>
 * <li>{@link #beforeBuild}</li>
 * <li>{@link #buildView}</li>
 * <li>{@link #afterBuild}</li>
 * <li>{@link BeforeViewChangeListener#afterViewChange(KrailViewChangeEvent)}</li>
 * </ol>
 * where build refers to the creation of UI fields and components which populate the view.  Each method, except
 * readFromEnvironment(),
 * is passed a
 * {@link KrailViewChangeEvent}, which contains the current {@link NavigationState} so that, for example, parameter
 * information can be used to determine how the View is to be built or respond in some other way to URL parameters.
 */
public interface KrailView extends View {
	
    /**
	 * To enable implementations to implement this interface without descending
	 * from Component. If the implementation does descend from Component, just
	 * return 'this'. Throws a ViewBuildException if the root component has not
	 * been set
     *
     * @return
     * @throws ViewBuildException
     */
    public default Component getRootComponent() throws ViewBuildException {
        if (!(this instanceof Component)) {
            throw new IllegalStateException(
                    "View is not a Component. Override getViewComponent() to return the root view component");
        }
        return (Component) this;
    }
    
    @Override
    default Component getViewComponent() {
    	return getRootComponent();
    }

    /**
     * A name for the view, typically displayed in a title bar
     *
     * @return
     */
    public String getViewName();

}
