/**
 *
 * Copyright © 2014-2019 Florian Schmaus
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

import org.jxmpp.util.cache.LruCache;

import static java.lang.Character.isWhitespace;

/**
 * Utility class for handling Strings in XMPP.
 */
public class XmppStringUtils {

	/**
	 * Returns the localpart of an XMPP address (JID). For example, for the address "user@xmpp.org/Resource", "user"
	 * would be returned. Returns <code>null</code> if the given JID has no localpart. Returns the empty string if
	 * the given JIDs localpart is the empty string (which is invalid).
	 * 
	 * @param jid
	 *            the XMPP address to parse.
	 * @return the name portion of the XMPP address, the empty String or <code>null</code>.
	 */
	public static String parseLocalpart(String jid) {
		int atIndex = jid.indexOf('@');
		if (atIndex < 0) {
			return null;
		}
		if (atIndex == 0) {
			// '@' as first character, i.e. '@example.org". Return emtpy string as
			// localpart, to make it possible to differentiate this from 'example.org'
			// (which would return 'null' as localpart).
			return "";
		}

		int slashIndex = jid.indexOf('/');
		if (slashIndex >= 0 && slashIndex < atIndex) {
			// This is an '@' character in the resourcepart.
			return null;
		}

		return jid.substring(0, atIndex);
	}

