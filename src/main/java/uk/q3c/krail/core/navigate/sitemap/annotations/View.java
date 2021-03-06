/*
 * Copyright (C) 2013 David Sowerby
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.q3c.krail.core.navigate.sitemap.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Target({ TYPE })
@Retention(RUNTIME)
public @interface View {
	/**
	 * Detault value, is equal to null and the appropriate default value is used if set in the navigator
	 */
	public static final Class<? extends ViewLayout> UNDEFINED_LAYOUT = UndefinedViewLayout.class;
	/**
	 * A layout that simply display the view, disabling any default layout
	 */
	public static final Class<? extends ViewLayout> NO_LAYOUT = EmptyViewLayout.class;
	
	String uri();
	String[] title() default {};
	Class<? extends ViewLayout> layout() default UndefinedViewLayout.class;
}
