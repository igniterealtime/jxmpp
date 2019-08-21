/**
 *
 * Copyright 2019 Florian Schmaus
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
package org.jxmpp.strings.testframework;

public final class ValidJid {

	public final String unnormalizedJid;

	public final String localpart;

	public final String domainpart;

	public final String resourcepart;

	public final String toString;

	// TODO: Add field containing the origin of the JID, i.e. file and row where this jid started.
	ValidJid(String unnormalizedJid, String localpart, String domainpart, String resourcepart) {
		this.unnormalizedJid = unnormalizedJid;
		this.localpart = localpart;
		this.domainpart = domainpart;
		this.resourcepart = resourcepart;

		StringBuilder sb = new StringBuilder(
				"ValidJid\n" +
				"unnormalized: " + unnormalizedJid + '\n'
				);
		if (!localpart.isEmpty()) {
			sb.append("localpart   : ").append(localpart).append('\n');
		}
		sb.append("domainpart  : ").append(domainpart).append('\n');
		if (!resourcepart.isEmpty()) {
			sb.append("resourcepart: ").append(resourcepart).append('\n');
		}
		this.toString = sb.toString();
	}

	@Override
	public String toString() {
		return toString;
	}
}
