package uk.q3c.krail.core.navigate;

import uk.q3c.krail.core.view.KrailView;
import uk.q3c.krail.core.view.KrailViewChangeEvent;
import uk.q3c.krail.core.view.KrailViewChangeEvent.CancellableKrailViewChangeEvent;
import uk.q3c.krail.core.view.KrailViewChangeEventImpl.CancellableWrapper;

public interface NavigationCallbackHandler {

	void beforeOutboundNavigationEvent(KrailView view,
			CancellableWrapper cancellable);

	void beforeInboundNavigationEvent(KrailView view,
			CancellableKrailViewChangeEvent cancellable);

	void afterInbounNavigationEvent(KrailView view, KrailViewChangeEvent event);

}
