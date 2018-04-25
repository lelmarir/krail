package uk.q3c.krail.core.guice;

import javax.servlet.annotation.WebFilter;

@WebFilter(urlPatterns = "/*", asyncSupported = true)
public class GuiceFilter extends com.google.inject.servlet.GuiceFilter {

}
