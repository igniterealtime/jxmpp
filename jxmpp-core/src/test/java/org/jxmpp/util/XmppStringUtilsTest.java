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
package org.jxmpp.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.jxmpp.util.XmppStringUtils.parseDomain;

public class XmppStringUtilsTest {

	@Test
	public void parseLocalpart() {
		final String error = "Error parsing localpart";
		assertEquals(error, null, XmppStringUtils.parseLocalpart("yahoo.myjabber.net"));
		assertEquals(error, null, XmppStringUtils.parseLocalpart("yahoo.myjabber.net/registred"));
		assertEquals(error, "user", XmppStringUtils.parseLocalpart("user@yahoo.myjabber.net/registred"));
		assertEquals(error, "user", XmppStringUtils.parseLocalpart("user@yahoo.myjabber.net"));

		assertEquals(error, "", XmppStringUtils.parseLocalpart("@example.org/"));

		// Some more advanced parsing cases
		assertEquals(error, null, XmppStringUtils.parseLocalpart("foo.jxmpp.org/resOne@resTwo"));
	}

	@Test
	public void parseDomainTest() {
		final String error = "Error parsing domain";
		final String result = "yahoo.myjabber.net";
		assertEquals(error, result, parseDomain("yahoo.myjabber.net"));
		assertEquals(error, result, parseDomain("yahoo.myjabber.net/registred"));
		assertEquals(error, result, parseDomain("user@yahoo.myjabber.net/registred"));
		assertEquals(error, result, parseDomain("user@yahoo.myjabber.net"));
	}

	@Test
	public void parseDomainCornerCases() {
		assertEquals("", parseDomain("/foo@"));
		assertEquals("", parseDomain("localpart@"));
		assertEquals("example.org", parseDomain("example.org/foo@"));
		assertEquals("foo.jxmpp.org", parseDomain("foo.jxmpp.org/resOne@resTwo"));
	}

	@Test
	public void parseResource() {
		final String error = "Error parsing resource";
		assertEquals(error, null, XmppStringUtils.parseResource("foo.jxmpp.org"));
		assertEquals(error, "registered", XmppStringUtils.parseResource("foo.jxmpp.org/registered"));
		assertEquals(error, "registered", XmppStringUtils.parseResource("user@foo.jxmpp.org/registered"));
		assertEquals(error, null, XmppStringUtils.parseResource("user@foo.jxmpp.org"));

		assertEquals(error, "", XmppStringUtils.parseResource("@example.org/"));

		// Some more advanced parsing cases
		assertEquals(error, "resOne@resTwo", XmppStringUtils.parseResource("user@foo.jxmpp.org/resOne@resTwo"));
	}

	@Test
	public void escapeLocalpart() {
		String specialChars = " \"&'/:<>@\\_string";
		assertEquals("\\20\\22\\26\\27\\2f\\3a\\3c\\3e\\40\\5c_string", XmppStringUtils.escapeLocalpart(specialChars));

		// is null-safe
		assertNull(XmppStringUtils.escapeLocalpart(null));
	}

	@Test
	public void escapeLocalpart_whitespaces() {
		// non ascii spaces, according to RFC 7613, Section 7.3, have to be mapped to '\20' as well
		char[] whiteSpaces = {
				'\u00A0', // NO-BREAK SPACE
				'\u2007', // FIGURE SPACE
				'\u202F', // NARROW NO-BREAK SPACE
				'\t', // CHARACTER TABULATION
				'\n', // LINE FEED (LF)
				'\u000B', // LINE TABULATION
				'\f', // FORM FEED (FF)
				'\r', // CARRIAGE RETURN (CR)
				'\u001C', // INFORMATION SEPARATOR FOUR
				'\u001D', // INFORMATION SEPARATOR THREE
				'\u001E', // INFORMATION SEPARATOR TWO
				'\u001F' // INFORMATION SEPARATOR ONE
		};
		for (int i = 0; i < whiteSpaces.length; i++) {
			char whiteSpace = whiteSpaces[i];
			String whiteSpaceStr = new String(new char[]{whiteSpace});
			String message = "character: index:" + i + ", name:" + Character.getName(whiteSpace);
			assertEquals(message,"\\20", XmppStringUtils.escapeLocalpart(whiteSpaceStr));
		}
	}

	@Test
	public void unescapeLocalpart() {
		String escapedString = "\\20\\22\\26\\27\\2f\\3a\\3c\\3e\\40\\5c";
		String specialChars = " \"&'/:<>@\\";
		assertEquals(specialChars, XmppStringUtils.unescapeLocalpart(escapedString));

		// is null-safe
		assertNull(XmppStringUtils.unescapeLocalpart(null));
	}
}
