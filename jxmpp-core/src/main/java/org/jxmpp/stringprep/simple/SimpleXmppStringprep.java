/**
 *
 * Copyright © 2014-2019 Florian Schmaus
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

import java.util.Arrays;
import java.util.Locale;

import org.jxmpp.JxmppContext;
import org.jxmpp.XmppAddressParttype;
import org.jxmpp.stringprep.XmppStringprep;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.ArraysUtil;

public final class SimpleXmppStringprep implements XmppStringprep {

	private static SimpleXmppStringprep instance;

	public static final String NAME = "simple";

	/**
	 * Setup Simple XMPP Stringprep as implementation to use.
	 */
	public static void setup() {
		JxmppContext.setDefaultXmppStringprep(getInstance());
	}

	/**
	 * Get the Simple XMPP Stringprep singleton.
	 *
	 * @return the simple XMPP Stringprep singleton.
	 */
	public static SimpleXmppStringprep getInstance() {
		if (instance == null) {
			instance = new SimpleXmppStringprep();
		}
		return instance;
	}

	private SimpleXmppStringprep() {
	}

	/**
	 * From <a href="https://tools.ietf.org/html/rfc7622#section-3.3.1">RFC 7622 §
	 * 3.3.1</a>.
	 */
	// @formatter:off
	private static final char[] LOCALPART_FURTHER_EXCLUDED_CHARACTERS = new char[] {
		'"',   // U+0022 (QUOTATION MARK) , i.e., "
		'&',   // U+0026 (AMPERSAND), i.e., &
		'\'',  // U+0027 (APOSTROPHE), i.e., '
		'/',   // U+002F (SOLIDUS), i.e., /
		':',   // U+003A (COLON), i.e., :
		'<',   // U+003C (LESS-THAN SIGN), i.e., <
		'>',   // U+003E (GREATER-THAN SIGN), i.e., >
		'@',   // U+0040 (COMMERCIAL AT), i.e., @
	};
	// @formatter:on

	// @formatter:off
	private static final char[] USERNAME_CASE_MAPPED_EXCLUDED_CHARACTERS = new char[] {
		' ',   // U+0020 (SPACE) - forbidden by PRECIS IdentifierClass.
	};
	// @formatter:on

	private static final char[] LOCALPART_EXCLUDED_CHARACTERS;

	static {
		// Ensure that the char array is sorted as we use Arrays.binarySearch() on it.
		Arrays.sort(LOCALPART_FURTHER_EXCLUDED_CHARACTERS);

		// Combine LOCALPART_FURTHER_EXCLUDED_CHARACTERS and USERNAME_CASE_MAPPED_EXCLUDED_CHARACTERS into
		// LOCALPART_EXCLUDED_CHARACTERS.
		LOCALPART_EXCLUDED_CHARACTERS = ArraysUtil.concatenate(
				LOCALPART_FURTHER_EXCLUDED_CHARACTERS,
				USERNAME_CASE_MAPPED_EXCLUDED_CHARACTERS);
		Arrays.sort(LOCALPART_EXCLUDED_CHARACTERS);
	}

	@Override
	public String localprep(String string) throws XmppStringprepException {
		string = simpleStringprep(string);
		ensurePartDoesNotContain(XmppAddressParttype.localpart, string, LOCALPART_EXCLUDED_CHARACTERS);
		return string;
	}

	private static void ensurePartDoesNotContain(XmppAddressParttype parttype, String input, char[] excludedChars)
			throws XmppStringprepException {
		assert isSorted(excludedChars);

		for (char c : input.toCharArray()) {
			int forbiddenCharPos = Arrays.binarySearch(excludedChars, c);
			if (forbiddenCharPos >= 0) {
				throw new XmppStringprepException(input, parttype.getCapitalizedName() + " must not contain '"
						+ LOCALPART_FURTHER_EXCLUDED_CHARACTERS[forbiddenCharPos] + "'");
			}
		}
	}

	/**
	 * Ensure that the input string does not contain any of the further excluded characters of XMPP localparts.
	 *
	 * @param localpart the input string.
	 * @throws XmppStringprepException if one of the further excluded characters is found.
	 * @see <a href="https://tools.ietf.org/html/rfc7622#section-3.3.1">RFC 7622 § 3.3.1</a>
	 */
	public static void ensureLocalpartDoesNotIncludeFurtherExcludedCharacters(String localpart)
			throws XmppStringprepException {
		ensurePartDoesNotContain(XmppAddressParttype.localpart, localpart, LOCALPART_FURTHER_EXCLUDED_CHARACTERS);
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

	private static boolean isSorted(char[] chars) {
		for (int i = 1; i < chars.length; i++) {
			if (chars[i-1] > chars[i]) {
				return false;
			}
		}
		return true;
	}
}
