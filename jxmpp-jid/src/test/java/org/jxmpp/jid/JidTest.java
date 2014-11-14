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
package org.jxmpp.jid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

public class JidTest {

	@Test
	public void testJidIsPartentOf() throws XmppStringprepException {
		final Jid domainBareJid = JidCreate.from("dom.example");
		final Jid domainFullJid = JidCreate.from("dom.example/res");
		final Jid bareJid = JidCreate.from("loc@dom.example");
		final Jid fullJid = JidCreate.from("loc@dom.example/res");

		assertTrue(domainBareJid.isParentOf(domainBareJid));
		assertTrue(domainBareJid.isParentOf(domainFullJid));
		assertTrue(domainBareJid.isParentOf(bareJid));
		assertTrue(domainBareJid.isParentOf(fullJid));

		assertFalse(domainFullJid.isParentOf(domainBareJid));
		assertTrue(domainFullJid.isParentOf(domainFullJid));
		assertFalse(domainFullJid.isParentOf(bareJid));
		assertFalse(domainFullJid.isParentOf(fullJid));

		assertFalse(bareJid.isParentOf(domainBareJid));
		assertFalse(bareJid.isParentOf(domainFullJid));
		assertTrue(bareJid.isParentOf(bareJid));
		assertTrue(bareJid.isParentOf(fullJid));

		assertFalse(fullJid.isParentOf(domainBareJid));
		assertFalse(fullJid.isParentOf(domainFullJid));
		assertFalse(fullJid.isParentOf(bareJid));
		assertTrue(fullJid.isParentOf(fullJid));
	}

	@Test
	public void stripFinalDot() throws XmppStringprepException {
		String domain = "foo.bar.";
		Jid jid = JidCreate.domainBareFrom(domain);
		assertEquals("foo.bar", jid.toString());
	}
}
