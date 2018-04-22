package uk.q3c.krail.core.navigate;

import uk.q3c.krail.core.navigate.parameters.Parameters;
import uk.q3c.krail.core.navigate.sitemap.impl.ParametersImpl;
import uk.q3c.krail.core.view.KrailView;

public class NavigationTarget {
	
	private final Class<? extends KrailView> view;
	private Parameters paraeters;
	
	public NavigationTarget(Class<? extends KrailView> view,
			Parameters paraeters) {
		this(view);
		this.paraeters = paraeters;
	}

	public NavigationTarget(Class<? extends KrailView> view) {
		super();
		this.view = view;
	}

	public Class<? extends KrailView> getViewClass() {
		return view;
	}
	
	public Parameters getParaeters() {
		if(paraeters == null){
			paraeters = new ParametersImpl(getViewClass());
		}
		return paraeters;
	}

	public void putParameter(String key, Object value) {
		getParaeters().put(key, value);
	}
	
	public NavigationTarget withParameter(String key, Object value) {
		putParameter(key, value);
		return this;
	}
	
}
