package uk.q3c.krail.core.guice.errors;

import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class ErrorModule extends AbstractModule {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ErrorModule.class);

	private final Reflections basePackageReflections;

	public ErrorModule(String basePackage) {
		this(new Reflections(basePackage));
	}

	public ErrorModule(Reflections basePackageReflections) {
		this.basePackageReflections = basePackageReflections;
	}

	@Override
	protected void configure() {
		bindVaadinErrorHandler();

		Multibinder<ErrorHandler> errorHandlersBinder = Multibinder
				.newSetBinder(binder(), ErrorHandler.class);

		bindAnnotatedErrorHandlers(errorHandlersBinder);
		bindErrorHandlers(errorHandlersBinder);
	}

	private void bindVaadinErrorHandler() {
		bind(com.vaadin.server.ErrorHandler.class).to(KrailErrorHandler.class);
		requestStaticInjection(KrailErrorHandler.class);
	}

	protected void bindAnnotatedErrorHandlers(
			Multibinder<ErrorHandler> errorHandlersBinder) {
		LOGGER.info("scanning {} for HandleErrors annotations", basePackageReflections.getConfiguration().getUrls());
		//TODO: usare sempre lo stesso Reflections in tutti i moduli, non ricrearlo sempre
		

		// find the View annotations
		Set<Class<?>> typesWithView = basePackageReflections
				.getTypesAnnotatedWith(HandleErrors.class);
		LOGGER.info("{} ErrorHandlers with HandleErrors annotation found",
				typesWithView.size());

		for (Class<?> type : typesWithView) {
			if (ErrorHandler.class.isAssignableFrom(type)) {
				@SuppressWarnings("unchecked")
				Class<? extends ErrorHandler> errorHandlerType = (Class<? extends ErrorHandler>) type;
				errorHandlersBinder.addBinding().to(errorHandlerType);
			} else {
				throw new RuntimeException("The type " + type
						+ " annotated with @HandleErrors must implement ErrorHandler.");
			}
		}
	}

	/**
	 * Override this to add more {@link ErrorHandler}s
	 * 
	 * <code>errorHandlersBinder.addBinding().to(MyErrorHandler.class);</code>
	 */
	protected void bindErrorHandlers(
			Multibinder<ErrorHandler> errorHandlersBinder) {
		;
	}

}
