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
package org.jxmpp.jid.util;

import org.jxmpp.jid.DomainFullJid;
import org.jxmpp.jid.FullJid;
import org.jxmpp.jid.Jid;

public class JidUtil {

	/**
	 * Safely transform a JID to any JID without a resource.
	 * <p>
	 * Useful for situations where you don't want to put a resource on the wire.
	 * </p>
	 * @param jid
	 * @return the equivalent JID without resource
	 */
	public static Jid withoutResource(Jid jid) {
		if (jid.hasNoResource()) {
			return jid;
		}

		if (jid.isFullJid()) {
			FullJid fullJid = (FullJid) jid;
			return fullJid.asBareJid();
		} else if (jid.isDomainFullJid()) {
			DomainFullJid domainFullJid = (DomainFullJid) jid;
			return domainFullJid.asDomainBareJid();
		} else {
			throw new IllegalStateException(
					"Given JID has a resource but is neither a FullJid or a DomainFullJid");
		}
	}

}
