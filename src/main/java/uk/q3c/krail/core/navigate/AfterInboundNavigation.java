package uk.q3c.krail.core.navigate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import uk.q3c.krail.core.view.KrailViewChangeEvent;

/**
 * Can inject {@link KrailViewChangeEvent} or any {@link Parameter} annotated parameters
 * @author mpreti
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface AfterInboundNavigation {

}
