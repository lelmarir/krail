package uk.q3c.krail.core.navigate.sitemap.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import uk.q3c.krail.core.navigate.CalculatedParameter;
import uk.q3c.krail.core.navigate.sitemap.NavigationState.Parameters;

public class ParametersImpl implements Parameters {

	private Map<String, Object> parameters = new LinkedHashMap<>();
	
	public ParametersImpl() {
	}
	
	@Override
	public Object put(String id, Object value) {
		assert value != null;
		if(value instanceof CalculatedParameter){
			((CalculatedParameter) value).setParameters(this);
		}
		return parameters.put(id, value);
	}

	@Override
	public String getAsString(String id) {
		return formatAsString(get(id));
	}

	private String formatAsString(Object object) {
		return object!=null?object.toString():null;
	}

	@Override
	public Object get(String id) {
		return get(id, false);
	}
	
	@Override
	public Object get(String id, boolean excludeCalculated) {
		Object value = parameters.get(id);
		if(value instanceof CalculatedParameter){
			if(excludeCalculated == false){
				if(((CalculatedParameter)value).canCalculate() == true){
					return ((CalculatedParameter) value).getValue();
				}else{
					return null;
				}
			}else{
				return null;
			}
		}
		return value;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "@" + parameters.toString();
	}

	@Override
	public boolean contains(String id) {
		Object parameter = parameters.get(id);
		
		if(parameter == null){
			return false;
		}
		
		if(parameter instanceof CalculatedParameter){
			return ((CalculatedParameter) parameter).canCalculate();
		}else{
			return true;
		}
	}
	
}
