/**
 *
 * Copyright © 2014-2015 Florian Schmaus
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.DomainFullJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.XmppStringUtils;

public class JidUtil {

	/**
	 * Check if the given CharSequence represents a valid bare JID.
	 * <p>
	 * This method is meant to validate user input and give fast feedback (e.g.
	 * with a red or green light) about if the user entered CharSequence represents a
	 * bare JID.
	 * </p>
	 * 
	 * @param jid the CharSequence to check.
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
	 * @throws NotABareJidStringException if the given CharSequence is not a bare JID.
	 * @throws XmppStringprepException if an error happens.
	 */
	public static EntityBareJid validateBareJid(CharSequence jidcs) throws NotABareJidStringException, XmppStringprepException {
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

		/**
		 * Construct a new "not a bare JID" exception.
		 *
		 * @param message the message of the exception.
		 */
		public NotABareJidStringException(String message) {
			super(message);
		}
	}

	/**
	 * Filter all bare JIDs.
	 *
	 * @param in the input collection.
	 * @param out the collection where the filtered JIDs are added to.
	 */
	public static void filterEntityBareJid(Collection<? extends Jid> in, Collection<EntityBareJid> out) {
		for (Jid jid : in) {
			EntityBareJid bareJid = jid.asEntityBareJidIfPossible();
			if (bareJid != null) {
				out.add(bareJid);
			}
		}
	}

	/**
	 * Filter all bare JIDs.
	 *
	 * @param input the input collection.
	 * @return a set containing all bare JIDs of the input collection.
	 */
	public static Set<EntityBareJid> filterEntityBareJidSet(Collection<? extends Jid> input) {
		Set<EntityBareJid> res = new HashSet<EntityBareJid>(input.size());
		filterEntityBareJid(input, res);
		return res;
	}

	/**
	 * Filter all bare JIDs.
	 *
	 * @param input the input collection.
	 * @return a list containing all bare JIDs of the input collection.
	 */
	public static List<EntityBareJid> filterEntityBareJidList(Collection<? extends Jid> input) {
		List<EntityBareJid> res = new ArrayList<EntityBareJid>(input.size());
		filterEntityBareJid(input, res);
		return res;
	}

	/**
	 * Filter all full JIDs.
	 *
	 * @param in the input collection.
	 * @param out the collection where the filtered JIDs are added to.
	 */
	public static void filterEntityFullJid(Collection<? extends Jid> in, Collection<EntityFullJid> out) {
		for (Jid jid : in) {
			EntityFullJid fullJid = jid.asEntityFullJidIfPossible();
			if (fullJid != null) {
				out.add(fullJid);
			}
		}
	}

	/**
	 * Filter all full JIDs.
	 *
	 * @param input the input collection.
	 * @return a set containing all full JIDs of the input collection.
	 */
	public static Set<EntityFullJid> filterEntityFullJidSet(Collection<? extends Jid> input) {
		Set<EntityFullJid> res = new HashSet<EntityFullJid>(input.size());
		filterEntityFullJid(input, res);
		return res;
	}

	/**
	 * Filter all full JIDs.
	 *
	 * @param input the input collection.
	 * @return a list containing all full JIDs of the input collection.
	 */
	public static List<EntityFullJid> filterEntityFullJidList(Collection<? extends Jid> input) {
		List<EntityFullJid> res = new ArrayList<EntityFullJid>(input.size());
		filterEntityFullJid(input, res);
		return res;
	}

	/**
	 * Filter all domain full JIDs.
	 *
	 * @param in the input collection.
	 * @param out the collection where the filtered JIDs are added to.
	 */
	public static void filterDomainFullJid(Collection<? extends Jid> in, Collection<DomainFullJid> out) {
		for (Jid jid : in) {
			DomainFullJid domainFullJid = jid.asDomainFullJidIfPossible();
			if (domainFullJid != null) {
				out.add(domainFullJid);
			}
		}
	}

	/**
	 * Filter all domain full JIDs.
	 *
	 * @param input the input collection.
	 * @return a set containing all domain full JIDs of the input collection.
	 */
	public static Set<DomainFullJid> filterDomainFullJidSet(Collection<? extends Jid> input) {
		Set<DomainFullJid> res = new HashSet<DomainFullJid>(input.size());
		filterDomainFullJid(input, res);
		return res;
	}

	/**
	 * Filter all domain full JIDs.
	 *
	 * @param input the input collection.
	 * @return a list containing all domain full JIDs of the input collection.
	 */
	public static List<DomainFullJid> filterDomainFullJidList(Collection<? extends Jid> input) {
		List<DomainFullJid> res = new ArrayList<DomainFullJid>(input.size());
		filterDomainFullJid(input, res);
		return res;
	}

	/**
	 * Convert the given collection of CharSequences to bare JIDs.
	 *
	 * @param jidStrings the collection of CharSequences.
	 * @return a set of bare JIDs.
	 */
	public static Set<EntityBareJid> entityBareJidSetFrom(Collection<? extends CharSequence> jidStrings) {
		Set<EntityBareJid> res = new HashSet<EntityBareJid>(jidStrings.size());
		entityBareJidsFrom(jidStrings, res, null);
		return res;
	}

	/**
	 * Convert a collection of Strings to a Set of {@link EntityBareJid}'s.
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
	 * @param exceptions the list of exceptions thrown while converting.
	 */
	public static void entityBareJidsFrom(Collection<? extends CharSequence> jidStrings, Collection<EntityBareJid> output,
			List<XmppStringprepException> exceptions) {
		for (CharSequence jid : jidStrings) {
			try {
				EntityBareJid bareJid = JidCreate.bareFrom(jid);
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

	/**
	 * Convert the given array of Strings to JIDs.
	 * <p>
	 * Note that errors while converting the Strings will be silently ignored.
	 * </p>
	 * 
	 * @param jids a array of JID Strings.
	 * @return a set of JIDs.
	 */
	public static Set<Jid> jidSetFrom(String[] jids) {
		return jidSetFrom(Arrays.asList(jids));
	}

	/**
	 * Convert the given collection of CharSequences to JIDs.
	 *
	 * @param jidStrings the collection of CharSequences.
	 * @return a set of JIDs.
	 */
	public static Set<Jid> jidSetFrom(Collection<? extends CharSequence> jidStrings) {
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
	 * @param exceptions the list of exceptions thrown while converting.
	 */
	public static void jidsFrom(Collection<? extends CharSequence> jidStrings, Collection<Jid> output,
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

	/**
	 * Convert a collection of JIDs to a list of Strings representing those JIDs.
	 *
	 * @param jids a collection of JIDs.
	 * @return a list of Strings.
	 */
	public static List<String> toStringList(Collection<? extends Jid> jids) {
		List<String> res = new ArrayList<String>(jids.size());
		toStrings(jids, res);
		return res;
	}

	/**
	 * convert a collection of JIDs to a set of Strings representing those JIDs.
	 *
	 * @param jids a collection of JIDs.
	 * @return a set of String.
	 */
	public static Set<String> toStringSet(Collection<? extends Jid> jids) {
		Set<String> res = new HashSet<String>(jids.size());
		toStrings(jids, res);
		return res;
	}

	/**
	 * Convert a collection of JIDs to a Collection of Strings.
	 *
	 * @param jids the collection of Strings to convert.
	 * @param jidStrings the collection of Strings to append to.
	 */
	public static void toStrings(Collection<? extends Jid> jids, Collection<? super String> jidStrings) {
		for (Jid jid : jids) {
			jidStrings.add(jid.toString());
		}
	}
}
