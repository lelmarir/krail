package uk.q3c.krail.core.ui;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.jsoup.nodes.Element;

import com.vaadin.server.ClientMethodInvocation;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.Extension;
import com.vaadin.server.Resource;
import com.vaadin.server.ServerRpcManager;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.shared.Registration;
import com.vaadin.shared.communication.SharedState;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.SingleComponentContainer;
import com.vaadin.ui.UI;
import com.vaadin.ui.declarative.DesignContext;

import elemental.json.JsonObject;

public class SingleComponentContainerWrapper implements SingleComponentContainer {

	private final ComponentContainer container;

	public SingleComponentContainerWrapper(ComponentContainer container) {
		super();
		this.container = container;
	}

	/**
	 * @return
	 * @see com.vaadin.ui.HasComponents#iterator()
	 */
	public Iterator<Component> iterator() {
		return container.iterator();
	}

	/**
	 * @param c
	 * @see com.vaadin.ui.ComponentContainer#addComponent(com.vaadin.ui.Component)
	 */
	public void addComponent(Component c) {
		container.addComponent(c);
	}

	/**
	 * @param components
	 * @see com.vaadin.ui.ComponentContainer#addComponents(com.vaadin.ui.Component[])
	 */
	public void addComponents(Component... components) {
		container.addComponents(components);
	}

	/**
	 * @return
	 * @see com.vaadin.shared.Connector#getConnectorId()
	 */
	public String getConnectorId() {
		return container.getConnectorId();
	}

	/**
	 * @param c
	 * @see com.vaadin.ui.ComponentContainer#removeComponent(com.vaadin.ui.Component)
	 */
	public void removeComponent(Component c) {
		container.removeComponent(c);
	}

	/**
	 * 
	 * @see com.vaadin.ui.ComponentContainer#removeAllComponents()
	 */
	public void removeAllComponents() {
		container.removeAllComponents();
	}

	/**
	 * @param listener
	 * @return
	 * @see com.vaadin.ui.HasComponents.ComponentAttachDetachNotifier#addComponentAttachListener(com.vaadin.ui.HasComponents.ComponentAttachListener)
	 */
	public Registration addComponentAttachListener(ComponentAttachListener listener) {
		return container.addComponentAttachListener(listener);
	}

	/**
	 * @param oldComponent
	 * @param newComponent
	 * @see com.vaadin.ui.ComponentContainer#replaceComponent(com.vaadin.ui.Component,
	 *      com.vaadin.ui.Component)
	 */
	public void replaceComponent(Component oldComponent, Component newComponent) {
		container.replaceComponent(oldComponent, newComponent);
	}

	/**
	 * @param listener
	 * @deprecated
	 * @see com.vaadin.ui.HasComponents.ComponentAttachDetachNotifier#removeComponentAttachListener(com.vaadin.ui.HasComponents.ComponentAttachListener)
	 */
	public void removeComponentAttachListener(ComponentAttachListener listener) {
		container.removeComponentAttachListener(listener);
	}

	/**
	 * @return
	 * @see com.vaadin.ui.Component#getStyleName()
	 */
	public String getStyleName() {
		return container.getStyleName();
	}

	/**
	 * @return
	 * @deprecated
	 * @see com.vaadin.ui.ComponentContainer#getComponentIterator()
	 */
	public Iterator<Component> getComponentIterator() {
		return container.getComponentIterator();
	}

	/**
	 * @param listener
	 * @return
	 * @see com.vaadin.ui.HasComponents.ComponentAttachDetachNotifier#addComponentDetachListener(com.vaadin.ui.HasComponents.ComponentDetachListener)
	 */
	public Registration addComponentDetachListener(ComponentDetachListener listener) {
		return container.addComponentDetachListener(listener);
	}

	/**
	 * @param listener
	 * @deprecated
	 * @see com.vaadin.ui.HasComponents.ComponentAttachDetachNotifier#removeComponentDetachListener(com.vaadin.ui.HasComponents.ComponentDetachListener)
	 */
	public void removeComponentDetachListener(ComponentDetachListener listener) {
		container.removeComponentDetachListener(listener);
	}

	/**
	 * @return
	 * @see com.vaadin.ui.ComponentContainer#getComponentCount()
	 */
	public int getComponentCount() {
		return container.getComponentCount();
	}

