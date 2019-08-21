/**
 *
 * Copyright 2019 Florian Schmaus
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
package org.jxmpp.strings.testframework;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jxmpp.JxmppContext;
import org.jxmpp.stringprep.XmppStringprep;
import org.jxmpp.stringprep.icu4j.Icu4jXmppStringprep;
import org.jxmpp.stringprep.libidn.LibIdnXmppStringprep;
import org.jxmpp.stringprep.rocksxmppprecis.RocksXmppPrecisStringprep;
import org.jxmpp.stringprep.simple.SimpleXmppStringprep;

public class XmppStringPrepper {

	private static final Map<String, XmppStringPrepper> KNOWN_STRINGPREPPERS = new HashMap<>();

	public static final XmppStringPrepper ICU4J = new XmppStringPrepper(Icu4jXmppStringprep.getInstance());
	public static final XmppStringPrepper LIBIDN = new XmppStringPrepper(LibIdnXmppStringprep.getInstance());
	public static final XmppStringPrepper SIMPLE = new XmppStringPrepper(SimpleXmppStringprep.getInstance());
	public static final XmppStringPrepper ROCKS_XMPP_PRECIS = new XmppStringPrepper(RocksXmppPrecisStringprep.INSTANCE);

	public final String name;
	public final Class<? extends XmppStringprep> xmppStringprepClass;
	public final JxmppContext context;

	public final String toString;

	XmppStringPrepper(XmppStringprep xmppStringprep) {
		this.xmppStringprepClass = xmppStringprep.getClass();

		String name;
		try {
			Field field = xmppStringprepClass.getDeclaredField("NAME");
			name = (String) field.get(null);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			name = xmppStringprepClass.getSimpleName();
		}
		this.name = name;

		this.context = JxmppContext.builder().withXmppStringprep(xmppStringprep).build();
		this.toString = XmppStringPrepper.class.getSimpleName() + " '" + name + "\'";

		XmppStringPrepper previous;
		synchronized (KNOWN_STRINGPREPPERS) {
			previous = KNOWN_STRINGPREPPERS.putIfAbsent(name, this);
		}
		if (previous != null) {
			throw new IllegalArgumentException("And XMPP Stringprepper with the name '" + name + "' already exists");
		}
	}

	/**
	 * Get the name of this XMPP String Prepper.
	 *
	 * @return the name of this XMPP String Prepper.
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return toString;
	}

	/**
	 * Lookup an XMPP String Prepper by name.
	 *
	 * @param name the name of the XMPP String Prepper.
	 * @return a prepper or <code>null</code>
	 */
	public static XmppStringPrepper lookup(String name) {
		synchronized (KNOWN_STRINGPREPPERS) {
			return KNOWN_STRINGPREPPERS.get(name);
		}
	}

	/**
	 * Get a list of all known XMPP String Preppers.
	 *
	 * @return a list of XMPP String Preppers.
	 */
	public static List<XmppStringPrepper> getKnownXmppStringpreppers() {
		List<XmppStringPrepper> res;
		synchronized (KNOWN_STRINGPREPPERS) {
			res = new ArrayList<>(KNOWN_STRINGPREPPERS.size());
			res.addAll(KNOWN_STRINGPREPPERS.values());
		}
		return res;
	}
}
