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
package org.jxmpp.jid.util;

import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.DomainFullJid;
import org.jxmpp.jid.FullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.XmppStringUtils;

public class JidUtil {

	/**
	 * Safely transform a JID to any JID without a resource.
	 * <p>
	 * Useful for situations where you don't want to put a resource on the wire.
	 * </p>
	 * @param jid
	 * @return the equivalent JID without resource
	 */
	public static Jid withoutResource(Jid jid) {
		if (jid.hasNoResource()) {
			return jid;
		}

		if (jid.isFullJid()) {
			FullJid fullJid = (FullJid) jid;
			return fullJid.asBareJid();
		} else if (jid.isDomainFullJid()) {
			DomainFullJid domainFullJid = (DomainFullJid) jid;
			return domainFullJid.asDomainBareJid();
		} else {
			throw new AssertionError(
					"Given JID has a resource but is neither a FullJid or a DomainFullJid");
		}
	}

	/**
	 * Check if the given string represents a valid bare JID.
	 * <p>
	 * This method is meant to validate user input and give fast feedback (e.g.
	 * with a red or green light) about if the user entered String represents a
	 * bare JID.
	 * </p>
	 * 
	 * @param jid
	 * @return true if @{code jid} represents a valid bare JID, false otherwise
	 */
	public static boolean isValidBareJid(String jid) {
		try {
			validateBareJid(jid);
		} catch (NotABareJidStringException | XmppStringprepException e) {
			return false;
		}
		return true;
	}

	/**
	 * Check if the given string is a valid bare JID.
	 * <p>
	 * This is a convenience method meant to validate user entered bare JIDs. If
	 * the given {@code jid} is not a valid bare JID, then this method will
	 * throw either {@link NotABareJidStringException} or
	 * {@link XmppStringprepException}. The NotABareJidStringException will
	 * contain a meaningful message explaining why the given string is not a
	 * valid bare JID (e.g. "does not contain a '@' character").
	 * </p>
	 * 
	 * @param jid the JID string
	 * @return a BareJid instance representing the given JID string
	 * @throws NotABareJidStringException
	 * @throws XmppStringprepException
	 */
	public static BareJid validateBareJid(String jid) throws NotABareJidStringException, XmppStringprepException {
		final int atIndex = jid.indexOf('@');
		if (atIndex == -1) {
			throw new NotABareJidStringException("'" + jid + "' does not contain a '@' character");
		} else if (jid.indexOf('@', atIndex + 1) != -1) {
			throw new NotABareJidStringException("'" + jid + "' contains multiple '@' characters");
		}
		final String localpart = XmppStringUtils.parseLocalpart(jid);
		if (localpart.length() == 0) {
			throw new NotABareJidStringException("'" + jid + "' has empty localpart");
		}
		final String domainpart = XmppStringUtils.parseDomain(jid);
		if (domainpart.length() == 0) {
			throw new NotABareJidStringException("'" + jid + "' has empty domainpart");
		}
		return JidCreate.bareFrom(jid);
	}

	public static class NotABareJidStringException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1710386661031655082L;

		public NotABareJidStringException(String message) {
			super(message);
		}
	}
}
