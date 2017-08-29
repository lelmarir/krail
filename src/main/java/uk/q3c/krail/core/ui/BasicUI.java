package uk.q3c.krail.core.ui;

import com.google.inject.Inject;
import com.vaadin.annotations.Theme;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.VerticalLayout;

// @PreserveOnRefresh
@Theme("chameleon")
public class BasicUI extends ScopedUI {

	@Inject
	protected BasicUI() {
		super();

	}

	@Override
	protected AbstractOrderedLayout screenLayout() {
		return new VerticalLayout(getViewDisplayPanel());
	}

	@Override
	protected String pageTitle() {
		return "Krail base";
	}

}