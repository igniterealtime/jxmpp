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
import java.util.Map;

/**
 * A XML splitter for XMPP. Unlike {@link XmlSplitter}, this splitter is aware
 * of the special semantics of XMPP's {@code &lt;stream:stream&gt;} element.
 */
public class XmppXmlSplitter extends XmlSplitter {

	private final int maxElementSize;

	private String streamNamespace;

	/**
	 * Construct a new XMPP XML splitter with a max element size of 10000.
	 * <p>
	 * RFC 6120 § 13.12 4. requires XMPP servers to use nothing more than 10000 as maximum stanza size.
	 * </p>
	 * @param completeElementCallback the callback invoked once a complete element has been processed.
	 */
	public XmppXmlSplitter(CompleteElementCallback completeElementCallback) {
		this(10000, completeElementCallback);
	}

	/**
	 * Construct a new XMPP XML splitter.
	 *
	 * @param maxElementSize the maximum size of a single top level element in bytes.
	 * @param completeElementCallback the callback invoked once a complete element has been processed.
	 */
	public XmppXmlSplitter(int maxElementSize, CompleteElementCallback completeElementCallback) {
		super(maxElementSize, completeElementCallback);
		this.maxElementSize = maxElementSize;
	}

	@Override
	protected void onNextChar() throws IOException {
		if (getCurrentElementSize() >= maxElementSize) {
			throw new IOException("Max element size exceeded");
		}
	}

	@Override
	protected void onStartTag(String prefix, String localpart, Map<String, String> attributes) {
		String namespace = attributes.get("xmlns:" + prefix);
		if ("stream".equals(localpart) && "http://etherx.jabber.org/streams".equals(namespace)) {
			streamNamespace = namespace;
			depth = 0;
		}
	}

	protected void onEndTag(String qName) {
		if ((streamNamespace + ":stream").equals(qName)) {
			completeElementCallback.onCompleteElement("</stream:stream>");
		}
	}
}
