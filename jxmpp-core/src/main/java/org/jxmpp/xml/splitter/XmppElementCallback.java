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

import java.util.Map;

/**
 * Callback for {@link XmlSplitter}.
 *
 */
public interface XmppElementCallback extends CompleteElementCallback {

	/**
	 * Invoked if a new XMPP 'stream' open tag has been read.
	 *
	 * @param prefix the prefix of the 'stream' element.
	 * @param attributes the attributes of the 'stream' element.
	 */
	public void streamOpened(String prefix, Map<String, String> attributes);

	/**
	 * Invoked when a XMPP 'stream' close tag has been read.
	 */
	public void streamClosed();

}
