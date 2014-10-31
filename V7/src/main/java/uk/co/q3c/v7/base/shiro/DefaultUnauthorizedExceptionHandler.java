package uk.co.q3c.v7.base.shiro;

import java.io.Serializable;

import org.apache.shiro.authz.UnauthorizedException;

import uk.co.q3c.v7.base.user.notify.UserNotifier;
import uk.co.q3c.v7.base.user.notify.UserNotifier.NotificationType;
import uk.co.q3c.v7.i18n.DescriptionKey;

import com.google.inject.Inject;

public class DefaultUnauthorizedExceptionHandler implements UnauthorizedExceptionHandler, Serializable {

	private final UserNotifier notifier;

	@Inject
	protected DefaultUnauthorizedExceptionHandler(UserNotifier notifier) {
		super();
		this.notifier = notifier;
	}

	@Override
	public void onUnauthorizedException(UnauthorizedException throwable) {
		notifier.show(NotificationType.ERROR, DescriptionKey.No_Permission);
	}
}
