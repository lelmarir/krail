package uk.q3c.krail.core.navigate.sitemap.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.NoSuchElementException;

import uk.q3c.krail.core.navigate.parameters.Parameters;

public class ParametersImpl implements Parameters {

	private Map<String, Object> parameters = new LinkedHashMap<>();

	public ParametersImpl() {
	}

	@Override
	public Object put(String id, Object value) {
		if (value != null) {
			return parameters.put(id, value);
		} else {
			return parameters.remove(id);
		}
	}

	@Override
	public Object get(String id) throws NoSuchElementException {
		Object value = parameters.get(id);
		if (value == null) {
			throw new NoSuchElementException("for parameter '" + id + "'");
		}
		return value;
	}

	@Override
	public String getAsString(String id) throws NoSuchElementException {
		return formatAsString(get(id));
	}

	private String formatAsString(Object object) {
		return object != null ? object.toString() : null;
	}

	@Override
	public boolean contains(String id) {
		return parameters.containsKey(id);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "@" + parameters.entrySet()
				.stream()
				.map(entry -> entry.getKey() + "='" + entry.getValue() + "'("
						+ entry.getValue().getClass().getSimpleName() + ")")
				.collect(Collectors.joining(",", "{", "}"));
	}

}
