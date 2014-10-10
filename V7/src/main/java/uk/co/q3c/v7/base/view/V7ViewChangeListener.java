package uk.co.q3c.v7.base.view;

public interface V7ViewChangeListener {
	/**
	 * Invoked before the view is changed.
	 * <p>
	 * This method may e.g. open a "save" dialog or question about the change, which may re-initiate the navigation
	 * operation after user action.
	 * <p>
	 * If this listener does not want to block the view change (e.g. does not know the view in question), it should
	 * return true. If any listener returns false, the view change is not allowed and <code>afterViewChange()</code>
	 * methods are not called.
	 * 
	 * @param event
	 *            view change event
	 * @return true if the view change should be allowed or this listener does not care about the view change, false to
	 *         block the change
	 */
	public void beforeViewChange(V7ViewChangeEvent event);

	/**
	 * Invoked after the view is changed. If a <code>beforeViewChange</code> method blocked the view change, this method
	 * is not called. Be careful of unbounded recursion if you decide to change the view again in the listener. Note
	 * that this is fired even if the view does not change, but the URL does (this would only happen if the same view
	 * class is used for multiple URLs). This is because some listeners actually want to know about the URL change
	 * 
	 * @param event
	 *            view change event
	 */
	public void afterViewChange(V7ViewChangeEvent event);
}
