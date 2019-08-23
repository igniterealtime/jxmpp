/**
 *
 * Copyright 2019 Florian Schmaus
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
package org.jxmpp.strings.testframework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jxmpp.stringprep.XmppStringprep;

public class KnownFailures {

	private static final Set<XmppStringprepStringCoupling> VALID_JIDS_WHITELIST = new HashSet<>();
	private static final Set<XmppStringprepStringCoupling> INVALID_JIDS_WHITELIST = new HashSet<>();

	static {
		// ICU4J
		whitelistValidJid(XmppStringPrepper.ICU4J, "fußball@example.com");

		whitelistInvalidJid(XmppStringPrepper.ICU4J, "♚@example.com");
		whitelistInvalidJid(XmppStringPrepper.ICU4J, "henry\u2163@example.com");
		whitelistInvalidJid(XmppStringPrepper.ICU4J, "user@@host/resource");
		whitelistInvalidJid(XmppStringPrepper.ICU4J, "user@@host");
		whitelistInvalidJid(XmppStringPrepper.ICU4J, "user@@");

		// LIBIDN
		whitelistValidJid(XmppStringPrepper.LIBIDN, "fußball@example.com");

		whitelistInvalidJid(XmppStringPrepper.LIBIDN, "♚@example.com");
		whitelistInvalidJid(XmppStringPrepper.LIBIDN, "henry\u2163@example.com");
		whitelistInvalidJid(XmppStringPrepper.LIBIDN, "user@@host/resource");
		whitelistInvalidJid(XmppStringPrepper.LIBIDN, "user@@host");
		whitelistInvalidJid(XmppStringPrepper.LIBIDN, "user@@");

		// ROCKS_XMPP_PRECIS
		whitelistValidJid(XmppStringPrepper.ROCKS_XMPP_PRECIS, "ς@example.com");
		whitelistValidJid(XmppStringPrepper.ROCKS_XMPP_PRECIS, "user@[2001:638:a000:4134::ffff:40]");
		whitelistValidJid(XmppStringPrepper.ROCKS_XMPP_PRECIS, "user@[2001:638:a000:4134::ffff:40%eno1]");
		whitelistValidJid(XmppStringPrepper.ROCKS_XMPP_PRECIS, "user@averylongdomainpartisstillvalideventhoughitexceedsthesixtyfourbytelimitofdnslabels");


		// SIMPLE
		whitelistValidJid(XmppStringPrepper.SIMPLE, "ς@example.com");

		whitelistInvalidJid(XmppStringPrepper.SIMPLE, "♚@example.com");
		whitelistInvalidJid(XmppStringPrepper.SIMPLE, "henry\u2163@example.com");
		whitelistInvalidJid(XmppStringPrepper.SIMPLE, "user@@host/resource");
		whitelistInvalidJid(XmppStringPrepper.SIMPLE, "user@@host");
		whitelistInvalidJid(XmppStringPrepper.SIMPLE, "user@@");
	}

	/**
	 * Check a XMPP Strings Testframeworkresult against the list of known failures (whitelist).
	 *
	 * @param result the result of the XMPP Strings Testframework.
	 * @return the result of whitelist check.
	 */
	public static Result checkAgainstWhitelist(StringsTestframework.Result result) {
		List<ValidJidTestresult.Failed> validJidFailedTestresults = new ArrayList<>(result.validJidFailedTestresults);
		List<InvalidJidTestresult.Failed> invalidJidFailedTestresults = new ArrayList<>(result.invalidJidFailedTestresults);

		Set<XmppStringprepStringCoupling> validJidsWhitelist, invalidJidsWhitelist;
		synchronized (VALID_JIDS_WHITELIST) {
			validJidsWhitelist = new HashSet<>(VALID_JIDS_WHITELIST);
		}
		synchronized (INVALID_JIDS_WHITELIST) {
			invalidJidsWhitelist = new HashSet<>(INVALID_JIDS_WHITELIST);
		}

		{
			Iterator<ValidJidTestresult.Failed> it = validJidFailedTestresults.iterator();
			while (it.hasNext()) {
				ValidJidTestresult.Failed failed = it.next();
				XmppStringprepStringCoupling coupling = new XmppStringprepStringCoupling(
						failed.xmppStringPrepper.xmppStringprepClass, failed.validJid.unnormalizedJid);
				boolean wasWhitelisted = validJidsWhitelist.remove(coupling);
				if (wasWhitelisted) {
					it.remove();
				}
			}
		}

		{
			Iterator<InvalidJidTestresult.Failed> it = invalidJidFailedTestresults.iterator();
			while (it.hasNext()) {
				InvalidJidTestresult.Failed failed = it.next();
				String invalidJid = failed.invalidJid.invalidJid;
				XmppStringprepStringCoupling coupling = new XmppStringprepStringCoupling(
						failed.xmppStringPrepper.xmppStringprepClass, invalidJid);
				boolean wasWhitelisted = invalidJidsWhitelist.remove(coupling);
				if (wasWhitelisted) {
					it.remove();
				}
			}
		}

		return new Result(validJidFailedTestresults, invalidJidFailedTestresults, validJidsWhitelist, invalidJidsWhitelist);
	}

	/**
	 * Whitelist the given valid JID and the used XMPP String prepper.
	 *
	 * @param xmppStringPrepper the used XMPP String prepper.
	 * @param validJid the valid JID.
	 */
	public static void whitelistValidJid(XmppStringPrepper xmppStringPrepper, String validJid) {
		whitelistValidJid(xmppStringPrepper.xmppStringprepClass, validJid);
	}

	/**
	 * Whitelist the given valid JID and the used XMPP String prepper.
	 *
	 * @param xmppStringprepClass the class of the used XMPP String prepper.
	 * @param validJid the valid JID.
	 */
	public static void whitelistValidJid(Class<? extends XmppStringprep> xmppStringprepClass, String validJid) {
		XmppStringprepStringCoupling coupling = new XmppStringprepStringCoupling(xmppStringprepClass, validJid);
		final boolean newCoupling;
		synchronized (VALID_JIDS_WHITELIST) {
			newCoupling = VALID_JIDS_WHITELIST.add(coupling);
		}
		if (!newCoupling) {
			throw new IllegalArgumentException(coupling + " is already whitelisted for valid JIDs");
		}
	}

	/**
	 * Whitelist the given invalid JID and the used XMPP String prepper.
	 *
	 * @param xmppStringPrepper the used XMPP String prepper.
	 * @param invalidJid the invalid JID.
	 */
	public static void whitelistInvalidJid(XmppStringPrepper xmppStringPrepper, String invalidJid) {
		whitelistInvalidJid(xmppStringPrepper.xmppStringprepClass, invalidJid);
	}

	/**
	 * Whitelist the given invalid JID and the used XMPP String prepper.
	 *
	 * @param xmppStringprepClass the class of the used XMPP String prepper.
	 * @param invalidJid the invalid JID.
	 */
	public static void whitelistInvalidJid(Class<? extends XmppStringprep> xmppStringprepClass, String invalidJid) {
		XmppStringprepStringCoupling coupling = new XmppStringprepStringCoupling(xmppStringprepClass, invalidJid);
		final boolean newCoupling;
		synchronized (INVALID_JIDS_WHITELIST) {
			newCoupling = INVALID_JIDS_WHITELIST.add(coupling);
		}
		if (!newCoupling) {
			throw new IllegalArgumentException(coupling + " is already whitelisted for invalid JIDs");
		}
	}

	public static class Result {
		public final boolean noUnknownFailures;
		public final List<ValidJidTestresult.Failed> remainingValidJidFailedTestresults;
		public final List<InvalidJidTestresult.Failed> remainingInvalidJidFailedTestresults;

		public final Set<XmppStringprepStringCoupling> remainingValidJidsWhitelist, remainingInvalidJidsWhitelist;

		private Result(List<ValidJidTestresult.Failed> remainingValidJidFailedTestresults,
				List<InvalidJidTestresult.Failed> remainingInvalidJidFailedTestresults,
				Set<XmppStringprepStringCoupling> remainingValidJidsWhitelist,
				Set<XmppStringprepStringCoupling> remainingInvalidJidsWhitelist) {
			noUnknownFailures = remainingValidJidFailedTestresults.isEmpty()
					&& remainingInvalidJidFailedTestresults.isEmpty() && remainingValidJidsWhitelist.isEmpty()
					&& remainingInvalidJidsWhitelist.isEmpty();

			this.remainingValidJidFailedTestresults = Collections.unmodifiableList(remainingValidJidFailedTestresults);
			this.remainingInvalidJidFailedTestresults = Collections.unmodifiableList(remainingInvalidJidFailedTestresults);
			this.remainingValidJidsWhitelist = Collections.unmodifiableSet(remainingValidJidsWhitelist);
			this.remainingInvalidJidsWhitelist = Collections.unmodifiableSet(remainingInvalidJidsWhitelist);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(
					"XMPP Strings Testframework Known Failure Report\n" +
					"===============================================\n");
			if (!remainingValidJidFailedTestresults.isEmpty()) {
				sb.append(
						"Remaining Valid JID failed results:\n" +
						"-----------------------------------\n"
				);
				for (ValidJidTestresult.Failed failed : remainingValidJidFailedTestresults) {
					sb.append(failed).append('\n');
				}
			}
			if (!remainingValidJidsWhitelist.isEmpty()) {
				sb.append(
						"Remaining Valid JID failed whitelist entries:\n" +
						"---------------------------------------------\n"
				);
				for (XmppStringprepStringCoupling coupling : remainingValidJidsWhitelist) {
					sb.append(coupling).append('\n');
				}
			}
			if (!remainingInvalidJidFailedTestresults.isEmpty()) {
				sb.append(
						"Remaining Invalid JID failed results:\n" +
						"-----------------------------------\n"
				);
				for (InvalidJidTestresult.Failed failed : remainingInvalidJidFailedTestresults) {
					sb.append(failed).append('\n');
				}
			}
			if (!remainingInvalidJidsWhitelist.isEmpty()) {
				sb.append(
						"Remaining Invalid JID failed whitelist entries:\n" +
						"---------------------------------------------\n"
				);
				for (XmppStringprepStringCoupling coupling : remainingInvalidJidsWhitelist) {
					sb.append(coupling).append('\n');
				}
			}
			return sb.toString();
		}
	}

	private static class XmppStringprepStringCoupling {
		public final Class<? extends XmppStringprep> xmppStringPrepClass;
		public final String string;

		private XmppStringprepStringCoupling(Class<? extends XmppStringprep> xmppStringprepClass, String string) {
			this.xmppStringPrepClass = xmppStringprepClass;
			this.string = string;
		}

		@Override
		public int hashCode() {
			int result = 17;
			result = 31 * result + xmppStringPrepClass.hashCode();
			result = 31 * result + string.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}

			if (!(other instanceof XmppStringprepStringCoupling)) {
				return false;
			}

			XmppStringprepStringCoupling lhs = (XmppStringprepStringCoupling) other;
			return xmppStringPrepClass.equals(lhs.xmppStringPrepClass) && string.equals(lhs.string);
		}

		@Override
		public String toString() {
			return xmppStringPrepClass.getName() + " '" + string + "\'";
		}
	}

}
