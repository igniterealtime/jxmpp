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

public final class LocalDomainAndResourcepartJid extends AbstractJid implements EntityFullJid {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private final EntityBareJid bareJid;
	private final Resourcepart resource;

	private String unescapedCache;

	LocalDomainAndResourcepartJid(String localpart, String domain, String resource) throws XmppStringprepException {
		this(new LocalAndDomainpartJid(localpart, domain), Resourcepart.from(resource));
	}

	LocalDomainAndResourcepartJid(EntityBareJid bareJid, Resourcepart resource) {
		this.bareJid = requireNonNull(bareJid, "The EntityBareJid must not be null");
		this.resource = requireNonNull(resource, "The Resourcepart must not be null");
	}

	@Override
	public String toString() {
		if (cache != null) {
			return cache;
		}
		cache = bareJid.toString() + '/' + resource;
		return cache;
	}

	@Override
	public String asUnescapedString() {
		if (unescapedCache != null) {
			return unescapedCache;
		}
		unescapedCache = bareJid.asUnescapedString() + '/' + resource;
		return unescapedCache;
	}

	@Override
	public EntityBareJid asEntityBareJid() {
		return bareJid;
	}

	@Override
	public String asEntityBareJidString() {
		return asEntityBareJid().toString();
	}

	@Override
	public boolean hasNoResource() {
		return false;
	}

	@Override
	public EntityBareJid asEntityBareJidIfPossible() {
		return asEntityBareJid();
	}

	@Override
	public EntityFullJid asEntityFullJidIfPossible() {
		return this;
	}

	@Override
	public DomainFullJid asDomainFullJidIfPossible() {
		return null;
	}

	@Override
	public Localpart getLocalpartOrNull() {
		return bareJid.getLocalpart();
	}

	@Override
	public Resourcepart getResourceOrNull() {
		return getResourcepart();
	}

	@Override
	public boolean isParentOf(EntityBareJid bareJid) {
		return false;
	}

	@Override
	public boolean isParentOf(EntityFullJid fullJid) {
		return this.equals(fullJid);
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
		return bareJid.asDomainBareJid();
	}

	@Override
	public Resourcepart getResourcepart() {
		return resource;
	}

	@Override
	public BareJid asBareJid() {
		return asEntityBareJid();
	}

	@Override
	public Domainpart getDomain() {
		return bareJid.getDomain();
	}

	@Override
	public Localpart getLocalpart() {
		return bareJid.getLocalpart();
	}

	@Override
	public EntityJid asEntityJidIfPossible() {
		return this;
	}

	@Override
	public FullJid asFullJidIfPossible() {
		return this;
	}
}
