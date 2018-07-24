package uk.q3c.krail.core.navigate.sitemap.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import java.util.Objects;

import uk.q3c.krail.core.navigate.parameters.ProvidesParameter;
import uk.q3c.krail.core.navigate.DefaultNavigationCallbackHandler;
import uk.q3c.krail.core.navigate.parameters.Parameters;
import uk.q3c.krail.core.view.KrailView;

public class ParametersImpl implements Parameters {

	// static injection
	@Inject
	private static Provider<Injector> injectorProvider;

	private static Map<Class<? extends KrailView>, Multimap<String, Method>> parametersProvidersCache = new HashMap<>();

	public static List<Method> getMethodsAnnotatedWith(final Class<?> type,
			final Class<? extends Annotation> annotation) {
		final List<Method> methods = new ArrayList<Method>();
		Class<?> klass = type;
		while (klass != Object.class && klass != null) { // need to iterated thought hierarchy in order to retrieve
															// methods from above
			// the current instance
			// iterate though the list of methods declared in the class represented by klass
			// variable, and add those annotated with the specified annotation
			final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(klass.getDeclaredMethods()));
			for (final Method method : allMethods) {
				if (method.isAnnotationPresent(annotation)) {
					Annotation annotInstance = method.getAnnotation(annotation);
					// TODO process annotInstance
					methods.add(method);
				}
			}
			// move to the upper class in the hierarchy in search for more methods
			klass = klass.getSuperclass();
		}
		return methods;
	}

	private static Multimap<String, Method> getParametersProviders(Class<? extends KrailView> viewClass) {

		Multimap<String, Method> providers = parametersProvidersCache.get(viewClass);
		if (providers == null) {
			providers = MultimapBuilder.hashKeys().arrayListValues().build();

			List<Method> providesParameterMethods = getMethodsAnnotatedWith(viewClass, ProvidesParameter.class);

			for (Method method : providesParameterMethods) {
				ProvidesParameter annotation = method.getAnnotation(ProvidesParameter.class);
				String name = annotation.name();
				providers.put(name, method);
			}
			parametersProvidersCache.put(viewClass, providers);
		}
		return providers;
	}

	private final Class<? extends KrailView> targetViewClass;
	private Map<String, Object> parameters = new LinkedHashMap<>();

	public ParametersImpl(Class<? extends KrailView> targetViewClass) {
		this.targetViewClass = targetViewClass;
	}

	public ParametersImpl(ParametersImpl parameters) {
		this(parameters.targetViewClass);
		this.parameters = parameters.parameters;
	}

	public Class<? extends KrailView> getTargetViewClass() {
		return targetViewClass;
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
	public Object get(String id, KrailView view) throws NoSuchElementException {
		return get(id, view, view != null);
	}

	@Override
	public Object get(String id, KrailView view, boolean useCalculatedParameters) throws NoSuchElementException {
		Object value = parameters.get(id);
		if (value == null && useCalculatedParameters == true) {
			try {
				value = calculateParameter(id, view);
			} catch (NoSuchElementException e) {
				;
			}
		}
		if (value == null) {
			throw new NoSuchElementException("for parameter '" + id + "'");
		}
		return value;
	}

	protected Object calculateParameter(String parameterKey, KrailView view) throws NoSuchElementException {

		Multimap<String, Method> map = getParametersProviders(targetViewClass);

		Collection<Method> providers = map.get(parameterKey);
		Map<Method, Object[]> parameterProviderMethods = DefaultNavigationCallbackHandler
				.getMatchingMethodForAvailibleParameters(injectorProvider.get(), view, null,
						LooplessCalculatedParametersWrapper.build(this, parameterKey), providers, true);
		if (!parameterProviderMethods.isEmpty()) {
			Throwable error = null;
			for (Entry<Method, Object[]> entry : parameterProviderMethods.entrySet()) {
				Method method = entry.getKey();
				Object[] args = entry.getValue();
				try {
					method.setAccessible(true);
					return method.invoke(view, args);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					error = e;
				}
			}
			if (error != null) {
				throw new RuntimeException(error);
			}
		}
		throw new NoSuchElementException();

	}

	@Override
	public String getAsString(String id, KrailView view) throws NoSuchElementException {
		return formatAsString(get(id, view));
	}

	private String formatAsString(Object object) {
		return object != null ? object.toString() : null;
	}

	@Override
	public boolean contains(String id) {
		return parameters.containsKey(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ParametersImpl) {
			Map<String, Object> objParameters = ((ParametersImpl) obj).parameters;
			return Objects.equals(targetViewClass, ((ParametersImpl) obj).targetViewClass)
					&& Objects.equals(parameters, objParameters);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(targetViewClass, parameters);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "@"
				+ parameters.entrySet().stream()
						.map(entry -> entry.getKey() + "='" + entry.getValue() + "'("
								+ entry.getValue().getClass().getSimpleName() + ")")
						.collect(Collectors.joining(",", "{", "}"));
	}

}
