/**
 *
 * Copyright 2015-2018 Florian Schmaus
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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class XmlSplitterTestUtil {

	public static void xmlSplitterTest(String... elements) throws IOException {
		Element[] elementParts = elementsFrom(elements);

		splitterTest(new XmlSplitterFactory() {
			@Override
			public XmlSplitter createXmlSplitter(CompleteElementCallback callback, DeclarationCallback declarationCallback, ProcessingInstructionCallback processingInstructionCallback) {
				return new XmlSplitter(4096, callback);
			}
		}, elementParts);
	}

	private static final String STREAM_OPEN_SUBSTITUTE = "<stream>";
	private static final String STREAM_CLOSE_SUBSTITUTE = "</stream>";

	public static void xmppSplitterTest(String... elements) throws IOException {
		Element[] elementParts = elementsFrom(elements);
		xmppSplitterTest(elementParts);
	}

	public static void xmppSplitterTest(SplittedPart... splittedParts) throws IOException {
		splitterTest(new XmlSplitterFactory() {
			@Override
			public XmlSplitter createXmlSplitter(final CompleteElementCallback callback, DeclarationCallback declarationCallback, ProcessingInstructionCallback processingInstructionCallback) {
				return new XmppXmlSplitter(transform(new CompleteElementCallback() {
					@Override
					public void onCompleteElement(String completeElement) {
						callback.onCompleteElement(completeElement);
					}
				}), declarationCallback, processingInstructionCallback);
			}
		}, splittedParts);
	}

	public static XmppElementCallback transform(final CompleteElementCallback completeElementCallback) {
		return new XmppElementCallback() {
			@Override
			public void onCompleteElement(String completeElement) {
				completeElementCallback.onCompleteElement(completeElement);
			}
			@Override
			public void streamOpened(String prefix, Map<String, String> attributes) {
				onCompleteElement(STREAM_OPEN_SUBSTITUTE);
			}
			@Override
			public void streamClosed() {
				onCompleteElement(STREAM_CLOSE_SUBSTITUTE);
			}
		};
	}

	public static void splitterTest(XmlSplitterFactory xmlSplitterFactory, SplittedPart... parts) throws IOException {
		final Queue<SplittedPart> queue = new ArrayDeque<>();
		StringBuilder xml = new StringBuilder();

		boolean hasDeclarationPart = false;
		boolean hasProcessingInstructionPart = false;
		boolean hasElementPart = false;

		for (SplittedPart part : parts) {
			if (part instanceof Declaration) {
				hasDeclarationPart = true;
			} else if (part instanceof ProcessingInstruction) {
				hasProcessingInstructionPart = true;
			} else if (part instanceof Element) {
				hasElementPart = true;
			}

			queue.add(part);
			xml.append(part.part);
		}

		final AtomicBoolean invoked = new AtomicBoolean();
		final AtomicBoolean declarationCallbackInvoked = new AtomicBoolean();
		final AtomicBoolean processingInstructionCallbackInvoked = new AtomicBoolean();

		try (XmlSplitter splitter = xmlSplitterFactory.createXmlSplitter(new CompleteElementCallback() {
			@Override
			public void onCompleteElement(String completeElement) {
				invoked.set(true);

				Element element = getExpectedPartAndAssertType(queue, Element.class);
				String stanza = element.part;

				// This assumes that all stream open and close elements use
				// 'stream' as xmlns prefix. Which may not be true for all real
				// world XMPP cases.
				if (stanza.startsWith("<stream:stream")) {
					stanza = STREAM_OPEN_SUBSTITUTE;
				} else if (stanza.startsWith("</stream:stream")) {
					stanza = STREAM_CLOSE_SUBSTITUTE;
				}

				assertEquals(stanza, completeElement);
			}
		},
		new DeclarationCallback() {
			@Override
			public void onDeclaration(String declaration) {
				declarationCallbackInvoked.set(true);

				Declaration expectedDeclaration = getExpectedPartAndAssertType(queue, Declaration.class);

				assertEquals(expectedDeclaration.part, declaration);
			}
		},
		new ProcessingInstructionCallback() {
			@Override
			public void onProcessingInstruction(String processingInstruction) {
				processingInstructionCallbackInvoked.set(true);

				ProcessingInstruction expectedProcessingInstruction = getExpectedPartAndAssertType(queue, ProcessingInstruction.class);

				assertEquals(expectedProcessingInstruction.part, processingInstruction);
			}
		});) {
			splitter.write(xml.toString());
		}

		assertTrue(queue.isEmpty());

		assertEquals(hasElementPart, invoked.get());
		assertEquals(hasDeclarationPart, declarationCallbackInvoked.get());
		assertEquals(hasProcessingInstructionPart, processingInstructionCallbackInvoked.get());
	}

	@SuppressWarnings("unchecked")
	private static <P extends SplittedPart> P getExpectedPartAndAssertType(Queue<SplittedPart> queue,
			Class<P> partClass) {
		SplittedPart part = queue.poll();
		assertNotNull(part);
		assertTrue("The part " + part + " is not an instance of " + partClass, partClass.isInstance(part));

		return (P) part;
	}

	abstract static class SplittedPart {
		final String part;

		protected SplittedPart(String part) {
			this.part = part;
		}
	}

	static final class Declaration extends SplittedPart {
		private Declaration(String part) {
			super(part);
		}
	}

	static final class ProcessingInstruction extends SplittedPart {
		private ProcessingInstruction(String part) {
			super(part);
		}
	}

	static final class Element extends SplittedPart {
		private Element(String part) {
			super(part);
		}
	}

	public static Declaration decl(String declaration) {
		return new Declaration(declaration);
	}

	public static ProcessingInstruction pi(String processingInstruction) {
		return new ProcessingInstruction(processingInstruction);
	}

	public static Element elem(String element) {
		return new Element(element);
	}

	public static Element[] elementsFrom(String... elements) {
		Element[] elementParts = new Element[elements.length];
		for (int i = 0; i < elements.length; i++) {
			elementParts[i] = elem(elements[i]);
		}
		return elementParts;
	}
}
