package uk.q3c.krail.core.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.ShiroException;

import uk.q3c.krail.core.navigate.DefaultInvalidURIExceptionHandler;
import uk.q3c.krail.core.navigate.InvalidURIExceptionHandler;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.ErrorHandler;

public class ShiroVaadinModule extends AbstractModule {

	public ShiroVaadinModule() {
		super();
	}

	@Override
	protected void configure() {
		bindErrorHandler();
		bindUnauthenticatedHandler();
		bindUnauthorisedHandler();
		bindLoginExceptionsHandler();
		bindInvalidURIHandler();

	}

	/**
	 * the {@link DefaultErrorHandler} calls this handler in response to an attempt to navigate to an invalid URI. If
	 * you have defined your own ErrorHandler you may of course do something different
	 */
	protected void bindInvalidURIHandler() {
		bind(InvalidURIExceptionHandler.class).to(DefaultInvalidURIExceptionHandler.class);
	}

	/**
	 * error handler for the VaadinSession, handles Krail (and therefore Shiro) exceptions
	 */
	protected void bindErrorHandler() {
		bind(ErrorHandler.class).to(KrailErrorHandler.class);
	}

	/**
	 * the {@link DefaultErrorHandler} calls this handler in response to an attempted unauthorised action. If you have
	 * defined your own ErrorHandler you may of course do something different
	 */
	protected void bindUnauthorisedHandler() {
		bind(UnauthorizedExceptionHandler.class).to(DefaultUnauthorizedExceptionHandler.class);
	}

	/**
	 * the {@link DefaultErrorHandler} calls this handler in response to an attempted unauthenticated action. If you
	 * have defined your own ErrorHandler you may of course do something different
	 */
	protected void bindUnauthenticatedHandler() {
		bind(UnauthenticatedExceptionHandler.class).to(DefaultUnauthenticatedExceptionHandler.class);
	}

	/**
	 * The login process may raise a number of {@link ShiroException}s. This handler is called to manage those
	 * exceptions gracefully.
	 */
	protected void bindLoginExceptionsHandler() {
		bind(LoginExceptionHandler.class).to(DefaultLoginExceptionHandler.class);
	}

	@Provides
	KrailSecurityManager providesSecurityManager() {
		return (KrailSecurityManager) SecurityUtils.getSecurityManager();
	}

}
