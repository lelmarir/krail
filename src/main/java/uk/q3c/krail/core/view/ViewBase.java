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
package uk.q3c.krail.core.view;

import com.google.inject.Inject;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.q3c.util.ID;

import java.util.Optional;

/**
 * Provides default View behaviour suitable for most view implementations
 */
public abstract class ViewBase<R extends Component> implements KrailView {

    private static Logger log = LoggerFactory.getLogger(ViewBase.class);
    private R rootComponent;

    @Inject
    protected ViewBase() {
        super();
    }

    /**
     * You only need to override / implement this method if you are using TestBench, or another testing tool which
     * looks
     * for debug
     * ids. If you do override it to add your own subclass ids, make sure you call super
     */
    protected void setIds() {
        getRootComponent().setId(ID.getId(Optional.empty(), this, getRootComponent()));
    }

    @Override
    public R getRootComponent() {
        if (rootComponent == null) {
            throw new ViewBuildException("Root component cannot be null in " + getClass().getName() + ". Has your " +
                    "buildView() method called " +
                    "setRootComponent()?");
        }
        return rootComponent;
    }

    public void setRootComponent(R rootComponent) {
        this.rootComponent = rootComponent;
    }
    
    @Override
    public String getViewName() {
        return getClass().getSimpleName();
    }
    
    @Override
    public Component getHeaderComponent() {
    	return new Label(getViewName());
    }
    
    public UI getUI() {
    	if(!getRootComponent().isAttached()) {
    		throw new IllegalStateException("can't get the UI before the component is attached");
    	}
    	return getRootComponent().getUI();
    }

}
