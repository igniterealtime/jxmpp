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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;

/**
 * Extended version of {@link XmppXmlSplitter} allowing input to be bytes or
 * {@link ByteBuffer} representing a UTF-8 encoded XML string for XMPP. Just as
 * they come from a network socket.
 * <p>
 * This class respects the byte order mark (BOM )requirement of RFC 6120 11.6
 * and treats the BOM as zero width no-break space, and not as byte order mark.
 * </p>
 * 
 * @author Florian Schmaus
 *
 */
public class Utf8ByteXmppXmlSplitter extends OutputStream {

	private final XmppXmlSplitter xmppXmlSplitter;

	/**
	 * Create a new splitter with the given callback.
	 *
	 * @param xmppElementCallback the callback invoked once a complete element has been processed.
	 */
	public Utf8ByteXmppXmlSplitter(XmppElementCallback xmppElementCallback) {
		this(new XmppXmlSplitter(xmppElementCallback));
	}

	/**
	 * Create a new UTF-8 splitter with the given XMPP XML splitter.
	 *
	 * @param xmppXmlSplitter the used XMPP XML splitter.
	 */
	public Utf8ByteXmppXmlSplitter(XmppXmlSplitter xmppXmlSplitter) {
		this.xmppXmlSplitter = xmppXmlSplitter;
	}

	private final byte[] buffer = new byte[6];

	private char[] writeBuffer = new char[1024];
	private int writeBufferPos;
	private byte count;
	private byte expectedLength;

	@Override
	public void write(int b) throws IOException {
		write((byte) (b & 0xff));
	}

	/**
	 * Write a single byte. The byte must be part of a UTF-8 String.
	 *
	 * @param b the byte to write.
	 * @throws IOException if an error occurs.
	 */
	public void write(byte b) throws IOException {
		process(b);
		afterInputProcessed();
	}

	/**
	 * Write the given array of byte buffers.
	 *
	 * @param byteBuffers the array of byte buffers.
	 * @throws IOException if an error occurs.
	 */
	public void write(ByteBuffer[] byteBuffers) throws IOException {
		write(Arrays.asList(byteBuffers));
	}

	/**
	 * Write the given collection of byte buffers.
	 *
	 * @param byteBuffers the collection of byte buffers.
	 * @throws IOException if an error occurs.
	 */
	public void write(Collection<? extends ByteBuffer> byteBuffers) throws IOException {
		int requiredNewCapacity = 0;
		for (ByteBuffer byteBuffer : byteBuffers) {
			requiredNewCapacity += byteBuffer.remaining();
		}

		ensureWriteBufferHasCapacityFor(requiredNewCapacity);

		for (ByteBuffer byteBuffer : byteBuffers) {
			writeByteBufferInternal(byteBuffer);
		}

		afterInputProcessed();
	}

	/**
	 * Write the given byte buffer.
	 *
	 * @param byteBuffer the byte buffer.
	 * @throws IOException if an error occurs.
	 */
	public void write(ByteBuffer byteBuffer) throws IOException {
		final int remaining = byteBuffer.remaining();
		ensureWriteBufferHasCapacityFor(remaining);

		writeByteBufferInternal(byteBuffer);

		afterInputProcessed();
	}

	private void writeByteBufferInternal(ByteBuffer byteBuffer) throws IOException {
		final int remaining = byteBuffer.remaining();

		if (byteBuffer.hasArray()) {
			writeInternal(byteBuffer.array(), byteBuffer.arrayOffset(), remaining);
		} else {
			int initialPosition = byteBuffer.position();
			for (int i = 0; i < remaining; i++) {
				process(byteBuffer.get(initialPosition + i));
			}
		}

		((java.nio.Buffer) byteBuffer).flip();
	}

	@Override
	public void write(byte[] b, int offset, int length) throws IOException {
		ensureWriteBufferHasCapacityFor(length);

		writeInternal(b, offset, length);

		afterInputProcessed();
	}

	private void writeInternal(byte[] b, int offset, int length) throws IOException {
		for (int i = 0; i < length; i++ ) {
			process(b[offset + i]);
		}
	}

	/**
	 * Reset the write buffer to the given size.
	 *
	 * @param size the new write buffer size.
	 */
	public void resetWriteBuffer(int size) {
		writeBuffer = new char[size];
		writeBufferPos = 0;
	}

	private void process(byte b) throws IOException {
		buffer[count] = b;

		if (count == 0) {
			int firstByte = buffer[0] & 0xff;
			if (firstByte < 0x80) {
				expectedLength = 1;
			} else if (firstByte < 0xe0) {
				expectedLength = 2;
			} else if (firstByte < 0xf0) {
				expectedLength = 3;
			} else if (firstByte < 0xf8) {
				expectedLength = 4;
			} else {
				throw new IOException("Invalid first UTF-8 byte: " + firstByte);
			}
		}

		if (++count == expectedLength) {
			int codepoint;
			if (expectedLength == 1) {
				codepoint = buffer[0] & 0x7f;
			} else {
				// The following switch-case could also be omitted. Note sure
				// how it would affect performance. Using switch-case means that
				// the bitsToMask does not need to be calculated, but the code
				// would be shorter if the switch-code was not here and maybe
				// this affects JIT'ed performance (maybe even positive).
				switch (expectedLength) {
				case 2:
					codepoint = buffer[0] & 0x1f;
					codepoint <<= 6 * 1;
					break;
				case 3:
					codepoint = buffer[0] & 0xf;
					codepoint <<= 6 * 2;
					break;
				case 4:
					codepoint = buffer[0] & 0x6;
					codepoint <<= 6 * 3;
					break;
				default:
					throw new IllegalStateException();
				}

				for (int i = 1; i < expectedLength; i++) {
					// Get the lower 6 bits.
					int bits = buffer[i] & 0x3f;
					// Shift the bits to the right position.
					bits <<= 6 * (expectedLength - 1 - i);
					codepoint |= bits;
				}
			}

			ensureWriteBufferHasCapacityFor(2);

			if (codepoint < 0x10000) {
				appendToWriteBuffer((char) codepoint);
			} else {
				// We have to convert the codepoint into a surrogate pair.
				// high surrogate: top ten bits added to 0xd800 give the first 16-bit code unit.
				appendToWriteBuffer((char) (0xd800 + (codepoint & 0xffa00000)));
				// low surrogate: low ten bits added to 0xdc00 give the second 16-bit code unit.
				appendToWriteBuffer((char) (0xdc00 + (codepoint & 0x3ff)));
			}

			// Reset count since we are done handling this UTF-8 codepoint.
			count = 0;
		}
	}

	private void afterInputProcessed() throws IOException {
		xmppXmlSplitter.write(writeBuffer, 0, writeBufferPos);
		writeBufferPos = 0;
	}

	private void appendToWriteBuffer(char c) {
		writeBuffer[writeBufferPos++] = c;
	}

	private void ensureWriteBufferHasCapacityFor(int additionalCapacity) {
		final int requiredCapacity = writeBufferPos + additionalCapacity;
		if (requiredCapacity <= writeBuffer.length) {
			return;
		}

		// Simple resize logic of write buffer.
		char[] newWriteBuffer = new char[requiredCapacity];
		System.arraycopy(writeBuffer, 0, newWriteBuffer, 0, writeBufferPos);
		writeBuffer = newWriteBuffer;
	}
}
