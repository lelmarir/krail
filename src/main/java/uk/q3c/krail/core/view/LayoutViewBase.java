package uk.q3c.krail.core.view;

import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;

public abstract class LayoutViewBase<T extends Layout> extends ViewBase<T> {

	public LayoutViewBase() {
		super();
	}
	
	public LayoutViewBase(T layout) {
		this();
		setRootComponent(layout);
	}
	
	public T getLayout() {
		return getRootComponent();
	}
	
	public void addComponent(Component c) {
		getLayout().addComponent(c);
	}
	
}
