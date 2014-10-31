package uk.co.q3c.v7.base.shiro;

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
