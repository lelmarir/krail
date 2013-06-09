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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SitemapLineParser {
	private static Logger log = LoggerFactory.getLogger(SitemapLineParser.class);

	private final String line;
	private int level = -1;
	private final int linenum;
	private String segment;
	private String view;
	private String key;

	private final Sitemap sitemap;

	private int errorCount;

	private boolean missingHyphen;

	public SitemapLineParser(Sitemap sitemap, String line, int linenum) {
		this.line = line;
		this.linenum = linenum;
		this.sitemap = sitemap;
		parse();
	}

	public int level() {
		return level;
	}

	public String segment() {
		return segment;
	}

	public String view() {
		return view;
	}

	public String key() {
		return key;
	}

	public String errorMsg_missingHyphen() {
		return "line in map must start with a'-', line " + linenum;
	}

	private void lastIndent() {
		if (!line.contains("-")) {
			log.warn(errorMsg_missingHyphen());
			errorCount++;
			missingHyphen = true;
			sitemap.error();
			return;
		}
		int index = 0;
		while (line.charAt(index) == '-') {
			index++;
		}
		level = index;
	}

	private void parse() {
		lastIndent();
		// invalid line
		if (level < 0) {
			return;
		}
		int viewStart = line.indexOf(":");
		int labelStart = line.indexOf("~");
		if ((labelStart > 0) && (viewStart > 0)) {
			if (viewStart < labelStart) {
				segment = line.substring(level, viewStart).trim();
				view = line.substring(viewStart + 1, labelStart).trim();
				key = line.substring(labelStart + 1).trim();
			} else {
				segment = line.substring(level, labelStart).trim();
				key = line.substring(labelStart + 1, viewStart).trim();
				view = line.substring(viewStart + 1).trim();
			}
		} else {
			// only label
			if (labelStart > 0) {
				segment = line.substring(level, labelStart).trim();
				key = line.substring(labelStart + 1).trim();
			}// only view
			else if (viewStart > 0) {
				segment = line.substring(level, viewStart).trim();
				view = line.substring(viewStart + 1).trim();
			}
			// only segment
			else {
				segment = line.substring(level).trim();
			}
		}

		if (view == null) {
			view = SitemapUtils.viewNameFromSegment(segment, false);
		}

		if (key == null) {
			key = SitemapUtils.keyNameFromSegment(segment);
		}
	}

	public int errorCount() {
		return errorCount;
	}

	public boolean isMissingHyphen() {
		return missingHyphen;
	}

}
