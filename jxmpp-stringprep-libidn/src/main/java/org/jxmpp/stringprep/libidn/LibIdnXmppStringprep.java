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
package org.jxmpp.stringprep.libidn;

import gnu.inet.encoding.Stringprep;
import gnu.inet.encoding.StringprepException;

import org.jxmpp.stringprep.XmppStringPrepUtil;
import org.jxmpp.stringprep.XmppStringprep;
import org.jxmpp.stringprep.XmppStringprepException;

public class LibIdnXmppStringprep implements XmppStringprep {

	private static LibIdnXmppStringprep instance;

	public static void setup() {
		XmppStringPrepUtil.setXmppStringprep(getInstance());
	}

	public static LibIdnXmppStringprep getInstance() {
		if (instance == null) {
			instance = new LibIdnXmppStringprep();
		}
		return instance;
	}

	private LibIdnXmppStringprep() {
	}

	@Override
	public String nodeprep(String string) throws XmppStringprepException {
		try {
			// Allow unassigned codepoints as of RFC6122 A.2
			return Stringprep.nodeprep(string, true);
		} catch (StringprepException e) {
			throw new XmppStringprepException(string, e);
		}
	}

	@Override
	public String nameprep(String string) throws XmppStringprepException {
		try {
			// Don't allow unassigned because this is a "stored string". See
			// RFC3453 7, RFC3490 4 1) and RFC6122 2.2
			return Stringprep.nameprep(string);
		} catch (StringprepException e) {
			throw new XmppStringprepException(string, e);
		}
	}

	@Override
	public String resourceprep(String string) throws XmppStringprepException {
		try {
			// Allow unassigned codepoints as of RFC6122 B.2
			return Stringprep.resourceprep(string, true);
		} catch (StringprepException e) {
			throw new XmppStringprepException(string, e);
		}
	}
}
