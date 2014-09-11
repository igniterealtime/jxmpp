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
package org.jxmpp.jid;

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
		return this instanceof FullJid || this instanceof DomainFullJid;
	}

	@Override
	public final boolean hasLocalpart() {
		return this instanceof BareJid || this instanceof FullJid;
	}

	@Override
	@SuppressWarnings("unchecked")
	public final <T extends AbstractJid> T downcast() {
		return (T) this;
	}
}
