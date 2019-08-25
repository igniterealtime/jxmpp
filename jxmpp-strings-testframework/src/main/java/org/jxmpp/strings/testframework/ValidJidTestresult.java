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
import org.jxmpp.stringprep.XmppStringprepException;

public abstract class ValidJidTestresult extends JidTestresult {

	public final ValidJid validJid;
	public final Jid jid;

	protected ValidJidTestresult(XmppStringPrepper xmppStringPrepper, long startNanos, long stopNanos,
			ValidJid validJid, Jid jid) {
		super(xmppStringPrepper, startNanos, stopNanos);
		this.validJid = validJid;
		this.jid = jid;
	}

	public static class Successful extends ValidJidTestresult {

		protected Successful(XmppStringPrepper xmppStringPrepper, long startNanos, long stopNanos, ValidJid validJid,
				Jid jid) {
			super(xmppStringPrepper, startNanos, stopNanos, validJid, jid);
		}

	}

	public abstract static class Failed extends ValidJidTestresult {

		protected Failed(XmppStringPrepper xmppStringPrepper, long startNanos, long stopNanos, ValidJid validJid,
				Jid jid) {
			super(xmppStringPrepper, startNanos, stopNanos, validJid, jid);
		}

		protected StringBuilder buildToString() {
			StringBuilder sb = new StringBuilder();
			sb.append(xmppStringPrepper)
				.append(" failed to handle the following valid JID:\n")
				.append(validJid);
			return sb;
		}
	}

	public static class FailedBecauseMismatch extends Failed {

		public final boolean domainpartMismatch, localpartMismatch, resourcepartMismatch;

		protected FailedBecauseMismatch(XmppStringPrepper xmppStringPrepper, long startNanos, long stopNanos, ValidJid validJid, Jid jid, boolean domainpartMismatch,
				boolean localpartMismatch, boolean resourcepartMismatch) {
			super(xmppStringPrepper, startNanos, stopNanos, validJid, jid);
			this.domainpartMismatch = domainpartMismatch;
			this.localpartMismatch = localpartMismatch;
			this.resourcepartMismatch = resourcepartMismatch;
		}

		@Override
		public String toString() {
			StringBuilder sb = buildToString();
			sb.append("as it returned the following JID:\n")
				.append(jid).append('\n')
				.append("but there is a mismatch in the:\n");
			if (domainpartMismatch) {
				sb.append("- domainpart\n");
				sb.append("  expected: ").append(validJid.domainpart).append('\n');
				sb.append("  actual  : ").append(jid.getDomain()).append('\n');
			}
			if (localpartMismatch) {
				sb.append("- localpart\n");
				sb.append("  expected: ").append(validJid.localpart).append('\n');
				sb.append("  actual  : ").append(jid.getLocalpartOrThrow()).append('\n');
			}
			if (resourcepartMismatch) {
				sb.append("- resourcepart\n");
				sb.append("  expected: ").append(validJid.resourcepart).append('\n');
				sb.append("  actual  : ").append(jid.getResourceOrThrow()).append('\n');
			}
			return sb.toString();
		}
	}

	public static class FailedBecauseException extends Failed {

		public final XmppStringprepException xmppStringprepException;

		protected FailedBecauseException(XmppStringPrepper xmppStringPrepper, long startNanos, long stopNanos,
				ValidJid validJid, XmppStringprepException xmppStringprepException) {
			super(xmppStringPrepper, startNanos, stopNanos, validJid, null);
			this.xmppStringprepException = xmppStringprepException;
		}

		@Override
		public String toString() {
			StringBuilder sb = buildToString();
			sb.append("as it threw the following exception:\n")
				.append(xmppStringprepException)
				.append('\n');
			return sb.toString();
		}
	}
}
