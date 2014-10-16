package uk.co.q3c.v7.base.view;

import com.vaadin.ui.Layout;

public abstract class LayoutViewBase<T extends Layout> extends ViewBase<T> {

	public LayoutViewBase() {
		super();
	}
	
	public LayoutViewBase(T layout) {
		this();
		setRootComponent(layout);
	}
	
	public Layout getLayout() {
		return getRootComponent();
	}
	
}
