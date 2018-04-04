/**
 *
 * Copyright © 2014-2018 Florian Schmaus
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


public final class LocalAndDomainpartJid extends AbstractJid implements EntityBareJid {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private final DomainBareJid domainBareJid;
	private final Localpart localpart;

	private transient String unescapedCache;

	LocalAndDomainpartJid(String localpart, String domain) throws XmppStringprepException {
		domainBareJid = new DomainpartJid(domain);
		this.localpart = Localpart.from(localpart);
	}

	LocalAndDomainpartJid(Localpart localpart, Domainpart domain) {
		this.localpart = requireNonNull(localpart, "The Localpart must not be null");
		this.domainBareJid = new DomainpartJid(domain);
	}

	@Override
	public Localpart getLocalpart() {
		return localpart;
	}

	@Override
	public String toString() {
		if (cache != null) {
			return cache;
		}
		cache = getLocalpart().toString() + '@' + domainBareJid.toString();
		return cache;
	}

	@Override
	public String asUnescapedString() {
		if (unescapedCache != null) {
			return unescapedCache;
		}
		unescapedCache = getLocalpart().asUnescapedString() + '@' + domainBareJid.toString();
		return unescapedCache;
	}

	@Override
	public EntityBareJid asEntityBareJidIfPossible() {
		return this;
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
		return domainBareJid.equals(bareJid.getDomain()) && localpart.equals(bareJid.getLocalpart());
	}

	@Override
	public boolean isParentOf(EntityFullJid fullJid) {
		return isParentOf(fullJid.asBareJid());
	}

	@Override
	public boolean isParentOf(DomainBareJid domainBareJid) {
		return false;
	}

	@Override
	public boolean isParentOf(DomainFullJid domainFullJid) {
		return false;
	}

	@Override
	public DomainBareJid asDomainBareJid() {
		return domainBareJid;
	}

	@Override
	public Domainpart getDomain() {
		return domainBareJid.getDomain();
	}

	@Override
	public BareJid asBareJid() {
		return this;
	}

	@Override
	public boolean hasNoResource() {
		return true;
	}

	@Override
	public EntityJid asEntityJidIfPossible() {
		return this;
	}

	@Override
	public FullJid asFullJidIfPossible() {
		return null;
	}

	@Override
	public EntityBareJid asEntityBareJid() {
		return this;
	}

	@Override
	public Resourcepart getResourceOrNull() {
		return null;
	}

	@Override
	public Localpart getLocalpartOrNull() {
		return getLocalpart();
	}

	@Override
	public String asEntityBareJidString() {
		return toString();
	}
}
