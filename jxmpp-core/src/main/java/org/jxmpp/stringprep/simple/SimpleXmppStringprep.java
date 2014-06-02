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
package org.jxmpp.stringprep.simple;

import java.util.Locale;

import org.jxmpp.stringprep.XmppStringPrepUtil;
import org.jxmpp.stringprep.XmppStringprep;
import org.jxmpp.stringprep.XmppStringprepException;

public class SimpleXmppStringprep implements XmppStringprep {

	private static SimpleXmppStringprep instance;

	public static void setup() {
		XmppStringPrepUtil.setXmppStringprep(getInstance());
	}

	public static SimpleXmppStringprep getInstance() {
		if (instance == null) {
			instance = new SimpleXmppStringprep();
		}
		return instance;
	}

	private SimpleXmppStringprep() {
	}

	@Override
	public String nodeprep(String string) throws XmppStringprepException {
		return simpleStringprep(string);
	}

	@Override
	public String nameprep(String string) throws XmppStringprepException {
		return simpleStringprep(string);
	}

	@Override
	public String resourceprep(String string) throws XmppStringprepException {
		return simpleStringprep(string);
	}

	private static String simpleStringprep(String string) {
		String res = string.toLowerCase(Locale.US);
		res = res.trim();
		return res;
	}
}
