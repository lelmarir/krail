/*
 * Copyright (c) 2014 David Sowerby
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package uk.q3c.krail.core.view;

public interface KrailAfterViewChangeListener {

	/**
	 * Invoked after the view is changed. If a <code>beforeViewChange</code>
	 * method blocked the view change, this method is not called. Be careful of
	 * unbounded recursion if you decide to change the view again in the
	 * listener. Note that this is fired even if the view does not change, but
	 * the URL does (this would only happen if the same view class is used for
	 * multiple URLs). This is because some listeners actually want to know
	 * about the URL change
	 * 
	 * @param event
	 *            view change event
	 */
	public void afterViewChange(KrailViewChangeEvent event);
}
