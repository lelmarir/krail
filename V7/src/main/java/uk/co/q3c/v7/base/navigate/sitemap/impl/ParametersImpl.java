package uk.co.q3c.v7.base.navigate.sitemap.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import uk.co.q3c.v7.base.navigate.sitemap.NavigationState.Parameters;

public class ParametersImpl implements Parameters {

	private Map<String, Object> parameters = new LinkedHashMap<>();
	
	public ParametersImpl() {
	}
	
	@Override
	public Object set(String id, Object value) {
		return parameters.put(id, value);
	}

	@Override
	public String getAsString(String id) {
		return formatAsString(get(id));
	}

	private String formatAsString(Object object) {
		return object!=null?object.toString():null;
	}

	private Object get(String id) {
		return parameters.get(id);
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "@" + parameters.toString();
	}

	@Override
	public boolean contains(String id) {
		return parameters.containsKey(id);
	}
	
}
