package uk.q3c.krail.core.shiro;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import com.vaadin.server.VaadinSession;

public class SecuritySessionModule extends AbstractModule {

	private static Logger LOGGER = LoggerFactory.getLogger(SecuritySessionModule.class);

	public static interface SessionProvider {
		SecuritySession get();
	}

	private static Multibinder<SessionProvider> multibinder;

	public static LinkedBindingBuilder<SessionProvider> addBinding() {
		return multibinder.addBinding();
	}

	@Override
	protected void configure() {
		multibinder = Multibinder.newSetBinder(binder(), SessionProvider.class);
	}

	@Provides
	private SecuritySession provideSecuritySession(Set<SessionProvider> sessionProviders) {
		SecuritySession session = null;
		for (SessionProvider sessionProvider : sessionProviders) {
			SecuritySession s = sessionProvider.get();
			if (s != null) {
				if (session != null) {
					// FIXME: non dovrebbero esservi mai 2 sessioni, ma sembra che il thread arrivi
					// sporco e quindi trovo anche la VaadinSession
					LOGGER.error("More than one SessionProvider returned a session: " + session + " and " + s + "");
					// anche se qualcosa non va, do la precedenza alla sessione http e scarto la
					// sessione di Vaadin
					if (session instanceof VaadinSecuritySession) {
						return s;
					} else if (s instanceof VaadinSecuritySession) {
						return session;
					} else {
						throw new AssertionError(
								"Nessuna delle due sessioni è una vaadin session, quindi questo caso non è contemplato: è stato aggiunto un nuovo session provider?");
					}
				}
				session = s;
			}
		}
		return session;
	}
}
