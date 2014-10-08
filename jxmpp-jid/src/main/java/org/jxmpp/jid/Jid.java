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

/**
 * A XMPP JID.
 * <p>
 * This is the super interface for all JID types. Every JID consists at least of
 * a domainpart. You can retrieve the escaped String with {@link #toString()}
 * or the unsecaped String of the JID with {@link #asUnescapedString()}.
 * </p>
 */
public interface Jid extends Comparable<Jid>, CharSequence {

	public String getDomain();

	/**
	 * Returns the escaped String representation of this JID.
	 *
	 * @return the escaped String representation of this JID.
	 */
	public String toString();

	public String asUnescapedString();

	public boolean isBareOrFullJid();

	/**
	 * Check if this is an instance of {@link BareJid}.
	 * 
	 * @return true if this is an instance of BareJid
	 */
	public boolean isBareJid();

	/**
	 * Check if this is an instance of {@link FullJid}.
	 * 
	 * @return true if this is an instance of FullJid
	 */
	public boolean isFullJid();

	/**
	 * Check if this is an instance of {@link DomainBareJid}.
	 *  
	 * @return true if this is an instance of DomainBareJid
	 */
	public boolean isDomainBareJid();

	/**
	 * Check if this is an instance of {@link DomainFullJid}.
	 * 
	 * @return true if this is an instance of DomainFullJid
	 */
	public boolean isDomainFullJid();

	/**
	 * Check if this is an instance of {@link BareJid} or {@link DomainBareJid}.
	 * 
	 * @return true if this is an instance of BareJid or DomainBareJid
	 */
	public boolean hasNoResource();

	public boolean hasResource();

	public boolean hasLocalpart();

	/**
	 * Convert this Jid to a BareJid if possible.
	 *
	 * @return the corresponding BareJid or null.
	 */
	public BareJid asBareJidIfPossible();

	/**
	 * Convert this Jid to a FullJid if possible.
	 *
	 * @return the corresponding FullJid or null.
	 */
	public FullJid asFullJidIfPossible();

	/**
	 * Convert this Jid to a DomainBareJid if possible.
	 *
	 * @return the corresponding DomainBareJid or null.
	 */
	public DomainBareJid asDomainBareJidIfPossible();

	/**
	 * Convert this Jid to a DomainFullJid if possible.
	 *
	 * @return the corresponding DomainFullJid or null.
	 */
	public DomainFullJid asDomainFullJidIfPossible();

	/**
	 * Return the downcasted instance of this Jid. This method is unsafe, make sure to check that this is actually of the type of are casting to.
	 * 
	 * @return the downcasted instanced of this
	 */
	public <T extends AbstractJid> T downcast();

}
