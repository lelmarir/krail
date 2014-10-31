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


public class InvalidURIException extends PageNotFoundException {
	
	private static final long serialVersionUID = -2965666735662621325L;

	public InvalidURIException(String uri) {
		this(uri, null);
	}

	public InvalidURIException(String uri, PageNotFoundException cause) {
		super("Unable to find the uri '"+uri+"'", cause);
	}
}
