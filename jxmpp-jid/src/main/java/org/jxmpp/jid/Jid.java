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

import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;

/**
 * A XMPP JID.
 * <p>
 * This is the super interface for all JID types. Every JID consists at least of
 * a domainpart. You can retrieve the escaped String with {@link #toString()}
 * or the unsecaped String of the JID with {@link #asUnescapedString()}.
 * </p>
 */
public interface Jid extends Comparable<Jid>, CharSequence {

	public Domainpart getDomain();

	public Localpart maybeGetLocalpart();

	public Resourcepart maybeGetResourcepart();

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
	 * @deprecated use {@link #asDomainBareJid()} instead.
	 */
	@Deprecated
	public DomainBareJid asDomainBareJidIfPossible();

	/**
	 * Convert this Jid to a DomainBareJid if possible.
	 *
	 * @return the corresponding DomainBareJid or null.
	 */
	public DomainBareJid asDomainBareJid();

	public String asDomainBareJidString();

	/**
	 * Convert this Jid to a DomainFullJid if possible.
	 *
	 * @return the corresponding DomainFullJid or null.
	 */
	public DomainFullJid asDomainFullJidIfPossible();

	/**
	 * Get the resourcepart of this JID or null.
	 * <p>
	 * If the JID is of form <localpart@domain.example/resource> then this method returns 'resource'. If the JID no
	 * resourcepart, then <code>null</code> is returned.
	 * </p>
	 * 
	 * @return the resource of this JID or null.
	 */
	public Resourcepart getResourceOrNull();

	/**
	 * Get the localpart of this JID or null.
	 * <p>
	 * If the JID is of form <localpart@domain.example> then this method returns 'localpart'. If the JID has no
	 * localpart, then <code>null</code> is returned.
	 * </p>
	 * 
	 * @return the localpart of this JID or null.
	 */
	public Localpart getLocalpartOrNull();

	/**
	 * 
	 * @return this Jid without a Resourcepart.
	 */
	public Jid withoutResource();

	/**
	 * Check if this JID is the parent of another JID. The <b>parent of</b> relation is defined, under the
	 * precondition that the JID parts (localpart, domainpart and resourcepart) are equal, as follows:
	 * <p>
	 * <pre>
	 * | this JID (parentOf) | other JID           | result |
	 * |---------------------+---------------------+--------|
	 * | dom.example         | dom.example         | true   |
	 * | dom.example         | dom.example/res     | true   |
	 * | dom.example         | loc@dom.example     | true   |
	 * | dom.example         | loc@dom.example/res | true   |
	 * | dom.example/res     | dom.exmple          | false  |
	 * | dom.example/res     | dom.example/res     | true   |
	 * | dom.example/res     | loc@dom.example     | false  |
	 * | dom.example/res     | loc@dom.example/res | false  |
	 * | loc@dom.example     | dom.example         | false  |
	 * | loc@dom.example     | dom.example/res     | false  |
	 * | loc@dom.example     | loc@dom.example     | true   |
	 * | loc@dom.example     | loc@dom.example/res | true   |
	 * | loc@dom.example/res | dom.example         | false  |
	 * | loc@dom.example/res | dom.example/res     | false  |
	 * | loc@dom.example/res | loc@dom.example     | false  |
	 * | loc@dom.example/res | loc@dom.example/res | true   |
	 * </pre>
	 * </p>
	 * 
	 * @param jid
	 *            the other JID to compare with
	 * @return true if this JID is a parent of the given JID.
	 */
	public boolean isParentOf(Jid jid);

	/**
	 * See {@link #isParentOf(Jid)}.
	 *
	 * @param bareJid
	 * @return true if this JID is a parent of the given JID.
	 */
	public boolean isParentOf(BareJid bareJid);

	/**
	 * See {@link #isParentOf(Jid)}.
	 *
	 * @param fullJid
	 * @return true if this JID is a parent of the given JID.
	 */
	public boolean isParentOf(FullJid fullJid);

	/**
	 * See {@link #isParentOf(Jid)}.
	 *
	 * @param domainBareJid
	 * @return true if this JID is a parent of the given JID.
	 */
	public boolean isParentOf(DomainBareJid domainBareJid);

	/**
	 * See {@link #isParentOf(Jid)}.
	 *
	 * @param domainFullJid
	 * @return true if this JID is a parent of the given JID.
	 */
	public boolean isParentOf(DomainFullJid domainFullJid);

	/**
	 * Return the downcasted instance of this Jid. This method is unsafe, make sure to check that this is actually of the type of are casting to.
	 * 
	 * @return the downcasted instanced of this
	 */
	public <T extends Jid> T downcast();

	/**
	 * Compares the given CharSequence with this JID. Returns true if {@code equals(charSequence.toString()} would
	 * return true.
	 *
	 * @param charSequence the CharSequence to compare this JID with.
	 * @return true if if {@code equals(charSequence.toString()} would return true.
	 * @see #equals(String)
	 */
	public boolean equals(CharSequence charSequence);

	/**
	 * Compares the given String wit this JID.
	 * <p>
	 * Returns true if {@code toString().equals(string)}, that is if the String representation of this JID matches the given string.
	 * </p>
	 *
	 * @param string the String to compare this JID with.
	 * @return true if {@code toString().equals(string)}.
	 */
	public boolean equals(String string);
}
