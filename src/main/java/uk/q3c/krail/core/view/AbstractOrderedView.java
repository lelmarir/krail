package uk.q3c.krail.core.view;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public abstract class AbstractOrderedView<T extends AbstractOrderedLayout>
		extends LayoutViewBase<T> {

	public AbstractOrderedView() {
		super();
	}

	public AbstractOrderedView(T layout) {
		this();
		setRootComponent(layout);
	}

	public void setComponentAlignment(Component childComponent,
			Alignment alignment) {
		getLayout().setComponentAlignment(childComponent, alignment);
	}

	public void setExpandRatio(Component component, float ratio) {
		getLayout().setExpandRatio(component, ratio);
	}
}
