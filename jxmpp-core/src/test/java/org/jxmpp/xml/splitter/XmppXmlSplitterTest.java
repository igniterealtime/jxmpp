/**
 *
 * Copyright © 2015 Florian Schmaus
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
package org.jxmpp.xml.splitter;

import java.io.IOException;

import org.junit.Test;

public class XmppXmlSplitterTest extends XmlSplitterTestUtil {

	@Test
	public void simpleXmppTest() throws IOException {
		final String stanza = "<message from='foo' to='bar'><body>Hi</body></message>";
		xmppSplitterTest(stanza);
	}

	@Test
	public void multipleStanzasTest() throws IOException {
		final String stanza1 = "<message><empty/></message>";
		final String stanza2 = "<message from='foo' to='bar'><body>Hi</body></message>";
		final String stanza3 = "<iq from='foo' to='bar'><ping><inner3>foo</inner3><inner3/></ping></iq>";
		xmppSplitterTest(stanza1, stanza2, stanza3);
	}

}
