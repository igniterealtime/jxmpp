/**
 *
 * Copyright © 2014-2015 Florian Schmaus
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
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.cache.Cache;
import org.jxmpp.util.cache.LruCache;
import org.jxmpp.util.XmppStringUtils;

/**
 * API to create Jids from Strings and CharSequences.
 * <p>
 * JIDs created from input received from a XMPP source should use {@link #from(String)}. If the input was user
 * generated, e.g. captured from some sort of user interface, {@link #fromUnescaped(String)} should be used instead. You
 * can use {@link org.jxmpp.jid.util.JidUtil#isValidBareJid(CharSequence)} to query, e.g. while the user it entering it,
 * if a given CharSequence is a valid bare JID.
 * </p>
 * <p>
 * JidCreate uses caches for efficient Jid construction, But it's not guaranteed that the same String or CharSequence
 * will yield the same Jid instance.
 * </p>
 *
 */
public class JidCreate {

	private static final Cache<String, Jid> JID_CACHE = new LruCache<String, Jid>(100);
	private static final Cache<String, BareJid> BAREJID_CACHE = new LruCache<String, BareJid>(100);
	private static final Cache<String, FullJid> FULLJID_CACHE = new LruCache<String, FullJid>(100);
	private static final Cache<String, DomainBareJid> DOMAINJID_CACHE = new LruCache<String, DomainBareJid>(100);
	private static final Cache<String, DomainFullJid> DOMAINRESOURCEJID_CACHE = new LruCache<String, DomainFullJid>(100);

	public static Jid from(CharSequence localpart, CharSequence domainpart, CharSequence resource)
			throws XmppStringprepException {
		return from(localpart.toString(), domainpart.toString(), resource.toString());
	}

