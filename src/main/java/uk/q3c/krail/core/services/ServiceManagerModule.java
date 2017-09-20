package uk.q3c.krail.core.services;

import java.util.Set;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class ServiceManagerModule extends AbstractModule {

	@Override
	protected void configure() {
		
	}

	@Provides
	private ServiceManager providesServiceManager(Set<Service> services) {
		return new ServiceManager(services);
	}
}
