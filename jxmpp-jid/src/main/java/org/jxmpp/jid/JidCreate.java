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
package org.jxmpp.jid;

import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.Cache;
import org.jxmpp.util.XmppStringUtils;

public class JidCreate {

	private static final Cache<String, Jid> JID_CACHE = new Cache<String, Jid>(100, -1);
	private static final Cache<String, BareJid> BAREJID_CACHE = new Cache<String, BareJid>(100, -1);
	private static final Cache<String, FullJid> FULLJID_CACHE = new Cache<String, FullJid>(100, -1);
	private static final Cache<String, DomainJid> DOMAINJID_CACHE = new Cache<String, DomainJid>(100, -1);
	private static final Cache<String, DomainResourceJid> DOMAINRESOURCEJID_CACHE = new Cache<String, DomainResourceJid>(100, -1);
	
	public static Jid from(String jidString) throws XmppStringprepException {
		Jid jid = JID_CACHE.get(jidString);
		if (jid != null) {
			return jid;
		}

		String localpart = XmppStringUtils.parseBareAddress(jidString);
		String domainpart = XmppStringUtils.parseDomain(jidString);
		String resource = XmppStringUtils.parseResource(jidString);

		if (localpart.length() > 0 && domainpart.length() > 0 && resource.length() > 0) {
			jid = new LocalDomainAndResourcepartJid(localpart, domainpart, resource);
		} else if (localpart.length() > 0 && domainpart.length() > 0 && resource.length() == 0) {
			jid = new LocalAndDomainpartJid(localpart, domainpart);
		} else if (localpart.length() == 0 && domainpart.length() > 0 && resource.length() == 0) {
			jid = new DomainpartJid(domainpart);
		} else if (localpart.length() == 0 && domainpart.length() > 0 && resource.length() > 0) {
			jid = new DomainAndResourcepartJid(domainpart, resource);
		} else {
			throw new IllegalArgumentException("Not a valid JID: '" + jidString + "'");
		}
		JID_CACHE.put(jidString, jid);
		return jid;
	}

	public static BareJid bareFrom(String jid) throws XmppStringprepException {
		BareJid bareJid = BAREJID_CACHE.get(jid);
		if (bareJid != null) {
			return bareJid;
		}

		String localpart = XmppStringUtils.parseLocalpart(jid);
		String domainpart = XmppStringUtils.parseDomain(jid);
		bareJid = new LocalAndDomainpartJid(localpart, domainpart);
		BAREJID_CACHE.put(jid, bareJid);
		return bareJid;
	}

	public static FullJid fullFrom(String jid) throws XmppStringprepException {
		FullJid fullJid = FULLJID_CACHE.get(jid);
		if (fullJid != null) {
			return fullJid;
		}

		String localpart = XmppStringUtils.parseLocalpart(jid);
		String domainpart = XmppStringUtils.parseDomain(jid);
		String resource = XmppStringUtils.parseResource(jid);
		fullJid = new LocalDomainAndResourcepartJid(localpart, domainpart, resource);
		FULLJID_CACHE.put(jid, fullJid);
		return fullJid;
	}

	public static DomainJid domainFrom(String jid) throws XmppStringprepException {
		DomainJid domainJid = DOMAINJID_CACHE.get(jid);
		if (domainJid != null) {
			return domainJid;
		}

		String domain = XmppStringUtils.parseDomain(jid);
		domainJid = new DomainpartJid(domain);
		DOMAINJID_CACHE.put(jid, domainJid);
		return domainJid;
	}

	public static DomainResourceJid domainResourceFrom(String jid) throws XmppStringprepException {
		DomainResourceJid domainResourceJid = DOMAINRESOURCEJID_CACHE.get(jid);
		if (domainResourceJid != null) {
			return domainResourceJid;
		}

		String domain = XmppStringUtils.parseDomain(jid);
		String resource = XmppStringUtils.parseResource(jid);
		domainResourceJid = new DomainAndResourcepartJid(domain, resource);
		DOMAINRESOURCEJID_CACHE.put(jid, domainResourceJid);
		return domainResourceJid;
	}
}
