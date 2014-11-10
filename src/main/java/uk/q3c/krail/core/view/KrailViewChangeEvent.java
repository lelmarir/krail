package uk.q3c.krail.core.view;

import uk.q3c.krail.core.navigate.KrailNavigator;
import uk.q3c.krail.core.navigate.sitemap.NavigationState;

public interface KrailViewChangeEvent {

	public static interface CancellableKrailViewChangeEvent extends KrailViewChangeEvent {

		boolean isCancelled();
		void cancel();

	}

	KrailNavigator getNavigator();
	NavigationState getSourceNavigationState();
	NavigationState getTargetNavigationState();

}
