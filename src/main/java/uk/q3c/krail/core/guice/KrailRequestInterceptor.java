package uk.q3c.krail.core.guice;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;

public interface KrailRequestInterceptor {

	void init();

	void destroy();

	void requestStart(VaadinRequest request, VaadinResponse response);

	void requestEnd(VaadinRequest request, VaadinResponse response,
			VaadinSession session);

}
