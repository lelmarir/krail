package uk.q3c.krail.core.services;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.multibindings.Multibinder;

public abstract class ServiceModule extends AbstractModule {

	private Multibinder<Service> servicesBinder;

	@Override
	protected final void configure() {
		this.servicesBinder = Multibinder.newSetBinder(binder(), Service.class);
		configureService();
	}
	
	protected abstract void configureService();

	protected ScopedBindingBuilder addService(Class<? extends Service> service) {
		return servicesBinder.addBinding().to(service);
	}
}
