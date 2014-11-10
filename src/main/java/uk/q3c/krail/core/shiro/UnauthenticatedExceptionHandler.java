package uk.q3c.krail.core.shiro;

import org.apache.shiro.authz.UnauthenticatedException;

public interface UnauthenticatedExceptionHandler {

	/**
	 * invoke the handler
	 * @param throwable 
	 * 
	 * @param t
	 * @return
	 */
	void onUnauthenticatedException(UnauthenticatedException throwable);

}
