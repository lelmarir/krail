package uk.q3c.krail.core.navigate.sitemap.impl;

import java.util.HashSet;
import java.util.NoSuchElementException;

import uk.q3c.krail.core.view.KrailView;

public class LooplessCalculatedParametersWrapper extends ParametersImpl {

	public static LooplessCalculatedParametersWrapper build(
			ParametersImpl parameters, String parameterKey) {
		if (parameters instanceof LooplessCalculatedParametersWrapper) {
			LooplessCalculatedParametersWrapper looplessCalculatedParametersWrapper = (LooplessCalculatedParametersWrapper) parameters;
			looplessCalculatedParametersWrapper.pendingCalculatedParameters
					.add(parameterKey);
			return looplessCalculatedParametersWrapper;
		} else {
			return new LooplessCalculatedParametersWrapper(parameters,
					parameterKey);
		}
	}

	private HashSet<String> pendingCalculatedParameters;

	private LooplessCalculatedParametersWrapper(ParametersImpl parameters,
			String currentCalculatedParameterId) {
		super(parameters);
		this.pendingCalculatedParameters = new HashSet<>();
		this.pendingCalculatedParameters.add(currentCalculatedParameterId);
	}

	@Override
	protected Object calculateParameter(String parameterKey, KrailView view)
			throws NoSuchElementException {
		if (pendingCalculatedParameters.contains(parameterKey)) {
			throw new NoSuchElementException();
		} else {
			return super.calculateParameter(parameterKey, view);
		}
	}
}
