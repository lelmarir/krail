package uk.q3c.krail.core.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.ShiroException;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.VaadinSession;

import uk.q3c.krail.core.guice.KrailRequestHandler;
import uk.q3c.krail.core.guice.errors.ErrorHandler;
import uk.q3c.krail.core.guice.uiscope.UIScoped;
import uk.q3c.krail.core.shiro.SecuritySessionModule.SessionProvider;
import uk.q3c.krail.core.shiro.loginevent.AuthenticationEvent.AuthenticationListener;

public class ShiroVaadinModule extends AbstractModule {

	private static class VaadinSessionProvider implements SessionProvider {
		@Override
		public SecuritySession get() {
			VaadinSession session = VaadinSession.getCurrent();
			if (session != null) {
				return new VaadinSecuritySession(session);
			} else {
				return null;
			}
		}
	}

	private Multibinder<AuthenticationListener> authenticationListenersBinder;

	public ShiroVaadinModule() {
		super();
	}

	@Override
	protected void configure() {
		Multibinder<ErrorHandler> errorHandlersBinder = Multibinder.newSetBinder(binder(), ErrorHandler.class);
		errorHandlersBinder.addBinding().to(UnauthorizedExceptionHandler.class);
		errorHandlersBinder.addBinding().to(UnauthenticatedExceptionHandler.class);

		authenticationListenersBinder = Multibinder.newSetBinder(binder(), AuthenticationListener.class);
		
		bindUnauthenticatedHandler();
		bindUnauthorisedHandler();
		bindLoginExceptionsHandler();

		Multibinder<KrailRequestHandler> krailRequestHandlerBinder = Multibinder.newSetBinder(binder(),
				KrailRequestHandler.class);
		krailRequestHandlerBinder.addBinding().to(MDCSubjectHandler.class);

		SecuritySessionModule.addBinding().to(VaadinSessionProvider.class);
	}
	
	public LinkedBindingBuilder<AuthenticationListener> addAuthenticationListenerBinding() {
		return authenticationListenersBinder.addBinding();
	}

	@Provides
	KrailSecurityManager providesSecurityManager() {
		return (KrailSecurityManager) SecurityUtils.getSecurityManager();
	}

	/**
	 * the {@link DefaultErrorHandler} calls this handler in response to an
	 * attempted unauthorised action. If you have defined your own ErrorHandler you
	 * may of course do something different
	 */
	protected void bindUnauthorisedHandler() {
		bind(UnauthorizedExceptionHandler.class).to(DefaultUnauthorizedExceptionHandler.class);
	}

	/**
	 * the {@link DefaultErrorHandler} calls this handler in response to an
	 * attempted unauthenticated action. If you have defined your own ErrorHandler
	 * you may of course do something different
	 */
	protected void bindUnauthenticatedHandler() {
		bind(UnauthenticatedExceptionHandler.class).to(AuthenticationHandler.class).in(UIScoped.class);
		addAuthenticationListenerBinding().to(AuthenticationHandler.class);
	}

	/**
	 * The login process may raise a number of {@link ShiroException}s. This handler
	 * is called to manage those exceptions gracefully.
	 */
	protected void bindLoginExceptionsHandler() {
		bind(LoginExceptionHandler.class).to(DefaultLoginExceptionHandler.class);
	}

}
