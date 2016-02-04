package uk.q3c.krail.core.navigate;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections15.map.LinkedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.q3c.krail.core.navigate.sitemap.NavigationState.Parameters;
import uk.q3c.krail.core.view.KrailView;
import uk.q3c.krail.core.view.KrailViewChangeEvent;
import uk.q3c.krail.core.view.KrailViewChangeEvent.CancellableKrailViewChangeEvent;
import uk.q3c.krail.core.view.KrailViewChangeEventImpl.CancellableWrapper;
import uk.q3c.util.ReversedList;

public class DefaultNavigationCallbackHandler implements
		NavigationCallbackHandler {

	@SuppressWarnings("serial")
	public static class ConversionException extends RuntimeException {
		public ConversionException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefaultNavigationCallbackHandler.class);

	private static void fireNavigationCallback(KrailView view,
			KrailViewChangeEvent event, Class<? extends Annotation> annotation)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		LinkedHashMap<Class<?>, LinkedList<Method>> methodshierarchy = getAnnotatedMethods(
				unproxy(view.getClass()), annotation);
		if (LOGGER.isDebugEnabled()) {
			report(annotation, methodshierarchy);
		}

		if (!methodshierarchy.isEmpty()) {
			List<Entry<Class<?>, LinkedList<Method>>> list = new ArrayList<>(
					methodshierarchy.entrySet());
			Iterator<Entry<Class<?>, LinkedList<Method>>> reverseIterator = new ReversedList<>(
					list).iterator();

			while (reverseIterator.hasNext()) {
				Entry<Class<?>, LinkedList<Method>> entry = reverseIterator
						.next();
				LinkedList<Method> alternateMethods = entry.getValue();

				checkReturnType(annotation, entry.getValue());

				Parameters parameters = event.getTargetNavigationState()
						.parameters();

				callMatchingMethodForAvailibleParameters(view, event,
						parameters, alternateMethods);
			}
		}
	}

	private static void callMatchingMethodForAvailibleParameters(
			KrailView view, KrailViewChangeEvent event, Parameters parameters,
			LinkedList<Method> alternateMethods) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		callMatchingMethodForAvailibleParameters(view, event, parameters,
				alternateMethods, null);
	}

	private static void callMatchingMethodForAvailibleParameters(
			KrailView view, KrailViewChangeEvent event, Parameters parameters,
			LinkedList<Method> alternateMethods, Boolean useCalculatedParameters)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		Map<Method, Object[]> matchingMethods = new HashMap<Method, Object[]>();

		methodsLoop: for (Method method : alternateMethods) {
			LOGGER.trace("checking method {}:", method);
			Class<?>[] parametersTypes = method.getParameterTypes();
			Annotation[][] parametersAnnotations = method
					.getParameterAnnotations();
			Object[] args = new Object[parametersTypes.length];

			for (int i = 0; i < parametersTypes.length; i++) {
				Parameter parameterAnnotation;
				if (parametersTypes[i].isAssignableFrom(event.getClass())) {
					args[i] = event;
					LOGGER.trace(
							"parameter {} of type {} -> CancellableKrailViewChangeEvent",
							i, parametersTypes[i]);
				} else if ((parameterAnnotation = getAnnotation(
						parametersAnnotations[i], Parameter.class)) != null) {

					String parameterKey = parameterAnnotation.value();
					boolean excludeCalculatedParameters = useCalculatedParameters == null
							|| useCalculatedParameters == false;
					Object parameterValue = parameters.get(parameterKey,
							excludeCalculatedParameters);
					
					if (parameterValue != null) {
						if (parametersTypes[i].isAssignableFrom(parameterValue
								.getClass())) {
							args[i] = parameterValue;
						} else if (parameterValue.getClass().equals(
								String.class)) {
							// proviene dall'uri? Provo a convertirlo nel tipo
							// richiesto
							try {
								args[i] = convert((String) parameterValue,
										parametersTypes[i]);
							} catch (ConversionException e) {
								throw new IllegalStateException(
										"The parameter '"
												+ parameterKey
												+ "' with value '"
												+ parameterValue
												+ "' is not of the required type ("
												+ parametersTypes[i] + ").", e);
							}
						} else {
							throw new IllegalStateException("The parameter '"
									+ parameterKey + "' with value '"
									+ parameterValue
									+ "' is not of the required type ("
									+ parametersTypes[i] + ").");
						}

					} else {
						// the required parameter is not present, this method is
						// not the correct one
						continue methodsLoop;
					}

				} else /* if(parameterAnnotation == null) */{
					// method parameter not annotated with @Parameter
					throw new IllegalStateException("Unable to bind parameter "
							+ i + " (of type " + parametersTypes[i]
							+ ") of the callback method " + method);
				}
			}// parameters for loop

			// all parameters found
			// TODO: optional parameters
			matchingMethods.put(method, args);

		}// methods foor loop

		if (matchingMethods.isEmpty()) {
			if (useCalculatedParameters == null) {
				// try with calculated parameters
				callMatchingMethodForAvailibleParameters(view, event,
						parameters, alternateMethods, true);
			} else {
				throw new IllegalStateException(
						"Unable to find the method to call for the provided parameters:\n"
								+ "   parameters: " + parameters + "\n"
								+ "   methods:    " + alternateMethods + "\n\n");
			}
		} else if (matchingMethods.size() > 1) {
			throw new IllegalStateException(
					"Unable to tell wich method to call within the matching ones:\n"
							+ "   parameters: " + parameters + "\n"
							+ "   methods:    " + alternateMethods + "\n\n");
		}

		Entry<Method, Object[]> entry = matchingMethods.entrySet().iterator()
				.next();
		Method method = entry.getKey();
		Object[] args = entry.getValue();
		method.setAccessible(true);
		method.invoke(view, args);
	}

	private static void report(Class<? extends Annotation> annotation,
			LinkedHashMap<Class<?>, LinkedList<Method>> methods) {
		StringBuilder sb = new StringBuilder();
		sb.append("found " + methods.size()
				+ " classes with annotated methods (" + annotation + "):\n");
		for (Entry<Class<?>, LinkedList<Method>> entry : methods.entrySet()) {
			Class<?> clazz = entry.getKey();
			LinkedList<Method> alternatives = entry.getValue();
			sb.append(" for  " + clazz + "\n");
			for (Method m : alternatives) {
				sb.append("   " + m);
			}
		}
		LOGGER.debug(sb.toString());
	}

	private static void checkReturnType(Class<? extends Annotation> annotation,
			LinkedList<Method> alternatives) {
		for (Method method : alternatives) {
			if (!method.getReturnType().equals(Void.TYPE)) {
				throw new IllegalStateException("The method annotated with "
						+ annotation.getClass() + " should return void: "
						+ method);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends KrailView> unproxy(
			Class<? extends KrailView> clazz) {
		if (clazz.getSimpleName().contains("EnhancerByGuice")) {
			return (Class<? extends KrailView>) clazz.getSuperclass();
		}
		return clazz;
	}

	/**
	 * Return all annotated methods for the class hierarchy. For each class
	 * level multiple annotated methods can exist but must have different
	 * signatures and will be considered as alternative to each others (only one
	 * will be called). The returned list is ordered from the provided class
	 * methods to the parents ones.
	 */
	// TODO: store found methos int a cache
	@SuppressWarnings("unchecked")
	private static LinkedHashMap<Class<?>, LinkedList<Method>> getAnnotatedMethods(
			Class<? extends KrailView> clazz,
			Class<? extends Annotation> annotation) {
		LinkedHashMap<Class<?>, LinkedList<Method>> list = new LinkedHashMap<>();
		while (clazz != null) {
			LinkedList<Method> alternatives = new LinkedList<Method>();
			for (Method m : clazz.getDeclaredMethods()) {
				if (m.isAnnotationPresent(annotation)) {
					alternatives.add(m);
				}
			}
			if (!alternatives.isEmpty()) {
				list.put(clazz, alternatives);
			}

			// TODO: check if any alternative has the same signature

			Class<?> superClazz = clazz.getSuperclass();
			if (KrailView.class.isAssignableFrom(superClazz)) {
				clazz = (Class<? extends KrailView>) superClazz;
			} else {
				break;
			}
		}
		return list;
	}

	private static <T extends Annotation> T getAnnotation(
			Annotation[] annotations, Class<T> annotation) {
		for (Annotation a : annotations) {
			if (annotation.isAssignableFrom(a.getClass())) {
				return (T) a;
			}
		}
		return null;
	}

	private static Object convert(String string, Class<?> clazz)
			throws ConversionException {

		if (string == null) {
			return null;
		}

		try {
			return clazz.getConstructor(String.class).newInstance(string);
		} catch (NoSuchMethodException | SecurityException
				| InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new ConversionException("Conversion Exception", e);
		}
	}

	public DefaultNavigationCallbackHandler() {
		;
	}

	@Override
	public void beforeOutboundNavigationEvent(KrailView view,
			CancellableWrapper event) {
		try {
			fireNavigationCallback(view, event, BeforeOutboundNavigation.class);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void beforeInboundNavigationEvent(KrailView view,
			CancellableKrailViewChangeEvent event) {
		try {
			fireNavigationCallback(view, event, BeforeInboundNavigation.class);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void afterInbounNavigationEvent(KrailView view,
			KrailViewChangeEvent event) {
		try {
			fireNavigationCallback(view, event, AfterInboundNavigation.class);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}