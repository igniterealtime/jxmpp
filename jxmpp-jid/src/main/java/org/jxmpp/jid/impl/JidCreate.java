/**
 *
 * Copyright © 2014-2022 Florian Schmaus
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.jxmpp.JxmppContext;
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
import org.jxmpp.stringprep.XmppStringprep;
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

	private static class JidStringAndStringprep {
		private final String jidString;
		private final XmppStringprep stringprep;

		private JidStringAndStringprep(String jidString, JxmppContext context) {
			this(jidString, context.xmppStringprep);
		}

		private JidStringAndStringprep(String jidString, XmppStringprep stringprep) {
			this.jidString = jidString;
			this.stringprep = stringprep;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof JidStringAndStringprep))
				return false;

			JidStringAndStringprep otherJidStringAndStringprep = (JidStringAndStringprep) other;
			return jidString.equals(otherJidStringAndStringprep.jidString) && stringprep.equals(otherJidStringAndStringprep.stringprep);
		}

		private transient Integer hashCode;

		@Override
		public int hashCode() {
			if (hashCode == null) {
				int result = 17;
				result = 31 * result + jidString.hashCode();
				result = 31 * result + stringprep.hashCode();
				hashCode = result;
			}
			return hashCode;
		}
	}

	private static final Cache<JidStringAndStringprep, Jid> JID_CACHE = new LruCache<>(100);
	private static final Cache<JidStringAndStringprep, BareJid> BAREJID_CACHE = new LruCache<>(100);
	private static final Cache<JidStringAndStringprep, EntityJid> ENTITYJID_CACHE = new LruCache<>(100);
	private static final Cache<JidStringAndStringprep, FullJid> FULLJID_CACHE = new LruCache<>(100);
	private static final Cache<JidStringAndStringprep, EntityBareJid> ENTITY_BAREJID_CACHE = new LruCache<>(100);
	private static final Cache<JidStringAndStringprep, EntityFullJid> ENTITY_FULLJID_CACHE = new LruCache<>(100);
	private static final Cache<JidStringAndStringprep, DomainBareJid> DOMAINJID_CACHE = new LruCache<>(100);
	private static final Cache<JidStringAndStringprep, DomainFullJid> DOMAINRESOURCEJID_CACHE = new LruCache<>(100);

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
		return from(localpart, domainpart, resource, JxmppContext.getDefaultContext());
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
	 * @param context the JXMPP context.
	 * @return a JID which consists of the given parts.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static Jid from(String localpart, String domainpart, String resource, JxmppContext context) throws XmppStringprepException {
		// Every JID must come with an domainpart.
		if (domainpart.isEmpty()) {
			throw XmppStringprepException.MissingDomainpart.from(localpart, resource);
		}

		String jidString = XmppStringUtils.completeJidFrom(localpart, domainpart, resource);
		Jid jid;

		JidStringAndStringprep jidStringAndStringprep = null;
		if (context.isCachingEnabled()) {
			jidStringAndStringprep = new JidStringAndStringprep(jidString, context);
		}
		if (jidStringAndStringprep != null) {
			jid = JID_CACHE.lookup(jidStringAndStringprep);
			if (jid != null) {
				return jid;
			}
		}

		jid = null;
		if (localpart != null && resource != null) {
			jid = new LocalDomainAndResourcepartJid(localpart, domainpart, resource, context);
		} else if (localpart != null && resource == null) {
			jid = new LocalAndDomainpartJid(localpart, domainpart, context);
		} else if (localpart == null && resource == null) {
			jid = new DomainpartJid(domainpart, context);
		} else if (localpart == null && resource != null) {
			jid = new DomainAndResourcepartJid(domainpart, resource, context);
		}
		assert jid != null;

		if (jidStringAndStringprep != null) {
			JID_CACHE.put(jidStringAndStringprep, jid);
		}
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
		return from(jidString, JxmppContext.getDefaultContext());
	}

	/**
	 * Get a {@link Jid} from the given String.
	 *
	 * @param jidString the input String.
	 * @param context the JXMPP context.
	 * @return the Jid represented by the input String.
	 * @throws XmppStringprepException if an error occurs.
	 * @see #from(CharSequence)
	 */
	public static Jid from(String jidString, JxmppContext context) throws XmppStringprepException {
		String localpart = XmppStringUtils.parseLocalpart(jidString);
		String domainpart = XmppStringUtils.parseDomain(jidString);
		String resource = XmppStringUtils.parseResource(jidString);
		try {
			return from(localpart, domainpart, resource, context);
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
	 * Get a {@link Jid} from an URL encoded CharSequence.
	 *
	 * @param cs a CharSequence representing an URL encoded JID.
	 * @return a JID
	 * @throws XmppStringprepException if an error occurs.
	 * @see URLDecoder
	 */
	public static Jid fromUrlEncoded(CharSequence cs) throws XmppStringprepException {
		String decoded = urlDecode(cs);
		return from(decoded);
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
		return bareFrom(jid, JxmppContext.getDefaultContext());
	}

	/**
	 * Get a {@link BareJid} representing the given String.
	 *
	 * @param jid the input String.
	 * @param context the JXMPP context.
	 * @return a bare JID representing the given String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static BareJid bareFrom(String jid, JxmppContext context) throws XmppStringprepException {
		BareJid bareJid;
		JidStringAndStringprep jidStringAndStringprep = null;
		if (context.isCachingEnabled()) {
			jidStringAndStringprep = new JidStringAndStringprep(jid, context);
		}

		if (jidStringAndStringprep != null) {
			bareJid = BAREJID_CACHE.lookup(jidStringAndStringprep);
			if (bareJid != null) {
				return bareJid;
			}
		}

		String localpart = XmppStringUtils.parseLocalpart(jid);
		String domainpart = XmppStringUtils.parseDomain(jid);
		try {
			if (localpart == null || localpart.length() == 0) {
				bareJid = new DomainpartJid(domainpart, context);
			} else {
				bareJid = new LocalAndDomainpartJid(localpart, domainpart, context);
			}
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(jid, e);
		}

		if (jidStringAndStringprep != null) {
			BAREJID_CACHE.put(jidStringAndStringprep, bareJid);
		}
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
	 * Get a {@link BareJid} from an URL encoded CharSequence.
	 *
	 * @param cs a CharSequence representing an URL encoded bare JID.
	 * @return a bare JID
	 * @throws XmppStringprepException if an error occurs.
	 * @see URLDecoder
	 */
	public static BareJid bareFromUrlEncoded(CharSequence cs) throws XmppStringprepException {
		String decoded = urlDecode(cs.toString());
		return bareFrom(decoded);
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
		return fullFrom(jid, JxmppContext.getDefaultContext());
	}

	/**
	 * Get a {@link FullJid} representing the given String.
	 *
	 * @param jid the JID's String.
	 * @param context the JXMPP context.
	 * @return a full JID representing the input String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static FullJid fullFrom(String jid, JxmppContext context) throws XmppStringprepException {
		JidStringAndStringprep jidStringAndStringprep = null;
		if (context.isCachingEnabled()) {
			jidStringAndStringprep = new JidStringAndStringprep(jid, context);
		}

		FullJid fullJid;
		if (jidStringAndStringprep != null) {
			fullJid = FULLJID_CACHE.lookup(jidStringAndStringprep);
			if (fullJid != null) {
				return fullJid;
			}
		}

		String localpart = XmppStringUtils.parseLocalpart(jid);
		String domainpart = XmppStringUtils.parseDomain(jid);
		String resource = XmppStringUtils.parseResource(jid);
		try {
			fullJid = fullFrom(localpart, domainpart, resource);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(jid, e);
		}

		if (jidStringAndStringprep != null) {
			FULLJID_CACHE.put(jidStringAndStringprep, fullJid);
		}

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
		return fullFrom(localpart, domainpart, resource, JxmppContext.getDefaultContext());
	}

	/**
	 * Get a {@link FullJid} constructed from the given parts.
	 *
	 * @param localpart a optional localpart.
	 * @param domainpart a domainpart.
	 * @param resource a resourcepart.
	 * @param context the JXMPP context.
	 * @return a full JID.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static FullJid fullFrom(String localpart, String domainpart, String resource, JxmppContext context) throws XmppStringprepException {
		FullJid fullJid;
		try {
			if (localpart == null || localpart.length() == 0) {
				fullJid = new DomainAndResourcepartJid(domainpart, resource, context);
			} else {
				fullJid = new LocalDomainAndResourcepartJid(localpart, domainpart, resource, context);
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
	 * Get a {@link FullJid} constructed from a {@link BareJid} and a {@link Resourcepart}.
	 *
	 * @param bareJid a entity bare JID.
	 * @param resource a resourcepart.
	 * @return a full JID.
	 */
	public static FullJid fullFrom(BareJid bareJid, Resourcepart resource) {
		if (bareJid.isEntityBareJid()) {
			EntityBareJid entityBareJid = (EntityBareJid) bareJid;
			return new LocalDomainAndResourcepartJid(entityBareJid, resource);
		} else {
			DomainBareJid domainBareJid = (DomainBareJid) bareJid;
			return new DomainAndResourcepartJid(domainBareJid, resource);
		}
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
	 * Get a {@link FullJid} from an URL encoded CharSequence.
	 *
	 * @param cs a CharSequence representing an URL encoded full JID.
	 * @return a full JID
	 * @throws XmppStringprepException if an error occurs.
	 * @see URLDecoder
	 */
	public static FullJid fullFromUrlEncoded(CharSequence cs) throws XmppStringprepException {
		String decoded = urlDecode(cs);
		return fullFrom(decoded);
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
		return entityFrom(jidString, JxmppContext.getDefaultContext());
	}

	/**
	 * Get a {@link EntityJid} representing the given String.
	 *
	 * @param jidString the JID's string.
	 * @param context the JXMPP context.
	 * @return an entity JID representing the given String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityJid entityFrom(String jidString, JxmppContext context) throws XmppStringprepException {
		return entityFrom(jidString, false, context);
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
		return entityFromUnescapedOrThrowUnchecked(cs, JxmppContext.getDefaultContext());
	}

	/**
	 * Like {@link #entityFromUnescaped(CharSequence)} but does throw an unchecked {@link IllegalArgumentException} instead of a
	 * {@link XmppStringprepException}.
	 *
	 * @param cs the character sequence which should be transformed to a {@link EntityJid}
	 * @param context the JXMPP context.
	 * @return the {@link EntityJid} if no exception occurs
	 * @throws IllegalArgumentException if the given input is not a valid JID
	 * @see #entityFromUnescaped(CharSequence)
	 * @since 0.6.2
	 */
	public static EntityJid entityFromUnescapedOrThrowUnchecked(CharSequence cs, JxmppContext context) {
		try {
			return entityFromUnescaped(cs, context);
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
		return entityFromUnescaped(jid, JxmppContext.getDefaultContext());
	}

	/**
	 * Get a {@link EntityJid} representing the given String.
	 *
	 * @param jid the JID.
	 * @param context the JXMPP context.
	 * @return an entity JID representing the given input.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityJid entityFromUnescaped(CharSequence jid, JxmppContext context) throws XmppStringprepException {
		return entityFromUnescaped(jid.toString(), context);
	}

	/**
	 * Get a {@link EntityJid} representing the given String.
	 *
	 * @param jidString the JID's string.
	 * @return an entity JID representing the given String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityJid entityFromUnescaped(String jidString) throws XmppStringprepException {
		return entityFromUnescaped(jidString, JxmppContext.getDefaultContext());
	}

	/**
	 * Get a {@link EntityJid} representing the given String.
	 *
	 * @param jidString the JID's string.
	 * @param context the JXMPP context.
	 * @return an entity JID representing the given String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityJid entityFromUnescaped(String jidString, JxmppContext context) throws XmppStringprepException {
		return entityFrom(jidString, true, context);
	}

	/**
	 * Get a {@link EntityJid} from a given {@link CharSequence} or {@code null} if the input does not represent a JID.
	 *
	 * @param cs the input {@link CharSequence}
	 * @return a JID or {@code null}
	 * @deprecated use {@link #entityFromUnescapedOrNull(CharSequence)} instead.
	 */
	// TODO: remove in jxmpp 1.1
	@Deprecated
	public static EntityJid entityFromUnesacpedOrNull(CharSequence cs) {
		return entityFromUnescapedOrNull(cs);
	}

	/**
	 * Get a {@link EntityJid} from a given {@link CharSequence} or {@code null} if the input does not represent a JID.
	 *
	 * @param cs the input {@link CharSequence}
	 * @return a JID or {@code null}
	 */
	public static EntityJid entityFromUnescapedOrNull(CharSequence cs) {
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
	private static EntityJid entityFrom(String jidString, boolean unescaped, JxmppContext context) throws XmppStringprepException {
		JidStringAndStringprep jidStringAndStringprep = null;
		if (context.isCachingEnabled()) {
			jidStringAndStringprep = new JidStringAndStringprep(jidString, context);
		}

		EntityJid entityJid;
		if (jidStringAndStringprep != null) {
			entityJid = ENTITYJID_CACHE.lookup(jidStringAndStringprep);
			if (entityJid != null) {
				return entityJid;
			}
		}
		String localpartString = XmppStringUtils.parseLocalpart(jidString);
		if (localpartString == null) {
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
		if (resourceString != null) {
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

		if (jidStringAndStringprep != null) {
			ENTITYJID_CACHE.put(jidStringAndStringprep, entityJid);
		}
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
	 * Get a {@link EntityJid} from an URL encoded CharSequence.
	 *
	 * @param cs a CharSequence representing an URL encoded entity JID.
	 * @return an entity JID
	 * @throws XmppStringprepException if an error occurs.
	 * @see URLDecoder
	 */
	public static EntityJid entityFromUrlEncoded(CharSequence cs) throws XmppStringprepException {
		String decoded = urlDecode(cs);
		return entityFrom(decoded);
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
		return entityBareFrom(jid, JxmppContext.getDefaultContext());
	}

	/**
	 * Get a {@link EntityBareJid} representing the given String.
	 *
	 * @param jid the input String.
	 * @param context the JXMPP context.
	 * @return a bare JID representing the given String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityBareJid entityBareFrom(String jid, JxmppContext context) throws XmppStringprepException {
		JidStringAndStringprep jidStringAndStringprep = null;
		if (context.isCachingEnabled()) {
			jidStringAndStringprep = new JidStringAndStringprep(jid, context);
		}

		EntityBareJid bareJid;
		if (jidStringAndStringprep != null) {
			bareJid = ENTITY_BAREJID_CACHE.lookup(jidStringAndStringprep);
			if (bareJid != null) {
				return bareJid;
			}
		}

		String localpart = XmppStringUtils.parseLocalpart(jid);
		String domainpart = XmppStringUtils.parseDomain(jid);
		try {
			bareJid = new LocalAndDomainpartJid(localpart, domainpart, context);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(jid, e);
		}

		if (jidStringAndStringprep != null) {
			ENTITY_BAREJID_CACHE.put(jidStringAndStringprep, bareJid);
		}
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
		return entityBareFromUnescaped(unescapedJidString, JxmppContext.getDefaultContext());
	}

	/**
	 * Get a {@link EntityBareJid} representing the given unescaped String.
	 *
	 * @param unescapedJidString the input String.
	 * @param context the JXMPP context.
	 * @return a bare JID representing the given String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityBareJid entityBareFromUnescaped(String unescapedJidString, JxmppContext context) throws XmppStringprepException {
		JidStringAndStringprep jidStringAndStringprep = null;
		if (context.isCachingEnabled()) {
			jidStringAndStringprep = new JidStringAndStringprep(unescapedJidString, context);
		}

		EntityBareJid bareJid;
		if (jidStringAndStringprep != null) {
			bareJid = ENTITY_BAREJID_CACHE.lookup(jidStringAndStringprep);
			if (bareJid != null) {
				return bareJid;
			}
		}

		String localpart = XmppStringUtils.parseLocalpart(unescapedJidString);
		// Some as from(String), but we escape the localpart
		localpart = XmppStringUtils.escapeLocalpart(localpart);

		String domainpart = XmppStringUtils.parseDomain(unescapedJidString);
		try {
			bareJid = new LocalAndDomainpartJid(localpart, domainpart, context);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(unescapedJidString, e);
		}

		if (jidStringAndStringprep != null) {
			ENTITY_BAREJID_CACHE.put(jidStringAndStringprep, bareJid);
		}

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
	 * Get a {@link EntityBareJid} from an URL encoded CharSequence.
	 *
	 * @param cs a CharSequence representing an URL encoded entity bare JID.
	 * @return an entity bare JID
	 * @throws XmppStringprepException if an error occurs.
	 * @see URLDecoder
	 */
	public static EntityBareJid entityBareFromUrlEncoded(CharSequence cs) throws XmppStringprepException {
		String decoded = urlDecode(cs);
		return entityBareFrom(decoded);
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
		return entityFullFrom(jid.toString(), JxmppContext.getDefaultContext());
	}


	/* avoid method not defined exception */
	/**
	 * Get a {@link EntityFullJid} representing the given String.
	 *
	 * @param jid a CharSequence representing a JID.
	 * @return a full JID representing the given CharSequence.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityFullJid entityFullFrom(String jid) throws XmppStringprepException {
		return entityFullFrom(jid, JxmppContext.getDefaultContext());
	}


	/**
	 * Get a {@link EntityFullJid} representing the given String.
	 *
	 * @param jid the JID's String.
	 * @param context the JXMPP context.
	 * @return a full JID representing the input String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityFullJid entityFullFrom(String jid, JxmppContext context) throws XmppStringprepException {
		JidStringAndStringprep jidStringAndStringprep = null;
		if (context.isCachingEnabled()) {
			jidStringAndStringprep = new JidStringAndStringprep(jid, context);
		}

		EntityFullJid fullJid;
		if (jidStringAndStringprep != null) {
			fullJid = ENTITY_FULLJID_CACHE.lookup(jidStringAndStringprep);
			if (fullJid != null) {
				return fullJid;
			}
		}

		String localpart = XmppStringUtils.parseLocalpart(jid);
		String domainpart = XmppStringUtils.parseDomain(jid);
		String resource = XmppStringUtils.parseResource(jid);
		try {
			fullJid = entityFullFrom(localpart, domainpart, resource);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(jid, e);
		}

		if (jidStringAndStringprep != null) {
			ENTITY_FULLJID_CACHE.put(jidStringAndStringprep, fullJid);
		}

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
		return entityFullFromUnescaped(unescapedJidString, JxmppContext.getDefaultContext());
	}

	/**
	 * Get a {@link EntityFullJid} representing the given unescaped String.
	 *
	 * @param unescapedJidString the JID's String.
	 * @param context the JXMPP context.
	 * @return a full JID representing the input String.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityFullJid entityFullFromUnescaped(String unescapedJidString, JxmppContext context) throws XmppStringprepException {
		JidStringAndStringprep jidStringAndStringprep = null;
		if (context.isCachingEnabled()) {
			jidStringAndStringprep = new JidStringAndStringprep(unescapedJidString, context);
		}

		EntityFullJid fullJid;
		if (jidStringAndStringprep != null) {
			fullJid = ENTITY_FULLJID_CACHE.lookup(jidStringAndStringprep);
			if (fullJid != null) {
				return fullJid;
			}
		}

		String localpart = XmppStringUtils.parseLocalpart(unescapedJidString);
		// Some as from(String), but we escape the localpart
		localpart = XmppStringUtils.escapeLocalpart(localpart);

		String domainpart = XmppStringUtils.parseDomain(unescapedJidString);
		String resource = XmppStringUtils.parseResource(unescapedJidString);
		try {
			fullJid = new LocalDomainAndResourcepartJid(localpart, domainpart, resource, context);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(unescapedJidString, e);
		}

		if (jidStringAndStringprep != null) {
			ENTITY_FULLJID_CACHE.put(jidStringAndStringprep, fullJid);
		}

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
		return entityFullFrom(localpart, domainpart, resource, JxmppContext.getDefaultContext());
	}

	/**
	 * Get a {@link EntityFullJid} constructed from the given parts.
	 *
	 * @param localpart a localpart.
	 * @param domainpart a domainpart.
	 * @param resource a resourcepart.
	 * @param context the JXMPP context.
	 * @return a full JID.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static EntityFullJid entityFullFrom(String localpart, String domainpart, String resource, JxmppContext context) throws XmppStringprepException {
		EntityFullJid fullJid;
		try {
			fullJid = new LocalDomainAndResourcepartJid(localpart, domainpart, resource, context);
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
	 * Get a {@link EntityFullJid} from an URL encoded CharSequence.
	 *
	 * @param cs a CharSequence representing an URL encoded entity full JID.
	 * @return an entity full JID
	 * @throws XmppStringprepException if an error occurs.
	 * @see URLDecoder
	 */
	public static EntityFullJid entityFullFromUrlEncoded(CharSequence cs) throws XmppStringprepException {
		String decoded = urlDecode(cs);
		return entityFullFrom(decoded);
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
		return domainBareFrom(jid, JxmppContext.getDefaultContext());
	}

	/**
	 * Get a domain bare JID.
	 *
	 * @param jid the JID String.
	 * @param context the JXMPP context.
	 * @return a domain bare JID.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static DomainBareJid domainBareFrom(String jid, JxmppContext context) throws XmppStringprepException {
		JidStringAndStringprep jidStringAndStringprep = null;
		if (context.isCachingEnabled()) {
			jidStringAndStringprep = new JidStringAndStringprep(jid, context);
		}

		DomainBareJid domainJid;
		if (jidStringAndStringprep != null) {
			domainJid = DOMAINJID_CACHE.lookup(jidStringAndStringprep);
			if (domainJid != null) {
				return domainJid;
			}
		}

		String domain = XmppStringUtils.parseDomain(jid);
		try {
			domainJid = new DomainpartJid(domain, context);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(jid, e);
		}

		if (context.isCachingEnabled()) {
			DOMAINJID_CACHE.put(jidStringAndStringprep, domainJid);
		}
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
	 * Get a {@link DomainBareJid} from an URL encoded CharSequence.
	 *
	 * @param cs a CharSequence representing an URL encoded domain bare JID.
	 * @return a domain bare JID
	 * @throws XmppStringprepException if an error occurs.
	 * @see URLDecoder
	 */
	public static DomainBareJid domainBareFromUrlEncoded(CharSequence cs) throws XmppStringprepException {
		String decode = urlDecode(cs);
		return domainBareFrom(decode);
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
		return domainFullFrom(jid, JxmppContext.getDefaultContext());
	}

	/**
	 * Get a domain full JID from the given String.
	 *
	 * @param jid the JID.
	 * @param context the JXMPP context.
	 * @return a DomainFullJid.
	 * @throws XmppStringprepException if an error happens.
	 */
	public static DomainFullJid domainFullFrom(String jid, JxmppContext context) throws XmppStringprepException {
		JidStringAndStringprep jidStringAndStringprep = null;
		if (context.isCachingEnabled()) {
			jidStringAndStringprep = new JidStringAndStringprep(jid, context);
		}

		DomainFullJid domainResourceJid;
		if (jidStringAndStringprep != null) {
			domainResourceJid = DOMAINRESOURCEJID_CACHE.lookup(jidStringAndStringprep);
			if (domainResourceJid != null) {
				return domainResourceJid;
			}
		}

		String domain = XmppStringUtils.parseDomain(jid);
		String resource = XmppStringUtils.parseResource(jid);
		try {
			domainResourceJid = new DomainAndResourcepartJid(domain, resource, context);
		} catch (XmppStringprepException e) {
			throw new XmppStringprepException(jid, e);
		}

		if (jidStringAndStringprep != null) {
			DOMAINRESOURCEJID_CACHE.put(jidStringAndStringprep, domainResourceJid);
		}

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

	/**
	 * Get a {@link DomainFullJid} from an URL encoded CharSequence.
	 *
	 * @param cs a CharSequence representing an URL encoded domain full JID.
	 * @return a domain full JID
	 * @throws XmppStringprepException if an error occurs.
	 * @see URLDecoder
	 */
	public static DomainFullJid domainFullFromUrlEncoded(CharSequence cs) throws XmppStringprepException {
		String decoded = urlDecode(cs);
		return domainFullFrom(decoded);
	}

	private static String urlDecode(CharSequence cs) {
		try {
			return URLDecoder.decode(cs.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}
}
