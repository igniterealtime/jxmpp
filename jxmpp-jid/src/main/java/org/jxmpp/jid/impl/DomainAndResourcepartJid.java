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

/**
 * RFC6122 2.4 allows JIDs with only a domain and resource part.
 * <p>
 * Note that this implementation does not require an cache for the unescaped
 * string, compared to {@link LocalDomainAndResourcepartJid}.
 * </p>
 *
 */
public final class DomainAndResourcepartJid extends AbstractJid implements DomainFullJid {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private final DomainBareJid domainBareJid;
	private final Resourcepart resource;

	DomainAndResourcepartJid(String domain, String resource) throws XmppStringprepException {
		this(new DomainpartJid(domain), Resourcepart.from(resource));
	}

	DomainAndResourcepartJid(DomainBareJid domainBareJid, Resourcepart resource) {
		this.domainBareJid = requireNonNull(domainBareJid, "The DomainBareJid must not be null");
		this.resource = requireNonNull(resource, "The Resource must not be null");
	}

	@Override
	public String toString() {
		if (cache != null) {
			return cache;
		}
		cache = domainBareJid.toString() + '/' + resource;
		return cache;
	}

	@Override
	public DomainBareJid asDomainBareJid() {
		return domainBareJid;
	}

	@Override
	public boolean hasNoResource() {
		return false;
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
		return this;
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
		return false;
	}

	@Override
	public boolean isParentOf(DomainBareJid domainBareJid) {
		return false;
	}

	@Override
	public boolean isParentOf(DomainFullJid domainFullJid) {
		return domainBareJid.equals(domainFullJid.getDomain()) && resource.equals(domainFullJid.getResourcepart());
	}

	@Override
	public Resourcepart getResourcepart() {
		return resource;
	}

	@Override
	public BareJid asBareJid() {
		return asDomainBareJid();
	}

	@Override
	public Domainpart getDomain() {
		return domainBareJid.getDomain();
	}

	@Override
	public String asUnescapedString() {
		return toString();
	}

	@Override
	public EntityJid asEntityJidIfPossible() {
		return null;
	}

	@Override
	public FullJid asFullJidIfPossible() {
		return this;
	}

	@Override
	public Localpart getLocalpartOrNull() {
		return null;
	}
}
