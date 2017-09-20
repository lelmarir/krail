package uk.q3c.krail.core.services;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;
import com.google.common.util.concurrent.ServiceManager.Listener;

public class ServiceManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceManager.class);
	
	private final com.google.common.util.concurrent.ServiceManager serviceManager;

	public ServiceManager(Iterable<? extends Service> services) {
		this(services, true);
	}
	
	public ServiceManager(Iterable<? extends Service> services, boolean autostart) {
		this(services, autostart, 10000);
	}
	
	public ServiceManager(Iterable<? extends Service> services, boolean autostart, int waitUntilStartedTimeout) {
		super();
		this.serviceManager = new com.google.common.util.concurrent.ServiceManager(services);
		if (autostart) {
			this.serviceManager.startAsync();
			if (waitUntilStartedTimeout > 0) {
				try {
					this.serviceManager.awaitHealthy(waitUntilStartedTimeout, TimeUnit.MILLISECONDS);
				} catch (TimeoutException e) {
					LOGGER.warn("Some services failed to star within the given timeout: {}", this.serviceManager.servicesByState());
				}
			}
		}
	}

	// delegate

	public int hashCode() {
		return serviceManager.hashCode();
	}

	public boolean equals(Object obj) {
		return serviceManager.equals(obj);
	}

	public void addListener(Listener listener, Executor executor) {
		serviceManager.addListener(listener, executor);
	}

	public void addListener(Listener listener) {
		serviceManager.addListener(listener);
	}

	public com.google.common.util.concurrent.ServiceManager startAsync() {
		return serviceManager.startAsync();
	}

	public void awaitHealthy() {
		serviceManager.awaitHealthy();
	}

	public void awaitHealthy(long timeout, TimeUnit unit) throws TimeoutException {
		serviceManager.awaitHealthy(timeout, unit);
	}

	public com.google.common.util.concurrent.ServiceManager stopAsync() {
		return serviceManager.stopAsync();
	}

	public void awaitStopped() {
		serviceManager.awaitStopped();
	}

	public void awaitStopped(long timeout, TimeUnit unit) throws TimeoutException {
		serviceManager.awaitStopped(timeout, unit);
	}

	public boolean isHealthy() {
		return serviceManager.isHealthy();
	}

	public ImmutableMultimap<State, Service> servicesByState() {
		return serviceManager.servicesByState();
	}

	public ImmutableMap<Service, Long> startupTimes() {
		return serviceManager.startupTimes();
	}

	public String toString() {
		return serviceManager.toString();
	}

}
