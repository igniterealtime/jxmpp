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
package org.jxmpp.stringprep.rocksxmppprecis;

import org.jxmpp.stringprep.XmppStringprep;
import org.jxmpp.stringprep.XmppStringprepException;

import rocks.xmpp.precis.InvalidCodePointException;
import rocks.xmpp.precis.PrecisProfiles;

public class RocksXmppPrecisStringprep implements XmppStringprep {

	public static final RocksXmppPrecisStringprep INSTANCE = new RocksXmppPrecisStringprep();

	public static final String NAME = "rocks-xmpp-precis";

	private RocksXmppPrecisStringprep() {
	}

	@Override
	public String localprep(String string) throws XmppStringprepException {
		try {
			return PrecisProfiles.USERNAME_CASE_MAPPED.enforce(string);
		} catch (InvalidCodePointException e) {
			throw new XmppStringprepException(string, e);
		}
	}

	@Override
	public String domainprep(String string) throws XmppStringprepException {
		try {
			return PrecisProfiles.IDN.enforce(string);
		} catch (IllegalArgumentException e) {
			throw new XmppStringprepException(string, e);
		}
	}

	@Override
	public String resourceprep(String string) throws XmppStringprepException {
		try {
			return PrecisProfiles.OPAQUE_STRING.enforce(string);
		} catch (InvalidCodePointException e) {
			throw new XmppStringprepException(string, e);
		}
	}
}
