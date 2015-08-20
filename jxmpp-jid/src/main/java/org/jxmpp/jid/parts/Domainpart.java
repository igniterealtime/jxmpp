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
	 * Get the {@link Domainpart} representing the input String.
	 *
	 * @param domain the input String.
	 * @return the domainpart.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static Domainpart from(String domain) throws XmppStringprepException {
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
