/*
 *
 * Copyright © 2018 Florian Schmaus
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
package org.jxmpp.jid.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.jxmpp.jid.JidTestUtil;
import org.jxmpp.stringprep.XmppStringprepException;

public class JidUtilTest {

	@Test
	public void testEquals() {
		assertTrue(JidUtil.equals(null, null));

		assertFalse(JidUtil.equals(null, JidTestUtil.BARE_JID_1));
		assertFalse(JidUtil.equals(JidTestUtil.BARE_JID_1, null));

		assertTrue(JidUtil.equals(JidTestUtil.BARE_JID_1, JidTestUtil.BARE_JID_1));
		assertFalse(JidUtil.equals(JidTestUtil.BARE_JID_1, JidTestUtil.BARE_JID_2));
	}

	@Test
	public void testEqualsBareJid() {
		assertTrue(JidUtil.equalsBareJid(null, null));

		assertFalse(JidUtil.equalsBareJid(null, JidTestUtil.BARE_JID_1));
		assertFalse(JidUtil.equalsBareJid(JidTestUtil.BARE_JID_1, null));

		assertTrue(JidUtil.equalsBareJid(JidTestUtil.BARE_JID_1, JidTestUtil.BARE_JID_1));
		assertTrue(JidUtil.equalsBareJid(JidTestUtil.BARE_JID_1, JidTestUtil.FULL_JID_1_RESOURCE_1));
		assertTrue(JidUtil.equalsBareJid(JidTestUtil.FULL_JID_1_RESOURCE_1, JidTestUtil.FULL_JID_1_RESOURCE_2));
		assertFalse(JidUtil.equalsBareJid(JidTestUtil.BARE_JID_1, JidTestUtil.BARE_JID_2));
		assertFalse(JidUtil.equalsBareJid(JidTestUtil.FULL_JID_1_RESOURCE_1, JidTestUtil.FULL_JID_2_RESOURCE_1));
	}

	@Test
	public void testEqualsCharSequence() throws XmppStringprepException {
		assertTrue(JidUtil.equals((CharSequence) null, (CharSequence) null));

		assertFalse(JidUtil.equals(null, "one@exampleOne.org"));
		assertFalse(JidUtil.equals("one@exampleOne.org", null));

		assertTrue(JidUtil.equals(JidTestUtil.BARE_JID_1, "one@exampleOne.org"));
		assertTrue(JidUtil.equals("one@exampleOne.org", JidTestUtil.BARE_JID_1));
		assertTrue(JidUtil.equals("one@exampleOne.org", "one@exampleone.org"));
		assertFalse(JidUtil.equals(JidTestUtil.BARE_JID_1, "one@exampleOne.org/resourceOne"));
		assertFalse(JidUtil.equals("one@exampleOne.org", JidTestUtil.BARE_JID_2));
	}

	@Test
	public void testEqualsBareJidCharSequence() throws XmppStringprepException {
		assertTrue(JidUtil.equalsBareJid((CharSequence) null, (CharSequence) null));

		assertFalse(JidUtil.equalsBareJid(null, "one@exampleOne.org"));
		assertFalse(JidUtil.equalsBareJid("one@exampleOne.org", null));

		assertTrue(JidUtil.equalsBareJid(JidTestUtil.BARE_JID_1, "one@exampleOne.org/resourceOne"));
		assertTrue(JidUtil.equalsBareJid("one@exampleOne.org/resourceOne", JidTestUtil.FULL_JID_1_RESOURCE_2));
		assertTrue(JidUtil.equalsBareJid("one@exampleOne.org/resourceOne", "one@exampleone.org/resourceTwo"));
		assertFalse(JidUtil.equalsBareJid(JidTestUtil.BARE_JID_1, "one@exampleTwo.org/resourceOne"));
		assertFalse(JidUtil.equalsBareJid("one@exampleOne.org", JidTestUtil.FULL_JID_2_RESOURCE_1));
	}

	@Test(expected = XmppStringprepException.class)
	public void testEqualsCharSequenceInvalidJid() throws XmppStringprepException {
		JidUtil.equals(JidTestUtil.BARE_JID_1, "@exampleOne.org");
	}
}
