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
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.cache.Cache;
import org.jxmpp.util.cache.LruCache;
import org.jxmpp.util.XmppStringUtils;

/**
 * API to create JIDs (XMPP addresses) from Strings and CharSequences.
 * <p>
 * If the input was user generated, e.g. captured from some sort of user
 * interface, {@link #fromUnescaped(String)} should be used instead. This allows
 * the user to enter unescaped JID values. You can use
 * {@link org.jxmpp.jid.util.JidUtil#isValidEntityBareJid(CharSequence)} to
 * query, e.g. while the user it entering it, if a given CharSequence is a valid
 * bare JID.
 * </p>
 * <p>
 * JIDs created from input received from an XMPP source should use
 * {@link #from(String)}.
 * </p>
 * <p>
 * JidCreate uses caches for efficient Jid construction, But it's not guaranteed
 * that the same String or CharSequence will yield the same Jid instance.
 * </p>
 *
 * @see Jid
 */
public class JidCreate {

	private static final Cache<String, Jid> JID_CACHE = new LruCache<String, Jid>(100);
	private static final Cache<String, BareJid> BAREJID_CACHE = new LruCache<>(100);
	private static final Cache<String, EntityJid> ENTITYJID_CACHE = new LruCache<>(100);
	private static final Cache<String, FullJid> FULLJID_CACHE = new LruCache<>(100);
	private static final Cache<String, EntityBareJid> ENTITY_BAREJID_CACHE = new LruCache<>(100);
	private static final Cache<String, EntityFullJid> ENTITY_FULLJID_CACHE = new LruCache<>(100);
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
		Jid jid = JID_CACHE.lookup(jidString);
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
	 * Like {@link #from(CharSequence)} but does throw an unchecked {@link IllegalArgumentException} instead of a
	 * {@link XmppStringprepException}.
	 *
	 * @param cs the character sequence which should be transformed to a {@link Jid}
	 * @return the {@link Jid} if no exception occurs
	 * @throws IllegalArgumentException if the given input is not a valid JID
	 * @see #from(String)
	 * @since 0.6.2
	 */
	public static Jid fromOrThrowUnchecked(CharSequence cs) {
		try {
			return from(cs);
		} catch (XmppStringprepException e) {
			throw new IllegalArgumentException(e);
		}
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
	 * Get a {@link Jid} from a given {@link CharSequence} or {@code null} if the input does not represent a JID.
	 *
	 * @param cs the input {@link CharSequence}
	 * @return a JID or {@code null}
	 */
	public static Jid fromOrNull(CharSequence cs) {
		try {
			return from(cs);
		} catch (XmppStringprepException e) {
			return null;
		}
	}

	/**
	 * Like {@link #fromUnescaped(CharSequence)} but does throw an unchecked {@link IllegalArgumentException} instead of a
	 * {@link XmppStringprepException}.
	 *
	 * @param cs the character sequence which should be transformed to a {@link Jid}
	 * @return the {@link Jid} if no exception occurs
	 * @throws IllegalArgumentException if the given input is not a valid JID
	 * @see #fromUnescaped(CharSequence)
	 * @since 0.6.2
	 */
	public static Jid fromUnescapedOrThrowUnchecked(CharSequence cs) {
		try {
			return fromUnescaped(cs);
		} catch (XmppStringprepException e) {
			throw new IllegalArgumentException(e);
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
	 * Get a {@link Jid} from a given {@link CharSequence} or {@code null} if the input does not represent a JID.
	 *
	 * @param cs the input {@link CharSequence}
	 * @return a JID or {@code null}
	 */
	public static Jid fromUnescapedOrNull(CharSequence cs) {
		try {
			return fromUnescaped(cs);
		} catch (XmppStringprepException e) {
			return null;
		}
	}

	/**
	 * Like {@link #bareFrom(CharSequence)} but does throw an unchecked {@link IllegalArgumentException} instead of a
	 * {@link XmppStringprepException}.
	 *
	 * @param cs the character sequence which should be transformed to a {@link BareJid}
	 * @return the {@link BareJid} if no exception occurs
	 * @throws IllegalArgumentException if the given input is not a valid JID
	 * @see #bareFrom(CharSequence)
	 * @since 0.6.2
	 */
	public static BareJid bareFromOrThrowUnchecked(CharSequence cs) {
		try {
			return bareFrom(cs);
		} catch (XmppStringprepException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Get a {@link BareJid} representing the given CharSequence.
	 *
	 * @param jid the input CharSequence.
	 * @return a bare JID representing the given CharSequence.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static BareJid bareFrom(CharSequence jid) throws XmppStringprepException {
		return bareFrom(jid.toString());
	}

	/**
	 * Get a {@link BareJid} representing the given String.
	 *
	 * @param jid the input String.
	 * @return a bare JID representing the given String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static BareJid bareFrom(String jid) throws XmppStringprepException {
		BareJid bareJid = BAREJID_CACHE.lookup(jid);
		if (bareJid != null) {
			return bareJid;
		}

		String localpart = XmppStringUtils.parseLocalpart(jid);
		String domainpart = XmppStringUtils.parseDomain(jid);
		try {
			if (localpart.length() != 0) {
				bareJid = new LocalAndDomainpartJid(localpart, domainpart);
			} else {
				bareJid = new DomainpartJid(domainpart);
			}
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(jid, e);
		}
		BAREJID_CACHE.put(jid, bareJid);
		return bareJid;
	}

	/**
	 * Get a {@link BareJid} constructed from the optionally given {@link Localpart} and {link DomainBareJid}.
	 *
	 * @param localpart a optional localpart.
	 * @param domainBareJid a domain bare JID.
	 * @return a bare JID.
	 */
	public static BareJid bareFrom(Localpart localpart, DomainBareJid domainBareJid) {
		return bareFrom(localpart, domainBareJid.getDomain());
	}

	/**
	 * Get a {@link BareJid} constructed from the optionally given {@link Localpart} and {@link Domainpart}.
	 *
	 * @param localpart a optional localpart.
	 * @param domain a domainpart.
	 * @return a bare JID constructed from the given parts.
	 */
	public static BareJid bareFrom(Localpart localpart, Domainpart domain) {
		if (localpart != null) {
			return new LocalAndDomainpartJid(localpart, domain);
		} else {
			return new DomainpartJid(domain);
		}
	}

	/**
	 * Get a {@link BareJid} from a given {@link CharSequence} or {@code null} if the input does not represent a JID.
	 *
	 * @param cs the input {@link CharSequence}
	 * @return a JID or {@code null}
	 */
	public static BareJid bareFromOrNull(CharSequence cs) {
		try {
			return bareFrom(cs);
		} catch (XmppStringprepException e) {
			return null;
		}
	}

	/**
	 * Like {@link #fullFrom(CharSequence)} but does throw an unchecked {@link IllegalArgumentException} instead of a
	 * {@link XmppStringprepException}.
	 *
	 * @param cs the character sequence which should be transformed to a {@link FullJid}
	 * @return the {@link FullJid} if no exception occurs
	 * @throws IllegalArgumentException if the given input is not a valid JID
	 * @see #fullFrom(CharSequence)
	 * @since 0.6.2
	 */
	public static FullJid fullFromOrThrowUnchecked(CharSequence cs) {
		try {
			return fullFrom(cs);
		} catch (XmppStringprepException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Get a {@link FullJid} representing the given CharSequence.
	 *
	 * @param jid a CharSequence representing a JID.
	 * @return a full JID representing the given CharSequence.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static FullJid fullFrom(CharSequence jid) throws XmppStringprepException {
		return fullFrom(jid.toString());
	}

	/**
	 * Get a {@link FullJid} representing the given String.
	 *
	 * @param jid the JID's String.
	 * @return a full JID representing the input String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static FullJid fullFrom(String jid) throws XmppStringprepException {
		FullJid fullJid = FULLJID_CACHE.lookup(jid);
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
	 * Get a {@link FullJid} constructed from the given parts.
	 *
	 * @param localpart a optional localpart.
	 * @param domainpart a domainpart.
	 * @param resource a resourcepart.
	 * @return a full JID.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static FullJid fullFrom(String localpart, String domainpart, String resource) throws XmppStringprepException {
		FullJid fullJid;
		try {
			if (localpart == null || localpart.length() == 0) {
				fullJid = new DomainAndResourcepartJid(domainpart, resource);
			} else {
				fullJid = new LocalDomainAndResourcepartJid(localpart, domainpart, resource);
			}
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(localpart + '@' + domainpart + '/' + resource, e);
		}
		return fullJid;
	}

	/**
	 * Get a {@link FullJid} constructed from the given parts.
	 *
	 * @param localpart a optional localpart.
	 * @param domainBareJid a domain bare JID. 
	 * @param resource a resourcepart
	 * @return a full JID.
	 */
	public static FullJid fullFrom(Localpart localpart, DomainBareJid domainBareJid, Resourcepart resource) {
		return fullFrom(localpart, domainBareJid.getDomain(), resource);
	}

	/**
	 * Get a {@link FullJid} constructed from the given parts.
	 *
	 * @param localpart the optional localpart.
	 * @param domainpart the domainpart.
	 * @param resource the resourcepart.
	 * @return a full JID.
	 */
	public static FullJid fullFrom(Localpart localpart, Domainpart domainpart, Resourcepart resource) {
		return fullFrom(entityBareFrom(localpart, domainpart), resource);
	}

	/**
	 * Get a {@link FullJid} from a given {@link CharSequence} or {@code null} if the input does not represent a JID.
	 *
	 * @param cs the input {@link CharSequence}
	 * @return a JID or {@code null}
	 */
	public static FullJid fullFromOrNull(CharSequence cs) {
		try {
			return fullFrom(cs);
		} catch (XmppStringprepException e) {
			return null;
		}
	}

	/**
	 * Like {@link #entityFrom(CharSequence)} but does throw an unchecked {@link IllegalArgumentException} instead of a
	 * {@link XmppStringprepException}.
	 *
	 * @param cs the character sequence which should be transformed to a {@link EntityJid}
	 * @return the {@link EntityJid} if no exception occurs
	 * @throws IllegalArgumentException if the given input is not a valid JID
	 * @see #entityFrom(CharSequence)
	 * @since 0.6.2
	 */
	public static EntityJid entityFromOrThrowUnchecked(CharSequence cs) {
		try {
			return entityFrom(cs);
		} catch (XmppStringprepException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Get a {@link EntityFullJid} constructed from a {@link EntityBareJid} and a {@link Resourcepart}.
	 *
	 * @param bareJid a entity bare JID.
	 * @param resource a resourcepart.
	 * @return a full JID.
	 */
	public static EntityFullJid fullFrom(EntityBareJid bareJid, Resourcepart resource) {
		return new LocalDomainAndResourcepartJid(bareJid, resource);
	}

	/**
	 * Get a {@link EntityJid} representing the given String.
	 *
	 * @param jid the JID's string.
	 * @return an entity JID representing the given String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityJid entityFrom(CharSequence jid) throws XmppStringprepException {
		return entityFrom(jid.toString());
	}

	/**
	 * Get a {@link EntityJid} representing the given String.
	 *
	 * @param jidString the JID's string.
	 * @return an entity JID representing the given String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityJid entityFrom(String jidString) throws XmppStringprepException {
		return entityFrom(jidString, false);
	}

	/**
	 * Like {@link #entityFromUnescaped(CharSequence)} but does throw an unchecked {@link IllegalArgumentException} instead of a
	 * {@link XmppStringprepException}.
	 *
	 * @param cs the character sequence which should be transformed to a {@link EntityJid}
	 * @return the {@link EntityJid} if no exception occurs
	 * @throws IllegalArgumentException if the given input is not a valid JID
	 * @see #entityFromUnescaped(CharSequence)
	 * @since 0.6.2
	 */
	public static EntityJid entityFromUnescapedOrThrowUnchecked(CharSequence cs) {
		try {
			return entityFromUnescaped(cs);
		} catch (XmppStringprepException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Get a {@link EntityJid} representing the given String.
	 *
	 * @param jid the JID.
	 * @return an entity JID representing the given input..
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityJid entityFromUnescaped(CharSequence jid) throws XmppStringprepException {
		return entityFromUnescaped(jid.toString());
	}

	/**
	 * Get a {@link EntityJid} representing the given String.
	 *
	 * @param jidString the JID's string.
	 * @return an entity JID representing the given String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityJid entityFromUnescaped(String jidString) throws XmppStringprepException {
		return entityFrom(jidString, true);
	}

	/**
	 * Get a {@link EntityJid} from a given {@link CharSequence} or {@code null} if the input does not represent a JID.
	 *
	 * @param cs the input {@link CharSequence}
	 * @return a JID or {@code null}
	 */
	public static EntityJid entityFromUnesacpedOrNull(CharSequence cs) {
		try {
			return entityFromUnescaped(cs.toString());
		} catch (XmppStringprepException e) {
			return null;
		}
	}

	/**
	 * Get a {@link EntityJid} representing the given String.
	 *
	 * @param jidString the JID's string.
	 * @param unescaped if the JID string is unescaped.
	 * @return an entity JID representing the given String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	private static EntityJid entityFrom(String jidString, boolean unescaped) throws XmppStringprepException {
		EntityJid entityJid = ENTITYJID_CACHE.lookup(jidString);
		if (entityJid != null) {
			return entityJid;
		}
		String localpartString = XmppStringUtils.parseLocalpart(jidString);
		if (localpartString.length() ==  0) {
			throw new XmppStringprepException("Does not contain a localpart", jidString);
		}
		Localpart localpart;
		try {
			if (unescaped) {
				localpart = Localpart.fromUnescaped(localpartString);
			} else {
				localpart = Localpart.from(localpartString);
			}
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(jidString, e);
		}

		String domainpartString = XmppStringUtils.parseDomain(jidString);
		Domainpart domainpart;
		try {
			domainpart = Domainpart.from(domainpartString);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(jidString, e);
		}

		String resourceString = XmppStringUtils.parseResource(jidString);
		if (resourceString.length() > 0) {
			Resourcepart resourcepart;
			try {
				resourcepart = Resourcepart.from(resourceString);
			} catch (XmppStringprepException e) {
				throw new XmppStringprepException(jidString, e);
			}
			entityJid = entityFullFrom(localpart, domainpart, resourcepart);
		} else {
			entityJid = entityBareFrom(localpart, domainpart);
		}

		ENTITYJID_CACHE.put(jidString, entityJid);
		return entityJid;
	}

	/**
	 * Get a {@link EntityJid} from a given {@link CharSequence} or {@code null} if the input does not represent a JID.
	 *
	 * @param cs the input {@link CharSequence}
	 * @return a JID or {@code null}
	 */
	public static EntityJid entityFromOrNull(CharSequence cs) {
		try {
			return entityFrom(cs);
		} catch (XmppStringprepException e) {
			return null;
		}
	}

	/**
	 * Like {@link #entityBareFrom(CharSequence)} but does throw an unchecked {@link IllegalArgumentException} instead of a
	 * {@link XmppStringprepException}.
	 *
	 * @param cs the character sequence which should be transformed to a {@link EntityBareJid}
	 * @return the {@link EntityBareJid} if no exception occurs
	 * @throws IllegalArgumentException if the given input is not a valid JID
	 * @see #entityBareFrom(CharSequence)
	 * @since 0.6.2
	 */
	public static EntityBareJid entityBareFromOrThrowUnchecked(CharSequence cs) {
		try {
			return entityBareFrom(cs);
		} catch (XmppStringprepException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Get a {@link EntityBareJid} representing the given CharSequence.
	 *
	 * @param jid the input CharSequence.
	 * @return a bare JID representing the given CharSequence.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityBareJid entityBareFrom(CharSequence jid) throws XmppStringprepException {
		return entityBareFrom(jid.toString());
	}

	/**
	 * Get a {@link EntityBareJid} representing the given String.
	 *
	 * @param jid the input String.
	 * @return a bare JID representing the given String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityBareJid entityBareFrom(String jid) throws XmppStringprepException {
		EntityBareJid bareJid = ENTITY_BAREJID_CACHE.lookup(jid);
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
		ENTITY_BAREJID_CACHE.put(jid, bareJid);
		return bareJid;
	}

	/**
	 * Like {@link #entityBareFromUnescaped(CharSequence)} but does throw an unchecked {@link IllegalArgumentException} instead of a
	 * {@link XmppStringprepException}.
	 *
	 * @param cs the character sequence which should be transformed to a {@link EntityBareJid}
	 * @return the {@link EntityBareJid} if no exception occurs
	 * @throws IllegalArgumentException if the given input is not a valid JID
	 * @see #entityBareFromUnescaped(CharSequence)
	 * @since 0.6.2
	 */
	public static EntityBareJid entityBareFromUnescapedOrThrowUnchecked(CharSequence cs) {
		try {
			return entityBareFromUnescaped(cs);
		} catch (XmppStringprepException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Get a {@link EntityBareJid} representing the given unescaped CharSequence.
	 *
	 * @param unescapedJid the input CharSequence.
	 * @return a bare JID representing the given CharSequence.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityBareJid entityBareFromUnescaped(CharSequence unescapedJid) throws XmppStringprepException {
		return entityBareFromUnescaped(unescapedJid.toString());
	}

	/**
	 * Get a {@link EntityBareJid} representing the given unescaped String.
	 *
	 * @param unescapedJidString the input String.
	 * @return a bare JID representing the given String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityBareJid entityBareFromUnescaped(String unescapedJidString) throws XmppStringprepException {
		EntityBareJid bareJid = ENTITY_BAREJID_CACHE.lookup(unescapedJidString);
		if (bareJid != null) {
			return bareJid;
		}

		String localpart = XmppStringUtils.parseLocalpart(unescapedJidString);
		// Some as from(String), but we escape the localpart
		localpart = XmppStringUtils.escapeLocalpart(localpart);

		String domainpart = XmppStringUtils.parseDomain(unescapedJidString);
		try {
			bareJid = new LocalAndDomainpartJid(localpart, domainpart);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(unescapedJidString, e);
		}
		ENTITY_BAREJID_CACHE.put(unescapedJidString, bareJid);
		return bareJid;
	}

	/**
	 * Get a {@link EntityBareJid} from a given {@link CharSequence} or {@code null} if the input does not represent a JID.
	 *
	 * @param cs the input {@link CharSequence}
	 * @return a JID or {@code null}
	 */
	public static EntityBareJid entityBareFromUnescapedOrNull(CharSequence cs) {
		try {
			return entityBareFromUnescaped(cs.toString());
		} catch (XmppStringprepException e) {
			return null;
		}
	}

	/**
	 * Get a {@link EntityBareJid} constructed from the given {@link Localpart} and {link DomainBareJid}.
	 *
	 * @param localpart a localpart.
	 * @param domainBareJid a domain bare JID.
	 * @return a bare JID.
	 */
	public static EntityBareJid entityBareFrom(Localpart localpart, DomainBareJid domainBareJid) {
		return entityBareFrom(localpart, domainBareJid.getDomain());
	}

	/**
	 * Get a {@link EntityBareJid} constructed from the given {@link Localpart} and {@link Domainpart}.
	 *
	 * @param localpart a localpart.
	 * @param domain a domainpart.
	 * @return a bare JID constructed from the given parts.
	 */
	public static EntityBareJid entityBareFrom(Localpart localpart, Domainpart domain) {
		return new LocalAndDomainpartJid(localpart, domain);
	}

	/**
	 * Get a {@link EntityBareJid} from a given {@link CharSequence} or {@code null} if the input does not represent a JID.
	 *
	 * @param cs the input {@link CharSequence}
	 * @return a JID or {@code null}
	 */
	public static EntityBareJid entityBareFromOrNull(CharSequence cs) {
		try {
			return entityBareFrom(cs);
		} catch (XmppStringprepException e) {
			return null;
		}
	}

	/**
	 * Like {@link #entityFullFrom(CharSequence)} but does throw an unchecked {@link IllegalArgumentException} instead of a
	 * {@link XmppStringprepException}.
	 *
	 * @param cs the character sequence which should be transformed to a {@link EntityFullJid}
	 * @return the {@link EntityFullJid} if no exception occurs
	 * @throws IllegalArgumentException if the given input is not a valid JID
	 * @see #entityFullFrom(CharSequence)
	 * @since 0.6.2
	 */
	public static EntityFullJid entityFullFromOrThrowUnchecked(CharSequence cs) {
		try {
			return entityFullFrom(cs);
		} catch (XmppStringprepException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Get a {@link EntityFullJid} representing the given CharSequence.
	 *
	 * @param jid a CharSequence representing a JID.
	 * @return a full JID representing the given CharSequence.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityFullJid entityFullFrom(CharSequence jid) throws XmppStringprepException {
		return entityFullFrom(jid.toString());
	}

	/**
	 * Get a {@link EntityFullJid} representing the given String.
	 *
	 * @param jid the JID's String.
	 * @return a full JID representing the input String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityFullJid entityFullFrom(String jid) throws XmppStringprepException {
		EntityFullJid fullJid = ENTITY_FULLJID_CACHE.lookup(jid);
		if (fullJid != null) {
			return fullJid;
		}

		String localpart = XmppStringUtils.parseLocalpart(jid);
		String domainpart = XmppStringUtils.parseDomain(jid);
		String resource = XmppStringUtils.parseResource(jid);
		try {
			fullJid = entityFullFrom(localpart, domainpart, resource);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(jid, e);
		}
		ENTITY_FULLJID_CACHE.put(jid, fullJid);
		return fullJid;
	}

	/**
	 * Get a {@link EntityFullJid} from a given {@link CharSequence} or {@code null} if the input does not represent a JID.
	 *
	 * @param cs the input {@link CharSequence}
	 * @return a JID or {@code null}
	 */
	public static EntityFullJid entityFullFromOrNull(CharSequence cs) {
		try {
			return entityFullFrom(cs);
		} catch (XmppStringprepException e) {
			return null;
		}
	}

	/**
	 * Like {@link #entityFullFromUnescaped(CharSequence)} but does throw an unchecked {@link IllegalArgumentException} instead of a
	 * {@link XmppStringprepException}.
	 *
	 * @param cs the character sequence which should be transformed to a {@link EntityFullJid}
	 * @return the {@link EntityFullJid} if no exception occurs
	 * @throws IllegalArgumentException if the given input is not a valid JID
	 * @see #entityFullFromUnescaped(CharSequence)
	 * @since 0.6.2
	 */
	public static EntityFullJid entityFullFromUnescapedOrThrowUnchecked(CharSequence cs) {
		try {
			return entityFullFromUnescaped(cs);
		} catch (XmppStringprepException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Get a {@link EntityFullJid} representing the given unescaped CharSequence.
	 *
	 * @param unescapedJid a CharSequence representing a JID.
	 * @return a full JID representing the given CharSequence.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityFullJid entityFullFromUnescaped(CharSequence unescapedJid) throws XmppStringprepException {
		return entityFullFromUnescaped(unescapedJid.toString());
	}

	/**
	 * Get a {@link EntityFullJid} representing the given unescaped String.
	 *
	 * @param unescapedJidString the JID's String.
	 * @return a full JID representing the input String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityFullJid entityFullFromUnescaped(String unescapedJidString) throws XmppStringprepException {
		EntityFullJid fullJid = ENTITY_FULLJID_CACHE.lookup(unescapedJidString);
		if (fullJid != null) {
			return fullJid;
		}

		String localpart = XmppStringUtils.parseLocalpart(unescapedJidString);
		// Some as from(String), but we escape the localpart
		localpart = XmppStringUtils.escapeLocalpart(localpart);

		String domainpart = XmppStringUtils.parseDomain(unescapedJidString);
		String resource = XmppStringUtils.parseResource(unescapedJidString);
		try {
			fullJid = new LocalDomainAndResourcepartJid(localpart, domainpart, resource);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(unescapedJidString, e);
		}

		ENTITY_FULLJID_CACHE.put(unescapedJidString, fullJid);
		return fullJid;
	}

	/**
	 * Get a {@link EntityFullJid} from a given {@link CharSequence} or {@code null} if the input does not represent a JID.
	 *
	 * @param cs the input {@link CharSequence}
	 * @return a JID or {@code null}
	 */
	public static EntityFullJid entityFullFromUnescapedOrNull(CharSequence cs) {
		try {
			return entityFullFromUnescaped(cs.toString());
		} catch (XmppStringprepException e) {
			return null;
		}
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
	public static EntityFullJid entityFullFrom(String localpart, String domainpart, String resource) throws XmppStringprepException {
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
	public static EntityFullJid entityFullFrom(Localpart localpart, DomainBareJid domainBareJid, Resourcepart resource) {
		return entityFullFrom(localpart, domainBareJid.getDomain(), resource);
	}

	/**
	 * Get a {@link EntityFullJid} constructed from the given parts.
	 * 
	 * @param localpart the localpart.
	 * @param domainpart the domainpart.
	 * @param resource the resourcepart.
	 * @return a full JID.
	 */
	public static EntityFullJid entityFullFrom(Localpart localpart, Domainpart domainpart, Resourcepart resource) {
		return entityFullFrom(entityBareFrom(localpart, domainpart), resource);
	}

	/**
	 * Get a {@link EntityFullJid} constructed from a {@link EntityBareJid} and a {@link Resourcepart}.
	 *
	 * @param bareJid a bare JID.
	 * @param resource a resourcepart.
	 * @return a full JID.
	 */
	public static EntityFullJid entityFullFrom(EntityBareJid bareJid, Resourcepart resource) {
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
	 * Like {@link #domainBareFrom(CharSequence)} but does throw an unchecked {@link IllegalArgumentException} instead of a
	 * {@link XmppStringprepException}.
	 *
	 * @param cs the character sequence which should be transformed to a {@link EntityFullJid}
	 * @return the {@link EntityFullJid} if no exception occurs
	 * @see #from(String)
	 * @since 0.6.2
	 */
	public static DomainBareJid domainBareFromOrThrowUnchecked(CharSequence cs) {
		try {
			return domainBareFrom(cs);
		} catch (XmppStringprepException e) {
			throw new IllegalArgumentException(e);
		}
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
		DomainBareJid domainJid = DOMAINJID_CACHE.lookup(jid);
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
	 * Get a {@link DomainBareJid} from a given {@link CharSequence} or {@code null} if the input does not represent a JID.
	 *
	 * @param cs the input {@link CharSequence}
	 * @return a JID or {@code null}
	 */
	public static DomainBareJid domainBareFromOrNull(CharSequence cs) {
		try {
			return domainBareFrom(cs);
		} catch (XmppStringprepException e) {
			return null;
		}
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
	 * Like {@link #domainFullFrom(CharSequence)} but does throw an unchecked {@link IllegalArgumentException} instead of a
	 * {@link XmppStringprepException}.
	 *
	 * @param cs the character sequence which should be transformed to a {@link DomainFullJid}
	 * @return the {@link DomainFullJid} if no exception occurs
	 * @throws IllegalArgumentException if the given input is not a valid JID
	 * @see #domainFullFrom(CharSequence)
	 * @since 0.6.2
	 */
	public static DomainFullJid domainFullFromOrThrowUnchecked(CharSequence cs) {
		try {
			return domainFullFrom(cs);
		} catch (XmppStringprepException e) {
			throw new IllegalArgumentException(e);
		}
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
		DomainFullJid domainResourceJid = DOMAINRESOURCEJID_CACHE.lookup(jid);
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

	/**
	 * Get a {@link DomainFullJid} from a given {@link CharSequence} or {@code null} if the input does not represent a JID.
	 *
	 * @param cs the input {@link CharSequence}
	 * @return a JID or {@code null}
	 */
	public static DomainFullJid domainFullFromOrNull(CharSequence cs) {
		try {
			return domainFullFrom(cs);
		} catch (XmppStringprepException e) {
			return null;
		}
	}
}
