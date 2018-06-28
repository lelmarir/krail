package uk.q3c.krail.core.guice;

import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;

public class StaticInjectionModule extends AbstractModule {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(StaticInjectionModule.class);

	private final Reflections basePackageReflections;

	public StaticInjectionModule(String basePackage) {
		this(new Reflections(basePackage));
	}

	public StaticInjectionModule(Reflections basePackageReflections) {
		this.basePackageReflections = basePackageReflections;
	}

	@Override
	protected void configure() {
		super.configure();

		Set<Class<?>> staticInjectTypes = basePackageReflections
				.getTypesAnnotatedWith(StaticInject.class);
		if (!staticInjectTypes.isEmpty()) {

			if(LOGGER.isDebugEnabled())  {
				LOGGER.warn("staticalliy injecting {} classes: {}", staticInjectTypes.size(), staticInjectTypes);
			}
			for (Class<?> type : staticInjectTypes) {
				requestStaticInjection(type);
			}
		}
	}

}
