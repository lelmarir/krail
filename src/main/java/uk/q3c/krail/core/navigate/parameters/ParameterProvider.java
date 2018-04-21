package uk.q3c.krail.core.navigate.parameters;

public interface ParameterProvider<T> {
	
	T get(Parameters parameters) throws UnsupportedOperationException;
	
}
