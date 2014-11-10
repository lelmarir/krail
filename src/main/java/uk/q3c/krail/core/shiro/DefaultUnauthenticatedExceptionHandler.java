package uk.q3c.krail.core.shiro;

import java.io.Serializable;

import org.apache.shiro.authz.UnauthenticatedException;

import uk.q3c.krail.core.navigate.Navigator;
import uk.q3c.krail.core.navigate.sitemap.StandardViewKey;
import uk.q3c.krail.core.user.notify.UserNotifier;
import uk.q3c.krail.core.user.notify.UserNotifier.NotificationType;
import uk.q3c.krail.i18n.DescriptionKey;

import com.google.inject.Inject;

public class DefaultUnauthenticatedExceptionHandler implements UnauthenticatedExceptionHandler, Serializable {

	private final UserNotifier notifier;
	private final Navigator navigator;

	@Inject
	protected DefaultUnauthenticatedExceptionHandler(UserNotifier notifier, Navigator navigator) {
		super();
		this.notifier = notifier;
		this.navigator = navigator;
	}

	@Override
	public void onUnauthenticatedException(UnauthenticatedException throwable) {
		navigator.navigateTo(StandardViewKey.Log_In);
		notifier.show(NotificationType.WARNING, DescriptionKey.You_have_not_logged_in);
		
	}

}
