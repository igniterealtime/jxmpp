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
package org.jxmpp.jid;

import org.jxmpp.jid.parts.Localpart;

/**
 * An XMPP address (JID) which has a {@link Localpart}. Either {@link EntityBareJid} or {@link EntityFullJid}.
 * <p>
 * Examples:
 * </p>
 * <ul>
 * <li><code>localpart@domain.part</code></li>
 * <li><code>localpart@domain.part/resourcepart</code></li>
 * </ul>
 *
 * @see Jid
 */
public interface EntityJid extends Jid {

	/**
	 * Return the {@link Localpart} of this JID.
	 *
	 * @return the localpart.
	 */
	Localpart getLocalpart();

	/**
	 * Return the bare JID of this entity JID.
	 * 
	 * @return the bare JID.
	 */
	EntityBareJid asEntityBareJid();

	/**
	 * Return the bare JID string of this full JID.
	 *
	 * @return the bare JID string.
	 */
	String asEntityBareJidString();

}
