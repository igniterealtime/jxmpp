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
package org.jxmpp.jid;

/**
 * A JID consisting of a localpart, domainpart and resourcepart. For example
 * "user@xmpp.org/resource".
 * 
 * @see Jid
 */
public interface FullJid extends Jid, JidWithResource, JidWithLocalpart {

	// TODO the two methods could be defined simply in JidWithLocalpart only

	/**
	 * Return the bare JID of this full JID.
	 * 
	 * @return the bare JID.
	 */
	public BareJid asBareJid();

	/**
	 * Return the bare JID string of this full JID.
	 *
	 * @return the bare JID string.
	 */
	public String asBareJidString();

}
