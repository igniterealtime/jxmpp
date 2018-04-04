/**
 *
 * Copyright © 2014-2016 Florian Schmaus
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

import org.jxmpp.jid.parts.Resourcepart;

/**
 * An XMPP address (JID) which has a {@link Resourcepart}. Either {@link EntityFullJid} or {@link DomainFullJid}.
 * <p>
 * Examples:
 * </p>
 * <ul>
 * <li><code>localpart@domain.part/resourcepart</code></li>
 * <li><code>domain.part/resourcepart</code></li>
 * </ul>
 *
 * @see Jid
 */
public interface FullJid extends Jid {

	/**
	 * Return the {@link Resourcepart} of this JID.
	 *
	 * @return the resourcepart.
	 */
	Resourcepart getResourcepart();

}
