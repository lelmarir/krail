/*
 * Copyright (C) 2014 David Sowerby
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
package uk.q3c.krail.core.view;

import com.google.inject.*;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.q3c.krail.core.guice.uiscope.UIScope;
import uk.q3c.krail.core.navigate.sitemap.annotations.View;
import uk.q3c.krail.core.navigate.sitemap.annotations.ViewLayout;
import uk.q3c.krail.core.ui.KrailUIModule.PrivateViewDefaultLayout;
import uk.q3c.krail.core.ui.KrailUIModule.PublicViewDefaultLayout;

public class DefaultLayoutFactory implements LayoutFactory {

	private static Logger log = LoggerFactory.getLogger(DefaultLayoutFactory.class);
	private final Injector injector;

	Class<? extends ViewLayout> publicViewDefaultLayout;
	Class<? extends ViewLayout> privateViewDefaultLayout;

	@Inject
	protected DefaultLayoutFactory(Injector injector) {
		super();
		this.injector = injector;
	}

	public Class<? extends ViewLayout> getPublicViewDefaultLayout() {
		return publicViewDefaultLayout;
	}

	@Inject(optional = true)
	public void setPublicViewDefaultLayout(
			@PublicViewDefaultLayout Class<? extends ViewLayout> publicViewDefaultLayout) {
		this.publicViewDefaultLayout = publicViewDefaultLayout;
	}

	public Class<? extends ViewLayout> getPrivateViewDefaultLayout() {
		return privateViewDefaultLayout;
	}

	@Inject(optional = true)
	public void setPrivateViewDefaultLayout(
			@PrivateViewDefaultLayout Class<? extends ViewLayout> privateViewDefaultLayout) {
		this.privateViewDefaultLayout = privateViewDefaultLayout;
	}

	public <T extends ViewLayout> T get(Class<T> viewLayoutClass) {
		if (viewLayoutClass != null) {
			TypeLiteral<T> typeLiteral = TypeLiteral.get(viewLayoutClass);
			Key<T> key = Key.get(typeLiteral);
			Provider<T> unscoped = injector.getProvider(key);
			UIScope.getCurrent().scope(key, unscoped);
			log.debug("getting or retrieving instance of {}", viewLayoutClass);
			T layout = injector.getInstance(key);
			return layout;
		} else {
			return null;
		}
	}

	@Override
	public ViewLayout get(KrailView view) {
		View viewAnnotation = view.getClass().getAnnotation(View.class);
		if (viewAnnotation != null) {
			Class<? extends ViewLayout> layoutClass = viewAnnotation.layout();
			if (layoutClass == View.NO_LAYOUT) {
				return null;
			} else if (layoutClass == View.UNDEFINED_LAYOUT) {
				return getDefault(view);
			} else {
				return get(layoutClass);
			}
		}else {
			return getDefault(view);
		}
	}

	private ViewLayout getDefault(KrailView view) {
		if (isPrivate(view)) {
			return get(privateViewDefaultLayout);
		} else {
			return get(publicViewDefaultLayout);
		}
	}

	private boolean isPrivate(KrailView view) {
		Class<? extends KrailView> viewClass = view.getClass();
		return viewClass.getAnnotation(RequiresAuthentication.class) != null
				|| viewClass.getAnnotation(RequiresPermissions.class) != null
				|| viewClass.getAnnotation(RequiresRoles.class) != null
				|| viewClass.getAnnotation(RequiresUser.class) != null;
	}
}
