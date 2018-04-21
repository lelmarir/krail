package uk.q3c.krail.core.navigate.parameters;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({TYPE})
@Retention(RUNTIME)
public @interface CalculatedParameter {
	
	String name();
	Class<? extends ParameterProvider<?>> provider();
}
