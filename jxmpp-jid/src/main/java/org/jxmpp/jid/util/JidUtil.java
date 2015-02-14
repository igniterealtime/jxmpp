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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

		BareJid bareJid = jid.asFullJidIfPossible();
		if (bareJid != null) {
			return bareJid;
		}

		return jid.asDomainBareJid();
	}

	/**
	 * Check if the given CharSequence represents a valid bare JID.
	 * <p>
	 * This method is meant to validate user input and give fast feedback (e.g.
	 * with a red or green light) about if the user entered CharSequence represents a
	 * bare JID.
	 * </p>
	 * 
	 * @param jid
	 * @return true if @{code jid} represents a valid bare JID, false otherwise
	 */
	public static boolean isValidBareJid(CharSequence jid) {
		try {
			validateBareJid(jid);
		} catch (NotABareJidStringException | XmppStringprepException e) {
			return false;
		}
		return true;
	}

	/**
	 * Check if the given CharSequence is a valid bare JID.
	 * <p>
	 * This is a convenience method meant to validate user entered bare JIDs. If
	 * the given {@code jid} is not a valid bare JID, then this method will
	 * throw either {@link NotABareJidStringException} or
	 * {@link XmppStringprepException}. The NotABareJidStringException will
	 * contain a meaningful message explaining why the given CharSequence is not a
	 * valid bare JID (e.g. "does not contain a '@' character").
	 * </p>
	 * 
	 * @param jidcs the JID CharSequence
	 * @return a BareJid instance representing the given JID CharSequence
	 * @throws NotABareJidStringException
	 * @throws XmppStringprepException
	 */
	public static BareJid validateBareJid(CharSequence jidcs) throws NotABareJidStringException, XmppStringprepException {
		String jid = jidcs.toString();
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

	public static void filterBareJid(Collection<? extends Jid> in, Collection<BareJid> out) {
		for (Jid jid : in) {
			BareJid bareJid = jid.asBareJidIfPossible();
			if (bareJid != null) {
				out.add(bareJid);
			}
		}
	}

	public static Set<BareJid> filterBareJidSet(Collection<? extends Jid> input) {
		Set<BareJid> res = new HashSet<BareJid>(input.size());
		filterBareJid(input, res);
		return res;
	}

	public static List<BareJid> filterBareJidList(Collection<? extends Jid> input) {
		List<BareJid> res = new ArrayList<BareJid>(input.size());
		filterBareJid(input, res);
		return res;
	}

	public static void filterFullJid(Collection<? extends Jid> in, Collection<FullJid> out) {
		for (Jid jid : in) {
			FullJid fullJid = jid.asFullJidIfPossible();
			if (fullJid != null) {
				out.add(fullJid);
			}
		}
	}

	public static Set<FullJid> filterFullJidSet(Collection<? extends Jid> input) {
		Set<FullJid> res = new HashSet<FullJid>(input.size());
		filterFullJid(input, res);
		return res;
	}

	public static List<FullJid> filterFullJidList(Collection<? extends Jid> input) {
		List<FullJid> res = new ArrayList<FullJid>(input.size());
		filterFullJid(input, res);
		return res;
	}

	public static void filterDomainFullJid(Collection<? extends Jid> in, Collection<DomainFullJid> out) {
		for (Jid jid : in) {
			DomainFullJid domainFullJid = jid.asDomainFullJidIfPossible();
			if (domainFullJid != null) {
				out.add(domainFullJid);
			}
		}
	}

	public static Set<DomainFullJid> filterDomainFullJidSet(Collection<? extends Jid> input) {
		Set<DomainFullJid> res = new HashSet<DomainFullJid>(input.size());
		filterDomainFullJid(input, res);
		return res;
	}

	public static List<DomainFullJid> filterDomainFullJidList(Collection<? extends Jid> input) {
		List<DomainFullJid> res = new ArrayList<DomainFullJid>(input.size());
		filterDomainFullJid(input, res);
		return res;
	}

	public static Set<BareJid> bareJidSetFrom(Collection<CharSequence> jidStrings) {
		Set<BareJid> res = new HashSet<BareJid>(jidStrings.size());
		bareJidsFrom(jidStrings, res, null);
		return res;
	}

	/**
	 * Convert a collection of Strings to a Set of {@link BareJid}'s.
	 * <p>
	 * If the optional argument <code>exceptions</code> is given, then all {@link XmppStringprepException} thrown while
	 * converting will be added to the list. Otherwise, if an XmppStringprepExceptions is thrown, it will be wrapped in
	 * a AssertionError Exception and throw.
	 * </p>
	 * 
	 * @param jidStrings
	 *            the strings that are going to get converted
	 * @param output
	 *            the collection where the BareJid's will be added to
	 * @param exceptions
	 */
	public static void bareJidsFrom(Collection<CharSequence> jidStrings, Collection<BareJid> output,
			List<XmppStringprepException> exceptions) {
		for (CharSequence jid : jidStrings) {
			try {
				BareJid bareJid = JidCreate.bareFrom(jid);
				output.add(bareJid);
			} catch (XmppStringprepException e) {
				if (exceptions != null) {
					exceptions.add(e);
				} else {
					throw new AssertionError(e);
				}
			}
		}
	}

	public static Set<Jid> jidSetFrom(Collection<CharSequence> jidStrings) {
		Set<Jid> res = new HashSet<Jid>(jidStrings.size());
		jidsFrom(jidStrings, res, null);
		return res;
	}

	/**
	 * Convert a collection of Strings to a Set of {@link Jid}'s.
	 * <p>
	 * If the optional argument <code>exceptions</code> is given, then all {@link XmppStringprepException} thrown while
	 * converting will be added to the list. Otherwise, if an XmppStringprepExceptions is thrown, it will be wrapped in
	 * a AssertionError Exception and throw.
	 * </p>
	 * 
	 * @param jidStrings
	 *            the strings that are going to get converted
	 * @param output
	 *            the collection where the Jid's will be added to
	 * @param exceptions
	 */
	public static void jidsFrom(Collection<CharSequence> jidStrings, Collection<Jid> output,
			List<XmppStringprepException> exceptions) {
		for (CharSequence jidString : jidStrings) {
			try {
				Jid jid = JidCreate.from(jidString);
				output.add(jid);
			} catch (XmppStringprepException e) {
				if (exceptions != null) {
					exceptions.add(e);
				} else {
					throw new AssertionError(e);
				}
			}
		}
	}

	public static List<String> toStringList(Collection<? extends Jid> jids) {
		List<String> res = new ArrayList<String>(jids.size());
		toStrings(jids, res);
		return res;
	}

	public static Set<String> toStringSet(Collection<? extends Jid> jids) {
		Set<String> res = new HashSet<String>(jids.size());
		toStrings(jids, res);
		return res;
	}

	public static void toStrings(Collection<? extends Jid> jids, Collection<String> jidStrings) {
		for (Jid jid : jids) {
			jidStrings.add(jid.toString());
		}
	}
}
