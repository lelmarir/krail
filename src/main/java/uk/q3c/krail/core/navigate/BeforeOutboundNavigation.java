package uk.q3c.krail.core.navigate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated method can have any of these parameters:
 * <ul>
 * <li>CancellableKrailViewChangeEvent</li>
 * <li>any @Parametr annotated parameter</li>
 * <li>any parameter injectable by guice</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface BeforeOutboundNavigation {

}
