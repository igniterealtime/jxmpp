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
import org.jxmpp.stringprep.XmppStringPrepUtil;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.XmppStringUtils;


public class LocalAndDomainpartJid extends DomainpartJid implements BareJid {

	private final String localpart;

	private String cache;
	private String escapedCache;

	LocalAndDomainpartJid(String localpart, String domain) throws XmppStringprepException {
		super(domain);
		localpart = XmppStringPrepUtil.nodeprep(localpart);
		assertNotLongerThen1023BytesOrEmpty(localpart);
		this.localpart = localpart;
	}

	public final String getLocalpart() {
		return localpart;
	}

	@Override
	public String toString() {
		if (cache != null) {
			return cache;
		}
		cache = localpart + '@' + super.toString();
		return cache;
	}

	@Override
	public String asEscapedString() {
		if (escapedCache != null) {
			return escapedCache;
		}
		escapedCache = XmppStringUtils.escapeLocalpart(localpart) + '@' + super.toString();
		return escapedCache;
	}

	@Override
	public boolean isBareJid() {
		return true;
	}

	@Override
	public boolean isFullJid() {
		return false;
	}

	@Override
	public boolean hasOnlyDomainpart() {
		return false;
	}

	@Override
	public boolean hasOnlyDomainAndResourcepart() {
		return false;
	}

	@Override
	public boolean hasResource() {
		return false;
	}

	@Override
	public boolean hasLocalpart() {
		return true;
	}

	@Override
	public final String asBareJidString() {
		return toString();
	}
}
