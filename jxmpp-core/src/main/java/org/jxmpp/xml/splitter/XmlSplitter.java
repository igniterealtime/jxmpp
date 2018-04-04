/**
 *
 * Copyright © 2015-2017 Florian Schmaus
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
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * A XML splitter capable of splitting XML into top-level elements.
 * <p>
 * Note that this class does not yet support the feature set of XML. Only the
 * required features for XMPP are supported. XML comments and processing
 * instructions are not supported.
 * </p>
 *
 */
public class XmlSplitter extends Writer {

	private enum State {
		START,
		AFTER_TAG_RIGHT_ANGLE_BRACKET,
		IN_TAG_NAME,
		IN_END_TAG,
		AFTER_START_NAME,
		IN_EMPTY_TAG,
		IN_ATTRIBUTE_NAME,
		AFTER_ATTRIBUTE_EQUALS,
		IN_ATTRIBUTE_VALUE,
		AFTER_COMMENT_BANG,
		AFTER_COMMENT_DASH1,
		AFTER_COMMENT_DASH2,
		AFTER_COMMENT,
		AFTER_COMMENT_CLOSING_DASH1,
		AFTER_COMMENT_CLOSING_DASH2,
		IN_PROCESSING_INSTRUCTION_OR_DECLARATION,
		IN_PROCESSING_INSTRUCTION_OR_DECLARATION_PSEUDO_ATTRIBUTE_VALUE,
		IN_PROCESSING_INSTRUCTION_OR_DECLARATION_QUESTION_MARK,
	}

	private final DeclarationCallback declarationCallback;
	private final ProcessingInstructionCallback processingInstructionCallback;

	protected final CompleteElementCallback completeElementCallback;

	private final StringBuilder splittedPartBuffer;

	private final StringBuilder tokenBuffer = new StringBuilder(256);
	private final Map<String, String> attributes = new HashMap<>();

	private int depth;
	private String qName;
	private String attributeName;
	private State state = State.START;

	private enum AttributeValueQuotes {
		apos('\''),
		quot('"'),
		;

		final char c;

		AttributeValueQuotes(char c) {
			this.c = c;
		}
	}

	/**
	 * The type of quotation used for the current (or last) attribute. Note that depending on which quotation is used,
	 * the other quotation does not need to be escaped within the value. Therefore we need to remember it to reliable
	 * detect the end quotation of the value.
	 */
	private AttributeValueQuotes attributeValueQuotes;

	/**
	 * Construct a new XML splitter.
	 *
	 * @param bufferSize the initial size of the buffer.
	 * @param completeElementCallback the callback invoked once a complete element has been processed.
	 * @param declarationCallback a optional callback for the XML declaration.
	 * @param processingInstructionCallback a optional callback for Processing Instructions.
	 */
	public XmlSplitter(int bufferSize, CompleteElementCallback completeElementCallback, DeclarationCallback declarationCallback, ProcessingInstructionCallback processingInstructionCallback) {
		this.splittedPartBuffer = new StringBuilder(bufferSize);
		if (completeElementCallback == null) {
			throw new IllegalArgumentException();
		}
		this.completeElementCallback = completeElementCallback;
		this.declarationCallback = declarationCallback;
		this.processingInstructionCallback = processingInstructionCallback;
	}

	/**
	 * Construct a new XML splitter.
	 *
	 * @param bufferSize the initial size of the buffer.
	 * @param completeElementCallback the callback invoked once a complete element has been processed.
	 */
	public XmlSplitter(int bufferSize, CompleteElementCallback completeElementCallback) {
		this(bufferSize, completeElementCallback, null, null);
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		for (int cur = off; cur < off+len; cur++) {
			processChar(cbuf[off+cur]);
		}
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
	}

	/**
	 * Get the size in bytes of the splitted part currently being processed.
	 * 
	 * @return the size of the current splitted part in chars.
	 */
	public final int getCurrentSplittedPartSize() {
		return splittedPartBuffer.length();
	}

	protected void onNextChar() throws IOException {
	}

	protected void onStartTag(String prefix, String localpart, Map<String, String> attributes) {
	}

	protected void onEndTag(String qName) {
	}

	protected final void newSplittedPart() {
		depth = 0;
		splittedPartBuffer.setLength(0);

		assert state != State.START;
		state = State.START;
	}

