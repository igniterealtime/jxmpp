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
package org.jxmpp.util;

public class XmppStringUtils {

	/**
	 * Returns the localpart of a JID. For example, for the address
	 * "matt@jivesoftware.com/Smack", "matt" would be returned. If no username
	 * is present in the address, the empty string will be returned.
	 * 
	 * @param jid
	 *            the XMPP address.
	 * @return the name portion of the XMPP address.
	 */
	public static String parseLocalpart(String jid) {
		int atIndex = jid.lastIndexOf("@");
		if (atIndex <= 0) {
			return "";
		} else {
			return jid.substring(0, atIndex);
		}
	}

	/**
	 * Returns the domain of a JID. For example, for the address
	 * "matt@jivesoftware.com/Smack", "jivesoftware.com" would be returned. If
	 * no server is present in the address, the empty string will be returned.
	 * 
	 * @param jid
	 *            the XMPP address.
	 * @return the server portion of the XMPP address.
	 */
	public static String parseDomain(String jid) {
		int atIndex = jid.lastIndexOf("@");
		// If the String ends with '@', return the empty string.
		if (atIndex + 1 > jid.length()) {
			return "";
		}
		int slashIndex = jid.indexOf("/");
		if (slashIndex > 0 && slashIndex > atIndex) {
			return jid.substring(atIndex + 1, slashIndex);
		} else {
			return jid.substring(atIndex + 1);
		}
	}

	/**
	 * Returns the resource portion of a JID. For example, for the address
	 * "matt@jivesoftware.com/Smack", "Smack" would be returned. If no resource
	 * is present in the address, the empty string will be returned.
	 * 
	 * @param jid
	 *            the XMPP address.
	 * @return the resource portion of the XMPP address.
	 */
	public static String parseResource(String jid) {
		int slashIndex = jid.indexOf("/");
		if (slashIndex + 1 > jid.length() || slashIndex < 0) {
			return "";
		} else {
			return jid.substring(slashIndex + 1);
		}
	}

	/**
	 * Returns the JID with any resource information removed. For example, for
	 * the address "matt@jivesoftware.com/Smack", "matt@jivesoftware.com" would
	 * be returned.
	 * 
	 * @param jid
	 *            the XMPP address.
	 * @return the bare XMPP address without resource information.
	 */
	public static String parseBareAddress(String jid) {
		int slashIndex = jid.indexOf("/");
		if (slashIndex < 0) {
			return jid;
		} else if (slashIndex == 0) {
			return "";
		} else {
			return jid.substring(0, slashIndex);
		}
	}

	/**
	 * Returns true if jid is a full JID (i.e. a JID with resource part).
	 * 
	 * @param jid
	 * @return true if full JID, false otherwise
	 */
	public static boolean isFullJID(String jid) {
		if (parseLocalpart(jid).length() <= 0 || parseDomain(jid).length() <= 0
				|| parseResource(jid).length() <= 0) {
			return false;
		}
		return true;
	}
}
