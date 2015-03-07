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
package org.jxmpp.stringprep;

import org.jxmpp.stringprep.simple.SimpleXmppStringprep;
import org.jxmpp.util.cache.Cache;
import org.jxmpp.util.cache.LruCache;

public class XmppStringPrepUtil {

	static {
		// Ensure that there is always at least the simple XMPP stringprep implementation active
		SimpleXmppStringprep.setup();
	}

	private static final Cache<String, String> NODEPREP_CACHE = new LruCache<String, String>(100);
	private static final Cache<String, String> DOMAINPREP_CACHE = new LruCache<String, String>(100);
	private static final Cache<String, String> RESOURCEPREP_CACHE = new LruCache<String, String>(100);

	private static XmppStringprep xmppStringprep;

	public static void setXmppStringprep(XmppStringprep xmppStringprep) {
		XmppStringPrepUtil.xmppStringprep = xmppStringprep;
	}

	public static String localprep(String string) throws XmppStringprepException {
		if (xmppStringprep == null) {
			return string;
		}
		// Avoid cache lookup if string is the empty string
		throwIfEmptyString(string);
		String res = NODEPREP_CACHE.get(string);
		if (res != null) {
			return res;
		}
		res = xmppStringprep.localprep(string);
		NODEPREP_CACHE.put(string, res);
		return res;
	}

	public static String domainprep(String string) throws XmppStringprepException {
		if (xmppStringprep == null) {
			return string;
		}
		// Avoid cache lookup if string is the empty string
		throwIfEmptyString(string);
		String res = DOMAINPREP_CACHE.get(string);
		if (res != null) {
			return res;
		}
		res = xmppStringprep.domainprep(string);
		DOMAINPREP_CACHE.put(string, res);
		return res;
	}

	public static String resourceprep(String string) throws XmppStringprepException {
		if (xmppStringprep == null) {
			return string;
		}
		// Avoid cache lookup if string is the empty string
		throwIfEmptyString(string);
		String res = RESOURCEPREP_CACHE.get(string);
		if (res != null) {
			return res;
		}
		res = xmppStringprep.resourceprep(string);
		RESOURCEPREP_CACHE.put(string, res);
		return res;
	}

	public static void setMaxCacheSizes(int size) {
		NODEPREP_CACHE.setMaxCacheSize(size);
		DOMAINPREP_CACHE.setMaxCacheSize(size);
		RESOURCEPREP_CACHE.setMaxCacheSize(size);
	}

	/**
	 * Throws a XMPP Stringprep exception if string is the empty string.
	 *
	 * @param string
	 * @throws XmppStringprepException
	 */
	private static void throwIfEmptyString(String string) throws XmppStringprepException {
		// TODO replace with string.isEmpty() once Smack's min Android SDK level is > 8
		if (string.length() == 0) {
			throw new XmppStringprepException(string, "Argument can't be the empty string");
		}
	}
}
