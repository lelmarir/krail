package uk.q3c.krail.core.view;

import uk.q3c.krail.core.navigate.KrailNavigator;
import uk.q3c.krail.core.navigate.sitemap.NavigationState;

public class KrailViewChangeEventImpl implements KrailViewChangeEvent {
	
	public static class CancellableWrapper implements CancellableKrailViewChangeEvent {
		private KrailViewChangeEvent delegate;
		private boolean cancel = false;
		
		public CancellableWrapper(KrailViewChangeEvent event) {
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

		public KrailNavigator getNavigator() {
			return delegate.getNavigator();
		}

		public NavigationState getSourceNavigationState() {
			return delegate.getSourceNavigationState();
		}

		public NavigationState getTargetNavigationState() {
			return delegate.getTargetNavigationState();
		}
		
	}
	
	private final KrailNavigator navigator;
	private final NavigationState oldNavigationState;
	private final NavigationState newNavigationState;
	

	public KrailViewChangeEventImpl(KrailNavigator navigator, NavigationState oldNavigationState, NavigationState newNavigationState) {
		super();
		this.navigator = navigator;
		this.oldNavigationState = oldNavigationState;
		this.newNavigationState = newNavigationState;
	}

	@Override
	public KrailNavigator getNavigator() {
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
