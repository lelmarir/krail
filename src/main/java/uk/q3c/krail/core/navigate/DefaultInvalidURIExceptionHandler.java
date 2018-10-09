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
package uk.q3c.krail.core.navigate;

import uk.q3c.krail.core.navigate.sitemap.StandardPageKey;
import uk.q3c.krail.core.user.notify.UserNotifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class DefaultInvalidURIExceptionHandler implements
		InvalidURIExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultInvalidURIExceptionHandler.class);
	
	private final UserNotifier notifier;
	private final Provider<Navigator> navigatorProvider;

	@Inject
	protected DefaultInvalidURIExceptionHandler(UserNotifier notifier,
			Provider<Navigator> navigatorProvider) {
		this.notifier = notifier;
		this.navigatorProvider = navigatorProvider;
	}

	private void onInvalidUri(InvalidURIException error) {
		//FIXME: localizzare
		Navigator navigator = navigatorProvider.get();
		notifier.notifyInvalidURI(error);
		LOGGER.info("Invalid URI: '" + error.getMessage() + "'");
		if (navigator.getCurrentNavigationState() != null) {
			navigator.updateUriFragment();
		} else {
			navigator.navigateTo(StandardPageKey.Public_Home);
		}
	}

	@Override
	public boolean handle(ErrorEvent event) {
		Throwable throwable = event.getThrowable();
		if (throwable instanceof InvalidURIException) {
			onInvalidUri((InvalidURIException) throwable);
			return true;
		}
		return false;
	}

}
