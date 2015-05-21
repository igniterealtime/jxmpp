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
package org.jxmpp.jid;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;
import org.jxmpp.jid.impl.JidCreate;

public class JidSerializeableTest {

	@Test
	public void basicTest() throws IOException, ClassNotFoundException {
		final Jid jid = JidCreate.from("foo@bar.org");
		final Jid deserializedJid = serializeAndDeserialze(jid);

		assertEquals(jid, deserializedJid);
	}

	@Test
	public void localAndDomainpartTest() throws ClassNotFoundException, IOException {
		final EntityBareJid bareJid = JidTestUtil.BARE_JID_1;
		final EntityBareJid deserializedBareJid = serializeAndDeserialze(bareJid);
		assertEquals(bareJid, deserializedBareJid);
	}

	@Test
	public void localDomainAndResourcepartTest() throws ClassNotFoundException, IOException {
		final EntityFullJid fullJid = JidTestUtil.FULL_JID_1_RESOURCE_1;
		final EntityFullJid deserializedFullJid = serializeAndDeserialze(fullJid);
		assertEquals(fullJid, deserializedFullJid);
	}

	@Test
	public void domainpartJidTest() throws ClassNotFoundException, IOException {
		final DomainBareJid domainpartJid = JidTestUtil.DOMAIN_BARE_JID_1;
		final DomainBareJid deserializedDomainpartJid = serializeAndDeserialze(domainpartJid);
		assertEquals(domainpartJid, deserializedDomainpartJid);
	}

	@Test
	public void domainAndResourcepartTest() throws ClassNotFoundException, IOException {
		final DomainFullJid domainFullJid = JidTestUtil.DOMAIN_FULL_JID_1;
		final DomainFullJid deserialziedDomainFullJid = serializeAndDeserialze(domainFullJid);
		assertEquals(domainFullJid, deserialziedDomainFullJid);
	}

	@SuppressWarnings("unchecked")
	private static <S> S serializeAndDeserialze(S serializable) throws IOException, ClassNotFoundException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(serializable);

		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
		return (S) ois.readObject();
	}
}
