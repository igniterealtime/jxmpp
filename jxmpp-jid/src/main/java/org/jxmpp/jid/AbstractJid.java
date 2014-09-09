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

	public final boolean isBareOrFullJid() {
		return isBareJid() || isFullJid();
	}

	/**
	 * Check if this is an instance of {@link BareJid}.
	 * 
	 * @return true if this is an instance of BareJid
	 */
	public final boolean isBareJid() {
		return this instanceof BareJid;
	}

	/**
	 * Check if this is an instance of {@link FullJid}.
	 * 
	 * @return true if this is an instance of FullJid
	 */
	public final boolean isFullJid() {
		return this instanceof FullJid;
	}

	/**
	 * Check if this is an instance of {@link DomainBareJid}.
	 *  
	 * @return true if this is an instance of DomainBareJid
	 */
	public final boolean isDomainBareJid() {
		return this instanceof DomainBareJid;
	}

	/**
	 * Check if this is an instance of {@link DomainFullJid}.
	 * 
	 * @return true if this is an instance of DomainFullJid
	 */
	public final boolean isDomainFullJid() {
		return this instanceof DomainFullJid;
	}

	public final boolean hasNoResource() {
		return this instanceof BareJid || this instanceof DomainBareJid;
	}

	public final boolean hasResource() {
		return this instanceof FullJid || this instanceof DomainFullJid;
	}

	public final boolean hasLocalpart() {
		return this instanceof BareJid || this instanceof FullJid;
	}

	/**
	 * Return the downcasted instance of this Jid. This method is unsafe, make sure to check that this is actually of the type of are casting to.
	 * 
	 * @return the downcasted instanced of this
	 */
	@SuppressWarnings("unchecked")
	public final <T extends AbstractJid> T downcast() {
		return (T) this;
	}
}
