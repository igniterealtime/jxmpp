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
package org.jxmpp.jid.impl;

import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.DomainFullJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.FullJid;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;

public abstract class AbstractJid implements Jid {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public final boolean isEntityJid() {
		return isEntityBareJid() || isEntityFullJid();
	}

	@Override
	public final boolean isEntityBareJid() {
		return this instanceof EntityBareJid;
	}

	@Override
	public final boolean isEntityFullJid() {
		return this instanceof EntityFullJid;
	}

	@Override
	public final boolean isDomainBareJid() {
		return this instanceof DomainBareJid;
	}

	@Override
	public final boolean isDomainFullJid() {
		return this instanceof DomainFullJid;
	}

	@Override
	public abstract boolean hasNoResource();

	@Override
	public final boolean hasResource() {
		return this instanceof FullJid;
	}

	@Override
	public final boolean hasLocalpart() {
		return this instanceof EntityJid;
	}

	@Override
	@SuppressWarnings("unchecked")
	public final <T extends Jid> T downcast() {
		return (T) this;
	}

	@Override
	public int length() {
		return toString().length();
	}

	@Override
	public char charAt(int index) {
		return toString().charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return toString().subSequence(start, end);
	}

	@Override
	public abstract Resourcepart getResourceOrNull();

	@Override
	public abstract Localpart getLocalpartOrNull();

	@Override
	public final boolean isParentOf(Jid jid) {
		EntityFullJid fullJid = jid.asEntityFullJidIfPossible();
		if (fullJid != null) {
			return isParentOf(fullJid);
		}
		EntityBareJid bareJid = jid.asEntityBareJidIfPossible();
		if (bareJid != null) {
			return isParentOf(bareJid);
		}
		DomainFullJid domainFullJid = jid.asDomainFullJidIfPossible();
		if (domainFullJid != null) {
			return isParentOf(domainFullJid);
		}

		return isParentOf(jid.asDomainBareJid());
	}

	@Override
	public final int hashCode() {
		return toString().hashCode();
	}

	@Override
	public final boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (other instanceof CharSequence) {
			return equals((CharSequence) other);
		}
		return false;
	}

	@Override
	public final boolean equals(CharSequence charSequence) {
		return equals(charSequence.toString());
	}

	@Override
	public final boolean equals(String string) {
		return toString().equals(string);
	}

	@Override
	public final int compareTo(Jid  other) {
		String otherString = other.toString();
		String myString = toString();
		return myString.compareTo(otherString);
	}

	/**
	 * The cache holding the internalized value of this part. This needs to be transient so that the
	 * cache is recreated once the data was de-serialized.
	 */
	private transient String internalizedCache;

	@Override
	public final String intern() {
		if (internalizedCache == null) {
			internalizedCache = toString().intern();
		}
		return internalizedCache;
	}
}
