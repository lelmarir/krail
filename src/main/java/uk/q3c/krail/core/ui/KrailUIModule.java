package uk.q3c.krail.core.ui;

import uk.q3c.krail.core.guice.errors.ErrorHandler;
import uk.q3c.krail.core.guice.errors.KrailErrorHandler;
import uk.q3c.krail.core.guice.uiscope.UIScoped;
import uk.q3c.krail.core.navigate.DefaultInvalidURIExceptionHandler;
import uk.q3c.krail.core.navigate.DefaultNavigationCallbackHandler;
import uk.q3c.krail.core.navigate.DefaultNavigator;
import uk.q3c.krail.core.navigate.InvalidURIExceptionHandler;
import uk.q3c.krail.core.navigate.Navigator;
import uk.q3c.krail.core.navigate.sitemap.annotations.ViewLayout;
import uk.q3c.krail.core.navigate.sitemap.impl.ParametersImpl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.vaadin.navigator.View;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.UI;

public abstract class KrailUIModule extends AbstractModule {

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.PARAMETER })
	@BindingAnnotation
	public @interface PublicViewDefaultLayout {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.PARAMETER })
	@BindingAnnotation
	public @interface PrivateViewDefaultLayout {
	}

	Class<? extends ViewLayout> publicViewDefaultLayout;
	Class<? extends ViewLayout> privateViewDefaultLayout;

	@Override
	protected void configure() {
		bind(WebBrowser.class).toProvider(BrowserProvider.class);

		bindUIProvider();
		bindNavigator();
		bindPublicViewDefaultLayout();
		bindPrivateViewDefaultLayout();
		Multibinder<ErrorHandler> errorHandlersBinder = Multibinder.newSetBinder(binder(), ErrorHandler.class);
		bindNavigationErrorHandlers(errorHandlersBinder);
	}

	/**
	 * Override to bind your ScopedUIProvider implementation
	 */
	protected abstract void bindUIProvider();

	protected void bindNavigator() {
		bind(Navigator.class).to(DefaultNavigator.class).in(UIScoped.class);
		//FIXME
		requestStaticInjection(ParametersImpl.class);
	}

	protected void bindPublicViewDefaultLayout() {
		Class<? extends ViewLayout> layout = getPublicViewDefaultLayout();
		if (layout != null) {
			bindConstant().annotatedWith(PublicViewDefaultLayout.class).to(layout);
		}
	}

	protected void bindPrivateViewDefaultLayout() {
		Class<? extends ViewLayout> layout = getPrivateViewDefaultLayout();
		if (layout != null) {
			bindConstant().annotatedWith(PrivateViewDefaultLayout.class).to(layout);
		}
	}

	protected void bindNavigationErrorHandlers(Multibinder<ErrorHandler> errorHandlersBinder) {
		errorHandlersBinder.addBinding().to(InvalidURIExceptionHandler.class);
		bindInvalidURIExceptionHandler();
	}

	/**
	 * the {@link KrailErrorHandler} calls this handler in response to an attempt to
	 * navigate to an invalid URI. If you have defined your own ErrorHandler you may
	 * of course do something different
	 */
	protected void bindInvalidURIExceptionHandler() {
		bind(InvalidURIExceptionHandler.class).to(DefaultInvalidURIExceptionHandler.class);
	}

	protected Class<? extends ViewLayout> getPublicViewDefaultLayout() {
		return publicViewDefaultLayout;
	}

	protected void setPublicViewDefaultLayout(Class<? extends ViewLayout> publicViewDefaultLayout) {
		this.publicViewDefaultLayout = publicViewDefaultLayout;
	}

	protected Class<? extends ViewLayout> getPrivateViewDefaultLayout() {
		return privateViewDefaultLayout;
	}

	protected void setPrivateViewDefaultLayout(Class<? extends ViewLayout> privateViewDefaultLayout) {
		this.privateViewDefaultLayout = privateViewDefaultLayout;
	}
}
