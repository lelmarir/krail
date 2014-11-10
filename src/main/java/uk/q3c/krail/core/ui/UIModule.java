package uk.q3c.krail.core.ui;

import uk.q3c.krail.core.data.KrailDefaultConverterFactory;
import uk.q3c.krail.core.guice.uiscope.UIScoped;
import uk.q3c.krail.core.navigate.DefaultNavigator;
import uk.q3c.krail.core.navigate.Navigator;
import uk.q3c.krail.i18n.I18NKey;
import uk.q3c.krail.i18n.LabelKey;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.vaadin.data.util.converter.ConverterFactory;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.UI;

public abstract class UIModule extends AbstractModule {

	@Override
	protected void configure() {
		bindApplicationTitle();
		MapBinder<String, UI> mapbinder = MapBinder.newMapBinder(binder(), String.class, UI.class);

		bind(WebBrowser.class).toProvider(BrowserProvider.class);

		bindUIProvider();
		addUIBindings(mapbinder);
		bindNavigator();
		bindConverterFactory();

	}

	private void bindApplicationTitle() {
		ApplicationTitle title = new ApplicationTitle(applicationTitleKey());
		bind(ApplicationTitle.class).toInstance(title);
	}

	/**
	 * override this method to provide the I18Nkey which defines your application title (which appears in your browser
	 * tab)
	 */
	protected I18NKey<?> applicationTitleKey() {
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
	 * @param mapbinder
	 */
	protected void addUIBindings(MapBinder<String, UI> mapbinder) {
		mapbinder.addBinding(BasicUI.class.getName()).to(BasicUI.class);
	}

}
