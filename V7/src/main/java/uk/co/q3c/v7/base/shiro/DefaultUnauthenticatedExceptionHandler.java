package uk.co.q3c.v7.base.shiro;

import java.io.Serializable;

import org.apache.shiro.authz.UnauthenticatedException;

import uk.co.q3c.v7.base.navigate.V7Navigator;
import uk.co.q3c.v7.base.navigate.sitemap.StandardViewKey;
import uk.co.q3c.v7.base.user.notify.UserNotifier;
import uk.co.q3c.v7.base.user.notify.UserNotifier.NotificationType;
import uk.co.q3c.v7.i18n.DescriptionKey;

import com.google.inject.Inject;

public class DefaultUnauthenticatedExceptionHandler implements UnauthenticatedExceptionHandler, Serializable {

	private final UserNotifier notifier;
	private final V7Navigator navigator;

	@Inject
	protected DefaultUnauthenticatedExceptionHandler(UserNotifier notifier, V7Navigator navigator) {
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
