package uk.q3c.krail.core.navigate.sitemap.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import java.util.NoSuchElementException;

import uk.q3c.krail.core.navigate.parameters.CalculatedParameter;
import uk.q3c.krail.core.navigate.parameters.CalculatedParameters;
import uk.q3c.krail.core.navigate.parameters.ParameterProvider;
import uk.q3c.krail.core.navigate.parameters.Parameters;
import uk.q3c.krail.core.view.KrailView;
import uk.q3c.krail.core.view.KrailViewChangeEvent;

public class ParametersImpl implements Parameters {

	// static injection
	@Inject
	private static Provider<Injector> injector;
	private static Map<Class<? extends KrailView>, Multimap<String, ParameterProvider<?>>> parametersProvidersCache = new HashMap<>();

	private static Multimap<String, ParameterProvider<?>> getParametersProviders(
			Class<? extends KrailView> viewClass) {

		Multimap<String, ParameterProvider<?>> providers = parametersProvidersCache
				.get(viewClass);
		if (providers == null) {
			providers = MultimapBuilder.hashKeys().arrayListValues().build();

			CalculatedParameters calculatedParametersAnnotation = viewClass
					.getAnnotation(CalculatedParameters.class);
			List<CalculatedParameter> providerClasses = new LinkedList<>(
					Arrays.asList(calculatedParametersAnnotation.value()));
			CalculatedParameter calculatedParameterAnnotation = viewClass
					.getAnnotation(CalculatedParameter.class);
			if (calculatedParameterAnnotation != null) {
				providerClasses.add(calculatedParameterAnnotation);
			}

			for (CalculatedParameter annotation : providerClasses) {
				String name = annotation.name();
				Class<? extends ParameterProvider<?>> providerClass = annotation
						.provider();
				ParameterProvider<?> instance;
				instance = injector.get().getInstance(providerClass);
				providers.put(name, instance);
			}

			parametersProvidersCache.put(viewClass, providers);
		}
		return providers;
	}

	private Object calculateParameter(String parameterKey) {

		Multimap<String, ParameterProvider<?>> map = getParametersProviders(
				targetViewClass);

		Collection<ParameterProvider<?>> providers = map.get(parameterKey);
		for (ParameterProvider<?> p : providers) {
			try {
				return p.get(this);
			} catch (UnsupportedOperationException e) {
				// can't calculate this parameter, try the next provider
				continue;
			}
		}
		throw new NoSuchElementException();
	}

	private final Class<? extends KrailView> targetViewClass;
	private Map<String, Object> parameters = new LinkedHashMap<>();

	public ParametersImpl(Class<? extends KrailView> targetViewClass) {
		this.targetViewClass = targetViewClass;
	}

	@Override
	public Object put(String id, Object value) {
		if (value != null) {
			return parameters.put(id, value);
		} else {
			return parameters.remove(id);
		}
	}

	@Override
	public Object get(String id) throws NoSuchElementException {
		return get(id, true);
	}

	@Override
	public Object get(String id, boolean useCalculatedParameters)
			throws NoSuchElementException {
		Object value = parameters.get(id);
		if (value == null && useCalculatedParameters == true) {
			try {
				value = calculateParameter(id);
			} catch (NoSuchElementException e) {
				;
			}
		}
		if (value == null) {
			throw new NoSuchElementException("for parameter '" + id + "'");
		}
		return value;
	}

	@Override
	public String getAsString(String id) throws NoSuchElementException {
		return formatAsString(get(id));
	}

	private String formatAsString(Object object) {
		return object != null ? object.toString() : null;
	}

	@Override
	public boolean contains(String id) {
		return parameters.containsKey(id);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "@" + parameters.entrySet()
				.stream()
				.map(entry -> entry.getKey() + "='" + entry.getValue() + "'("
						+ entry.getValue().getClass().getSimpleName() + ")")
				.collect(Collectors.joining(",", "{", "}"));
	}

}
