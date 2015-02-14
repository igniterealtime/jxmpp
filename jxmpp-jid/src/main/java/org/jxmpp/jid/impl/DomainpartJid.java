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
package org.jxmpp.jid.impl;

import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.DomainFullJid;
import org.jxmpp.jid.FullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.stringprep.XmppStringprepException;

public class DomainpartJid extends AbstractJid implements DomainBareJid {

	protected final Domainpart domain;

	DomainpartJid(String domain) throws XmppStringprepException {
		this.domain = Domainpart.from(domain);
	}

	public final Domainpart getDomain() {
		return domain;
	}

	@Override
	public String toString() {
		return domain.toString();
	}

	@Override
	public String asUnescapedString() {
		// No un-escaping necessary for DomainpartJid
		return toString();
	}

	@Override
	public final int compareTo(Jid  other) {
		String otherString = other.toString();
		String myString = toString();
		return myString.compareTo(otherString);
	}

	@Override
	public DomainBareJid asDomainBareJid() {
		return this;
	}

	@Override
	public boolean hasNoResource() {
		return true;
	}

	@Override
	public BareJid asBareJidIfPossible() {
		return null;
	}

	@Override
	public FullJid asFullJidIfPossible() {
		return null;
	}

	@Override
	public DomainBareJid asDomainBareJidIfPossible() {
		return asDomainBareJid();
	}

	@Override
	public DomainFullJid asDomainFullJidIfPossible() {
		return null;
	}

	@Override
	public boolean isParentOf(BareJid bareJid) {
		return domain.equals(bareJid.getDomain());
	}

	@Override
	public boolean isParentOf(FullJid fullJid) {
		return domain.equals(fullJid.getDomain());
	}

	@Override
	public boolean isParentOf(DomainBareJid domainBareJid) {
		return domain.equals(domainBareJid.getDomain());
	}

	@Override
	public boolean isParentOf(DomainFullJid domainFullJid) {
		return domain.equals(domainFullJid.getDomain());
	}
}