	/**
	 * @param listener
	 * @return
	 * @see com.vaadin.server.ClientConnector#addAttachListener(com.vaadin.server.ClientConnector.AttachListener)
	 */
	public Registration addAttachListener(AttachListener listener) {
		return container.addAttachListener(listener);
	}

	/**
	 * @param source
	 * @see com.vaadin.ui.ComponentContainer#moveComponentsFrom(com.vaadin.ui.ComponentContainer)
	 */
	public void moveComponentsFrom(ComponentContainer source) {
		container.moveComponentsFrom(source);
	}

	/**
	 * @param style
	 * @see com.vaadin.ui.Component#setStyleName(java.lang.String)
	 */
	public void setStyleName(String style) {
		container.setStyleName(style);
	}

	/**
	 * @param listener
	 * @deprecated
	 * @see com.vaadin.server.ClientConnector#removeAttachListener(com.vaadin.server.ClientConnector.AttachListener)
	 */
	public void removeAttachListener(AttachListener listener) {
		container.removeAttachListener(listener);
	}

	/**
	 * @param listener
	 * @return
	 * @see com.vaadin.server.ClientConnector#addDetachListener(com.vaadin.server.ClientConnector.DetachListener)
	 */
	public Registration addDetachListener(DetachListener listener) {
		return container.addDetachListener(listener);
	}

	/**
	 * @param listener
	 * @deprecated
	 * @see com.vaadin.server.ClientConnector#removeDetachListener(com.vaadin.server.ClientConnector.DetachListener)
	 */
	public void removeDetachListener(DetachListener listener) {
		container.removeDetachListener(listener);
	}

	/**
	 * @return
	 * @see com.vaadin.server.Sizeable#getWidth()
	 */
	public float getWidth() {
		return container.getWidth();
	}

	/**
	 * @return
	 * @see com.vaadin.server.ClientConnector#retrievePendingRpcCalls()
	 */
	public List<ClientMethodInvocation> retrievePendingRpcCalls() {
		return container.retrievePendingRpcCalls();
	}

	/**
	 * @return
	 * @see com.vaadin.server.Sizeable#getHeight()
	 */
	public float getHeight() {
		return container.getHeight();
	}

	/**
	 * @return
	 * @see com.vaadin.server.ClientConnector#isConnectorEnabled()
	 */
	public boolean isConnectorEnabled() {
		return container.isConnectorEnabled();
	}

	/**
	 * @return
	 * @see com.vaadin.server.Sizeable#getWidthUnits()
	 */
	public Unit getWidthUnits() {
		return container.getWidthUnits();
	}

	/**
	 * @return
	 * @see com.vaadin.server.Sizeable#getHeightUnits()
	 */
	public Unit getHeightUnits() {
		return container.getHeightUnits();
	}

	/**
	 * @param style
	 * @see com.vaadin.ui.Component#addStyleName(java.lang.String)
	 */
	public void addStyleName(String style) {
		container.addStyleName(style);
	}

	/**
	 * @return
	 * @see com.vaadin.server.ClientConnector#getStateType()
	 */
	public Class<? extends SharedState> getStateType() {
		return container.getStateType();
	}

	/**
	 * @param height
	 * @see com.vaadin.server.Sizeable#setHeight(java.lang.String)
	 */
	public void setHeight(String height) {
		container.setHeight(height);
	}

	/**
	 * @deprecated
	 * @see com.vaadin.server.ClientConnector#requestRepaint()
	 */
	public void requestRepaint() {
		container.requestRepaint();
	}

	/**
	 * 
	 * @see com.vaadin.server.ClientConnector#markAsDirty()
	 */
	public void markAsDirty() {
		container.markAsDirty();
	}

	/**
	 * @param width
	 * @param unit
	 * @see com.vaadin.server.Sizeable#setWidth(float,
	 *      com.vaadin.server.Sizeable.Unit)
	 */
	public void setWidth(float width, Unit unit) {
		container.setWidth(width, unit);
	}

	/**
	 * @deprecated
	 * @see com.vaadin.server.ClientConnector#requestRepaintAll()
	 */
	public void requestRepaintAll() {
		container.requestRepaintAll();
	}

