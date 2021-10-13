/**
 *
 * Copyright © 2014-2021 Florian Schmaus
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

import org.jxmpp.JxmppContext;
import org.jxmpp.XmppAddressParttype;
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

	/**
	 * Perform localprep on the input String.
	 *
	 * @param string the input String.
	 * @return the localpreped String.
	 * @throws XmppStringprepException if the input String can not be transformed.
	 */
	public static String localprep(String string) throws XmppStringprepException {
		return localprep(string, JxmppContext.getDefaultContext());
	}

	/**
	 * Perform localprep on the input String.
	 *
	 * @param string the input String.
	 * @param context the JXMPP JID context.
	 * @return the localpreped String.
	 * @throws XmppStringprepException if the input String can not be transformed.
	 */
	public static String localprep(String string, JxmppContext context) throws XmppStringprepException {
		throwIfNullOrEmpty(string, XmppAddressParttype.localpart);
		String res;
		if (context.isCachingEnabled()) {
			res = NODEPREP_CACHE.lookup(string);
			if (res != null) {
				return res;
			}
		}

		res = context.xmppStringprep.localprep(string);

		if (context.isCachingEnabled()) {
			NODEPREP_CACHE.put(string, res);
		}
		return res;
	}

	/**
	 * Perform domainprep on the input String.
	 *
	 * @param string the input String.
	 * @return the domainprep String.
	 * @throws XmppStringprepException if the input String can not be transformed.
	 */
	public static String domainprep(String string) throws XmppStringprepException {
		return domainprep(string, JxmppContext.getDefaultContext());
	}


	/**
	 * Perform domainprep on the input String.
	 *
	 * @param string the input String.
	 * @param context the JXMPP JID context.
	 * @return the domainprep String.
	 * @throws XmppStringprepException if the input String can not be transformed.
	 */
	public static String domainprep(String string, JxmppContext context) throws XmppStringprepException {
		throwIfNullOrEmpty(string, XmppAddressParttype.domainpart);
		String res;
		if (context.isCachingEnabled()) {
			res = DOMAINPREP_CACHE.lookup(string);
			if (res != null) {
				return res;
			}
		}

		res = context.xmppStringprep.domainprep(string);

		if (context.isCachingEnabled()) {
			DOMAINPREP_CACHE.put(string, res);
		}
		return res;
	}

	/**
	 * Perform resourceprep on the input String.
	 *
	 * @param string the input String.
	 * @return the resourceprep String.
	 * @throws XmppStringprepException if the input String can not be transformed.
	 */
	public static String resourceprep(String string) throws XmppStringprepException {
		return resourceprep(string, JxmppContext.getDefaultContext());
	}

	/**
	 * Perform resourceprep on the input String.
	 *
	 * @param string the input String.
	 * @param context the JXMPP JID context.
	 * @return the resourceprep String.
	 * @throws XmppStringprepException if the input String can not be transformed.
	 */
	public static String resourceprep(String string, JxmppContext context) throws XmppStringprepException {
		throwIfNullOrEmpty(string, XmppAddressParttype.resourcepart);
		String res;
		if (context.isCachingEnabled()) {
			res = RESOURCEPREP_CACHE.lookup(string);
			if (res != null) {
				return res;
			}
		}

		res = context.xmppStringprep.resourceprep(string);

		if (context.isCachingEnabled()) {
			RESOURCEPREP_CACHE.put(string, res);
		}
		return res;
	}

	/**
	 * Set the maximum cache sizes.
	 *
	 * @param size the maximum cache size.
	 */
	public static void setMaxCacheSizes(int size) {
		NODEPREP_CACHE.setMaxCacheSize(size);
		DOMAINPREP_CACHE.setMaxCacheSize(size);
		RESOURCEPREP_CACHE.setMaxCacheSize(size);
	}

	/**
	 * Throws a XMPP Stringprep exception if string is the empty string.
	 *
	 * @param string the string to check
	 * @throws XmppStringprepException exception telling that the argument was the empty string
	 */
	private static void throwIfNullOrEmpty(String string, XmppAddressParttype type) throws XmppStringprepException {
		if (string == null) {
			throw new XmppStringprepException(string, type + " can't be null");
		}
		if (string.isEmpty()) {
			throw new XmppStringprepException(string, type + " can't be the empty string");
		}
	}
}
