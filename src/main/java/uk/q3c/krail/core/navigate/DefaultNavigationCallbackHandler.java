package uk.q3c.krail.core.navigate;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.q3c.krail.core.navigate.sitemap.NavigationState.Parameters;
import uk.q3c.krail.core.view.KrailView;
import uk.q3c.krail.core.view.KrailViewChangeEvent;
import uk.q3c.krail.core.view.KrailViewChangeEvent.CancellableKrailViewChangeEvent;
import uk.q3c.krail.core.view.KrailViewChangeEventImpl.CancellableWrapper;

public class DefaultNavigationCallbackHandler<T extends KrailView> implements
		NavigationCallbackHandler<T> {

	public static class ConversionException extends RuntimeException {
		public ConversionException(String message, Throwable cause) {
			super(message, cause);
		}
	}
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNavigationCallbackHandler.class);
	
	private static void fireNavigationCallback(KrailView view,
			KrailViewChangeEvent event, Class<? extends Annotation> annotation)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		List<Method> methods = getAnnotatedMethods(unproxy(view.getClass()), annotation);
		if (methods.size() > 1) {
			throw new IllegalStateException(
					"Only one method can be annotated with "
							+ annotation.getClass() + ":" + methods);
		}

		if (!methods.isEmpty()) {
			Method method = methods.get(0);
			LOGGER.debug("found method annotated with " + annotation.getClass()
					+ ": ", method);

			if (!method.getReturnType().equals(Void.TYPE)) {
				throw new IllegalStateException("The method annotated with "
						+ annotation.getClass() + " should return void: "
						+ method);
			}

			Class<?>[] parametersTypes = method.getParameterTypes();
			Annotation[][] parametersAnnotations = method
					.getParameterAnnotations();
			Object[] args = new Object[parametersTypes.length];
			for (int i = 0; i < parametersTypes.length; i++) {
				Parameter parameter;
				if (parametersTypes[i].isAssignableFrom(event.getClass())) {
					args[i] = event;
					LOGGER.trace(
							"parameter {} of type {} -> CancellableKrailViewChangeEvent",
							i, parametersTypes[i]);
				} else if ((parameter = getAnnotation(parametersAnnotations[i],
						Parameter.class)) != null) {
					Parameters parameters = event.getTargetNavigationState().parameters();
					String id = parameter.value();
					//TODO: check that the parameter exist (it could be null, but defined)
					try{
						args[i] = convert(parameters.getAsString(id), parametersTypes[i]);
					}catch(ConversionException e) {
						throw new InvalidURIException(event.getTargetNavigationState().getFragment(), e);
					}
					
				} else {
					throw new IllegalStateException("Unable to bind parameter "
							+ i + " (of type " + parametersTypes[i]
							+ ") of the callback method " + method);
				}
			}

			method.invoke(view, args);
		}
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends KrailView> unproxy(
			Class<? extends KrailView> clazz) {
		if(clazz.getSimpleName().contains("EnhancerByGuice")) {
			return (Class<? extends KrailView>) clazz.getSuperclass();
		}
		return clazz;
	}

	private static List<Method> getAnnotatedMethods(
			Class<? extends KrailView> clazz,
			Class<? extends Annotation> annotation) {
		LinkedList<Method> list = new LinkedList<>();
		for (Method m : clazz.getMethods()) {
			if (m.isAnnotationPresent(annotation)) {
				list.add(m);
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
	
	private static Object convert(String string, Class<?> clazz) throws ConversionException {
		
		if(string == null) {
			return null;
		}
		
		try {
			return clazz.getConstructor(String.class).newInstance(string);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new ConversionException("Conversion Exception", e);
		}
	}
	
	public DefaultNavigationCallbackHandler() {
		;
	}

	@Override
	public void beforeOutboundNavigationEvent(T view, CancellableWrapper event) {
		try {
			fireNavigationCallback(view, event, BeforeOutboundNavigation.class);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void beforeInboundNavigationEvent(T view,
			CancellableKrailViewChangeEvent event) {
		try {
			fireNavigationCallback(view, event, BeforeInboundNavigation.class);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void afterInbounNavigationEvent(T view, KrailViewChangeEvent event) {
		try {
			fireNavigationCallback(view, event, AfterInboundNavigation.class);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}