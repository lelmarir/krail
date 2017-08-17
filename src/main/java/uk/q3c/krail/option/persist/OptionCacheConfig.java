/*
 *
 *  * Copyright (c) 2016. David Sowerby
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 *  * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 *  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations under the License.
 *
 */

package uk.q3c.krail.option.persist;

import com.google.inject.BindingAnnotation;
import uk.q3c.krail.core.eventbus.SessionBus;
import uk.q3c.krail.core.guice.vsscope.VaadinSessionScope;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Binding annotation to identify an Event Bus as being global to the application (to distinguish from event buses which have {@link VaadinSessionScope} -
 * the latter are annotated with {@link SessionBus}
 * <p>
 * Created by David Sowerby on 06/02/15.
 */
@BindingAnnotation
@Target({FIELD, PARAMETER, METHOD})
@Retention(RUNTIME)
public @interface OptionCacheConfig {
}