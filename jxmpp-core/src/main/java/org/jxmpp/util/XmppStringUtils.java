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
		int atIndex = jid.indexOf('@');
		if (atIndex <= 0) {
			return "";
		}
		int slashIndex = jid.indexOf('/');
		if (slashIndex >= 0 && slashIndex < atIndex) {
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
		int atIndex = jid.indexOf('@');
		// If the String ends with '@', return the empty string.
		if (atIndex + 1 > jid.length()) {
			return "";
		}
		int slashIndex = jid.indexOf('/');
		if (slashIndex > 0) {
			// 'local@domain.foo/resource' and 'local@domain.foo/res@otherres' case
			if (slashIndex > atIndex) {
				return jid.substring(atIndex + 1, slashIndex);
			// 'domain.foo/res@otherres' case
			} else {
				return jid.substring(0, slashIndex);
			}
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
		int slashIndex = jid.indexOf('/');
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
		int slashIndex = jid.indexOf('/');
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

	private static final Cache<String, String> LOCALPART_ESACPE_CACHE = new Cache<String, String>(100, -1);
	private static final Cache<String, String> LOCALPART_UNESCAPE_CACHE = new Cache<String, String>(100, -1);

	/**
	 * Escapes the localpart of a JID according to "JID Escaping" (XEP-0106).
	 * Escaping replaces characters prohibited by Nodeprep with escape sequences,
	 * as follows:
	 * <p>
	 * <table border="1">
	 * <tr><td><b>Unescaped Character</b></td><td><b>Encoded Sequence</b></td></tr>
	 * <tr><td>&lt;space&gt;</td><td>\20</td></tr>
	 * <tr><td>"</td><td>\22</td></tr>
	 * <tr><td>&</td><td>\26</td></tr>
	 * <tr><td>'</td><td>\27</td></tr>
	 * <tr><td>/</td><td>\2f</td></tr>
	 * <tr><td>:</td><td>\3a</td></tr>
	 * <tr><td>&lt;</td><td>\3c</td></tr>
	 * <tr><td>&gt;</td><td>\3e</td></tr>
	 * <tr><td>@</td><td>\40</td></tr>
	 * <tr><td>\</td><td>\5c</td></tr>
	 * </table>
	 * </p>
	 *
	 * <p>
	 * This process is useful when the localpart comes from an external source that doesn't
	 * conform to Nodeprep. For example, a username in LDAP may be "Joe Smith". Because
	 * the &lt;space&gt; character isn't a valid part of a localpart, the username should
	 * be escaped to "Joe\20Smith" before being made into a JID (e.g. "joe\20smith@example.com"
	 * after case-folding, etc. has been applied).
	 * </p>
	 *
	 * All localpart escaping and un-escaping must be performed manually at the appropriate
	 * time; the JID class will not escape or un-escape automatically.
	 *
	 * @param localpart the localpart.
	 * @return the escaped version of the localpart.
	 * @see <a href="http://xmpp.org/extensions/xep-0106.html">XEP-106: JID Escaping</a>
	 */
	public static String escapeLocalpart(String localpart) {
		if (localpart == null) {
			return null;
		}
		String res = LOCALPART_ESACPE_CACHE.get(localpart);
		if (res != null) {
			return res;
		}
		StringBuilder buf = new StringBuilder(localpart.length() + 8);
		for (int i = 0, n = localpart.length(); i < n; i++) {
			char c = localpart.charAt(i);
			switch (c) {
			case '"':
				buf.append("\\22");
				break;
			case '&':
				buf.append("\\26");
				break;
			case '\'':
				buf.append("\\27");
				break;
			case '/':
				buf.append("\\2f");
				break;
			case ':':
				buf.append("\\3a");
				break;
			case '<':
				buf.append("\\3c");
				break;
			case '>':
				buf.append("\\3e");
				break;
			case '@':
				buf.append("\\40");
				break;
			case '\\':
				buf.append("\\5c");
				break;
			default: {
				if (Character.isWhitespace(c)) {
					buf.append("\\20");
				} else {
					buf.append(c);
				}
			}
			}
		}
		res = buf.toString();
		LOCALPART_ESACPE_CACHE.put(localpart, res);
		return res;
	}

	/**
	 * Un-escapes the localpart of a JID according to "JID Escaping" (XEP-0106).
	 * Escaping replaces characters prohibited by Nodeprep with escape sequences,
	 * as follows:
	 * 
	 * <p>
	 * <table border="1">
	 * <tr><td><b>Unescaped Character</b></td><td><b>Encoded Sequence</b></td></tr>
	 * <tr><td>&lt;space&gt;</td><td>\20</td></tr>
	 * <tr><td>"</td><td>\22</td></tr>
	 * <tr><td>&</td><td>\26</td></tr>
	 * <tr><td>'</td><td>\27</td></tr>
	 * <tr><td>/</td><td>\2f</td></tr>
	 * <tr><td>:</td><td>\3a</td></tr>
	 * <tr><td>&lt;</td><td>\3c</td></tr>
	 * <tr><td>&gt;</td><td>\3e</td></tr>
	 * <tr><td>@</td><td>\40</td></tr>
	 * <tr><td>\</td><td>\5c</td></tr>
	 * </table>
	 * </p>
	 *
	 * <p>
	 * This process is useful when the localpart comes from an external source that doesn't
	 * conform to Nodeprep. For example, a username in LDAP may be "Joe Smith". Because
	 * the &lt;space&gt; character isn't a valid part of a localpart, the username should
	 * be escaped to "Joe\20Smith" before being made into a JID (e.g. "joe\20smith@example.com"
	 * after case-folding, etc. has been applied).
	 * </p>
	 *
	 * All localpart escaping and un-escaping must be performed manually at the appropriate
	 * time; the JID class will not escape or un-escape automatically.
	 *
	 * @param localpart the escaped version of the localpart.
	 * @return the un-escaped version of the localpart.
	 * @see <a href="http://xmpp.org/extensions/xep-0106.html">XEP-106: JID Escaping</a>
	 */
	public static String unescapeLocalpart(String localpart) {
		if (localpart == null) {
			return null;
		}
		String res = LOCALPART_UNESCAPE_CACHE.get(localpart);
		if (res != null) {
			return res;
		}
		char[] localpartChars = localpart.toCharArray();
		StringBuilder buf = new StringBuilder(localpartChars.length);
		for (int i = 0, n = localpartChars.length; i < n; i++) {
			compare: {
				char c = localpart.charAt(i);
				if (c == '\\' && i + 2 < n) {
					char c2 = localpartChars[i + 1];
					char c3 = localpartChars[i + 2];
					if (c2 == '2') {
						switch (c3) {
						case '0':
							buf.append(' ');
							i += 2;
							break compare;
						case '2':
							buf.append('"');
							i += 2;
							break compare;
						case '6':
							buf.append('&');
							i += 2;
							break compare;
						case '7':
							buf.append('\'');
							i += 2;
							break compare;
						case 'f':
							buf.append('/');
							i += 2;
							break compare;
						}
					} else if (c2 == '3') {
						switch (c3) {
						case 'a':
							buf.append(':');
							i += 2;
							break compare;
						case 'c':
							buf.append('<');
							i += 2;
							break compare;
						case 'e':
							buf.append('>');
							i += 2;
							break compare;
						}
					} else if (c2 == '4') {
						if (c3 == '0') {
							buf.append("@");
							i += 2;
							break compare;
						}
					} else if (c2 == '5') {
						if (c3 == 'c') {
							buf.append("\\");
							i += 2;
							break compare;
						}
					}
				}
				buf.append(c);
			}
		}
		res = buf.toString();
		LOCALPART_UNESCAPE_CACHE.put(localpart, res);
		return res;
	}

	public static String completeJidFrom(String localpart, String domainpart, String resource) {
		int localpartLength = localpart.length();
		int domainpartLength = domainpart.length();
		int resourceLength = resource.length();
		int maxResLength = localpartLength + domainpartLength + resourceLength + 2;
		StringBuilder sb = new StringBuilder(maxResLength);
		if (localpartLength > 0) {
			sb.append(localpart).append('@');
		}
		sb.append(domainpart);
		if (resourceLength > 0) {
			sb.append('/').append(resource);
		}
		return sb.toString();
	}
}
