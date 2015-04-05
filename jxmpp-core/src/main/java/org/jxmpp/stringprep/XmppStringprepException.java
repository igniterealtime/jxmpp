/**
 *
 * Copyright © 2014-2015 Florian Schmaus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jxmpp.stringprep;

import java.io.IOException;

/**
 * XMPP Stringprep exceptions signal an error when performing a particular Stringprep profile on a String.
 */
public class XmppStringprepException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8491853210107124624L;

	private final String causingString;

	/**
	 * Construct a new XMPP Stringprep exception with the given causing String and exception.
	 *
	 * @param causingString the String causing the exception.
	 * @param exception the exception.
	 */
	public XmppStringprepException(String causingString, Exception exception) {
		super("XmppStringprepException caused by '" + causingString + "': " + exception);
		initCause(exception);
		this.causingString = causingString;
	}

	/**
	 * Construct a new XMPP Stringprep exception with the given causing String and exception message.
	 *
	 * @param causingString the String causing the exception.
	 * @param message the message of the exception.
	 */
	public XmppStringprepException(String causingString, String message) {
		super(message);
		this.causingString = causingString;
	}

	/**
	 * Get the String causing the XMPP Stringprep exception.
	 *
	 * @return the causing String.
	 */
	public String getCausingString() {
		return causingString;
	}
}
