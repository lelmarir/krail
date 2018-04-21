package uk.q3c.krail.core.ui;

import uk.q3c.krail.core.guice.errors.ErrorHandler;
import uk.q3c.krail.core.guice.errors.KrailErrorHandler;
import uk.q3c.krail.core.guice.uiscope.UIScoped;
import uk.q3c.krail.core.navigate.DefaultInvalidURIExceptionHandler;
import uk.q3c.krail.core.navigate.DefaultNavigationCallbackHandler;
import uk.q3c.krail.core.navigate.DefaultNavigator;
import uk.q3c.krail.core.navigate.InvalidURIExceptionHandler;
import uk.q3c.krail.core.navigate.Navigator;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.UI;

public abstract class KrailUIModule extends AbstractModule {

	@Override
	protected void configure() {
		MapBinder<String, UI> uiBinder = MapBinder.newMapBinder(binder(),
				String.class, UI.class);

		bind(WebBrowser.class).toProvider(BrowserProvider.class);

		bindUIProvider();
		addUIBindings(uiBinder);
		bindNavigator();
		Multibinder<ErrorHandler> errorHandlersBinder = Multibinder
				.newSetBinder(binder(), ErrorHandler.class);
		bindNavigationErrorHandlers(errorHandlersBinder);
	}

	/**
	 * Override to bind your ScopedUIProvider implementation
	 */
	protected abstract void bindUIProvider();

	protected void bindNavigator() {
		bind(Navigator.class).to(DefaultNavigator.class).in(UIScoped.class);
		//TODO: cercare e iniettare dutti i field statici senza dover richidere esplicitamente
		requestStaticInjection(DefaultNavigationCallbackHandler.class);
	}

	/**
	 * Override with your UI bindings
	 * 
	 * @param uiBinder
	 */
	protected void addUIBindings(MapBinder<String, UI> uiBinder) {
		uiBinder.addBinding(BasicUI.class.getName()).to(BasicUI.class);
	}

	protected void bindNavigationErrorHandlers(Multibinder<ErrorHandler> errorHandlersBinder) {
		errorHandlersBinder.addBinding().to(InvalidURIExceptionHandler.class);
		bindInvalidURIExceptionHandler();
	}

	/**
	 * the {@link KrailErrorHandler} calls this handler in response to an
	 * attempt to navigate to an invalid URI. If you have defined your own
	 * ErrorHandler you may of course do something different
	 */
	protected void bindInvalidURIExceptionHandler() {
		bind(InvalidURIExceptionHandler.class).to(
				DefaultInvalidURIExceptionHandler.class);
	}
}
