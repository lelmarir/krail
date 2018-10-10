package uk.q3c.krail.core.navigate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import uk.q3c.krail.core.view.KrailViewChangeEvent;

/**
 * The annotated method can have any of these parameters:
 * <ul>
 * <li>KrailViewChangeEvent</li>
 * <li>any @Parametr annotated parameter</li>
 * <li>any parameter injectable by guice</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface AfterInboundNavigation {

}
