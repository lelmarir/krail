package uk.q3c.krail.core.navigate;

import uk.q3c.krail.core.navigate.sitemap.NavigationState;
import uk.q3c.krail.core.navigate.sitemap.NavigationState.Parameters;

public abstract class CalculatedParameter {

	public interface ParameterCalculator<E> {
		Object derive(E baseValue);
	}
	
	private NavigationState.Parameters parameters;

	protected NavigationState.Parameters getParameters() {
		assert parameters != null;
		return parameters;
	}
	
	public void setParameters(Parameters parameters){
		this.parameters = parameters;
	}

	public boolean canCalculate(){
		return true;
	}

	public abstract Object getValue();

	@Override
	public String toString() {
		return getValue().toString();
	}

}