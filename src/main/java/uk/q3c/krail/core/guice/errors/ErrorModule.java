package uk.q3c.krail.core.guice.errors;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.vaadin.server.ErrorHandler;

public class ErrorModule extends AbstractModule {

	@Override
	protected void configure() {
		bindVaadinErrorHandler();
		
		Multibinder<ErrorHandler> errorHandlersBinder = Multibinder
				.newSetBinder(binder(), ErrorHandler.class);
		bindErrorHandlers(errorHandlersBinder);
	}

	private void bindVaadinErrorHandler() {
		bind(ErrorHandler.class).to(KrailErrorHandler.class);
	}
	
	/**
	 * Override this to add more {@link ErrorHandler}s
	 * 
	 * <code>errorHandlersBinder.addBinding().to(MyErrorHandler.class);</code>
	 */
	protected void bindErrorHandlers(Multibinder<ErrorHandler> errorHandlersBinder) {
		;
	}

}
