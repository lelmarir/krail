package uk.q3c.krail.core.guice;

import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class AutoAsssitedInjectModule extends AbstractModule {

	private static final Logger LOGGER = LoggerFactory.getLogger(AutoAsssitedInjectModule.class);

	private final Reflections basePackageReflections;

	public AutoAsssitedInjectModule(String basePackage) {
		this(new Reflections(basePackage));
	}

	public AutoAsssitedInjectModule(Reflections basePackageReflections) {
		this.basePackageReflections = basePackageReflections;
	}

	@Override
	protected void configure() {
		super.configure();

		Set<Class<?>> autoFactoryTypes = basePackageReflections.getTypesAnnotatedWith(AutoFactory.class);
		if (!autoFactoryTypes.isEmpty()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.warn("binding {} interfaces as factory with AssistedInject: {}", autoFactoryTypes.size(),
						autoFactoryTypes);
			}
			for (Class<?> type : autoFactoryTypes) {
				if (!type.isInterface()) {
					throw new IllegalStateException(
							"'" + type + "' is not an interface: @AutoFactory can be applied only on iterfaces");
				}
				install(new FactoryModuleBuilder().build(type));
			}
		}
	}

}
