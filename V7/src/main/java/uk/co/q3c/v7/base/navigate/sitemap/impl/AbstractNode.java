package uk.co.q3c.v7.base.navigate.sitemap.impl;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.q3c.v7.base.navigate.sitemap.NavigationState;
import uk.co.q3c.v7.base.navigate.sitemap.SitemapNode;
import uk.co.q3c.v7.base.navigate.sitemap.NavigationState.Parameters;
import uk.co.q3c.v7.base.view.V7View;

public abstract class AbstractNode implements SitemapNode {

	private static final Pattern OPTIONAL_GROUP_PATTERN = Pattern.compile("\\[(.*)\\]");
	private static final Pattern PARAMETER_PATTERN = Pattern
			.compile("\\{(\\w*)(?::(.*))?\\}");
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
		Matcher m = OPTIONAL_GROUP_PATTERN.matcher(string);
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			StringBuffer before = pharse(string.subSequence(lastAppendPosition, m.start()));
			sb.append(before);
			sb.append("(?:" + pharse(m.group(1)) + ")?");
			lastAppendPosition = m.end();
		}
		sb.append(string.subSequence(lastAppendPosition, string.length()));
		
		if(lastAppendPosition != 0) {
			//found at least 1 optional group, may there be some nested
			sb = pharse(sb);
		}
		
		//now there are no more optionals groups, search for parameters
		lastAppendPosition = 0;
		m = PARAMETER_PATTERN.matcher(sb.toString());
		string = sb;
		sb = new StringBuffer();
		while(m.find()) {
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
		if(lastAppendPosition == 0) {
			//dont escape if no match
			sb.append(string.subSequence(lastAppendPosition, string.length()));
		}else{
			sb.append(escape(string.subSequence(lastAppendPosition, string.length())));
		}
		
		return sb;
	}

	private static StringBuffer escape(CharSequence charSequence) {
		if(charSequence.length() == 0) {
			return new StringBuffer();
		}
		
		StringBuffer sb = new StringBuffer(4 + charSequence.length());
		sb.append("\\Q");;
		sb.append(charSequence);
		sb.append("\\E");
		return sb;
	}

	/**
	 * Check that every [ has a correponding ]
	 * @param s 
	 */
	private void chechMatchingBrakesPairs(StringBuffer s) {
		int count = 0;
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(c == '[') {
				count ++;
			}else if(c == ']'){
				count --;
			}
			
			if(count > 1) {
				throw new IllegalArgumentException("This implementation does not allow nested optional groups (yet)");
			}
		}
		if(count != 0) {
			throw new IllegalArgumentException("Something wrong with optionals groups (opening brackets does not match closing count)");
		}
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
				params.set(id, value);
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
			String value = parameters.getAsString(m.group(1));
			m.appendReplacement(s, value!=null?value:"");
		}
		m.appendTail(s);
		
		//FIXME: strip out optionals groups if the contained parameter value is null
		int startIndex = -1;
		while((startIndex = s.indexOf("[")) != -1){
			s.replace(startIndex, startIndex+1, "");
		}
		while((startIndex = s.indexOf("]")) != -1){
			s.replace(startIndex, startIndex+1, "");
		}
		
		return s.toString();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "@" + "{pattern="+rawUriPattern+"}";
	}

	@Override
	public abstract Class<? extends V7View> getViewClass();
}
