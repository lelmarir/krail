package uk.q3c.krail.core.services;

import java.util.Collections;
import java.util.Set;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;

public class ServiceManagerModule extends AbstractModule {

	// non è possibile iniettare un parametro opzionale nei contruttori o nei
	// metodi @Provides, questa classe scavalca il problema
	static class ServicesHolder {
		@Inject(optional = true)
		Set<Service> services = Collections.emptySet();
	}

	@Override
	protected void configure() {

	}

	@Provides
	private ServiceManager providesServiceManager(ServicesHolder services) {
		if (services.services != null && !services.services.isEmpty()) {
			return new ServiceManager(services.services);
		} else {
			return null;
		}
	}
}
