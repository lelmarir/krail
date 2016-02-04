package uk.q3c.krail.core.navigate;

public class DerivedCalculatedParameterImpl<T> extends CalculatedParameter {
	private String dependOnParemeterId;
	private ParameterCalculator<T> calculator;

	public DerivedCalculatedParameterImpl(ParameterCalculator<T> calculator,
			String dependOnParemeterId) {
		this.dependOnParemeterId = dependOnParemeterId;
		this.calculator = calculator;
	}
	
	@Override
	public boolean canCalculate() {
		return getParameters().contains(dependOnParemeterId);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object getValue() {
		return calculator.derive((T) getParameters().get(dependOnParemeterId));
	}
}
