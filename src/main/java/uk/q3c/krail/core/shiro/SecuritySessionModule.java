package uk.q3c.krail.core.shiro;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;

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
				if (LOGGER.isDebugEnabled()) {
					if (session != null) {
						throw new IllegalStateException(
								"More than one SessionProvider returned a session: " + session + " and " + s + "");
					}
					session = s;
				} else {
					return s;
				}
			}
		}
		return session;
	}
}
