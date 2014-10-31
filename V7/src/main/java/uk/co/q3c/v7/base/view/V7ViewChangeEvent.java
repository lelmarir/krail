package uk.co.q3c.v7.base.view;

import uk.co.q3c.v7.base.navigate.V7Navigator;
import uk.co.q3c.v7.base.navigate.sitemap.NavigationState;

public interface V7ViewChangeEvent {

	public static interface CancellableV7ViewChangeEvent extends V7ViewChangeEvent {

		boolean isCancelled();
		void cancel();
		
	}
	
	V7Navigator getNavigator();
	NavigationState getOldNavigationState();
	NavigationState getNewNavigationState();
	
}
