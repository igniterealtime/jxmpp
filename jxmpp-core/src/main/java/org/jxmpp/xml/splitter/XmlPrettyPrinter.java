/**
 *
 * Copyright 2018 Florian Schmaus
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

import org.jxmpp.xml.splitter.XmlSplitter.State;

public class XmlPrettyPrinter extends XmlPrinter {

	private final int indent;
	private final int attributeIndent;
	private final int tabWidth;

	private final PrettyPrintedXmlChunkWithCurrentPartCallback newChunkCallback;
	private final PrettyPrintedXmlPartCallback newPartCallback;
	private final PrettyPrintedXmlChunkSink prettyWriter;

	private StringBuilder currentPart;
	private StringBuilder currentChunk;
	private StringBuilder currentChunkWithCurrentPart;

	/**
	 * Construct a new XML pretty printer.
	 *
	 * @param partCallback a part callback.
	 */
	public XmlPrettyPrinter(PrettyPrintedXmlPartCallback partCallback) {
		this(builder().setPartCallback(partCallback));
	}

	/**
	 * Construct a new XML pretty printer.
	 *
	 * @param prettyWriter a writer for the pretty printed XML stream.
	 */
	public XmlPrettyPrinter(PrettyPrintedXmlChunkSink prettyWriter) {
		this(builder().setPrettyWriter(prettyWriter));
	}

	private XmlPrettyPrinter(Builder builder) {
		this.indent = builder.indent;
		this.attributeIndent = builder.attributeIndent;
		this.tabWidth = builder.tabWidth;
		this.newChunkCallback = builder.newChunkCallback;
		this.newPartCallback = builder.newPartCallback;
		this.prettyWriter = builder.prettyWriter;
	}

	@Override
	void onChunkStart() {
		if (newChunkCallback != null) {
			currentChunkWithCurrentPart = new StringBuilder(currentPart.length() + 1024);
			currentChunkWithCurrentPart.append(currentPart);
			currentChunkWithCurrentPart.append('[');
		}

		if (prettyWriter != null) {
			currentChunk = new StringBuilder(1024);
		}
	}

	@Override
	void onChunkEnd() {
		if (newChunkCallback != null) {
			currentChunkWithCurrentPart.append(']');
			newChunkCallback.onPrettyPrintedXmlChunk(currentChunkWithCurrentPart);
			currentChunkWithCurrentPart = null;
		}

		if (prettyWriter != null) {
			prettyWriter.sink(currentChunk);
			currentChunk = null;
		}
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	void onNextChar(char c, int depth, State initialState, State currentState) throws IOException {
		final boolean stateChange = initialState != currentState;
		final StringBuilder sb = new StringBuilder(stateChange ? 16 : 1);

		if (stateChange) {
			boolean deferredLeftAngle = false;
			int indent = 0;
			switch (currentState) {
			case TAG_LEFT_ANGLE_BRACKET:
				// Note that we return here because we need to see if this is a start tag or end tag.
				return;
			case END_TAG_SOLIDUS:
				indent = getElementIndent(depth - 1);
				deferredLeftAngle = true;
				break;
			case IN_TAG_NAME:
				indent = getElementIndent(depth);
				deferredLeftAngle = true;
				break;
			case IN_ATTRIBUTE_NAME:
				if (attributeIndent > 0) {
					indent = getAttributeIndent(depth);
				}
				break;
			case START:
				indent = getElementIndent(depth);
				break;
			case IN_PROCESSING_INSTRUCTION_OR_DECLARATION:
				if (initialState == State.TAG_LEFT_ANGLE_BRACKET) {
					deferredLeftAngle = true;
				}
				break;
			}

			if (indent > 0 || deferredLeftAngle) {
				sb.append('\n');
			}

			appendIndent(sb, indent);

			if (deferredLeftAngle) {
				sb.append('<');
			}
		}

		sb.append(c);

		if (currentChunkWithCurrentPart != null) {
			currentChunkWithCurrentPart.append(sb);
		}
		if (newPartCallback != null) {
			if (currentPart == null) {
				currentPart = new StringBuilder(1024);
			}
			currentPart.append(sb);
		}
		if (prettyWriter != null) {
			currentChunk.append(sb);
		}
	}

	@Override
	void onCompleteElement() {
		if (newPartCallback == null) {
			return;
		}
		if (currentPart.charAt(0) == '\n') {
			currentPart.deleteCharAt(0);
		}
		newPartCallback.onPrettyPrintedXmlPart(currentPart);
		currentPart = null;
	}

	private int getElementIndent(int depth) {
		return indent * depth;
	}

	private int getAttributeIndent(int depth) {
		return getElementIndent(depth) + attributeIndent;
	}

	private void appendIndent(StringBuilder sb, int indent) {
		int spaces = indent;
		if (tabWidth > 0) {
			spaces = indent % tabWidth;
			int tabs = indent / tabWidth;
			for (int i = 0; i < tabs; i++) {
				sb.append('\t');
			}
		}
		for (int i = 0; i < spaces; i++) {
			sb.append(' ');
		}
	}

	public interface PrettyPrintedXmlChunkWithCurrentPartCallback {

		/**
		 * Invoked after the XML pretty printer handled a chunk. The pretty printed chunk will contain the current part
		 * and the newly handled chunk enclosing in '[' and ']'.
		 *
		 * @param chunk the state of the current part with the newly handled chunk marked.
		 */
		void onPrettyPrintedXmlChunk(StringBuilder chunk);
	}

	public interface PrettyPrintedXmlPartCallback {

		/**
		 * Invoked after a part was completed.
		 *
		 * @param part the pretty printed part.
		 */
		void onPrettyPrintedXmlPart(StringBuilder part);
	}

	/**
	 * A functional interface which acts as sink for character sequences.
	 */
	public interface PrettyPrintedXmlChunkSink {

		/**
		 * Sink of the pretty printed XML chunk.
		 *
		 * @param stringBuilder a StringBuilder containing the pretty printed XML of the current chunk.
		 */
		void sink(StringBuilder stringBuilder);
	}

	/**
	 * Create a new builder.
	 *
	 * @return a new builder.
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private int indent = 2;
		private int attributeIndent;
		private int tabWidth;

		private PrettyPrintedXmlChunkWithCurrentPartCallback newChunkCallback;
		private PrettyPrintedXmlPartCallback newPartCallback;
		private PrettyPrintedXmlChunkSink prettyWriter;

		private Builder() {
		}

		/**
		 * Set the indent for elements in whitespace characters.
		 *
		 * @param indent the indent for elements in whitespace characters.
		 * @return a reference to this builders.
		 */
		public Builder setIndent(int indent) {
			ensureNotNegative(indent);

			this.indent = indent;
			return this;
		}

		/**
		 * Set the attribute indent in whitespace characters. Use a value smaller one to disable attribute indentation.
		 *
		 * @param attributeIndent the attribute indent in whitespace characters.
		 * @return a reference to this builder.
		 */
		public Builder setAttributeIndent(int attributeIndent) {
			ensureNotNegative(attributeIndent);

			this.attributeIndent = attributeIndent;
			return this;
		}

		/**
		 * Set the tab width in whitespace characters. Use a value smaller one to disable pretty printing with tabs.
		 *
		 * @param tabWidth the tab width in whitespace characters.
		 * @return a reference to this builder.
		 */
		public Builder setTabWidth(int tabWidth) {
			ensureNotNegative(tabWidth);

			this.tabWidth = tabWidth;
			return this;
		}

		/**
		 * Set a chunk callback.
		 *
		 * @param chunkCallback the chunk callback.
		 * @return a reference to this builder.
		 */
		public Builder setChunkCallback(PrettyPrintedXmlChunkWithCurrentPartCallback chunkCallback) {
			this.newChunkCallback = chunkCallback;
			return this;
		}

		/**
		 * Set a part callback.
		 *
		 * @param partCallback the part callback.
		 * @return a reference to this builder.
		 */
		public Builder setPartCallback(PrettyPrintedXmlPartCallback partCallback) {
			this.newPartCallback = partCallback;
			return this;
		}

		/**
		 * Set a {@link PrettyPrintedXmlChunkSink} for the pretty printed XML stream.
		 *
		 * @param prettyWriter the writer to pretty print to.
		 * @return a reference to this builder.
		 */
		public Builder setPrettyWriter(PrettyPrintedXmlChunkSink prettyWriter) {
			this.prettyWriter = prettyWriter;
			return this;
		}

		/**
		 * Build an XML pretty printer.
		 *
		 * @return the newly build XML pretty printer.
		 */
		public XmlPrettyPrinter build() {
			return new XmlPrettyPrinter(this);
		}

		private static void ensureNotNegative(int i) {
			if (i < 0) {
				throw new IllegalArgumentException();
			}
		}
	}
}
