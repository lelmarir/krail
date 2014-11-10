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

public interface AccesControl {

	static class Public implements AccesControl {

		@Override
		public void checkAuthorization(Subject subject)
				throws AuthorizationException {
			;
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

	}

	static class Guest implements AccesControl {

		@Override
		public void checkAuthorization(Subject subject)
				throws AuthorizationException {
			if (!((!subject.isAuthenticated()) && (!subject.isRemembered()))) {
				throw new UnauthorizedException();
			}
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

	}

	static class Roles implements AccesControl {

		private String[] roles;
		private Logical logical;

		public Roles(String[] roles, Logical logical) {
			this.roles = roles;
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

	}

	static class User implements AccesControl {

		@Override
		public void checkAuthorization(Subject subject)
				throws AuthorizationException {
			if(!(subject.isAuthenticated() || subject.isRemembered())) {
				throw new UnauthorizedException();
			}
		}

	}

	public static final Public PUBLIC = new Public();
	public static final Authenticated AUTHENTICATED = new Authenticated();
	public static final Guest GUEST = new Guest();
	public static final User USER = new User();

	public abstract void checkAuthorization(Subject subject)
			throws AuthorizationException;

}