	/**
	 * Returns the domain of an XMPP address (JID). For example, for the address "user@xmpp.org/Resource", "xmpp.org"
	 * would be returned. If <code>jid</code> is <code>null</code>, then this method returns also <code>null</code>. If
	 * the input String is no valid JID or has no domainpart, then this method will return the empty String.
	 * 
	 * @param jid
	 *            the XMPP address to parse.
	 * @return the domainpart of the XMPP address, the empty String or <code>null</code>.
	 */
	public static String parseDomain(String jid) {
		if (jid == null) return null;

		int atIndex = jid.indexOf('@');
		int slashIndex = jid.indexOf('/');
		if (slashIndex >= 0) {
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
	 * Returns the resource portion of an XMPP address (JID). For example, for the address "user@xmpp.org/Resource",
	 * "Resource" would be returned. Returns <code>null</code> if the given JID has no resourcepart. Returns the
	 * empty string if the given JID has an empty resourcepart (which is invalid).
	 * 
	 * @param jid
	 *            the XMPP address to parse.
	 * @return the resource portion of the XMPP address.
	 */
	public static String parseResource(String jid) {
		int slashIndex = jid.indexOf('/');
		if (slashIndex < 0) {
			return null;
		}
		if (slashIndex + 1 > jid.length()) {
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
	 *            the XMPP JID.
	 * @return the bare XMPP JID without resource information.
	 */
	public static String parseBareJid(String jid) {
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
	 * @param jid the String to check.
	 * @return true if full JID, false otherwise
	 */
	public static boolean isFullJID(String jid) {
		if (parseLocalpart(jid).length() <= 0 || parseDomain(jid).length() <= 0
				|| parseResource(jid).length() <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * Returns true if <code>jid</code> is a bare JID ("foo@bar.com").
	 * <p>
	 * This method may return true for Strings that are not valid JIDs (e.g. because of Stringprep violations). Consider
	 * using <code>org.jxmpp.jid.util.JidUtil.validateBareJid(String)</code> from jxmpp-jid instead of this method as it
	 * exceptions provide a meaningful message string why the JID is not a bare JID and will also check for Stringprep
	 * errors.
	 * </p>
	 *
	 * @param jid the String to check.
	 * @return true if bare JID, false otherwise
	 */
	public static boolean isBareJid(String jid) {
		return parseLocalpart(jid).length() > 0
				&& parseDomain(jid).length() > 0
				&& parseResource(jid).length() == 0;
	}

	private static final LruCache<String, String> LOCALPART_ESACPE_CACHE = new LruCache<String, String>(100);
	private static final LruCache<String, String> LOCALPART_UNESCAPE_CACHE = new LruCache<String, String>(100);

	/**
	 * Escapes the localpart of a JID according to "JID Escaping" (XEP-0106).
	 * Escaping replaces characters prohibited by Nodeprep with escape sequences,
	 * as follows:
	 * <table border="1">
	 * <caption>Character mappings</caption>
	 * <tr><td><b>Unescaped Character</b></td><td><b>Encoded Sequence</b></td></tr>
	 * <tr><td>&lt;space&gt;</td><td>\20</td></tr>
	 * <tr><td>"</td><td>\22</td></tr>
	 * <tr><td>&amp;</td><td>\26</td></tr>
	 * <tr><td>'</td><td>\27</td></tr>
	 * <tr><td>/</td><td>\2f</td></tr>
	 * <tr><td>:</td><td>\3a</td></tr>
	 * <tr><td>&lt;</td><td>\3c</td></tr>
	 * <tr><td>&gt;</td><td>\3e</td></tr>
	 * <tr><td>@</td><td>\40</td></tr>
	 * <tr><td>\</td><td>\5c</td></tr>
	 * </table>
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
		String res = LOCALPART_ESACPE_CACHE.lookup(localpart);
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
				if (isWhitespace(c) || isNonBreakingSpace(c)) {
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

	private static boolean isNonBreakingSpace(char chr) {
		return chr == '\u00A0' || chr == '\u2007' || chr == '\u202F';
	}

	/**
	 * Un-escapes the localpart of a JID according to "JID Escaping" (XEP-0106).
	 * Escaping replaces characters prohibited by Nodeprep with escape sequences,
	 * as follows:
	 * 
	 * <table border="1">
	 * <caption>Character mapping</caption>
	 * <tr><td><b>Unescaped Character</b></td><td><b>Encoded Sequence</b></td></tr>
	 * <tr><td>&lt;space&gt;</td><td>\20</td></tr>
	 * <tr><td>"</td><td>\22</td></tr>
	 * <tr><td>&amp;</td><td>\26</td></tr>
	 * <tr><td>'</td><td>\27</td></tr>
	 * <tr><td>/</td><td>\2f</td></tr>
	 * <tr><td>:</td><td>\3a</td></tr>
	 * <tr><td>&lt;</td><td>\3c</td></tr>
	 * <tr><td>&gt;</td><td>\3e</td></tr>
	 * <tr><td>@</td><td>\40</td></tr>
	 * <tr><td>\</td><td>\5c</td></tr>
	 * </table>
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
		String res = LOCALPART_UNESCAPE_CACHE.lookup(localpart);
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
					switch(c2) {
					case '2':
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
						break;
					case '3':
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
						break;
					case '4':
						if (c3 == '0') {
							buf.append("@");
							i += 2;
							break compare;
						}
						break;
					case '5':
						if (c3 == 'c') {
							buf.append("\\");
							i += 2;
							break compare;
						}
						break;
					}
				}
				buf.append(c);
			}
		}
		res = buf.toString();
		LOCALPART_UNESCAPE_CACHE.put(localpart, res);
		return res;
	}

	/**
	 * Construct a JID String from the given parts.
	 *
	 * @param localpart the localpart.
	 * @param domainpart the domainpart.
	 * @return the constructed JID String.
	 */
	public static String completeJidFrom(CharSequence localpart, CharSequence domainpart) {
		return completeJidFrom(localpart != null ? localpart.toString() : null, domainpart.toString());
	}

	/**
	 * Construct a JID String from the given parts.
	 *
	 * @param localpart the localpart.
	 * @param domainpart the domainpart.
	 * @return the constructed JID String.
	 */
	public static String completeJidFrom(String localpart, String domainpart) {
		return completeJidFrom(localpart, domainpart, null);
	}

	/**
	 * Construct a JID String from the given parts.
	 *
	 * @param localpart the localpart.
	 * @param domainpart the domainpart.
	 * @param resource the resourcepart.
	 * @return the constructed JID String.
	 */
	public static String completeJidFrom(CharSequence localpart, CharSequence domainpart, CharSequence resource) {
		return completeJidFrom(localpart != null ? localpart.toString() : null, domainpart.toString(),
				resource != null ? resource.toString() : null);
	}

	/**
	 * Construct a JID String from the given parts.
	 *
	 * @param localpart the localpart.
	 * @param domainpart the domainpart.
	 * @param resource the resourcepart.
	 * @return the constructed JID String.
	 */
	public static String completeJidFrom(String localpart, String domainpart, String resource) {
		if (domainpart == null) {
			throw new IllegalArgumentException("domainpart must not be null");
		}
		int localpartLength = localpart != null ? localpart.length() : 0;
		int domainpartLength = domainpart.length();
		int resourceLength = resource != null ? resource.length() : 0;
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

	/**
	 * Generate a unique key from a element name and namespace. This key can be used to lookup element/namespace
	 * information. The key is simply generated by concatenating the strings as follows:
	 * <code>element + '\t' + namespace</code>.
	 * <p>
	 * The tab character (\t) was chosen because it will be normalized, i.e. replace by space, in attribute values. It
	 * therefore should never appear in <code>element</code> or <code>namespace</code>. For more information about the
	 * normalization, see the XML specification § <a href="http://www.w3.org/TR/REC-xml/#AVNormalize">3.3.3
	 * Attribute-Value Normalization</a>.
	 * </p>
	 * 
	 * @param element the element.
	 * @param namespace the namespace.
	 * @return the unique key of element and namespace.
	 */
	public static String generateKey(String element, String namespace) {
		return element + '\t' + namespace;
	}
}
