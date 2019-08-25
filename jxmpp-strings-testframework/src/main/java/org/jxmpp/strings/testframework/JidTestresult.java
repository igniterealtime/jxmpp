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

public abstract class JidTestresult {

	public final XmppStringPrepper xmppStringPrepper;

	public final long startNanos, stopNanos, durationInNanos;

	protected JidTestresult(XmppStringPrepper xmppStringPrepper, long startNanos, long stopNanos) {
		this.xmppStringPrepper = xmppStringPrepper;
		this.startNanos = startNanos;
		this.stopNanos = stopNanos;
		this.durationInNanos = stopNanos - startNanos;
	}
}
