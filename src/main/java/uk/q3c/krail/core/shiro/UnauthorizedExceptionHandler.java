package uk.q3c.krail.core.shiro;

import org.apache.shiro.authz.UnauthorizedException;


public interface UnauthorizedExceptionHandler {
	/**
	 * Returns true if exception is handled
	 * @param throwable 
	 * 
	 * @param exception
	 * @return
	 */
	void onUnauthorizedException(UnauthorizedException throwable);

}
