/**
 *
 * Copyright © 2014-2017 Florian Schmaus
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
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.DomainFullJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.FullJid;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

public final class DomainpartJid extends AbstractJid implements DomainBareJid {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	protected final Domainpart domain;

	DomainpartJid(String domain) throws XmppStringprepException {
		this(Domainpart.from(domain));
	}

	DomainpartJid(Domainpart domain) {
		this.domain = requireNonNull(domain, "The Domainpart must not be null");
	}

	@Override
	public Domainpart getDomain() {
		return domain;
	}

	@Override
	public String toString() {
		// Prefer the cached version over the domain.toString() one, since the cached version may
		// also be the internalized representation of the String. Which, e.g. provides benefits when
		// comparing JIDs.
		if (cache != null) {
			return cache;
		}
		cache = domain.toString();
		return cache;
	}

	@Override
	public String asUnescapedString() {
		// No un-escaping necessary for DomainpartJid
		return toString();
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
	public EntityBareJid asEntityBareJidIfPossible() {
		return null;
	}

	@Override
	public EntityFullJid asEntityFullJidIfPossible() {
		return null;
	}

	@Override
	public DomainFullJid asDomainFullJidIfPossible() {
		return null;
	}

	@Override
	public boolean isParentOf(EntityBareJid bareJid) {
		return domain.equals(bareJid.getDomain());
	}

	@Override
	public boolean isParentOf(EntityFullJid fullJid) {
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

	@Override
	public BareJid asBareJid() {
		return this;
	}

	@Override
	public EntityJid asEntityJidIfPossible() {
		return null;
	}

	@Override
	public FullJid asFullJidIfPossible() {
		return null;
	}

	@Override
	public Resourcepart getResourceOrNull() {
		return null;
	}

	@Override
	public Localpart getLocalpartOrNull() {
		return null;
	}

}
