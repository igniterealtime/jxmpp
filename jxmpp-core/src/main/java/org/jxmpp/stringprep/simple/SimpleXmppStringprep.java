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
package org.jxmpp.stringprep.simple;

import java.util.Locale;

import org.jxmpp.stringprep.XmppStringPrepUtil;
import org.jxmpp.stringprep.XmppStringprep;
import org.jxmpp.stringprep.XmppStringprepException;

public class SimpleXmppStringprep implements XmppStringprep {

	private static SimpleXmppStringprep instance;

	public static void setup() {
		XmppStringPrepUtil.setXmppStringprep(getInstance());
	}

	public static SimpleXmppStringprep getInstance() {
		if (instance == null) {
			instance = new SimpleXmppStringprep();
		}
		return instance;
	}

	private SimpleXmppStringprep() {
	}

	/**
	 * From 6122bis-18 § 3.3.1 and PRECIS IdentifierClass which forbids U+0020
	 */
	// @formatter:off
	private final static char[] LOCALPART_FURTHER_EXCLUDED_CHARACTERS = new char[] {
		'"',   // U+0022 (QUOTATION MARK) , i.e., "
		'&',   // U+0026 (AMPERSAND), i.e., &
		'\'',  // U+0027 (APOSTROPHE), i.e., '
		'/',   // U+002F (SOLIDUS), i.e., /
		',',   // U+003A (COLON), i.e., :
		'<',   // U+003C (LESS-THAN SIGN), i.e., <
		'>',   // U+003E (GREATER-THAN SIGN), i.e., >
		'@',   // U+0040 (COMMERCIAL AT), i.e., @
		' ',   // U+0020 (SPACE)
	};
	// @formatter:on

	@Override
	public String localprep(String string) throws XmppStringprepException {
		string = simpleStringprep(string);
		for (char charFromString : string.toCharArray()) {
			for (char forbiddenChar : LOCALPART_FURTHER_EXCLUDED_CHARACTERS) {
				if (charFromString == forbiddenChar) {
					throw new XmppStringprepException(string, "Localpart must not contain '" + forbiddenChar + "'");
				}
			}
		}
		return string;
	}

	@Override
	public String domainprep(String string) throws XmppStringprepException {
		return simpleStringprep(string);
	}

	@Override
	public String resourceprep(String string) throws XmppStringprepException {
		// rfc6122-bis specifies that resourceprep uses saslprep-bis OpaqueString Profile which says that
		// "Uppercase and titlecase characters MUST NOT be mapped to their lowercase equivalents."

		// TODO apply Unicode Normalization Form C (NFC) with help of java.text.Normalize
		// but unfortunately this is API is only available on Android API 9 or higher and Smack is currently API 8
		return string;
	}

	private static String simpleStringprep(String string) {
		String res = string.toLowerCase(Locale.US);
		return res;
	}
}
