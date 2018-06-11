package uk.q3c.krail.core.view;

import org.jsoup.nodes.Element;

import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.declarative.DesignContext;

public abstract class AbstractOrderedView<T extends AbstractOrderedLayout>
		extends LayoutViewBase<T> {

	public AbstractOrderedView() {
		super();
	}

	public AbstractOrderedView(T layout) {
		this();
		setRootComponent(layout);
	}

	public void addComponent(Component c) {
		getLayout().addComponent(c);
	}

	public void addComponentAsFirst(Component c) {
		getLayout().addComponentAsFirst(c);
	}

	public void addComponent(Component c, int index) {
		getLayout().addComponent(c, index);
	}

	public void removeComponent(Component c) {
		getLayout().removeComponent(c);
	}

	public int getComponentCount() {
		return getLayout().getComponentCount();
	}

	public Alignment getComponentAlignment(Component childComponent) {
		return getLayout().getComponentAlignment(childComponent);
	}

	public void setSpacing(boolean spacing) {
		getLayout().setSpacing(spacing);
	}

	public boolean isSpacing() {
		return getLayout().isSpacing();
	}

	public void setExpandRatio(Component component, float ratio) {
		getLayout().setExpandRatio(component, ratio);
	}

	public float getExpandRatio(Component component) {
		return getLayout().getExpandRatio(component);
	}

	public Registration addLayoutClickListener(LayoutClickListener listener) {
		return getLayout().addLayoutClickListener(listener);
	}

	public void removeLayoutClickListener(LayoutClickListener listener) {
		getLayout().removeLayoutClickListener(listener);
	}

	public int getComponentIndex(Component component) {
		return getLayout().getComponentIndex(component);
	}

	public void setMargin(boolean enabled) {
		getLayout().setMargin(enabled);
	}

	public MarginInfo getMargin() {
		return getLayout().getMargin();
	}

	public void setMargin(MarginInfo marginInfo) {
		getLayout().setMargin(marginInfo);
	}

	public Alignment getDefaultComponentAlignment() {
		return getLayout().getDefaultComponentAlignment();
	}

	public void setDefaultComponentAlignment(Alignment defaultAlignment) {
		getLayout().setDefaultComponentAlignment(defaultAlignment);
	}

	public void readDesign(Element design, DesignContext designContext) {
		getLayout().readDesign(design, designContext);
	}

	public void writeDesign(Element design, DesignContext designContext) {
		getLayout().writeDesign(design, designContext);
	}
}
