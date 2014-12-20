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
package org.jxmpp.jid.impl;

import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.DomainFullJid;
import org.jxmpp.jid.FullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.JidWithLocalpart;
import org.jxmpp.jid.JidWithResource;

public abstract class AbstractJid implements Jid {

	@Override
	public final boolean isBareOrFullJid() {
		return isBareJid() || isFullJid();
	}

	@Override
	public final boolean isBareJid() {
		return this instanceof BareJid;
	}

	@Override
	public final boolean isFullJid() {
		return this instanceof FullJid;
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
		return this instanceof JidWithResource;
	}

	@Override
	public final boolean hasLocalpart() {
		return this instanceof JidWithLocalpart;
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
	public String getResourceOrNull() {
		return null;
	}

	@Override
	public String getLocalpartOrNull() {
		return null;
	}

	@Override
	public final boolean isParentOf(Jid jid) {
		FullJid fullJid = jid.asFullJidIfPossible();
		if (fullJid != null) {
			return isParentOf(fullJid);
		}
		BareJid bareJid = jid.asBareJidIfPossible();
		if (bareJid != null) {
			return isParentOf(bareJid);
		}
		DomainFullJid domainFullJid = jid.asDomainFullJidIfPossible();
		if (domainFullJid != null) {
			return isParentOf(domainFullJid);
		}
		DomainBareJid domainBareJid = jid.asDomainBareJidIfPossible();
		if (domainBareJid != null) {
			return isParentOf(domainBareJid);
		}
		throw new AssertionError("Unkown JID class: " + jid.getClass().getName());
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
}
