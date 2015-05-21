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

import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.DomainFullJid;
import org.jxmpp.jid.EntityFullJid;
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
	private static final Cache<String, EntityBareJid> BAREJID_CACHE = new LruCache<String, EntityBareJid>(100);
	private static final Cache<String, EntityFullJid> FULLJID_CACHE = new LruCache<String, EntityFullJid>(100);
	private static final Cache<String, DomainBareJid> DOMAINJID_CACHE = new LruCache<String, DomainBareJid>(100);
	private static final Cache<String, DomainFullJid> DOMAINRESOURCEJID_CACHE = new LruCache<String, DomainFullJid>(100);

	/**
	 * Get a {@link Jid} from the given parts.
	 * <p>
	 * Only the domainpart is required.
	 * </p>
	 *
	 * @param localpart a optional localpart.
	 * @param domainpart a required domainpart.
	 * @param resource a optional resourcepart.
	 * @return a JID which consists of the given parts.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static Jid from(CharSequence localpart, CharSequence domainpart, CharSequence resource)
			throws XmppStringprepException {
		return from(localpart.toString(), domainpart.toString(), resource.toString());
	}

	/**
	 * Get a {@link Jid} from the given parts.
	 * <p>
	 * Only the domainpart is required.
	 * </p>
	 *
	 * @param localpart a optional localpart.
	 * @param domainpart a required domainpart.
	 * @param resource a optional resourcepart.
	 * @return a JID which consists of the given parts.
	 * @throws XmppStringprepException if an error occurs.
	 */
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
	 * Get a {@link Jid} from a CharSequence.
	 *
	 * @param jid the input CharSequence.
	 * @return the Jid represented by the input CharSequence.
	 * @throws XmppStringprepException if an error occurs.
	 * @see #from(String)
	 */
	public static Jid from(CharSequence jid) throws XmppStringprepException {
		return from(jid.toString());
	}

	/**
	 * Get a {@link Jid} from the given String.
	 *
	 * @param jidString the input String.
	 * @return the Jid represented by the input String.
	 * @throws XmppStringprepException if an error occurs.
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

	/**
	 * Get a {@link Jid} from the given unescaped CharSequence.
	 *
	 * @param unescapedJid an unescaped CharSequence representing a JID.
	 * @return a JID.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static Jid fromUnescaped(CharSequence unescapedJid) throws XmppStringprepException {
		return fromUnescaped(unescapedJid.toString());
	}

	/**
	 * Get a {@link Jid} from the given unescaped String.
	 *
	 * @param unescapedJidString a unescaped String representing a JID.
	 * @return a JID.
	 * @throws XmppStringprepException if an error occurs.
	 */
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

	/**
	 * Get a {@link EntityBareJid} representing the given CharSequence.
	 *
	 * @param jid the input CharSequence.
	 * @return a bare JID representing the given CharSequence.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityBareJid bareFrom(CharSequence jid) throws XmppStringprepException {
		return bareFrom(jid.toString());
	}

	/**
	 * Get a {@link EntityBareJid} representing the given String.
	 *
	 * @param jid the input String.
	 * @return a bare JID representing the given String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityBareJid bareFrom(String jid) throws XmppStringprepException {
		EntityBareJid bareJid = BAREJID_CACHE.get(jid);
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

	/**
	 * Get a {@link EntityBareJid} constructed from the given {@link Localpart} and {link DomainBareJid}.
	 *
	 * @param localpart a localpart.
	 * @param domainBareJid a domain bare JID.
	 * @return a bare JID.
	 */
	public static EntityBareJid bareFrom(Localpart localpart, DomainBareJid domainBareJid) {
		return bareFrom(localpart, domainBareJid.getDomain());
	}

	/**
	 * Get a {@link EntityBareJid} constructed from the given {@link Localpart} and {@link Domainpart}.
	 *
	 * @param localpart a localpart.
	 * @param domain a domainpart.
	 * @return a bare JID constructed from the given parts.
	 */
	public static EntityBareJid bareFrom(Localpart localpart, Domainpart domain) {
		return new LocalAndDomainpartJid(localpart, domain);
	}

	/**
	 * Get a {@link EntityFullJid} representing the given CharSequence.
	 *
	 * @param jid a CharSequence representing a JID.
	 * @return a full JID representing the given CharSequence.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityFullJid fullFrom(CharSequence jid) throws XmppStringprepException {
		return fullFrom(jid.toString());
	}

	/**
	 * Get a {@link EntityFullJid} representing the given String.
	 *
	 * @param jid the JID's String.
	 * @return a full JID representing the input String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityFullJid fullFrom(String jid) throws XmppStringprepException {
		EntityFullJid fullJid = FULLJID_CACHE.get(jid);
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

	/**
	 * Get a {@link EntityFullJid} constructed from the given parts.
	 *
	 * @param localpart a localpart.
	 * @param domainpart a domainpart.
	 * @param resource a resourcepart.
	 * @return a full JID.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityFullJid fullFrom(String localpart, String domainpart, String resource) throws XmppStringprepException {
		EntityFullJid fullJid;
		try {
			fullJid = new LocalDomainAndResourcepartJid(localpart, domainpart, resource);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(localpart + '@' + domainpart + '/' + resource, e);
		}
		return fullJid;
	}

	/**
	 * Get a {@link EntityFullJid} constructed from the given parts.
	 *
	 * @param localpart a localpart.
	 * @param domainBareJid a domain bare JID.. 
	 * @param resource a resourcepart
	 * @return a full JID.
	 */
	public static EntityFullJid fullFrom(Localpart localpart, DomainBareJid domainBareJid, Resourcepart resource) {
		return fullFrom(localpart, domainBareJid.getDomain(), resource);
	}

	/**
	 * Get a {@link EntityFullJid} constructed from the given parts.
	 * 
	 * @param localpart the localpart.
	 * @param domainpart the domainpart.
	 * @param resource the resourcepart.
	 * @return a full JID.
	 */
	public static EntityFullJid fullFrom(Localpart localpart, Domainpart domainpart, Resourcepart resource) {
		return fullFrom(bareFrom(localpart, domainpart), resource);
	}

	/**
	 * Get a {@link EntityFullJid} constructed from a {@link EntityBareJid} and a {@link Resourcepart}.
	 *
	 * @param bareJid a bare JID.
	 * @param resource a resourcepart.
	 * @return a full JID.
	 */
	public static EntityFullJid fullFrom(EntityBareJid bareJid, Resourcepart resource) {
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

	/**
	 * Get a domain bare JID.
	 *
	 * @param jid the JID CharSequence.
	 * @return a domain bare JID.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static DomainBareJid domainBareFrom(CharSequence jid) throws XmppStringprepException {
		return domainBareFrom(jid.toString());
	}

	/**
	 * Get a domain bare JID.
	 *
	 * @param jid the JID String.
	 * @return a domain bare JID.
	 * @throws XmppStringprepException if an error occurs.
	 */
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

	/**
	 * Get a {@link DomainBareJid} consisting of the given {@link Domainpart}.
	 *
	 * @param domainpart the domainpart.
	 * @return a domain bare JID.
	 */
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

	/**
	 * Get a domain full JID from the given CharSequence.
	 *
	 * @param jid the JID.
	 * @return a domain full JID.
	 * @throws XmppStringprepException if an error happens.
	 */
	public static DomainFullJid domainFullFrom(CharSequence jid) throws XmppStringprepException {
		return domainFullFrom(jid.toString());
	}

	/**
	 * Get a domain full JID from the given String.
	 *
	 * @param jid the JID.
	 * @return a DomainFullJid.
	 * @throws XmppStringprepException if an error happens.
	 */
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

	/**
	 * Get a domain full JID.
	 *
	 * @param domainpart the domainpart.
	 * @param resource the resourcepart.
	 * @return a domain full JID.
	 */
	public static DomainFullJid domainFullFrom(Domainpart domainpart, Resourcepart resource) {
		return domainFullFrom(domainBareFrom(domainpart), resource);
	}

	/**
	 * Get a domain full JID.
	 *
	 * @param domainBareJid a domain bare JID.
	 * @param resource a resourcepart.
	 * @return a domain full JID.
	 */
	public static DomainFullJid domainFullFrom(DomainBareJid domainBareJid, Resourcepart resource) {
		return new DomainAndResourcepartJid(domainBareJid, resource);
	}
}
