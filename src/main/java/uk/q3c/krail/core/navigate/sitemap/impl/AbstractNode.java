package uk.q3c.krail.core.navigate.sitemap.impl;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.q3c.krail.core.navigate.parameters.Parameters;
import uk.q3c.krail.core.navigate.sitemap.NavigationState;
import uk.q3c.krail.core.navigate.sitemap.SitemapNode;
import uk.q3c.krail.core.view.KrailView;

public abstract class AbstractNode implements SitemapNode {

	private static final Pattern OUTER_OPTIONAL_GROUP_PATTERN = Pattern.compile("\\[(.*)\\]");
	private static final Pattern INNER_OPTIONAL_GROUP_PATTERN = Pattern.compile("\\[([^\\[\\]]*)\\]");
	private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\{(\\w*)(?::(.*))?\\}");
	private static final String DEFAULT_PARAM_CONSTRAINT = "\\w*";

	private String rawUriPattern;
	private Pattern pattern;
	private LinkedList<String> parametersId;

	public AbstractNode(String uri) {
		this.rawUriPattern = uri;
		this.parametersId = new LinkedList<>();

		pattern = Pattern.compile(pharse(rawUriPattern).toString());
	}

	private StringBuffer pharse(CharSequence string) {
		int lastAppendPosition = 0;
		Matcher m = OUTER_OPTIONAL_GROUP_PATTERN.matcher(string);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			StringBuffer before = pharse(string.subSequence(lastAppendPosition, m.start()));
			sb.append(before);
			sb.append("(?:" + pharse(m.group(1)) + ")?");
			lastAppendPosition = m.end();
		}
		sb.append(string.subSequence(lastAppendPosition, string.length()));

		if (lastAppendPosition != 0) {
			// found at least 1 optional group, may there be some nested
			sb = pharse(sb);
		}

		// now there are no more optionals groups, search for parameters
		lastAppendPosition = 0;
		m = PARAMETER_PATTERN.matcher(sb.toString());
		string = sb;
		sb = new StringBuffer();
		while (m.find()) {
			StringBuffer before = escape(string.subSequence(lastAppendPosition, m.start()));
			sb.append(before);
			parametersId.add(m.group(1));
			String paramConstraint = m.group(2);
			if (paramConstraint == null || paramConstraint.isEmpty()) {
				paramConstraint = DEFAULT_PARAM_CONSTRAINT;
			}
			sb.append("(" + paramConstraint + ")");
			lastAppendPosition = m.end();
		}
		if (lastAppendPosition == 0) {
			// dont escape if no match
			sb.append(string.subSequence(lastAppendPosition, string.length()));
		} else {
			sb.append(escape(string.subSequence(lastAppendPosition, string.length())));
		}

		return sb;
	}

	private static StringBuffer escape(CharSequence charSequence) {
		if (charSequence.length() == 0) {
			return new StringBuffer();
		}

		StringBuffer sb = new StringBuffer(4 + charSequence.length());
		sb.append("\\Q");
		sb.append(charSequence);
		sb.append("\\E");
		return sb;
	}

	@Override
	public String getUriPattern() {
		return rawUriPattern;
	}

	@Override
	public NavigationState buildNavigationState(String fragment) {
		assert fragment != null;
		Matcher m = pattern.matcher(fragment);
		if (m.matches()) {
			Parameters params = new ParametersImpl(getViewClass());
			for (int i = 0; i < parametersId.size(); i++) {
				String id = parametersId.get(i);
				Object value = m.group(i + 1);// groups are 1 based
				params.put(id, value);
			}
			return buildNavigationState(params);
		} else {
			return null;
		}
	}

	@Override
	public abstract NavigationState buildNavigationState(Parameters params);

	public String buildFragment(KrailView viewInstance, Parameters parameters) {
		return buildFragment(viewInstance, rawUriPattern, parameters, false).toString();
	}

	/**
	 * 
	 * @param viewInstance 
	 * @param pattern
	 * @param parameters
	 * @param optional
	 *            if true and no parameters are replaced in the pattern (becouse
	 *            they are null) a empty sring will be returned
	 * @return
	 */
	private CharSequence buildFragment(KrailView viewInstance, CharSequence pattern, Parameters parameters, boolean optional) {
		boolean foundNotNullParameter = false;

		int lastAppendPosition = 0;
		Matcher m = INNER_OPTIONAL_GROUP_PATTERN.matcher(pattern);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			CharSequence before = pattern.subSequence(lastAppendPosition, m.start());
			sb.append(before);
			CharSequence optionalFragment = buildFragment(viewInstance, m.group(1), parameters, true);
			if (optionalFragment.length() > 0) {
				foundNotNullParameter = true;
			}
			sb.append(optionalFragment);
			lastAppendPosition = m.end();
		}
		sb.append(pattern.subSequence(lastAppendPosition, pattern.length()));

		lastAppendPosition = 0;
		m = PARAMETER_PATTERN.matcher(sb.toString());
		pattern = sb;
		sb = new StringBuffer();
		while (m.find()) {
			CharSequence before = pattern.subSequence(lastAppendPosition, m.start());
			sb.append(before);
			String value = parameters.getAsString(m.group(1), viewInstance);
			if (value != null) {
				foundNotNullParameter = true;
			}
			sb.append(value);
			lastAppendPosition = m.end();
		}
		sb.append(pattern.subSequence(lastAppendPosition, pattern.length()));

		if (optional == true && foundNotNullParameter == false) {
			return "";
		} else {
			return sb;
		}
	}

	@Override
	public abstract Class<? extends KrailView> getViewClass();
}
