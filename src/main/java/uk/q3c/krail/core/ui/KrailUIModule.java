package uk.q3c.krail.core.ui;

import uk.q3c.krail.core.data.KrailDefaultConverterFactory;
import uk.q3c.krail.core.guice.errors.ErrorHandler;
import uk.q3c.krail.core.guice.errors.KrailErrorHandler;
import uk.q3c.krail.core.guice.uiscope.UIScoped;
import uk.q3c.krail.core.navigate.DefaultInvalidURIExceptionHandler;
import uk.q3c.krail.core.navigate.DefaultNavigator;
import uk.q3c.krail.core.navigate.InvalidURIExceptionHandler;
import uk.q3c.krail.core.navigate.Navigator;
import uk.q3c.krail.i18n.I18NKey;
import uk.q3c.krail.i18n.LabelKey;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.vaadin.data.util.converter.ConverterFactory;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.UI;

public abstract class KrailUIModule extends AbstractModule {

	@Override
	protected void configure() {
		bindApplicationTitle();
		MapBinder<String, UI> uiBinder = MapBinder.newMapBinder(binder(),
				String.class, UI.class);

		bind(WebBrowser.class).toProvider(BrowserProvider.class);

		bindUIProvider();
		addUIBindings(uiBinder);
		bindNavigator();
		bindConverterFactory();
		Multibinder<ErrorHandler> errorHandlersBinder = Multibinder
				.newSetBinder(binder(), ErrorHandler.class);
		bindNavigationErrorHandlers(errorHandlersBinder);
	}

	private void bindApplicationTitle() {
		ApplicationTitle title = new ApplicationTitle(applicationTitleKey());
		bind(ApplicationTitle.class).toInstance(title);
	}

	/**
	 * override this method to provide the I18Nkey which defines your
	 * application title (which appears in your browser tab)
	 */
	protected I18NKey applicationTitleKey() {
		return LabelKey.Krail;
	}

	private void bindConverterFactory() {
		bind(ConverterFactory.class).to(KrailDefaultConverterFactory.class);
	}

	/**
	 * Override to bind your ScopedUIProvider implementation
	 */
	protected abstract void bindUIProvider();

	protected void bindNavigator() {
		bind(Navigator.class).to(DefaultNavigator.class).in(UIScoped.class);
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
