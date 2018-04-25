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
package org.jxmpp.jid.parts;

import org.jxmpp.stringprep.XmppStringPrepUtil;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.XmppStringUtils;

/**
 * A <i>localpart</i> of an XMPP address (JID). The localpart is the part before the
 * first @ sign in an XMPP address and usually identifies the user (or the XMPP
 * entity) within an XMPP service. It is also often referred to as "username",
 * but note that the actual username used to login may be different from the
 * resulting localpart of the user's JID.
 * <p>
 * You can create instances of this class from Strings using {@link #from(String)}.
 * </p>
 *
 * @see <a href="http://xmpp.org/rfcs/rfc6122.html#addressing-localpart">RFC
 *      6122 § 2.3. Localpart</a>
 */
public class Localpart extends Part {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private transient String unescapedCache;

	private Localpart(String localpart) {
		super(localpart);
	}

	/**
	 * Return the <b>unescaped</b> String representation of this Localpart.
	 * <p>
	 * Since certain Unicode code points are disallowed in the localpart of a JID by the required stringprep profile,
	 * those need to get escaped when used in a real JID. The unescaped representation of the JID is only for
	 * presentation to a human user or for gatewaying to a non-XMPP system.
	 * </p>
	 *
	 * @return the unescaped String representation of this JID.
	 * @see org.jxmpp.jid.Jid#asUnescapedString()
	 * @since 0.6.1
	 */
	public String asUnescapedString() {
		if (unescapedCache != null) {
			return unescapedCache;
		}
		unescapedCache = XmppStringUtils.unescapeLocalpart(toString());
		return unescapedCache;
	}

	/**
	 * Like {@link #from(String)} but does throw an unchecked {@link IllegalArgumentException} instead of a
	 * {@link XmppStringprepException}.
	 *
	 * @param cs the character sequence which should be transformed to a {@link Localpart}
	 * @return the {@link Localpart} if no exception occurs
	 * @throws IllegalArgumentException if the given input is not a valid {@link Localpart}
	 * @see #from(String)
	 * @since 0.6.2
	 */
	public static Localpart fromOrThrowUnchecked(CharSequence cs) {
		try {
			return from(cs.toString());
		} catch (XmppStringprepException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Like {@link #fromUnescaped(String)} but does throw an unchecked {@link IllegalArgumentException} instead of a
	 * {@link XmppStringprepException}.
	 *
	 * @param cs the character sequence which should be transformed to a {@link Localpart}
	 * @return the {@link Localpart} if no exception occurs
	 * @see #from(String)
	 * @since 0.6.2
	 */
	public static Localpart fromUnescapedOrThrowUnchecked(CharSequence cs) {
		try {
			return fromUnescaped(cs.toString());
		} catch (XmppStringprepException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Get a {@link Localpart} from a given {@link CharSequence} or {@code null} if the input is not a valid localpart.
	 *
	 * @param cs the input CharSequence
	 * @return a Localpart or {@code null}
	 */
	public static Localpart formUnescapedOrNull(CharSequence cs) {
		try {
			return fromUnescaped(cs);
		} catch (XmppStringprepException e) {
			return null;
		}
	}

	/**
	 * Get a {@link Localpart} from an unescaped String.
	 *
	 * @param unescapedLocalpart an unescaped String representing a Localpart.
	 * @return a Localpart
	 * @throws XmppStringprepException if an error occurs.
	 * @since 0.6.2
	 */
	public static Localpart fromUnescaped(String unescapedLocalpart) throws XmppStringprepException {
		String escapedLocalpartString = XmppStringUtils.escapeLocalpart(unescapedLocalpart);
		return from(escapedLocalpartString);
	}

	/**
	 * Get a {@link Localpart} from an unescaped CharSequence.
	 *
	 * @param unescapedLocalpart an unescaped CharSequence representing a Localpart.
	 * @return a Localpart
	 * @throws XmppStringprepException if an error occurs.
	 * @since 0.6.2
	 */
	public static Localpart fromUnescaped(CharSequence unescapedLocalpart) throws XmppStringprepException {
		return fromUnescaped(unescapedLocalpart.toString());
	}

	/**
	 * Get a {@link Localpart} from a given {@link CharSequence} or {@code null} if the input is not a valid localpart.
	 *
	 * @param cs the input CharSequence
	 * @return a Localpart or {@code null}
	 */
	public static Localpart fromOrNull(CharSequence cs) {
		try {
			return from(cs.toString());
		} catch (XmppStringprepException e) {
			return null;
		}
	}

	/**
	 * Get the {@link Localpart} representing the input String.
	 *
	 * @param localpart the input String.
	 * @return the localpart.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static Localpart from(String localpart) throws XmppStringprepException {
		localpart = XmppStringPrepUtil.localprep(localpart);
		// First prep the String, then assure the limits of the *result*
		assertNotLongerThan1023BytesOrEmpty(localpart);
		return new Localpart(localpart);
	}
}
