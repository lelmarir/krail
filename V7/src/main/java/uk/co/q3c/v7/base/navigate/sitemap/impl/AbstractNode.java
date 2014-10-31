package uk.co.q3c.v7.base.navigate.sitemap.impl;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.catalina.ha.deploy.FarmWarDeployer;

import uk.co.q3c.v7.base.navigate.sitemap.NavigationState;
import uk.co.q3c.v7.base.navigate.sitemap.SitemapNode;
import uk.co.q3c.v7.base.navigate.sitemap.NavigationState.Parameters;
import uk.co.q3c.v7.base.view.V7View;

public abstract class AbstractNode implements SitemapNode {

	private static final Pattern PARAMETER_PATTERN = Pattern
			.compile("\\{(\\w*)(?::(.*))?\\}");
	private static final String DEFAULT_PARAM_CONSTRAINT = "\\w*";

	private String rawUriPattern;
	private Pattern pattern;
	private LinkedList<String> parametersId;
	

	public AbstractNode(String uri) {
		this.rawUriPattern = uri;
		this.parametersId = new LinkedList<>();

		Matcher m = PARAMETER_PATTERN.matcher(uri);
		StringBuffer s = new StringBuffer();
		int lastAppendPosition = 0;
		while (m.find()) {
			parametersId.add(m.group(1));
			String paramConstraint = m.group(2);
			if (paramConstraint == null || paramConstraint.isEmpty()) {
				paramConstraint = DEFAULT_PARAM_CONSTRAINT;
			}
			s.append(Pattern.quote(uri.substring(lastAppendPosition,
					m.start())));
			s.append("(" + paramConstraint + ")");
			lastAppendPosition = m.end();
		}
		if (lastAppendPosition <= uri.length()) {
			s.append(uri.substring(lastAppendPosition));
		}

		pattern = Pattern.compile(s.toString());
	}
	
	 @Override
	public String getUriPattern() {
		return rawUriPattern;
	}

	public NavigationState match(String fragment) {
		assert fragment != null;
		Matcher m = pattern.matcher(fragment);
		if (m.matches()) {
			Parameters params = new ParametersImpl();
			for(int i = 0; i < parametersId.size(); i++) {
				String id = parametersId.get(i);
				Object value = m.group(i+1);//groups are 1 based
				params.setParameter(id, value);
			}
			return new NavigationStateImpl(this, params);
			
		} else {
			return null;
		}
	}
	
	public String buildFragment(Parameters parameters) {
		Matcher m = PARAMETER_PATTERN.matcher(rawUriPattern);
		StringBuffer s = new StringBuffer();
		while(m.find()) {
			m.appendReplacement(s, parameters.getAsAtring(m.group(1)));
		}
		m.appendTail(s);
		
		return s.toString();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "@" + "{uri="+rawUriPattern+"}";
	}

	@Override
	public abstract Class<? extends V7View> getViewClass();
}
