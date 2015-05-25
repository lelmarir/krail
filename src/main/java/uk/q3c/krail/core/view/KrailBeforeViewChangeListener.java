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


public interface KrailBeforeViewChangeListener {
	/**
     * Receives an event fired before an imminent view change.  At this point the event:<ol> <
     * <li><{@code fromState} represents the current navigation state/li>
     * li>{@code toState} represents the navigation state which will be moved to if the change is successful.</li></ol>
     * <p/>
     * Listeners are called in registration order. If any listener cancels the event, {@link
     * V7ViewChangeEvent#cancel()}, the rest of the listeners are not called and the view change is blocked.
	 */
	public void beforeViewChange(KrailViewChangeEvent event);
}
