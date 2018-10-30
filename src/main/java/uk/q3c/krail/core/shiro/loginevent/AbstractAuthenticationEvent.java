package uk.q3c.krail.core.shiro.loginevent;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

import com.vaadin.ui.UI;

public abstract class AbstractAuthenticationEvent implements AuthenticationEvent {

	public static class SuccesfulLoginEventImpl extends AbstractAuthenticationEvent implements SuccesfulLoginEvent {
		public SuccesfulLoginEventImpl(UI sourceUI, Subject subject, AuthenticationToken token,
				AuthenticationInfo info) {
			super(sourceUI, subject);
		}
	}

	public static class FailedLoginEventImpl extends AbstractAuthenticationEvent implements FailedLoginEvent {
		private final AuthenticationException exception;
		private final AuthenticationToken token;

		public FailedLoginEventImpl(UI sourceUI, Subject subject, AuthenticationToken token,
				AuthenticationException ae) {
			super(sourceUI, subject);
			this.token = token;
			this.exception = ae;
		}

		@Override
		public AuthenticationException getException() {
			return exception;
		}
		
		@Override
		public AuthenticationToken getToken() {
			return token;
		}
	}

	public static class LogoutEventImpl extends AbstractAuthenticationEvent implements LogoutEvent {
		private final PrincipalCollection loggedOutSubjectPrincipals;

		public LogoutEventImpl(UI sourceUI, Subject subject, PrincipalCollection loggedOutSubjectPrincipals) {
			super(sourceUI, subject);
			this.loggedOutSubjectPrincipals = loggedOutSubjectPrincipals;
		}

		@Override
		public PrincipalCollection getLoggedOutSubjectPrincipals() {
			return loggedOutSubjectPrincipals;
		}
	}

	private final Subject subject;
	private final UI sourceUI;

	public AbstractAuthenticationEvent(UI sourceUI, Subject subject) {
		this.subject = subject;
		this.sourceUI = sourceUI;
	}

	@Override
	public Subject getSubject() {
		return subject;
	}

	@Override
	public UI getSourceUI() {
		return sourceUI;
	}

}
