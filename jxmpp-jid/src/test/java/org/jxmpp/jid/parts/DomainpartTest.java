/*
 *
 * Copyright © 2026 Florian Schmaus
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
package org.jxmpp.jid.parts;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.Normalizer;

import org.junit.Test;
import org.jxmpp.JxmppContext;
import org.jxmpp.stringprep.XmppStringprep;
import org.jxmpp.stringprep.XmppStringprepException;

public class DomainpartTest {

	@Test
	public void atSignTest() {
		assertThrows(XmppStringprepException.class, () -> Domainpart.from("boo@example.org"));
	}

	@Test
	public void atSignAfterDomainprepTest() {
		// Mimic the NFKC mapping of nameprep, as done by e.g. the ICU4J and LibIDN stringprep implementations,
		// which maps U+FF20 (FULLWIDTH COMMERCIAL AT) to '@'.
		XmppStringprep nfkcStringprep = new XmppStringprep() {
			@Override
			public String localprep(String string) {
				return string;
			}

			@Override
			public String domainprep(String string) {
				return Normalizer.normalize(string, Normalizer.Form.NFKC);
			}

			@Override
			public String resourceprep(String string) {
				return string;
			}
		};
		JxmppContext context = JxmppContext.builder().withXmppStringprep(nfkcStringprep).build();

		assertThrows(XmppStringprepException.class, () -> Domainpart.from("boo＠example.org", context));
	}
}
