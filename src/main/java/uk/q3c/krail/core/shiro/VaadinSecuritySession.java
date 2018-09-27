package uk.q3c.krail.core.shiro;

import java.util.Objects;

import com.google.inject.Inject;
import com.vaadin.server.VaadinSession;

public class VaadinSecuritySession implements SecuritySession {

	private final VaadinSession session;
	
	@Inject
	public VaadinSecuritySession(VaadinSession session) {
		this.session = Objects.requireNonNull(session);
	}
	
	@Override
	public Object getSessionId() {
		return session;
	}
	
	@Override
	public Object getAttribute(String name) {
		return session.getAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		session.setAttribute(name, value);
	}
	
	@Override
	public String toString() {
		return session.toString();
	}
}
