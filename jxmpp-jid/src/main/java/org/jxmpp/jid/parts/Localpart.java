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
 * A XMPP Localpart.
 *
 * @see <a href="http://xmpp.org/rfcs/rfc6122.html#addressing-localpart">RFC 6122 § 2.3. Localpart</a>
 */
public class Localpart extends Part {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private Localpart(String localpart) {
		super(localpart);
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
