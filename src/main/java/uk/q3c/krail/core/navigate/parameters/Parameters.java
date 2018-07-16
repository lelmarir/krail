package uk.q3c.krail.core.navigate.parameters;

import java.util.NoSuchElementException;

import uk.q3c.krail.core.navigate.sitemap.impl.ParametersImpl;
import uk.q3c.krail.core.view.KrailView;

public interface Parameters {

	public static Parameters create(Class<? extends KrailView> targetViewClass) {
		return new ParametersImpl(targetViewClass);
	}
	
	Object get(String id, KrailView view) throws NoSuchElementException;

	Object get(String id, KrailView view, boolean useCalculatedParameters)
			throws NoSuchElementException;

	Object put(String id, Object value);

	String getAsString(String id, KrailView view) throws NoSuchElementException;

	boolean contains(String id);


}