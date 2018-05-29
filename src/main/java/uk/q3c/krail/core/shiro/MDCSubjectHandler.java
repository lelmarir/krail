package uk.q3c.krail.core.shiro;

import java.io.IOException;

import org.apache.shiro.subject.Subject;
import org.slf4j.MDC;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;

import uk.q3c.krail.core.guice.KrailRequestHandler;

@Singleton
public class MDCSubjectHandler implements KrailRequestHandler {

	private static final String SESSION = "session";
	private static final String UI = "ui";
	private static final String SUBJECT = "subject";

	private Provider<Subject> subjectProvider;

	@Inject
	public MDCSubjectHandler(Provider<Subject> subjectProvider) {
		this.subjectProvider = subjectProvider;
	}

	@Override
	public void init() {
		;
	}

	@Override
	public void destroy() {
		;
	}

	@Override
	public void requestStart(VaadinRequest request, VaadinResponse response) {
		;
	}

	@Override
	public boolean handleRequest(VaadinSession session, VaadinRequest request,
			VaadinResponse response) throws IOException {
		session.lock();
		try {
			updateMDCSession();
			updateMDCUI();
			updateMDCSubject();

		} finally {
			session.unlock();
		}
		return false;
	}

	@Override
	public void requestEnd(VaadinRequest request, VaadinResponse response,
			VaadinSession session) {
		clear();
	}

	private void updateMDCSession() {
		MDC.put(SESSION, formatSession(VaadinSession.getCurrent()));
	}

	private void updateMDCUI() {
		if (com.vaadin.ui.UI.getCurrent() != null) {
			MDC.put(UI, com.vaadin.ui.UI.getCurrent().getConnectorId());
		}
	}

	private void updateMDCSubject() {
		MDC.put(SUBJECT, formatSubject(subjectProvider.get()));
	}

	private String formatSubject(Subject subject) {
		if (subject != null) {
			return String.valueOf(subject.getPrincipal());
		} else {
			throw new IllegalArgumentException(
					"The subject should not be null");
		}
	}

	private String formatSession(VaadinSession session) {
		return session.getSession().getId();
	}

	/**
	 * Clear all MDC states
	 */
	public void clear() {
		MDC.remove(SESSION);
		MDC.remove(UI);
		MDC.remove(SUBJECT);
	}

}
