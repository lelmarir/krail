package uk.co.q3c.v7.base.view;

import uk.co.q3c.v7.base.navigate.NavigationState;
import uk.co.q3c.v7.base.navigate.V7Navigator;

public class V7ViewChangeEvent {
	private final V7Navigator navigator;
	private final NavigationState oldNavigationState;
	private final NavigationState newNavigationState;	
	private boolean cancel = false;

	public V7ViewChangeEvent(V7Navigator navigator, NavigationState oldNavigationState, NavigationState newNavigationState) {
		super();
		this.navigator = navigator;
		this.oldNavigationState = oldNavigationState;
		this.newNavigationState = newNavigationState;
	}

	public V7Navigator getNavigator() {
		return navigator;
	}

	public NavigationState getOldNavigationState() {
		return oldNavigationState;
	}

	public NavigationState getNewNavigationState() {
		return newNavigationState;
	}
	
	public boolean isCancelled(){
		return cancel;
	}
	
	public void cancel(){
		this.cancel = true;
	}

}
