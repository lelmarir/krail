package uk.q3c.krail.core.view;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.jsoup.nodes.Element;

import com.vaadin.server.ClientConnector.AttachListener;
import com.vaadin.server.ClientConnector.DetachListener;
import com.vaadin.server.ClientMethodInvocation;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.Extension;
import com.vaadin.server.Resource;
import com.vaadin.server.ServerRpcManager;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.shared.Registration;
import com.vaadin.shared.communication.SharedState;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Listener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HasComponents.ComponentAttachListener;
import com.vaadin.ui.HasComponents.ComponentDetachListener;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import com.vaadin.ui.declarative.DesignContext;

import elemental.json.JsonObject;

public abstract class LayoutViewBase<T extends Layout> extends ViewBase<T> {

	private boolean layoutBuilt = false;

	public LayoutViewBase() {
		super();
	}

	public LayoutViewBase(T layout) {
		this();
		setRootComponent(layout);
	}

	@Override
	public T getRootComponent() {
		if(!layoutBuilt) {
			layoutBuilt = true;
			build();
		}
		return super.getRootComponent();
	}
	
	@Override
	public void setRootComponent(T rootComponent) {
		this.layoutBuilt = false;
		super.setRootComponent(rootComponent);
	}
	
	public T getLayout() {
		return getRootComponent();
	}

	// delegates

	public Iterator<Component> iterator() {
		return getLayout().iterator();
	}

	public void addComponent(Component c) {
		getLayout().addComponent(c);
	}

	public void addComponents(Component... components) {
		getLayout().addComponents(components);
	}

	public String getConnectorId() {
		return getLayout().getConnectorId();
	}

	public void removeComponent(Component c) {
		getLayout().removeComponent(c);
	}

	public void removeAllComponents() {
		getLayout().removeAllComponents();
	}

	public Registration addComponentAttachListener(ComponentAttachListener listener) {
		return getLayout().addComponentAttachListener(listener);
	}

	public void replaceComponent(Component oldComponent, Component newComponent) {
		getLayout().replaceComponent(oldComponent, newComponent);
	}

	public void removeComponentAttachListener(ComponentAttachListener listener) {
		getLayout().removeComponentAttachListener(listener);
	}

	public String getStyleName() {
		return getLayout().getStyleName();
	}

	public Iterator<Component> getComponentIterator() {
		return getLayout().getComponentIterator();
	}

	public Registration addComponentDetachListener(ComponentDetachListener listener) {
		return getLayout().addComponentDetachListener(listener);
	}

	public void removeComponentDetachListener(ComponentDetachListener listener) {
		getLayout().removeComponentDetachListener(listener);
	}

	public int getComponentCount() {
		return getLayout().getComponentCount();
	}

	public Registration addAttachListener(AttachListener listener) {
		return getLayout().addAttachListener(listener);
	}

	public void moveComponentsFrom(ComponentContainer source) {
		getLayout().moveComponentsFrom(source);
	}

	public void setStyleName(String style) {
		getLayout().setStyleName(style);
	}

	public void removeAttachListener(AttachListener listener) {
		getLayout().removeAttachListener(listener);
	}

	public Registration addDetachListener(DetachListener listener) {
		return getLayout().addDetachListener(listener);
	}

	public void removeDetachListener(DetachListener listener) {
		getLayout().removeDetachListener(listener);
	}

	public float getWidth() {
		return getLayout().getWidth();
	}

	public List<ClientMethodInvocation> retrievePendingRpcCalls() {
		return getLayout().retrievePendingRpcCalls();
	}

	public float getHeight() {
		return getLayout().getHeight();
	}

	public boolean isConnectorEnabled() {
		return getLayout().isConnectorEnabled();
	}

	public Unit getWidthUnits() {
		return getLayout().getWidthUnits();
	}

	public Unit getHeightUnits() {
		return getLayout().getHeightUnits();
	}

	public void addStyleName(String style) {
		getLayout().addStyleName(style);
	}

