/**
 *
 * Copyright © 2015-2017 Florian Schmaus
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
package org.jxmpp.jid.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.DomainFullJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

public class JidCreateTest {

	@Test
	public void bareFromThrowTest() {
		final String notABareJid = "example.org/test";
		try {
			EntityBareJid jid = JidCreate.entityBareFrom(notABareJid);
			// Should throw
			fail(jid + " should never been created");
		} catch (XmppStringprepException e) {
			assertEquals(notABareJid, e.getCausingString());
		}
	}

	@Test
	public void fullFromThrowTest() {
		final String notAFullJid = "user@example.org/";
		try {
			EntityFullJid jid = JidCreate.entityFullFrom(notAFullJid);
			// Should throw
			fail(jid + " should never been created");
		} catch (XmppStringprepException e) {
			assertEquals(notAFullJid, e.getCausingString());
		}
	}

	@Test
	public void domainBareThrowTest() {
		final String notADomainBareJid = "";
		try {
			DomainBareJid jid = JidCreate.domainBareFrom(notADomainBareJid);
			// Should throw
			fail(jid + " should never been created");
		} catch (XmppStringprepException e) {
			assertEquals(notADomainBareJid, e.getCausingString());
		}
	}

	@Test
	public void domainFullThrowTest() {
		final String notADomainFullJid = "example.org";
		try {
			DomainFullJid jid = JidCreate.domainFullFrom(notADomainFullJid);
			// Should throw
			fail(jid + " should never been created");
		} catch (XmppStringprepException e) {
			assertEquals(notADomainFullJid, e.getCausingString());
		}
	}

	@Test
	public void entityBareFromUnescapedTest() throws XmppStringprepException {
		EntityBareJid entityBareJid = JidCreate.entityBareFromUnescaped("foo@boo@example.org/baz");

		// Tricky question. Currently yields 'boo@example.org'. Domainparts are U-Labels, so this may be correct, even
		// if it is not a valid DNS label/name.
		Domainpart domainpart = entityBareJid.getDomain();
		assertEquals(Domainpart.from("boo@example.org"), domainpart);

		Localpart localpart = entityBareJid.getLocalpart();
		assertEquals(Localpart.from("foo"), localpart);
	}

	@Test
	public void entityFullFromComplexTest() throws XmppStringprepException {
		EntityFullJid entityFullJid = JidCreate.entityFullFrom("foo@boo@example.org/bar@baz");

		Domainpart domainpart = entityFullJid.getDomain();
		assertEquals(Domainpart.from("boo@example.org"), domainpart);

		Localpart localpart = entityFullJid.getLocalpart();
		assertEquals(Localpart.from("foo"), localpart);

		Resourcepart resourcepart = entityFullJid.getResourcepart();
		assertEquals(Resourcepart.from("bar@baz"), resourcepart);
	}

	@Test
	public void entityFullFromUnsecapedComplexTest() throws XmppStringprepException {
		EntityFullJid entityFullJid = JidCreate.entityFullFromUnescaped("foo@boo@example.org/bar@baz");

		Domainpart domainpart = entityFullJid.getDomain();
		assertEquals(Domainpart.from("boo@example.org"), domainpart);

		Localpart localpart = entityFullJid.getLocalpart();
		assertEquals(Localpart.from("foo"), localpart);

		Resourcepart resourcepart = entityFullJid.getResourcepart();
		assertEquals(Resourcepart.from("bar@baz"), resourcepart);
	}

	@Test
	public void entityFromUnescapedBareTest() throws XmppStringprepException {
		EntityJid entityJid = JidCreate.entityFromUnescaped("d'artagnan@musketeers.lit");

		Domainpart domainpart = entityJid.getDomain();
		assertEquals(Domainpart.from("musketeers.lit"), domainpart);

		Localpart localpart = entityJid.getLocalpart();
		assertEquals(Localpart.from("d\\27artagnan"), localpart);

		assertEquals(localpart, Localpart.fromUnescaped("d'artagnan"));
	}

	@Test
	public void entityFromUnescapedFullTest() throws XmppStringprepException {
		EntityJid entityBareJid = JidCreate.entityFromUnescaped("d'artagnan@gascon.fr/elder");

		Domainpart domainpart = entityBareJid.getDomain();
		assertEquals(Domainpart.from("gascon.fr"), domainpart);

		Resourcepart resourcepart = entityBareJid.getResourceOrThrow();
		assertEquals(Resourcepart.from("elder"), resourcepart);

		Localpart localpart = entityBareJid.getLocalpart();
		assertEquals(Localpart.from("d\\27artagnan"), localpart);
	}
}
