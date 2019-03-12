package uk.q3c.krail.core.view;

import uk.q3c.krail.core.navigate.Navigator;
import uk.q3c.krail.core.navigate.sitemap.NavigationState;

public interface KrailViewChangeEvent {

	public static interface CancellableKrailViewChangeEvent extends KrailViewChangeEvent {

		boolean isCancelled();
		void cancel();

	}

	Navigator getNavigator();
	NavigationState getSourceNavigationState();
	NavigationState getTargetNavigationState();
	
	default void updateUriFragment() {
		getNavigator().updateUriFragment();
	}

}