	public Class<? extends SharedState> getStateType() {
		return getLayout().getStateType();
	}

	public void setHeight(String height) {
		getLayout().setHeight(height);
	}

	public void requestRepaint() {
		getLayout().requestRepaint();
	}

	public void markAsDirty() {
		getLayout().markAsDirty();
	}

	public void setWidth(float width, Unit unit) {
		getLayout().setWidth(width, unit);
	}

	public void requestRepaintAll() {
		getLayout().requestRepaintAll();
	}

	public void markAsDirtyRecursive() {
		getLayout().markAsDirtyRecursive();
	}

	public void setHeight(float height, Unit unit) {
		getLayout().setHeight(height, unit);
	}

	public boolean isAttached() {
		return getLayout().isAttached();
	}

	public void setWidth(String width) {
		getLayout().setWidth(width);
	}

	public void removeStyleName(String style) {
		getLayout().removeStyleName(style);
	}

	public void setSizeFull() {
		getLayout().setSizeFull();
	}

	public void setSizeUndefined() {
		getLayout().setSizeUndefined();
	}

	public void setWidthUndefined() {
		getLayout().setWidthUndefined();
	}

	public void detach() {
		getLayout().detach();
	}

	public void setHeightUndefined() {
		getLayout().setHeightUndefined();
	}

	public Collection<Extension> getExtensions() {
		return getLayout().getExtensions();
	}

	public void removeExtension(Extension extension) {
		getLayout().removeExtension(extension);
	}

	public String getPrimaryStyleName() {
		return getLayout().getPrimaryStyleName();
	}

	public void setPrimaryStyleName(String style) {
		getLayout().setPrimaryStyleName(style);
	}

	public void beforeClientResponse(boolean initial) {
		getLayout().beforeClientResponse(initial);
	}

	public JsonObject encodeState() {
		return getLayout().encodeState();
	}

	public boolean isEnabled() {
		return getLayout().isEnabled();
	}

	public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response, String path)
			throws IOException {
		return getLayout().handleConnectorRequest(request, response, path);
	}

	public void setEnabled(boolean enabled) {
		getLayout().setEnabled(enabled);
	}

	public ServerRpcManager<?> getRpcManager(String rpcInterfaceName) {
		return getLayout().getRpcManager(rpcInterfaceName);
	}

	public boolean isVisible() {
		return getLayout().isVisible();
	}

	public ErrorHandler getErrorHandler() {
		return getLayout().getErrorHandler();
	}

	public void setErrorHandler(ErrorHandler errorHandler) {
		getLayout().setErrorHandler(errorHandler);
	}

	public void setVisible(boolean visible) {
		getLayout().setVisible(visible);
	}

	public void setParent(HasComponents parent) {
		getLayout().setParent(parent);
	}

	public HasComponents getParent() {
		return getLayout().getParent();
	}

	public String getCaption() {
		return getLayout().getCaption();
	}

	public void setCaption(String caption) {
		getLayout().setCaption(caption);
	}

	public Resource getIcon() {
		return getLayout().getIcon();
	}

	public void setIcon(Resource icon) {
		getLayout().setIcon(icon);
	}

	public UI getUI() {
		return getLayout().getUI();
	}

	public void attach() {
		getLayout().attach();
	}

	public Locale getLocale() {
		return getLayout().getLocale();
	}

	public void setId(String id) {
		getLayout().setId(id);
	}

	public String getId() {
		return getLayout().getId();
	}

	public String getDescription() {
		return getLayout().getDescription();
	}

	public void readDesign(Element design, DesignContext designContext) {
		getLayout().readDesign(design, designContext);
	}

	public void writeDesign(Element design, DesignContext designContext) {
		getLayout().writeDesign(design, designContext);
	}

	public Registration addListener(Listener listener) {
		return getLayout().addListener(listener);
	}

	public void removeListener(Listener listener) {
		getLayout().removeListener(listener);
	}

}
