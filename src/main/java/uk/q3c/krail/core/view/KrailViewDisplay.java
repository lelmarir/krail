package uk.q3c.krail.core.view;


/**
 * Interface for displaying a view in an appropriate location.
 * 
 * The view display can be a component/layout itself or can modify a separate layout.
 * 
 */

public interface KrailViewDisplay {

	/**
	 * Remove previously shown view and show the newly selected view in its place.
	 * 
	 * The parameters for the view have been set before this method is called.
	 * 
	 * @param view
	 *            new view to show
	 */
	public void showView(KrailView view);
}