	public static Jid from(String localpart, String domainpart, String resource) throws XmppStringprepException {
		String jidString = XmppStringUtils.completeJidFrom(localpart, domainpart, resource);
		Jid jid = JID_CACHE.get(jidString);
		if (jid != null) {
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

	/**
	 * Create a {@link Jid} from a CharSequence.
	 *
	 * @param jid the input CharSequence.
	 * @return the Jid represented by the input CharSequence.
	 * @throws XmppStringprepException if the input CharSequence is not a valid Jid.
	 * @see #from(String)
	 */
	public static Jid from(CharSequence jid) throws XmppStringprepException {
		return from(jid.toString());
	}

	/**
	 * Create a {@link Jid} from a String.
	 *
	 * @param jidString the input String.
	 * @return the Jid represented by the input String.
	 * @throws XmppStringprepException if the input String is not a valid Jid.
	 * @see #from(CharSequence)
	 */
	public static Jid from(String jidString) throws XmppStringprepException {
		String localpart = XmppStringUtils.parseLocalpart(jidString);
		String domainpart = XmppStringUtils.parseDomain(jidString);
		String resource = XmppStringUtils.parseResource(jidString);
		try {
			return from(localpart, domainpart, resource);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(jidString, e);
		}
	}

	public static Jid fromUnescaped(CharSequence unescapedJid) throws XmppStringprepException {
		return fromUnescaped(unescapedJid.toString());
	}

	public static Jid fromUnescaped(String unescapedJidString) throws XmppStringprepException {
		String localpart = XmppStringUtils.parseLocalpart(unescapedJidString);
		// Some as from(String), but we escape the localpart
		localpart = XmppStringUtils.escapeLocalpart(localpart);

		String domainpart = XmppStringUtils.parseDomain(unescapedJidString);
		String resource = XmppStringUtils.parseResource(unescapedJidString);
		try {
			return from(localpart, domainpart, resource);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(unescapedJidString, e);
		}
	}

	public static BareJid bareFrom(CharSequence jid) throws XmppStringprepException {
		return bareFrom(jid.toString());
	}

	public static BareJid bareFrom(String jid) throws XmppStringprepException {
		BareJid bareJid = BAREJID_CACHE.get(jid);
		if (bareJid != null) {
			return bareJid;
		}

		String localpart = XmppStringUtils.parseLocalpart(jid);
		String domainpart = XmppStringUtils.parseDomain(jid);
		try {
			bareJid = new LocalAndDomainpartJid(localpart, domainpart);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(jid, e);
		}
		BAREJID_CACHE.put(jid, bareJid);
		return bareJid;
	}

	public static BareJid bareFrom(Localpart localpart, DomainBareJid domainBareJid) {
		return bareFrom(localpart, domainBareJid.getDomain());
	}

	public static BareJid bareFrom(Localpart localpart, Domainpart domain) {
		return new LocalAndDomainpartJid(localpart, domain);
	}

	public static FullJid fullFrom(CharSequence jid) throws XmppStringprepException {
		return fullFrom(jid.toString());
	}

	public static FullJid fullFrom(String jid) throws XmppStringprepException {
		FullJid fullJid = FULLJID_CACHE.get(jid);
		if (fullJid != null) {
			return fullJid;
		}

		String localpart = XmppStringUtils.parseLocalpart(jid);
		String domainpart = XmppStringUtils.parseDomain(jid);
		String resource = XmppStringUtils.parseResource(jid);
		try {
			fullJid = fullFrom(localpart, domainpart, resource);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(jid, e);
		}
		FULLJID_CACHE.put(jid, fullJid);
		return fullJid;
	}

	public static FullJid fullFrom(String localpart, String domainpart, String resource) throws XmppStringprepException {
		FullJid fullJid;
		try {
			fullJid = new LocalDomainAndResourcepartJid(localpart, domainpart, resource);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(localpart + '@' + domainpart + '/' + resource, e);
		}
		return fullJid;
	}

	public static FullJid fullFrom(Localpart localpart, DomainBareJid domainBareJid, Resourcepart resource) {
		return fullFrom(localpart, domainBareJid.getDomain(), resource);
	}

	public static FullJid fullFrom(Localpart localpart, Domainpart domainpart, Resourcepart resource) {
		return fullFrom(bareFrom(localpart, domainpart), resource);
	}

	public static FullJid fullFrom(BareJid bareJid, Resourcepart resource) {
		return new LocalDomainAndResourcepartJid(bareJid, resource);
	}

	/**
	 * Deprecated.
	 *
	 * @param jid the JID.
	 * @return a DopmainBareJid
	 * @throws XmppStringprepException if an error happens.
	 * @deprecated use {@link #domainBareFrom(String)} instead
	 */
	@Deprecated
	public static DomainBareJid serverBareFrom(String jid) throws XmppStringprepException {
		return domainBareFrom(jid);
	}

	public static DomainBareJid domainBareFrom(CharSequence jid) throws XmppStringprepException {
		return domainBareFrom(jid.toString());
	}

	public static DomainBareJid domainBareFrom(String jid) throws XmppStringprepException {
		DomainBareJid domainJid = DOMAINJID_CACHE.get(jid);
		if (domainJid != null) {
			return domainJid;
		}

		String domain = XmppStringUtils.parseDomain(jid);
		try {
			domainJid = new DomainpartJid(domain);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(jid, e);
		}
		DOMAINJID_CACHE.put(jid, domainJid);
		return domainJid;
	}

	public static DomainBareJid domainBareFrom(Domainpart domainpart) {
		return new DomainpartJid(domainpart);
	}

	/**
	 * Deprecated.
	 *
	 * @param jid the JID.
	 * @return a DomainFullJid
	 * @throws XmppStringprepException if an error happens.
	 * @deprecated use {@link #domainFullFrom(String)} instead
	 */
	@Deprecated
	public static DomainFullJid serverFullFrom(String jid) throws XmppStringprepException {
		return donmainFullFrom(jid);
	}

	/**
	 * Get a domain full JID from the given String.
	 *
	 * @param jid the JID.
	 * @return a DomainFullJid.
	 * @throws XmppStringprepException if an error happens.
	 * @deprecated use {@link #domainFullFrom(String)} instead.
	 */
	@Deprecated
	public static DomainFullJid donmainFullFrom(String jid) throws XmppStringprepException {
		return domainFullFrom(jid);
	}

	public static DomainFullJid domainFullFrom(CharSequence jid) throws XmppStringprepException {
		return domainFullFrom(jid.toString());
	}

	public static DomainFullJid domainFullFrom(String jid) throws XmppStringprepException {
		DomainFullJid domainResourceJid = DOMAINRESOURCEJID_CACHE.get(jid);
		if (domainResourceJid != null) {
			return domainResourceJid;
		}

		String domain = XmppStringUtils.parseDomain(jid);
		String resource = XmppStringUtils.parseResource(jid);
		try {
			domainResourceJid = new DomainAndResourcepartJid(domain, resource);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(jid, e);
		}
		DOMAINRESOURCEJID_CACHE.put(jid, domainResourceJid);
		return domainResourceJid;
	}

	public static DomainFullJid domainFullFrom(Domainpart domainpart, Resourcepart resource) {
		return domainFullFrom(domainBareFrom(domainpart), resource);
	}

	public static DomainFullJid domainFullFrom(DomainBareJid domainBareJid, Resourcepart resource) {
		return new DomainAndResourcepartJid(domainBareJid, resource);
	}
}
