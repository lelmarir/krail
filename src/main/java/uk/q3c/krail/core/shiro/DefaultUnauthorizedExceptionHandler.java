package uk.q3c.krail.core.shiro;

import java.io.Serializable;

import org.apache.shiro.authz.UnauthorizedException;

import uk.q3c.krail.core.user.notify.UserNotifier;
import uk.q3c.krail.core.user.notify.UserNotifier.NotificationType;
import uk.q3c.krail.i18n.DescriptionKey;

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
