package uk.q3c.krail.core.navigate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafeVaadinNavigatorWrapper extends VaadinNavigatorWrapper{

	private static final Logger LOGGER = LoggerFactory.getLogger(SafeVaadinNavigatorWrapper.class);
	
	public SafeVaadinNavigatorWrapper(DefaultNavigator navigator) {
		super(navigator);
	}
	
	@Override
	public void navigateTo(String navigationState) {
		try {
		super.navigateTo(navigationState);
		}catch (Throwable e) {
			LOGGER.error("Exception while navigation, will be suppressed and navigation cancelled:", e);
		}
	}

}