	private void processChar(char c) throws IOException {
		onNextChar();

		// Append every char we see to the buffer. This helps for example XmppXmlSplitter to ensure a certain size is
		// not exceeded. In case of XMPP, the size is usually for the top level stream element (Stanzas and Nonzas), but
		// also other XML pseudo-elements like the Declaration or Processing Instructions's size is limited by this.
		splittedPartBuffer.append(c);

		switch (state) {
		case START:
			switch (c) {
			case '<':
				state = State.AFTER_TAG_RIGHT_ANGLE_BRACKET;
				break;
			}
			break;
		case AFTER_TAG_RIGHT_ANGLE_BRACKET:
			switch (c) {
			case '/':
				state = State.IN_END_TAG;
				break;
			case '?':
				state = State.IN_PROCESSING_INSTRUCTION_OR_DECLARATION;
				break;
			case '!':
				state = State.AFTER_COMMENT_BANG;
				break;
			default:
				tokenBuffer.append(c);
				state = State.IN_TAG_NAME;
				break;
			}
			break;
		case IN_TAG_NAME:
			switch (c) {
			// XML 1.1 § 2.3 "White Space"
			case ' ':
			case '\n':
			case '\r':
			case '\t':
				qName = getToken();
				state = State.AFTER_START_NAME;
				break;
			case '/':
				qName = getToken();
				onStartTagFinished();
				state = State.IN_EMPTY_TAG;
				break;
			case '>':
				qName = getToken();
				onStartTagFinished();
				state = State.START;
				break;
			default:
				tokenBuffer.append(c);
				break;
			}
			break;
		case IN_END_TAG:
			switch (c) {
			case '>':
				onEndTagFinished();
				break;
			default:
				tokenBuffer.append(c);
				break;
			}
			break;
		case AFTER_START_NAME:
			switch (c) {
			case '/':
				onStartTagFinished();
				state = State.IN_EMPTY_TAG;
				break;
			case '>':
				onStartTagFinished();
				state = State.START;
				break;
			// XML 1.1 § 2.3 "White Space"
			case ' ':
			case '\n':
			case '\r':
			case '\t':
				break;
			// Attribute Name
			default:
				tokenBuffer.append(c);
				state = State.IN_ATTRIBUTE_NAME;
				break;
			}
			break;
		case IN_ATTRIBUTE_NAME:
			switch (c) {
			case '=':
				attributeName = getToken();
				state = State.AFTER_ATTRIBUTE_EQUALS;
				break;
			default:
				tokenBuffer.append(c);
			}
			break;
		case AFTER_ATTRIBUTE_EQUALS:
			switch (c) {
			case '\'':
				attributeValueQuotes = AttributeValueQuotes.apos;
				state = State.IN_ATTRIBUTE_VALUE;
				break;
			case '\"':
				attributeValueQuotes = AttributeValueQuotes.quot;
				state = State.IN_ATTRIBUTE_VALUE;
				break;
			default:
				throw new IOException();
			}
			break;
		case IN_ATTRIBUTE_VALUE:
			if (c == attributeValueQuotes.c) {
				attributes.put(attributeName, getToken());
				state = State.AFTER_START_NAME;
			} else {
				tokenBuffer.append(c);
			}
			break;
		case IN_EMPTY_TAG:
			switch (c) {
			case '>':
				onEndTagFinished();
				break;
			default:
				throw new IOException();
			}
			break;
		case IN_PROCESSING_INSTRUCTION_OR_DECLARATION:
			switch (c) {
				case '\'':
					attributeValueQuotes = AttributeValueQuotes.apos;
					state = State.IN_PROCESSING_INSTRUCTION_OR_DECLARATION_PSEUDO_ATTRIBUTE_VALUE;
					break;
				case '\"':
					attributeValueQuotes = AttributeValueQuotes.quot;
					state = State.IN_PROCESSING_INSTRUCTION_OR_DECLARATION_PSEUDO_ATTRIBUTE_VALUE;
					break;
				case '?':
					state = State.IN_PROCESSING_INSTRUCTION_OR_DECLARATION_QUESTION_MARK;
					break;
			}
			break;
		case IN_PROCESSING_INSTRUCTION_OR_DECLARATION_PSEUDO_ATTRIBUTE_VALUE:
			if (c == attributeValueQuotes.c) {
				state = State.IN_PROCESSING_INSTRUCTION_OR_DECLARATION;
			}
			break;
		case IN_PROCESSING_INSTRUCTION_OR_DECLARATION_QUESTION_MARK:
			if (c == '>') {
				String processingInstructionOrDeclaration = splittedPartBuffer.toString();
				onProcessingInstructionOrDeclaration(processingInstructionOrDeclaration);
				newSplittedPart();
			} else {
				state = State.IN_PROCESSING_INSTRUCTION_OR_DECLARATION;
			}
			break;
		case AFTER_COMMENT_BANG:
		case AFTER_COMMENT_DASH1:
		case AFTER_COMMENT_DASH2:
		case AFTER_COMMENT:
		case AFTER_COMMENT_CLOSING_DASH1:
		case AFTER_COMMENT_CLOSING_DASH2:
			throw new UnsupportedOperationException();
		}
	}

	private void onStartTagFinished() {
		// qName should already be set correctly.
		depth++;
		String prefix = extractPrefix(qName);
		String localpart = extractLocalpart(qName);
		onStartTag(prefix, localpart, attributes);
		attributes.clear();
	}

	private void onEndTagFinished() {
		String endTagName = getToken();
		if (endTagName.length() == 0) {
			// empty element case
			endTagName = qName;
		}
		depth--;
		if (depth == 0) {
			String completeElement = splittedPartBuffer.toString();
			splittedPartBuffer.setLength(0);
			completeElementCallback.onCompleteElement(completeElement);
		}
		onEndTag(endTagName);

		assert state != State.START;
		state = State.START;
	}

	private String getToken() {
		String token = tokenBuffer.toString();
		tokenBuffer.setLength(0);
		return token;
	}

	private void onProcessingInstructionOrDeclaration(String processingInstructionOrDeclaration) {
		if (processingInstructionOrDeclaration.startsWith("<?xml ")) {
			if (declarationCallback != null) {
				declarationCallback.onDeclaration(processingInstructionOrDeclaration);
			}
		} else {
			if (processingInstructionCallback != null) {
				processingInstructionCallback.onProcessingInstruction(processingInstructionOrDeclaration);
			}
		}
	}

	private static String extractPrefix(String qName) {
		int index = qName.indexOf(':');
		return index > -1  ? qName.substring(0, index) : qName;
	}

	private static String extractLocalpart(String qName) {
		int index = qName.indexOf(':');
		return index > -1 ? qName.substring(index + 1) : qName;
	}
}
