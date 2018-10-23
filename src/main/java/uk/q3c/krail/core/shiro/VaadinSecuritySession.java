package uk.q3c.krail.core.shiro;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.google.inject.Inject;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.util.CurrentInstance;

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
		try {
			return VaadinUtils.runInSession(session, s -> s.getAttribute(name)).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
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
