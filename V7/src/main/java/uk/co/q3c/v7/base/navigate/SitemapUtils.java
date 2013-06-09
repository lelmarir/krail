/*
 * Copyright (C) 2013 David Sowerby
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.co.q3c.v7.base.navigate;

import org.apache.commons.lang.WordUtils;

/**
 * Standardise conversion between url segments, enum constants and view names
 * 
 * @author David Sowerby 4 Jun 2013
 * 
 */
public class SitemapUtils {
	/**
	 * Returns a view name converted from a segment, capitalised.<br>
	 * <br>
	 * Examples: <br>
	 * <br>
	 * <em>system-account, system_account, system account</em> all become:<br>
	 * <br>
	 * <b>SystemAccount</b> or <b>SystemAccountView</b>, depending on the value of {@code appendView}<br>
	 * <br>
	 * home becomes <b>Home</b><br>
	 * 
	 * @param segment
	 * @param appendView
	 *            if true append "View" to the name
	 * @return
	 */
	public static String viewNameFromSegment(String segment, boolean appendView) {

		if (segment.isEmpty()) {
			return "Home";
		}
		String s = segment.replace("-", " ").replace("_", " ");
		s = WordUtils.capitalizeFully(s).replace(" ", "");
		return appendView ? s + "View" : s;

	}

	/**
	 * Returns a key name converted from a segment, capitalised.<br>
	 * <br>
	 * Examples: <br>
	 * <br>
	 * <em>system-account, system_account, system account</em> all become:<br>
	 * <br>
	 * <b>System_Account</b> <br>
	 * home becomes <b>Home</b><br>
	 * 
	 * @param segment
	 * @return
	 */
	public static String keyNameFromSegment(String segment) {
		if (segment.isEmpty()) {
			return "Home";
		}
		String s = segment.replace("-", " ").replace("_", " ");
		s = WordUtils.capitalizeFully(s);
		return s.replace(" ", "_");
	}

	/**
	 * Replaces underscore with hyphen, returns all lower case
	 * 
	 * @param keyName
	 * @return
	 */
	public static String segmentFromKeyName(String keyName) {
		return keyName.replace("_", "-").toLowerCase();
	}
}
