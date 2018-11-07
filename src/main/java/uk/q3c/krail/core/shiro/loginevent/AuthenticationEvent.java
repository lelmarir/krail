package uk.q3c.krail.core.shiro.loginevent;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

import com.vaadin.shared.Registration;
import com.vaadin.ui.UI;

public interface AuthenticationEvent {
	
	public static interface AuthenticationNotifier {
		Registration addListener(AuthenticationListener listener);
		void removeListener(AuthenticationListener listener);
	}
	
	public static interface SuccesfulLoginEvent extends AuthenticationEvent {
		
	}
	
	public static interface FailedLoginEvent extends AuthenticationEvent {

		AuthenticationException getException();

		AuthenticationToken getToken();
		
	}
	
	public static interface LogoutEvent extends AuthenticationEvent {

		PrincipalCollection getLoggedOutSubjectPrincipals();
		
	}
	
	public static interface AuthenticationListener {
		/**
	     * Callback triggered when an authentication attempt for a {@code Subject} has succeeded.
	     */
	    void onSuccess(SuccesfulLoginEvent event);

	    /**
	     * Callback triggered when an authentication attempt for a {@code Subject} has failed.
	     */
	    void onFailure(FailedLoginEvent event);

	    /**
	     * Callback triggered when a {@code Subject} logs-out of the system.
	     * <p/>
	     * This method will only be triggered when a Subject explicitly logs-out of the session.  It will not
	     * be triggered if their Session times out.
	     */
	    void onLogout(LogoutEvent event);
	}

	Subject getSubject();

	/**
	 * The UI that has generated the event
	 */
	UI getSourceUI();
}
