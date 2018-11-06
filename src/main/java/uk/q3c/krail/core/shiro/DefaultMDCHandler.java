package uk.q3c.krail.core.shiro;

import java.io.IOException;

import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionExpiredException;
import com.vaadin.server.SystemMessages;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

import uk.q3c.krail.core.guice.KrailRequestInterceptor;

@Singleton
public class DefaultMDCHandler implements KrailRequestInterceptor {

	private static final String SESSION_KEY = "session";
	private static final String UI_KEY = "ui";
	private static final String SUBJECT_KEY = "subject";

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMDCHandler.class);

	@Inject
	private Provider<Subject> subjectProvider;

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
		callWithUi(() -> {
			set();
		}, request);
	}

	private static void callWithUi(Runnable runnable, VaadinRequest request) {
		VaadinService service = request.getService();

		VaadinSession session;
		try {
			session = service.findVaadinSession(request);
			assert VaadinSession.getCurrent() == session;
		} catch (ServiceException | SessionExpiredException e) {
			return;
		}

		UI ui = null;
		session.lock();
		try {
			ui = service.findUI(request);
			assert UI.getCurrent() == ui;

			if (ui != null) {
				ui.accessSynchronously(runnable);
			}
		} finally {
			try {
				session.unlock();
			} catch (Exception e) {
				LOGGER.error("Error while unlocking session", e);
				// can't call ErrorHandler, we (hopefully) don't have a lock
			}
		}
	}

	protected void set() {
		setMDCSession();
		setMDCUI();
		setMDCSubject();
	}

	@Override
	public void requestEnd(VaadinRequest request, VaadinResponse response, VaadinSession session) {
		clear();
	}

	private void setMDCSession() {
		MDC.put(SESSION_KEY, formatSession(VaadinSession.getCurrent()));
	}

	private void setMDCUI() {
		if (com.vaadin.ui.UI.getCurrent() != null) {
			MDC.put(UI_KEY, UI.getCurrent().getConnectorId());
		}
	}

	private void setMDCSubject() {
		MDC.put(SUBJECT_KEY, formatSubject(subjectProvider.get()));
	}

	private String formatSubject(Subject subject) {
		if (subject != null) {
			if(subject.isAuthenticated()) {
				Object principal = subject.getPrincipal();
				if(LOGGER.isDebugEnabled() && principal == null) {
					LOGGER.warn("The authenticated Subject has a null Principal");
				}
				return String.valueOf(principal);
			}else {
				return "guest";
			}
		} else {
			throw new IllegalArgumentException("The subject should not be null");
		}
	}

	private String formatSession(VaadinSession session) {
		return session.getSession().getId();
	}

	/**
	 * Clear all MDC states
	 */
	protected void clear() {
		MDC.remove(SESSION_KEY);
		MDC.remove(UI_KEY);
		MDC.remove(SUBJECT_KEY);
	}

}