	/**
	 * 
	 * @see com.vaadin.server.ClientConnector#markAsDirtyRecursive()
	 */
	public void markAsDirtyRecursive() {
		container.markAsDirtyRecursive();
	}

	/**
	 * @param height
	 * @param unit
	 * @see com.vaadin.server.Sizeable#setHeight(float,
	 *      com.vaadin.server.Sizeable.Unit)
	 */
	public void setHeight(float height, Unit unit) {
		container.setHeight(height, unit);
	}

	/**
	 * @return
	 * @see com.vaadin.server.ClientConnector#isAttached()
	 */
	public boolean isAttached() {
		return container.isAttached();
	}

	/**
	 * @param width
	 * @see com.vaadin.server.Sizeable#setWidth(java.lang.String)
	 */
	public void setWidth(String width) {
		container.setWidth(width);
	}

	/**
	 * @param style
	 * @see com.vaadin.ui.Component#removeStyleName(java.lang.String)
	 */
	public void removeStyleName(String style) {
		container.removeStyleName(style);
	}

	/**
	 * 
	 * @see com.vaadin.server.Sizeable#setSizeFull()
	 */
	public void setSizeFull() {
		container.setSizeFull();
	}

	/**
	 * 
	 * @see com.vaadin.server.Sizeable#setSizeUndefined()
	 */
	public void setSizeUndefined() {
		container.setSizeUndefined();
	}

	/**
	 * 
	 * @see com.vaadin.server.Sizeable#setWidthUndefined()
	 */
	public void setWidthUndefined() {
		container.setWidthUndefined();
	}

	/**
	 * 
	 * @see com.vaadin.server.ClientConnector#detach()
	 */
	public void detach() {
		container.detach();
	}

	/**
	 * 
	 * @see com.vaadin.server.Sizeable#setHeightUndefined()
	 */
	public void setHeightUndefined() {
		container.setHeightUndefined();
	}

	/**
	 * @return
	 * @see com.vaadin.server.ClientConnector#getExtensions()
	 */
	public Collection<Extension> getExtensions() {
		return container.getExtensions();
	}

	/**
	 * @param extension
	 * @see com.vaadin.server.ClientConnector#removeExtension(com.vaadin.server.Extension)
	 */
	public void removeExtension(Extension extension) {
		container.removeExtension(extension);
	}

	/**
	 * @return
	 * @see com.vaadin.ui.Component#getPrimaryStyleName()
	 */
	public String getPrimaryStyleName() {
		return container.getPrimaryStyleName();
	}

	/**
	 * @param style
	 * @see com.vaadin.ui.Component#setPrimaryStyleName(java.lang.String)
	 */
	public void setPrimaryStyleName(String style) {
		container.setPrimaryStyleName(style);
	}

	/**
	 * @param initial
	 * @see com.vaadin.server.ClientConnector#beforeClientResponse(boolean)
	 */
	public void beforeClientResponse(boolean initial) {
		container.beforeClientResponse(initial);
	}

	/**
	 * @return
	 * @see com.vaadin.server.ClientConnector#encodeState()
	 */
	public JsonObject encodeState() {
		return container.encodeState();
	}

	/**
	 * @return
	 * @see com.vaadin.ui.Component#isEnabled()
	 */
	public boolean isEnabled() {
		return container.isEnabled();
	}

