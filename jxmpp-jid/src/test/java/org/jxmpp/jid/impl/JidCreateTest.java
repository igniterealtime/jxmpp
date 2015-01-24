/**
 *
 * Copyright © 2015 Florian Schmaus
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
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.DomainFullJid;
import org.jxmpp.jid.FullJid;
import org.jxmpp.stringprep.XmppStringprepException;

public class JidCreateTest {

	@Test
	public void bareFromThrowTest() {
		final String notABareJid = "example.org/test";
		try {
			BareJid jid = JidCreate.bareFrom(notABareJid);
			// Should throw
			fail(jid + " should never been created");
		} catch (XmppStringprepException e) {
			assertEquals(notABareJid, e.getCausingString());
		}
	}

	@Test
	public void fullFromThrowTest() {
		final String notAFullJid = "user@example.org";
		try {
			FullJid jid = JidCreate.fullFrom(notAFullJid);
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
}
