/**
 *
 * Copyright © 2014 Florian Schmaus
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
package org.jxmpp.stringprep;

/**
 * Interface for commonly used Stringprep operations used in XMPP.
 * <p>
 * The relevant RFCs are:
 * <ul>
 * <li><a href="http://tools.ietf.org/html/rfc6122">RFC 6122</a></li>
 * <li><a href="https://tools.ietf.org/html/draft-ietf-xmpp-6122bis-14">draft-ietf-xmpp-6122bis-14</a></li>
 * </ul>
 *
 */
public interface XmppStringprep {

	/**
	 * Performs String preparation on the localpart String of a JID. In RFC 6122 terms this means applying the
	 * <i>nodeprep</i> profile of Stringprep.
	 * 
	 * @param string
	 * @return the prepared String.
	 * @throws XmppStringprepException
	 */
	public String localprep(String string) throws XmppStringprepException;

	/**
	 * Performs String preparation on the domainpart String of a JID. In RFC 61ss terms, this means applying the
	 * <i>nameprep</i> profile of Stringprep.
	 * 
	 * @param string
	 * @return the prepared String.
	 * @throws XmppStringprepException
	 */
	public String domainprep(String string) throws XmppStringprepException;

	/**
	 * Performs String preparation on the resourcepart String of a JID. In RFC 6122 terms this means applying the <i>resourceprep</i> profile of Stringprep.
	 * @param string
	 * @return the prepared String.
	 * @throws XmppStringprepException
	 */
	public String resourceprep(String string) throws XmppStringprepException;
}
