package uk.q3c.krail.core.view;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;

public interface KrailAfterViewChangeNotifier {

	/**
	 * Listen to changes of the active view.
	 * <p/>
	 * Registered listeners are invoked in registration order before (
	 * {@link ViewChangeListener#beforeViewChange(ViewChangeEvent)
	 * beforeViewChange()}) and after (
	 * {@link ViewChangeListener#afterViewChange(ViewChangeEvent)
	 * afterViewChange()}) a view change occurs.
	 * 
	 * @param listener
	 *            Listener to invoke during a view change.
	 */
	void addKrailAfterViewChangeListenerListener(KrailAfterViewChangeListener listener);

	/**
	 * Removes a view change listener.
	 * 
	 * @param listener
	 *            Listener to remove.
	 */
	void removeKrailAfterViewChangeListener(KrailAfterViewChangeListener listener);

}
