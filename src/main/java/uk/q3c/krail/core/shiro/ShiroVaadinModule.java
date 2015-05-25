package uk.q3c.krail.core.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.ShiroException;

import uk.q3c.krail.core.guice.errors.ErrorHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.vaadin.server.DefaultErrorHandler;

public class ShiroVaadinModule extends AbstractModule {

	public ShiroVaadinModule() {
		super();
	}

	@Override
	protected void configure() {
		Multibinder<ErrorHandler> errorHandlersBinder = Multibinder
				.newSetBinder(binder(), ErrorHandler.class);
		errorHandlersBinder.addBinding().to(UnauthorizedExceptionHandler.class);
		errorHandlersBinder.addBinding().to(
				UnauthenticatedExceptionHandler.class);

		bindUnauthenticatedHandler();
		bindUnauthorisedHandler();
		bindLoginExceptionsHandler();
	}

	@Provides
	KrailSecurityManager providesSecurityManager() {
		return (KrailSecurityManager) SecurityUtils.getSecurityManager();
	}

	/**
	 * the {@link DefaultErrorHandler} calls this handler in response to an
	 * attempted unauthorised action. If you have defined your own ErrorHandler
	 * you may of course do something different
	 */
	protected void bindUnauthorisedHandler() {
		bind(UnauthorizedExceptionHandler.class).to(
				DefaultUnauthorizedExceptionHandler.class);
	}

	/**
	 * the {@link DefaultErrorHandler} calls this handler in response to an
	 * attempted unauthenticated action. If you have defined your own
	 * ErrorHandler you may of course do something different
	 */
	protected void bindUnauthenticatedHandler() {
		bind(UnauthenticatedExceptionHandler.class).to(
				AutenticationHandler.class);
	}

	/**
	 * The login process may raise a number of {@link ShiroException}s. This
	 * handler is called to manage those exceptions gracefully.
	 */
	protected void bindLoginExceptionsHandler() {
		bind(LoginExceptionHandler.class)
				.to(DefaultLoginExceptionHandler.class);
	}

}
