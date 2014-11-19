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
package org.jxmpp.jid.parts;

import org.jxmpp.stringprep.XmppStringPrepUtil;
import org.jxmpp.stringprep.XmppStringprepException;

public class Domainpart extends Part {

	private Domainpart(String domain) {
		super(domain);
	}

	public static Domainpart from(String domain) throws XmppStringprepException {
		// TODO cache
		// RFC 6122 § 2.2 "If the domainpart includes a final character considered to be a label
		// separator (dot) by [IDNA2003] or [DNS], this character MUST be stripped …"
		if (domain.charAt(domain.length() - 1) == '.') {
			domain = domain.substring(0, domain.length() - 1);
		}
		domain = XmppStringPrepUtil.localprep(domain);
		// First prep the String, then assure the limits of the *result*
		assertNotLongerThen1023BytesOrEmpty(domain);
		return new Domainpart(domain);
	}
}
