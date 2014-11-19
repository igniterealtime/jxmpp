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
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.XmppStringUtils;


public class LocalDomainAndResourcepartJid extends LocalAndDomainpartJid implements FullJid {

	private final Resourcepart resource;

	private String cache;
	private String unescapedCache;
	private BareJid bareJidCache;

	public LocalDomainAndResourcepartJid(String localpart, String domain, String resource) throws XmppStringprepException {
		super(localpart, domain);
		this.resource = Resourcepart.from(resource);
	}

	public final String getResource() {
		return resource.toString();
	}

	@Override
	public String toString() {
		if (cache != null) {
			return cache;
		}
		cache = super.toString() + '/' + resource;
		return cache;
	}

	@Override
	public String asUnescapedString() {
		if (unescapedCache != null) {
			return unescapedCache;
		}
		unescapedCache = super.asUnescapedString() + '/' + resource;
		return unescapedCache;
	}

	@Override
	public BareJid asBareJid() {
		if (bareJidCache == null) {
			try {
				bareJidCache = JidCreate.bareFrom(XmppStringUtils
						.completeJidFrom(localpart, domain));
			} catch (XmppStringprepException e) {
				throw new AssertionError(e);
			}
		}
		return bareJidCache;
	}

	@Override
	public String asBareJidString() {
		return asBareJid().toString();
	}

	@Override
	public final boolean hasNoResource() {
		return false;
	}

	@Override
	public BareJid asBareJidIfPossible() {
		return asBareJid();
	}

	@Override
	public FullJid asFullJidIfPossible() {
		return this;
	}

	@Override
	public DomainBareJid asDomainBareJidIfPossible() {
		return null;
	}

	@Override
	public DomainFullJid asDomainFullJidIfPossible() {
		return null;
	}

	@Override
	public String getLocalpartOrNull() {
		return getLocalpart();
	}

	@Override
	public String getResourceOrNull() {
		return getResource();
	}

	@Override
	public boolean isParentOf(BareJid bareJid) {
		return false;
	}

	@Override
	public boolean isParentOf(FullJid fullJid) {
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
}
