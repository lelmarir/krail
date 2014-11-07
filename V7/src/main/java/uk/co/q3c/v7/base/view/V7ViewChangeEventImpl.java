package uk.co.q3c.v7.base.view;

import uk.co.q3c.v7.base.navigate.V7Navigator;
import uk.co.q3c.v7.base.navigate.sitemap.NavigationState;

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

		public NavigationState getSourceNavigationState() {
			return delegate.getSourceNavigationState();
		}

		public NavigationState getTargetNavigationState() {
			return delegate.getTargetNavigationState();
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
	public NavigationState getSourceNavigationState() {
		return oldNavigationState;
	}

	@Override
	public NavigationState getTargetNavigationState() {
		return newNavigationState;
	}

}