	/**
	 * @param request
	 * @param response
	 * @param path
	 * @return
	 * @throws IOException
	 * @see com.vaadin.server.ClientConnector#handleConnectorRequest(com.vaadin.server.VaadinRequest,
	 *      com.vaadin.server.VaadinResponse, java.lang.String)
	 */
	public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response, String path)
			throws IOException {
		return container.handleConnectorRequest(request, response, path);
	}

	/**
	 * @param enabled
	 * @see com.vaadin.ui.Component#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		container.setEnabled(enabled);
	}

	/**
	 * @param rpcInterfaceName
	 * @return
	 * @see com.vaadin.server.ClientConnector#getRpcManager(java.lang.String)
	 */
	public ServerRpcManager<?> getRpcManager(String rpcInterfaceName) {
		return container.getRpcManager(rpcInterfaceName);
	}

	/**
	 * @return
	 * @see com.vaadin.ui.Component#isVisible()
	 */
	public boolean isVisible() {
		return container.isVisible();
	}

	/**
	 * @return
	 * @see com.vaadin.server.ClientConnector#getErrorHandler()
	 */
	public ErrorHandler getErrorHandler() {
		return container.getErrorHandler();
	}

	/**
	 * @param errorHandler
	 * @see com.vaadin.server.ClientConnector#setErrorHandler(com.vaadin.server.ErrorHandler)
	 */
	public void setErrorHandler(ErrorHandler errorHandler) {
		container.setErrorHandler(errorHandler);
	}

	/**
	 * @param visible
	 * @see com.vaadin.ui.Component#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		container.setVisible(visible);
	}

	/**
	 * @param parent
	 * @see com.vaadin.ui.Component#setParent(com.vaadin.ui.HasComponents)
	 */
	public void setParent(HasComponents parent) {
		container.setParent(parent);
	}

	/**
	 * @return
	 * @see com.vaadin.ui.Component#getParent()
	 */
	public HasComponents getParent() {
		return container.getParent();
	}

	/**
	 * @return
	 * @see com.vaadin.ui.Component#getCaption()
	 */
	public String getCaption() {
		return container.getCaption();
	}

	/**
	 * @param caption
	 * @see com.vaadin.ui.Component#setCaption(java.lang.String)
	 */
	public void setCaption(String caption) {
		container.setCaption(caption);
	}

	/**
	 * @return
	 * @see com.vaadin.ui.Component#getIcon()
	 */
	public Resource getIcon() {
		return container.getIcon();
	}

	/**
	 * @param icon
	 * @see com.vaadin.ui.Component#setIcon(com.vaadin.server.Resource)
	 */
	public void setIcon(Resource icon) {
		container.setIcon(icon);
	}

	/**
	 * @return
	 * @see com.vaadin.ui.Component#getUI()
	 */
	public UI getUI() {
		return container.getUI();
	}

	/**
	 * 
	 * @see com.vaadin.ui.Component#attach()
	 */
	public void attach() {
		container.attach();
	}

	/**
	 * @return
	 * @see com.vaadin.ui.Component#getLocale()
	 */
	public Locale getLocale() {
		return container.getLocale();
	}

	/**
	 * @param id
	 * @see com.vaadin.ui.Component#setId(java.lang.String)
	 */
	public void setId(String id) {
		container.setId(id);
	}

	/**
	 * @return
	 * @see com.vaadin.ui.Component#getId()
	 */
	public String getId() {
		return container.getId();
	}

	/**
	 * @return
	 * @see com.vaadin.ui.Component#getDescription()
	 */
	public String getDescription() {
		return container.getDescription();
	}

	/**
	 * @param design
	 * @param designContext
	 * @see com.vaadin.ui.Component#readDesign(org.jsoup.nodes.Element,
	 *      com.vaadin.ui.declarative.DesignContext)
	 */
	public void readDesign(Element design, DesignContext designContext) {
		container.readDesign(design, designContext);
	}

	/**
	 * @param design
	 * @param designContext
	 * @see com.vaadin.ui.Component#writeDesign(org.jsoup.nodes.Element,
	 *      com.vaadin.ui.declarative.DesignContext)
	 */
	public void writeDesign(Element design, DesignContext designContext) {
		container.writeDesign(design, designContext);
	}

	/**
	 * @param listener
	 * @return
	 * @see com.vaadin.ui.Component#addListener(com.vaadin.ui.Component.Listener)
	 */
	public Registration addListener(Listener listener) {
		return container.addListener(listener);
	}

	/**
	 * @param listener
	 * @deprecated
	 * @see com.vaadin.ui.Component#removeListener(com.vaadin.ui.Component.Listener)
	 */
	public void removeListener(Listener listener) {
		container.removeListener(listener);
	}

	@Override
	public Component getContent() {
		if (iterator().hasNext()) {
			return iterator().next();
		} else {
			return null;
		}
	}

	@Override
	public void setContent(Component content) {
		removeAllComponents();
		addComponent(content);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof SingleComponentContainerWrapper) {
			return Objects.equals(((SingleComponentContainerWrapper) obj).container, container);
		}else {
			return Objects.equals(obj, container);
		}
	}

}
