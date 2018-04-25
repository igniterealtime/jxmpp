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

/**
 * A <i>domainpart</i> of an XMPP address (JID).
 * <p>
 * You can create instances of this class from Strings using {@link #from(String)}.
 * </p>
 *
 * @see <a href="http://xmpp.org/rfcs/rfc6122.html#addressing-domain">RFC 6122 § 2.2. Domainpart</a>
 */
public class Domainpart extends Part {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private Domainpart(String domain) {
		super(domain);
	}

	/**
	 * Get a {@link Domainpart} from a given {@link CharSequence} or {@code null} if the input is not a valid domainpart.
	 *
	 * @param cs the input CharSequence
	 * @return a Domainpart or {@code null}
	 */
	public static Domainpart fromOrNull(CharSequence cs) {
		try {
			return from(cs.toString());
		} catch (XmppStringprepException e) {
			return null;
		}
	}

	/**
	 * Like {@link #from(String)} but does throw an unchecked {@link IllegalArgumentException} instead of a
	 * {@link XmppStringprepException}.
	 *
	 * @param cs the character sequence which should be transformed to a {@link Domainpart}
	 * @return the {@link Domainpart} if no exception occurs
	 * @throws IllegalArgumentException if the given input is not a valid {@link Domainpart}
	 * @see #from(String)
	 * @since 0.6.2
	 */
	public static Domainpart fromOrThrowUnchecked(CharSequence cs) {
		try {
			return from(cs.toString());
		} catch (XmppStringprepException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Get the {@link Domainpart} representing the input String.
	 *
	 * @param domain the input String.
	 * @return the domainpart.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static Domainpart from(String domain) throws XmppStringprepException {
		if (domain == null) {
			throw new XmppStringprepException(domain, "Input 'domain' must not be null");
		}
		// TODO cache
		// RFC 6122 § 2.2 "If the domainpart includes a final character considered to be a label
		// separator (dot) by [IDNA2003] or [DNS], this character MUST be stripped …"
		if (domain.length() > 0 && domain.charAt(domain.length() - 1) == '.') {
			domain = domain.substring(0, domain.length() - 1);
		}
		domain = XmppStringPrepUtil.domainprep(domain);
		// First prep the String, then assure the limits of the *result*
		assertNotLongerThan1023BytesOrEmpty(domain);
		return new Domainpart(domain);
	}
}
