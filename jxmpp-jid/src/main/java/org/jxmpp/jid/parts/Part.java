/**
 *
 * Copyright © 2014 Florian Schmaus
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
package org.jxmpp.jid.parts;

import java.io.Serializable;

import org.jxmpp.stringprep.XmppStringprepException;

public abstract class Part implements CharSequence, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private final String part;

	protected Part(String part) {
		this.part = part;
	}

	@Override
	public final int length() {
		return part.length();
	}

	@Override
	public final char charAt(int index) {
		return part.charAt(index);
	}

	@Override
	public final CharSequence subSequence(int start, int end) {
		return part.subSequence(start, end);
	}

	@Override
	public final String toString() {
		return part;
	}

	@Override
	public final boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		return part.equals(other.toString());
	}

	@Override
	public final int hashCode() {
		return part.hashCode();
	}

	protected static void assertNotLongerThan1023BytesOrEmpty(String string) throws XmppStringprepException {
		char[] bytes = string.toCharArray();

		// Better throw XmppStringprepException instead of IllegalArgumentException here, because users don't expect an
		// IAE and it also makes the error handling for users easier.
		if (bytes.length > 1023) {
			throw new XmppStringprepException(string, "Given string is longer then 1023 bytes");
		} else if (bytes.length == 0) {
			throw new XmppStringprepException(string, "Argument can't be the empty string");
		}
	}

	/**
	 * The cache holding the internalized value of this part. This needs to be transient so that the
	 * cache is recreated once the data was de-serialized.
	 */
	private transient String internalizedCache;

	/**
	 * Returns the canonical String representation of this Part. See {@link String#intern} for details.
	 * 
	 * @return the canonical String representation.
	 */
	public final String intern() {
		if (internalizedCache == null) {
			internalizedCache = toString().intern();
		}
		return internalizedCache;
	}
}
