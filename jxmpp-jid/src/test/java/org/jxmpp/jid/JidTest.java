/**
 *
 * Copyright © 2014-2024 Florian Schmaus
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
	public void testJidIsParentOf() throws XmppStringprepException {
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
	public void testJidIsStrictParentOf() throws XmppStringprepException {
		final Jid domainBareJid = JidCreate.from("dom.example");
		final Jid domainFullJid = JidCreate.from("dom.example/res");
		final Jid bareJid = JidCreate.from("loc@dom.example");
		final Jid fullJid = JidCreate.from("loc@dom.example/res");

		assertFalse(domainBareJid.isStrictParentOf(domainBareJid)); // different from isParentOf
		assertTrue(domainBareJid.isStrictParentOf(domainFullJid));
		assertTrue(domainBareJid.isStrictParentOf(bareJid));
		assertTrue(domainBareJid.isStrictParentOf(fullJid));

		assertFalse(domainFullJid.isStrictParentOf(domainBareJid));
		assertFalse(domainFullJid.isStrictParentOf(domainFullJid)); // different from isParentOf
		assertFalse(domainFullJid.isStrictParentOf(bareJid));
		assertFalse(domainFullJid.isStrictParentOf(fullJid));

		assertFalse(bareJid.isStrictParentOf(domainBareJid));
		assertFalse(bareJid.isStrictParentOf(domainFullJid));
		assertFalse(bareJid.isStrictParentOf(bareJid)); // different from isParentOf
		assertTrue(bareJid.isStrictParentOf(fullJid));

		assertFalse(fullJid.isStrictParentOf(domainBareJid));
		assertFalse(fullJid.isStrictParentOf(domainFullJid));
		assertFalse(fullJid.isStrictParentOf(bareJid));
		assertFalse(fullJid.isStrictParentOf(fullJid)); // different from isParentOf
	}

	@Test
	public void stripFinalDot() throws XmppStringprepException {
		String domain = "foo.bar.";
		Jid jid = JidCreate.domainBareFrom(domain);
		assertEquals("foo.bar", jid.toString());
	}

	@Test
	public void equalsBareJid() throws XmppStringprepException {
		Jid romeoBareJid = JidCreate.from("romeo@montague.net");
		Jid romeoFullJid1 = JidCreate.from("romeo@montague.net/balcony");
		Jid romeoFullJid2 = JidCreate.from("romeo@montague.net/home");
		Jid romeoDomainJid = JidCreate.domainBareFrom("montague.net");
		Jid julietBareJid = JidCreate.from("juliet@capulet.org");
		Jid julietFullJid = JidCreate.from("juliet@capulet.org/balcony");
		assertTrue(romeoBareJid.equalsBareJid(romeoBareJid));
		assertTrue(romeoBareJid.equalsBareJid(romeoFullJid1));
		assertTrue(romeoFullJid1.equalsBareJid(romeoFullJid2));
		assertFalse(romeoBareJid.equalsBareJid(romeoDomainJid));
		assertFalse(romeoBareJid.equalsBareJid(julietBareJid));
		assertFalse(romeoBareJid.equalsBareJid(julietFullJid));
		assertFalse(romeoBareJid.equalsBareJid(null));
	}
}
