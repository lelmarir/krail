package uk.q3c.krail.core.guice.errors;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotated class must implement {@link ErrorHandler}
 * @author mpreti
 *
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface HandleErrors {

}
