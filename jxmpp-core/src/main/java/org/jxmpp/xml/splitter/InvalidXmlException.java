/**
 *
 * Copyright © 2021 Florian Schmaus
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
package org.jxmpp.xml.splitter;

import java.io.IOException;

public abstract class InvalidXmlException extends IOException {

	private static final long serialVersionUID = 1L;

	private final char unexpectedChar;
	private final String xml;

	protected InvalidXmlException(CharSequence message, char unexpectedChar, CharSequence xml) {
		super(message.toString());
		this.unexpectedChar = unexpectedChar;
		this.xml = xml.toString();
	}

	public char getUnexpectedChar() {
		return unexpectedChar;
	}

	public String getParsedXmlSoFar() {
		return xml;
	}

	public static final class InvalidEmptyTagException extends InvalidXmlException {

		private static final long serialVersionUID = 1L;

		private InvalidEmptyTagException(CharSequence message, char c, CharSequence xml) {
			super(message, c, xml);
		}

		public static InvalidEmptyTagException create(char c, CharSequence xml) {
			StringBuilder message = new StringBuilder();
			message.append("Invalid empty tag, expected '>', but got '").append(c).append("'. Parsed xml so far: ")
					.append(xml);
			return new InvalidEmptyTagException(message, c, xml);
		}
	}

	public static final class InvalidAttributeDeclarationException extends InvalidXmlException {

		private static final long serialVersionUID = 1L;

		private InvalidAttributeDeclarationException(CharSequence message, char c, CharSequence xml) {
			super(message, c, xml);
		}

		public static InvalidAttributeDeclarationException create(char c, CharSequence xml) {
			StringBuilder message = new StringBuilder();
			message.append("Invalid attribute declaration, expected ''' or '\"', but got '").append(c).append("'. Parsed xml so far: ")
					.append(xml);
			return new InvalidAttributeDeclarationException(message, c, xml);
		}
	}
}
