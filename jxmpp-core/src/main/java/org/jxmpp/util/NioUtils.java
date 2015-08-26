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
package org.jxmpp.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Utilities for Java's NIO API.
 *
 * @author Florian Schmaus
 *
 */
public class NioUtils {

	/**
	 * Write the contents of the given {@link ByteBuffer} into a
	 * {@link OutputStream}.
	 *
	 * @param byteBuffer the buffer to read from.
	 * @param outputStream the output stream to write to.
	 * @throws IOException if an error occurs.
	 */
	public static void write(ByteBuffer byteBuffer, OutputStream outputStream)
			throws IOException {
		while (byteBuffer.remaining() > 0) {
			outputStream.write(byteBuffer.get());
		}
	}
}
