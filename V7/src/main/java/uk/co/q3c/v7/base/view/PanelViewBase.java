package uk.co.q3c.v7.base.view;

import com.google.inject.Inject;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;

public class PanelViewBase extends ViewBase<Panel>  {
    @Inject
    protected PanelViewBase() {
        super();
        setRootComponent(new Panel());
    }
    
    public Component getContent() {
		return getRootComponent().getContent();
	}
    
    public void setContent(Component content) {
		getRootComponent().setContent(content);
	}

}
