package uk.q3c.krail.core.services;

import java.util.Set;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.multibindings.Multibinder;

public class ServiceManagerModule extends AbstractModule {

	private ServiceManager servicesManager;
	private Multibinder<Service> uriBinder;

	@Override
	protected void configure() {
		this.uriBinder = Multibinder.newSetBinder(binder(), Service.class);
		bind(ServiceManager.class).asEagerSingleton();
	}

	public Multibinder<Service> getServiceBinder() {
		return uriBinder;
	}

	public ScopedBindingBuilder addService(Class<? extends Service> service) {
		return getServiceBinder().addBinding().to(service);
	}

	@Provides
	private ServiceManager provideServiceManager(Set<Service> services) {
		if (this.servicesManager == null) {
			this.servicesManager = new ServiceManager(services);
		}
		return this.servicesManager;
	}

}
