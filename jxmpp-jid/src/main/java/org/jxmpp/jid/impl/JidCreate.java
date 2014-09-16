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
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.cache.Cache;
import org.jxmpp.util.cache.LruCache;
import org.jxmpp.util.XmppStringUtils;

public class JidCreate {

	private static final Cache<String, Jid> JID_CACHE = new LruCache<String, Jid>(100);
	private static final Cache<String, BareJid> BAREJID_CACHE = new LruCache<String, BareJid>(100);
	private static final Cache<String, FullJid> FULLJID_CACHE = new LruCache<String, FullJid>(100);
	private static final Cache<String, DomainBareJid> DOMAINJID_CACHE = new LruCache<String, DomainBareJid>(100);
	private static final Cache<String, DomainFullJid> DOMAINRESOURCEJID_CACHE = new LruCache<String, DomainFullJid>(100);

	public static Jid from(String localpart, String domainpart, String resource) throws XmppStringprepException {
		String jidString = XmppStringUtils.completeJidFrom(localpart, domainpart, resource);
		Jid jid = JID_CACHE.get(jidString);
		// In order to avoid JID String hash collision attacks, we have to
		// compare the JID Strings char by char
		if (jid != null && jid.toString().equals(jidString)) {
			return jid;
		}
		if (localpart.length() > 0 && domainpart.length() > 0 && resource.length() > 0) {
			jid = new LocalDomainAndResourcepartJid(localpart, domainpart, resource);
		} else if (localpart.length() > 0 && domainpart.length() > 0 && resource.length() == 0) {
			jid = new LocalAndDomainpartJid(localpart, domainpart);
		} else if (localpart.length() == 0 && domainpart.length() > 0 && resource.length() == 0) {
			jid = new DomainpartJid(domainpart);
		} else if (localpart.length() == 0 && domainpart.length() > 0 && resource.length() > 0) {
			jid = new DomainAndResourcepartJid(domainpart, resource);
		} else {
			throw new IllegalArgumentException("Not a valid combination of localpart, domainpart and resource");
		}
		JID_CACHE.put(jidString, jid);
		return jid;
	}

	public static Jid from(String jidString) throws XmppStringprepException {
		String localpart = XmppStringUtils.parseLocalpart(jidString);
		String domainpart = XmppStringUtils.parseDomain(jidString);
		String resource = XmppStringUtils.parseResource(jidString);
		return from(localpart, domainpart, resource);
	}

	public static Jid fromUnescaped(String escapedJidString) throws XmppStringprepException {
		String localpart = XmppStringUtils.parseLocalpart(escapedJidString);
		// Some as from(String), but we escape the localpart
		localpart = XmppStringUtils.escapeLocalpart(localpart);

		String domainpart = XmppStringUtils.parseDomain(escapedJidString);
		String resource = XmppStringUtils.parseResource(escapedJidString);
		return from(localpart, domainpart, resource);
	}

	public static BareJid bareFrom(String jid) throws XmppStringprepException {
		BareJid bareJid = BAREJID_CACHE.get(jid);
		if (bareJid != null) {
			String bareJidString = XmppStringUtils.parseBareJid(jid);
			// This could result in false negatives of the Cache, but helps
			// preventing Jid String hash collision attacks
			if (bareJid.toString().equals(bareJidString)) {
				return bareJid;
			}
		}

		String localpart = XmppStringUtils.parseLocalpart(jid);
		String domainpart = XmppStringUtils.parseDomain(jid);
		bareJid = new LocalAndDomainpartJid(localpart, domainpart);
		BAREJID_CACHE.put(jid, bareJid);
		return bareJid;
	}

	public static FullJid fullFrom(String jid) throws XmppStringprepException {
		FullJid fullJid = FULLJID_CACHE.get(jid);
		// In order to avoid JID hash String collision attacks, we have to
		// compare the JID Strings char by char
		if (fullJid != null && fullJid.toString().equals(jid)) {
			return fullJid;
		}

		String localpart = XmppStringUtils.parseLocalpart(jid);
		String domainpart = XmppStringUtils.parseDomain(jid);
		String resource = XmppStringUtils.parseResource(jid);
		fullJid = new LocalDomainAndResourcepartJid(localpart, domainpart, resource);
		FULLJID_CACHE.put(jid, fullJid);
		return fullJid;
	}

	/**
	 * 
	 * @param jid
	 * @return a DopmainBareJid
	 * @throws XmppStringprepException
	 * @deprecated use {@link #domainBareFrom(String)} instead
	 */
	@Deprecated
	public static DomainBareJid serverBareFrom(String jid) throws XmppStringprepException {
		return domainBareFrom(jid);
	}

	public static DomainBareJid domainBareFrom(String jid) throws XmppStringprepException {
		String domain = XmppStringUtils.parseDomain(jid);
		DomainBareJid domainJid = DOMAINJID_CACHE.get(jid);
		if (domainJid != null) {
			if (domainJid.toString().equals(domain)) {
				return domainJid;
			}
		}

		domainJid = new DomainpartJid(domain);
		DOMAINJID_CACHE.put(jid, domainJid);
		return domainJid;
	}

	/**
	 * 
	 * @param jid
	 * @return a DomainFullJid
	 * @throws XmppStringprepException
	 * @deprecated use {@link #donmainFullFrom(String)} instead
	 */
	@Deprecated
	public static DomainFullJid serverFullFrom(String jid) throws XmppStringprepException {
		return donmainFullFrom(jid);
	}

	public static DomainFullJid donmainFullFrom(String jid) throws XmppStringprepException {
		DomainFullJid domainResourceJid = DOMAINRESOURCEJID_CACHE.get(jid);
		// In order to avoid JID hash String collision attacks, we have to
		// compare the JID Strings char by char
		if (domainResourceJid != null && domainResourceJid.toString().equals(jid)) {
			return domainResourceJid;
		}

		String domain = XmppStringUtils.parseDomain(jid);
		String resource = XmppStringUtils.parseResource(jid);
		domainResourceJid = new DomainAndResourcepartJid(domain, resource);
		DOMAINRESOURCEJID_CACHE.put(jid, domainResourceJid);
		return domainResourceJid;
	}

}
