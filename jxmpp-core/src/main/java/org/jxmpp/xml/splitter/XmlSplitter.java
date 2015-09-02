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
		IN_DECLARATION,
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
	}

	protected final CompleteElementCallback completeElementCallback;

	private final StringBuilder elementBuffer;

	private final StringBuilder tokenBuffer = new StringBuilder(256);
	private final Map<String, String> attributes = new HashMap<>();

	private int depth;
	private String qName;
	private String attributeName;
	private State state = State.START;

	/**
	 * Construct a new XML splitter.
	 *
	 * @param bufferSize the initial size of the buffer.
	 * @param completeElementCallback the callback invoked once a complete element has been processed.
	 */
	public XmlSplitter(int bufferSize, CompleteElementCallback completeElementCallback) {
		this.elementBuffer = new StringBuilder(bufferSize);
		if (completeElementCallback == null) {
			throw new IllegalArgumentException();
		}
		this.completeElementCallback = completeElementCallback;
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
	 * Get the size in bytes of the element currently being processed.
	 * 
	 * @return the size of the current element in chars.
	 */
	public final int getCurrentElementSize() {
		return elementBuffer.length();
	}

	protected void onNextChar() throws IOException {
	}

	protected void onStartTag(String prefix, String localpart, Map<String, String> attributes) {
	}

	protected void onEndTag(String qName) {
	}

	protected final void newTopLevelElement() {
		depth = 0;
		elementBuffer.setLength(0);
	}

	private void processChar(char c) throws IOException {
		onNextChar();
		elementBuffer.append(c);
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
				state = State.IN_DECLARATION;
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
			case '\"':
				state = State.IN_ATTRIBUTE_VALUE;
				break;
			default:
				throw new IOException();
			}
			break;
		case IN_ATTRIBUTE_VALUE:
			switch (c) {
			case '\'':
			case '\"':
				attributes.put(attributeName, getToken());
				state = State.AFTER_START_NAME;
				break;
			default:
				tokenBuffer.append(c);
				break;
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
		case AFTER_COMMENT_BANG:
		case AFTER_COMMENT_DASH1:
		case AFTER_COMMENT_DASH2:
		case AFTER_COMMENT:
		case AFTER_COMMENT_CLOSING_DASH1:
		case AFTER_COMMENT_CLOSING_DASH2:
		case IN_DECLARATION:
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
			String completeElement = elementBuffer.toString();
			elementBuffer.setLength(0);
			completeElementCallback.onCompleteElement(completeElement);
		}
		onEndTag(endTagName);
		state = State.START;
	}

	private final String getToken() {
		String token = tokenBuffer.toString();
		tokenBuffer.setLength(0);
		return token;
	}

	private final static String extractPrefix(String qName) {
		int index = qName.indexOf(':');
		return index > -1  ? qName.substring(0, index) : qName;
	}

	private final static String extractLocalpart(String qName) {
		int index = qName.indexOf(':');
		return index > -1 ? qName.substring(index + 1) : qName;
	}
}
