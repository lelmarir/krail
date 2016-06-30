package uk.q3c.krail.core.navigate.sitemap;

import java.util.Arrays;
import java.util.Collection;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.subject.Subject;

import com.google.common.base.Joiner;

public interface AccesControl {

	static class Public implements AccesControl {

		@Override
		public void checkAuthorization(Subject subject)
				throws AuthorizationException {
			;
		}
		
		@Override
		public String toString() {
			return "Public";
		}

	}

	static class Authenticated implements AccesControl {

		@Override
		public void checkAuthorization(Subject subject)
				throws AuthorizationException {
			if (!subject.isAuthenticated()) {
				throw new UnauthenticatedException();
			}
		}

		@Override
		public String toString() {
			return "Authenticated";
		}
	}

	static class Guest implements AccesControl {

		@Override
		public void checkAuthorization(Subject subject)
				throws AuthorizationException {
			if (!((!subject.isAuthenticated()) && (!subject.isRemembered()))) {
				throw new UnauthorizedException();
			}
		}

		@Override
		public String toString() {
			return "Guest";
		}
	}

	static class Permission implements AccesControl {

		private String[] permissions;
		private Logical logical;

		public Permission(String[] permissions, Logical logical) {
			this.permissions = permissions;
			this.logical = logical;
		}

		@Override
		public void checkAuthorization(Subject subject)
				throws AuthorizationException {
			if (permissions.length == 1) {
				subject.checkPermission(permissions[0]);
				return;
			}
			if (Logical.AND.equals(logical)) {
				subject.checkPermissions(permissions);
				return;
			}
			if (Logical.OR.equals(logical)) {
				// Avoid processing exceptions unnecessarily - "delay" throwing
				// the exception by calling hasRole first
				boolean hasAtLeastOnePermission = false;
				for (String permission : permissions) {
					if (subject.isPermitted(permission)) {
						hasAtLeastOnePermission = true;
					}
				}
				// Cause the exception if none of the role match, note that the
				// exception message will be a bit misleading
				if (!hasAtLeastOnePermission)
					subject.checkPermission(permissions[0]);
			}
		}
		
		@Override
		public String toString() {
			return "Permission("+Joiner.on(" "+logical.name()+" ").join(permissions)+")";
		}

	}

	static class Roles implements AccesControl {

		private final String[] roles;
		private final Logical logical;

		public Roles(String[] roles, Logical logical) {
			if(roles == null) {
				throw new IllegalArgumentException("roles should not be null");
			}
			if(logical == null) {
				throw new IllegalArgumentException("logical should not be null");
			}
			this.roles = roles;
			this.logical = logical;
		}

		@Override
		public void checkAuthorization(Subject subject)
				throws AuthorizationException {
			if (roles.length == 1) {
	            subject.checkRole(roles[0]);
	            return;
	        }
	        if (Logical.AND.equals(logical)) {
	        	subject.checkRoles(Arrays.asList(roles));
	            return;
	        }
	        if (Logical.OR.equals(logical)) {
	            // Avoid processing exceptions unnecessarily - "delay" throwing the exception by calling hasRole first
	            boolean hasAtLeastOneRole = false;
	            for (String role : roles) if (subject.hasRole(role)) hasAtLeastOneRole = true;
	            // Cause the exception if none of the role match, note that the exception message will be a bit misleading
	            if (!hasAtLeastOneRole) subject.checkRole(roles[0]);
	        }
		}

		@Override
		public String toString() {
			return "Roles("+Joiner.on(" "+logical.name()+" ").join(roles)+")";
		}
	}

	/**
	 * Less strict than Authenticated. The user my be Remembered but not yet Authenticated
	 */
	static class User implements AccesControl {

		@Override
		public void checkAuthorization(Subject subject)
				throws AuthorizationException {
			if(!(subject.isAuthenticated() || subject.isRemembered())) {
				throw new UnauthorizedException();
			}
		}

		@Override
		public String toString() {
			return "User";
		}
	}

	public static final Public PUBLIC = new Public();
	public static final Authenticated AUTHENTICATED = new Authenticated();
	public static final Guest GUEST = new Guest();
	public static final User USER = new User();

	public abstract void checkAuthorization(Subject subject)
			throws AuthorizationException;

}
