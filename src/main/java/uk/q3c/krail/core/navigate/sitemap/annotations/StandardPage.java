package uk.q3c.krail.core.navigate.sitemap.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import uk.q3c.krail.core.navigate.sitemap.StandardPageKey;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StandardPage {
	StandardPageKey[] value();
}
