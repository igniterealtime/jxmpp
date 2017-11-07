/**
 *
 * Copyright © 2014-2017 Florian Schmaus
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

	/**
	 * Cache for the String representation of this JID.
	 */
	protected String cache;

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
	public final <T extends Jid> T downcast(Class<T> jidClass) {
		return jidClass.cast(this);
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
	public final EntityBareJid asEntityBareJidOrThrow() {
		EntityBareJid entityBareJid = asEntityBareJidIfPossible();
		if (entityBareJid == null) throwIse("can not be converted to EntityBareJid");
		return entityBareJid;
	}

	@Override
	public EntityFullJid asEntityFullJidOrThrow() {
		EntityFullJid entityFullJid = asEntityFullJidIfPossible();
		if (entityFullJid == null) throwIse("can not be converted to EntityFullJid");
		return entityFullJid;
	}

	@Override
	public EntityJid asEntityJidOrThrow() {
		EntityJid entityJid = asEntityJidIfPossible();
		if (entityJid == null) throwIse("can not be converted to EntityJid");
		return entityJid;
	}

	@Override
	public EntityFullJid asFullJidOrThrow() {
		EntityFullJid entityFullJid = asEntityFullJidIfPossible();
		if (entityFullJid == null) throwIse("can not be converted to EntityBareJid");
		return entityFullJid;
	}

	@Override
	public DomainFullJid asDomainFullJidOrThrow() {
		DomainFullJid domainFullJid = asDomainFullJidIfPossible();
		if (domainFullJid == null) throwIse("can not be converted to DomainFullJid");
		return domainFullJid;
	}

	@Override
	public abstract Resourcepart getResourceOrNull();

	@Override
	public final Resourcepart getResourceOrEmpty() {
		Resourcepart resourcepart = getResourceOrNull();
		if (resourcepart == null) return Resourcepart.EMPTY;
		return resourcepart;
	}

	@Override
	public final Resourcepart getResourceOrThrow() {
		Resourcepart resourcepart = getResourceOrNull();
		if (resourcepart == null) throwIse("has no resourcepart");
		return resourcepart;
	}

	@Override
	public abstract Localpart getLocalpartOrNull();

	@Override
	public final Localpart getLocalpartOrThrow() {
		Localpart localpart = getLocalpartOrNull();
		if (localpart == null) throwIse("has no localpart");
		return localpart;
	}

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

	@SuppressWarnings("NonOverridingEquals")
	@Override
	public final boolean equals(CharSequence charSequence) {
		if (charSequence == null) {
			return false;
		}
		return equals(charSequence.toString());
	}

	@SuppressWarnings("NonOverridingEquals")
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
			cache = internalizedCache = toString().intern();
		}
		return internalizedCache;
	}

	private void throwIse(String message) {
		String exceptionMessage = "The JID '" + this + "' " + message;
		throw new IllegalStateException(exceptionMessage);
	}

	static <O extends Object> O requireNonNull(O object, String message) {
		if (object != null) {
			return object;
		}
		throw new IllegalArgumentException(message);
	}
}
