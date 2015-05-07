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
package org.jxmpp.util;

import static org.junit.Assert.assertEquals;

import static org.jxmpp.util.XmppStringUtils.parseDomain;

import org.junit.Test;

public class XmppStringUtilsTest {

	@Test
	public void parseLocalpart() {
		final String error = "Error parsing localpart";
		assertEquals(error, "", XmppStringUtils.parseLocalpart("yahoo.myjabber.net"));
		assertEquals(error, "", XmppStringUtils.parseLocalpart("yahoo.myjabber.net/registred"));
		assertEquals(error, "user", XmppStringUtils.parseLocalpart("user@yahoo.myjabber.net/registred"));
		assertEquals(error, "user", XmppStringUtils.parseLocalpart("user@yahoo.myjabber.net"));

		// Some more advanced parsing cases
		assertEquals(error, "", XmppStringUtils.parseLocalpart("foo.jxmpp.org/resOne@resTwo"));
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
		assertEquals(error, "", XmppStringUtils.parseResource("foo.jxmpp.org"));
		assertEquals(error, "registered", XmppStringUtils.parseResource("foo.jxmpp.org/registered"));
		assertEquals(error, "registered", XmppStringUtils.parseResource("user@foo.jxmpp.org/registered"));
		assertEquals(error, "", XmppStringUtils.parseResource("user@foo.jxmpp.org"));

		// Some more advanced parsing cases
		assertEquals(error, "resOne@resTwo", XmppStringUtils.parseResource("user@foo.jxmpp.org/resOne@resTwo"));
	}
}
