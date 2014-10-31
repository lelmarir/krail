package uk.co.q3c.v7.base.shiro;

import java.io.Serializable;

import org.apache.shiro.authz.UnauthenticatedException;

import uk.co.q3c.v7.base.user.notify.UserNotifier;
import uk.co.q3c.v7.base.user.notify.UserNotifier.NotificationType;
import uk.co.q3c.v7.i18n.DescriptionKey;

import com.google.inject.Inject;

public class DefaultUnauthenticatedExceptionHandler implements UnauthenticatedExceptionHandler, Serializable {

	private final UserNotifier notifier;

	@Inject
	protected DefaultUnauthenticatedExceptionHandler(UserNotifier notifier) {
		super();
		this.notifier = notifier;
	}

	@Override
	public void onUnauthenticatedException(UnauthenticatedException throwable) {
		notifier.show(NotificationType.WARNING, DescriptionKey.You_have_not_logged_in);
	}

}
