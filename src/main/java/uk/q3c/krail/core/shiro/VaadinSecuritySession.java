package uk.q3c.krail.core.shiro;

import com.google.inject.Inject;
import com.vaadin.server.VaadinSession;

public class VaadinSecuritySession implements SecuritySession {

	private final VaadinSession session;
	
	@Inject
	public VaadinSecuritySession(VaadinSession session) {
		this.session = session;
	}
	
	@Override
	public Object getAttribute(String name) {
		return session.getAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		session.setAttribute(name, value);
	}

}
