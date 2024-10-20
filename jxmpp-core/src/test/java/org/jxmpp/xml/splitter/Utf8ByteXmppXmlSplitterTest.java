/**
 *
 * Copyright © 2015-2024 Florian Schmaus
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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
			byte[] utf8bytes = element.getBytes(StandardCharsets.UTF_8);
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
		// Size the write buffer small that a resize is likely and we are able to test the code.
		splitter.resetWriteBuffer(1);
		splitter.write(utf8bytes);
		assertTrue(queue.isEmpty());
	}

	@Test
	public void simpleByteBufferArrayWriteTest() throws IOException {
		final String stanza1 = "<message from='foo' to='bar'><body>Hi there</body></message>";
		final String stanza2 = "<message from='foo' to='bar'><body>My name is John</body></message>";
		testUtf8ByteSplitterByteBufferArray(stanza1, stanza2);
	}

	private static void testUtf8ByteSplitterByteBufferArray(String... elements) throws IOException {
		final Queue<String> queue = new ArrayDeque<>();
		List<ByteBuffer> byteBufferArray = new ArrayList<>();
		for (String element : elements) {
			queue.add(element);
			byte[] utf8bytes = element.getBytes(StandardCharsets.UTF_8);
			ByteBuffer byteBuffer = ByteBuffer.wrap(utf8bytes);
			byteBufferArray.add(byteBuffer);
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
		// Size the write buffer small that a resize is likely and we are able to test the code.
		splitter.resetWriteBuffer(1);
		splitter.write(byteBufferArray);
		assertTrue(queue.isEmpty());
	}

	private static String forCodepoint(int codepoint) {
		return new String(new int[] { codepoint }, 0, 1);
	}
}
