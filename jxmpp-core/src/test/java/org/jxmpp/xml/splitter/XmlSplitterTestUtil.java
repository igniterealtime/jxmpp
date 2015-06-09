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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class XmlSplitterTestUtil {

	public static void xmlSplitterTest(String... elements) throws IOException {
		splitterTest(new XmlSplitterFactory() {
			@Override
			public XmlSplitter createXmlSplitter(CompleteElementCallback callback) {
				return new XmlSplitter(4096, callback);
			}
		}, elements);
	}

	public static void xmppSplitterTest(String... elements) throws IOException {
		splitterTest(new XmlSplitterFactory() {
			@Override
			public XmlSplitter createXmlSplitter(CompleteElementCallback callback) {
				return new XmppXmlSplitter(callback);
			}
		}, elements);
	}

	public static void splitterTest(XmlSplitterFactory xmlSplitterFactory, String... elements) throws IOException {
		final Queue<String> queue = new LinkedList<>();
		StringBuilder xml = new StringBuilder();
		for (String element : elements) {
			queue.add(element);
			xml.append(element);
		}
		final AtomicBoolean invoked = new AtomicBoolean();
		try (XmlSplitter splitter = xmlSplitterFactory.createXmlSplitter(new CompleteElementCallback() {
			@Override
			public void onCompleteElement(String completeElement) {
				invoked.set(true);
				String stanza = queue.poll();
				assertEquals(completeElement, stanza);
			}
		});) {
			splitter.write(xml.toString());
			assertTrue(invoked.get());
			assertTrue(queue.isEmpty());
		}
	}
}
