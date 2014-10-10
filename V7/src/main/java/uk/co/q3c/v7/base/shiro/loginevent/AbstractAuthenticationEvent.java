package uk.co.q3c.v7.base.shiro.loginevent;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;

public abstract class AbstractAuthenticationEvent implements
		AuthenticationEvent {

	public static class SuccesfulLoginEventImpl extends
			AbstractAuthenticationEvent implements SuccesfulLoginEvent {
		public SuccesfulLoginEventImpl(Subject subject,
				AuthenticationToken token, AuthenticationInfo info) {
			super(subject);
		}
	}

	public static class FailedLoginEventImpl extends
			AbstractAuthenticationEvent implements FailedLoginEvent {
		public FailedLoginEventImpl(Subject subject, AuthenticationToken token,
				AuthenticationException ae) {
			super(subject);
		}
	}
	
	public static class LogoutEventImpl extends AbstractAuthenticationEvent implements LogoutEvent {
		public LogoutEventImpl(Subject subject) {
			super(subject);
		}		
	}

	private Subject subject;

	public AbstractAuthenticationEvent(Subject subject) {
		this.subject = subject;
	}

	@Override
	public Subject getSubject() {
		return subject;
	}

}
