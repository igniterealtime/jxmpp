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

import org.jxmpp.jid.Jid;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

public abstract class InvalidJidTestresult extends JidTestresult {

	public final InvalidJid invalidJid;

	protected InvalidJidTestresult(XmppStringPrepper xmppStringPrepper, long startNanos, long stopNanos, InvalidJid invalidJid) {
		super(xmppStringPrepper, startNanos, stopNanos);
		this.invalidJid = invalidJid;
	}

	public static class Successful extends InvalidJidTestresult {

		public final XmppStringprepException xmppStringprepException;

		protected Successful(XmppStringPrepper xmppStringPrepper, long startNanos, long stopNanos,
				InvalidJid invalidJid, XmppStringprepException xmppStringprepException) {
			super(xmppStringPrepper, startNanos, stopNanos, invalidJid);
			this.xmppStringprepException = xmppStringprepException;
		}

	}

	public static class Failed extends InvalidJidTestresult {

		public final Jid jid;

		protected Failed(XmppStringPrepper xmppStringPrepper, long startNanos, long stopNanos, InvalidJid invalidJid,
				Jid jid) {
			super(xmppStringPrepper, startNanos, stopNanos, invalidJid);
			this.jid = jid;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(xmppStringPrepper)
			.append(" failed to handle the following invalid JID:\n")
			.append(invalidJid).append('\n')
			.append("as it produced the following JID (when it should have thrown an exception):\n")
			.append(jid).append('\n');
			Localpart localpart = jid.getLocalpartOrNull();
			if (localpart != null) {
				sb.append("- localpart: ").append(localpart).append('\n');
			}
			Domainpart domainpart = jid.getDomain();
			sb.append("- domainpart: ").append(domainpart).append('\n');
			Resourcepart resourcepart = jid.getResourceOrNull();
			if (resourcepart != null) {
				sb.append("- resourcepart: ").append(resourcepart).append('\n');
			}
			return sb.toString();
		}
	}

}
