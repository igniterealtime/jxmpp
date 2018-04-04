/**
 *
 * Copyright © 2015-2018 Florian Schmaus
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.jxmpp.xml.splitter.XmlSplitterTestUtil.transform;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.junit.Test;

public class Utf8ByteXmppXmlSplitterTest {

	@Test
	public void simpleTest0to7f() throws IOException {
		final String stanza = "<message from='foo' to='bar'><body>Hi</body></message>";
		testUtf8ByteSplitter(stanza);
	}

	@Test
	public void simpleTest80to7ff() throws IOException {
		final String stanza = "<message from='foo' to='bar'><body>Section Sign (0xA7): §</body></message>";
		testUtf8ByteSplitter(stanza);
	}

	@Test
	public void simpleTest800toffff() throws IOException {
		final String stanza = "<message from='foo' to='bar'><body>Euro Sign (0x20AC): €</body></message>";
		testUtf8ByteSplitter(stanza);
	}

	@Test
	public void simpleTest10000to1fffff() throws IOException {
		final String greekAcrophonicAtticOneHalf = forCodepoint(0x10141);
		final String stanza = "<message from='foo' to='bar'><body>GREEK ACROPHONIC ATTIC ONE HALF (0x10141): "+ greekAcrophonicAtticOneHalf + "</body></message>";
		testUtf8ByteSplitter(stanza);
	}

	private static void testUtf8ByteSplitter(String... elements) throws IOException {
		final Queue<String> queue = new ArrayDeque<>();
		List<Byte> bytesArray = new ArrayList<>();
		for (String element : elements) {
			queue.add(element);
			byte[] utf8bytes = element.getBytes("UTF-8");
			for (byte b : utf8bytes) {
				bytesArray.add(b);
			}
		}
		byte[] utf8bytes = new byte[bytesArray.size()];
		for (int i = 0; i < bytesArray.size(); i++) {
			utf8bytes[i] = bytesArray.get(i);
		}

		// TODO This is basically duplicate code which is also found in
		// XmlSplitterTestUtil and should be replaced by it.
		@SuppressWarnings("resource")
		Utf8ByteXmppXmlSplitter splitter = new Utf8ByteXmppXmlSplitter(transform(new CompleteElementCallback() {
			@Override
			public void onCompleteElement(String completeElement) {
				String nextElement = queue.poll();
				assertEquals(nextElement, completeElement);
			}
		}));
		splitter.write(utf8bytes);
		assertTrue(queue.isEmpty());
	}

	private static String forCodepoint(int codepoint) {
		return new String(new int[] { codepoint }, 0, 1);
	}
}
