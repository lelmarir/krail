package uk.q3c.krail.core.navigate;

import uk.q3c.krail.core.view.KrailView;
import uk.q3c.krail.core.view.KrailViewChangeEvent;
import uk.q3c.krail.core.view.KrailViewChangeEvent.CancellableKrailViewChangeEvent;
import uk.q3c.krail.core.view.KrailViewChangeEventImpl.CancellableWrapper;

public interface NavigationCallbackHandler<T extends KrailView> {

	void beforeOutboundNavigationEvent(T view,
			CancellableWrapper cancellable);

	void beforeInboundNavigationEvent(T view,
			CancellableKrailViewChangeEvent cancellable);

	void afterInbounNavigationEvent(T view, KrailViewChangeEvent event);

}
