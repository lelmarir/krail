package uk.co.q3c.v7.base.view;

import uk.co.q3c.v7.base.navigate.NavigationState;
import uk.co.q3c.v7.base.navigate.V7Navigator;

public class V7ViewChangeEventImpl implements V7ViewChangeEvent {
	
	public static class CancellableWrapper implements CancellableV7ViewChangeEvent {
		private V7ViewChangeEvent delegate;
		private boolean cancel = false;
		
		public CancellableWrapper(V7ViewChangeEvent event) {
			this.delegate = event;
		}
		
		@Override
		public boolean isCancelled(){
			return cancel;
		}
		
		@Override
		public void cancel(){
			this.cancel = true;
		}

		public V7Navigator getNavigator() {
			return delegate.getNavigator();
		}

		public NavigationState getOldNavigationState() {
			return delegate.getOldNavigationState();
		}

		public NavigationState getNewNavigationState() {
			return delegate.getNewNavigationState();
		}
		
	}
	
	private final V7Navigator navigator;
	private final NavigationState oldNavigationState;
	private final NavigationState newNavigationState;
	

	public V7ViewChangeEventImpl(V7Navigator navigator, NavigationState oldNavigationState, NavigationState newNavigationState) {
		super();
		this.navigator = navigator;
		this.oldNavigationState = oldNavigationState;
		this.newNavigationState = newNavigationState;
	}

	@Override
	public V7Navigator getNavigator() {
		return navigator;
	}

	@Override
	public NavigationState getOldNavigationState() {
		return oldNavigationState;
	}

	@Override
	public NavigationState getNewNavigationState() {
		return newNavigationState;
	}

}
