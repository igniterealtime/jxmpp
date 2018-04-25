/**
 *
 * Copyright © 2014-2018 Florian Schmaus
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
package org.jxmpp.jid.parts;

import org.jxmpp.stringprep.XmppStringPrepUtil;
import org.jxmpp.stringprep.XmppStringprepException;

/**
 * A <i>resourcepart</i> of an XMPP address (JID).
 * <p>
 * You can create instances of this class from Strings using {@link #from(String)}.
 * </p>
 *
 * @see <a href="http://xmpp.org/rfcs/rfc6122.html#addressing-resource">RFC 6122 § 2.4. Resourcepart</a>
 */
public class Resourcepart extends Part {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The empty resource part.
	 * <p>
	 * This empty resource part is the part that is represented by the empty String. This is useful in cases where you
	 * have a collection of Resourceparts that does not allow <code>null</code> values, but you want to deal with the
	 * "no resource" case.
	 * </p>
	 */
	public static final Resourcepart EMPTY = new Resourcepart("");

	private Resourcepart(String resource) {
		super(resource);
	}

	/**
	 * Get a {@link Resourcepart} from a given {@link CharSequence} or {@code null} if the input is not a valid resourcepart.
	 *
	 * @param cs the input CharSequence
	 * @return a Resourcepart or {@code null}
	 */
	public static Resourcepart fromOrNull(CharSequence cs) {
		try {
			return from(cs.toString());
		} catch (XmppStringprepException e) {
			return null;
		}
	}

	/**
	 * Like {@link #from(String)} but does throw an unchecked {@link IllegalArgumentException} instead of a
	 * {@link XmppStringprepException}.
	 *
	 * @param cs the character sequence which should be transformed to a {@link Resourcepart}
	 * @return the {@link Resourcepart} if no exception occurs
	 * @throws IllegalArgumentException if the given input is not a valid {@link Resourcepart}
	 * @see #from(String)
	 * @since 0.6.2
	 */
	public static Resourcepart fromOrThrowUnchecked(CharSequence cs) {
		try {
			return from(cs.toString());
		} catch (XmppStringprepException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Get the {@link Resourcepart} representing the input String.
	 *
	 * @param resource the input String.
	 * @return the resource part.
	 * @throws XmppStringprepException if an error occurs.
	 */
	public static Resourcepart from(String resource) throws XmppStringprepException {
		resource = XmppStringPrepUtil.resourceprep(resource);
		// First prep the String, then assure the limits of the *result*
		assertNotLongerThan1023BytesOrEmpty(resource);
		return new Resourcepart(resource);
	}
}
