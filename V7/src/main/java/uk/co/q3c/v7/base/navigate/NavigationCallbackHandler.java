package uk.co.q3c.v7.base.navigate;

import uk.co.q3c.v7.base.view.V7View;
import uk.co.q3c.v7.base.view.V7ViewChangeEvent;
import uk.co.q3c.v7.base.view.V7ViewChangeEvent.CancellableV7ViewChangeEvent;
import uk.co.q3c.v7.base.view.V7ViewChangeEventImpl.CancellableWrapper;

public interface NavigationCallbackHandler<T extends V7View> {

	void beforeOutboundNavigationEvent(T view,
			CancellableWrapper cancellable);

	void beforeInboundNavigationEvent(T view,
			CancellableV7ViewChangeEvent cancellable);

	void afterInbounNavigationEvent(T view, V7ViewChangeEvent event);

}
