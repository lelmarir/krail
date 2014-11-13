package uk.q3c.krail.core.view;

import com.vaadin.ui.Layout;

public abstract class LayoutViewBase<T extends Layout> extends ViewBase<T> {
	
	public LayoutViewBase(T layout) {
		super();
		setRootComponent(layout);
	}
	
	public T getLayout() {
		return getRootComponent();
	}
	
}
