package uk.q3c.krail.core.navigate.parameters;

import java.util.NoSuchElementException;

public interface Parameters {

	Object get(String id) throws NoSuchElementException;

	Object put(String id, Object value);

	String getAsString(String id) throws NoSuchElementException;

	boolean contains(String id);

}