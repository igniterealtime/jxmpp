/**
 *
 * Copyright © 2015-2023 Florian Schmaus
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
package org.jxmpp.stringprep.icu4j;

import org.jxmpp.JxmppContext;
import org.jxmpp.stringprep.XmppStringprep;
import org.jxmpp.stringprep.XmppStringprepException;

import com.ibm.icu.text.StringPrep;
import com.ibm.icu.text.StringPrepParseException;

/**
 * XMPP string preparation using libidn.
 */
public class Icu4jXmppStringprep implements XmppStringprep {

	/**
	 * The name of the stringprep implementation.
	 */
	public static final String NAME = "icu4j";

	private static final StringPrep NODEPREP = StringPrep.getInstance(StringPrep.RFC3920_NODEPREP);
	private static final StringPrep DOMAINPREP = StringPrep.getInstance(StringPrep.RFC3491_NAMEPREP);
	private static final StringPrep RESOURCEPREP = StringPrep.getInstance(StringPrep.RFC3920_RESOURCEPREP);

	private static Icu4jXmppStringprep instance;

	/**
	 * Setup the ICU4J Stringprep implementation as active Stringprep implementation.
	 */
	public static void setup() {
		JxmppContext.setDefaultXmppStringprep(getInstance());
	}

	/**
	 * Get the ICU4J Stringprep implementation singleton.
	 * @return the ICU4J Stringprep implementation.
	 */
	public static Icu4jXmppStringprep getInstance() {
		if (instance == null) {
			instance = new Icu4jXmppStringprep();
		}
		return instance;
	}

	private Icu4jXmppStringprep() {
	}

	@Override
	public String localprep(String string) throws XmppStringprepException {
		try {
			return NODEPREP.prepare(string, StringPrep.DEFAULT);
		} catch (StringPrepParseException e) {
			throw new XmppStringprepException(string, e);
		}
	}

	@Override
	public String domainprep(String string) throws XmppStringprepException {
		try {
			return DOMAINPREP.prepare(string, StringPrep.DEFAULT);
		} catch (StringPrepParseException e) {
			throw new XmppStringprepException(string, e);
		}
	}

	@Override
	public String resourceprep(String string) throws XmppStringprepException {
		try {
			return RESOURCEPREP.prepare(string, StringPrep.DEFAULT);
		} catch (StringPrepParseException e) {
			throw new XmppStringprepException(string, e);
		}
	}
}
