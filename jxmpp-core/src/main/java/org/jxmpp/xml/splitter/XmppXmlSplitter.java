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
import java.util.Collections;
import java.util.Map;

/**
 * A XML splitter for XMPP. Unlike {@link XmlSplitter}, this splitter is aware
 * of the special semantics of XMPP's {@code &lt;stream:stream&gt;} element.
 */
public class XmppXmlSplitter extends XmlSplitter {

	private final XmppElementCallback xmppElementCallback;
	private final int maxElementSize;

	private String streamPrefix;

	/**
	 * Construct a new XMPP XML splitter with a max element size of 10000.
	 * <p>
	 * RFC 6120 § 13.12 4. requires XMPP servers to use nothing less then 10000 as maximum stanza size.
	 * </p>
	 * @param xmppElementCallback the callback invoked once a complete element has been processed.
	 */
	public XmppXmlSplitter(XmppElementCallback xmppElementCallback) {
		this(10000, xmppElementCallback);
	}

	/**
	 * Construct a new XMPP XML splitter.
	 *
	 * @param maxElementSize the maximum size of a single top level element in bytes.
	 * @param xmppElementCallback the callback invoked once a complete element has been processed.
	 */
	public XmppXmlSplitter(int maxElementSize, XmppElementCallback xmppElementCallback) {
		super(maxElementSize, xmppElementCallback);
		this.maxElementSize = maxElementSize;
		this.xmppElementCallback = xmppElementCallback;
	}

	@Override
	protected void onNextChar() throws IOException {
		if (getCurrentElementSize() >= maxElementSize) {
			throw new IOException("Max element size exceeded");
		}
	}

	@Override
	protected void onStartTag(String prefix, String localpart, Map<String, String> attributes) {
		if (!"stream".equals(localpart)) {
			// If the open tag's name is not 'stream' then we are not interested.
			return;
		}

		if ("http://etherx.jabber.org/streams".equals(attributes.get("xmlns:" + prefix))) {
			streamPrefix = prefix;
			newTopLevelElement();
			xmppElementCallback.streamOpened(prefix, Collections.unmodifiableMap(attributes));
		}
	}

	protected void onEndTag(String qName) {
		if (streamPrefix == null || !qName.startsWith(streamPrefix)) {
			// Shortcut if streamPrefix is not yet set or if qName does not even
			// start with it.
			return;
		}

		if ((streamPrefix + ":stream").equals(qName)) {
			xmppElementCallback.streamClosed();
		}
	}
}
