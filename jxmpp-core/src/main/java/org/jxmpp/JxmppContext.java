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
package org.jxmpp;

import org.jxmpp.stringprep.XmppStringprep;
import org.jxmpp.stringprep.simple.SimpleXmppStringprep;
import org.jxmpp.util.Objects;

/**
 * A context with describes the configuration used by JXMPP.
 */
public final class JxmppContext {

	private static JxmppContext defaultContext;

	/**
	 * Get the default context.
	 *
	 * @return the default context.
	 */
	public static JxmppContext getDefaultContext() {
		return defaultContext;
	}

	private static XmppStringprep defaultXmppStringprep;

	static {
		SimpleXmppStringprep.setup();
	}

	/**
	 * Set the default XmppStringprep interface.
	 *
	 * @param defaultXmppStringprep the default XmppStringprep interface.
	 */
	public static void setDefaultXmppStringprep(XmppStringprep defaultXmppStringprep) {
		JxmppContext.defaultXmppStringprep = Objects.requireNonNull(defaultXmppStringprep, "defaultXmppStringprep");
		updateDefaultContext();
	}

	private static void updateDefaultContext() {
		defaultContext = builder()
				.enableCaching()
				.withXmppStringprep(defaultXmppStringprep)
				.build();
	}

	private final boolean cachingEnabled;

	public final XmppStringprep xmppStringprep;

	private JxmppContext(Builder builder) {
		cachingEnabled = builder.cachingEnabled;
		xmppStringprep = Objects.requireNonNull(builder.xmppStringprep, "xmppStringprep");
	}

	/**
	 * Returns true if String, Part and Jid caching is enabled.
	 *
	 * @return true if caching is enabled.
	 */
	public boolean isCachingEnabled() {
		return cachingEnabled;
	}

	/**
	 * Construct and retrieve a new builder.
	 *
	 * @return a new builder.
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private boolean cachingEnabled;

		private XmppStringprep xmppStringprep;

		/**
		 * Enable String, Part and Jid caching.
		 *
		 * @return a reference to this builder.
		 */
		public Builder enableCaching() {
			cachingEnabled = true;
			return this;
		}

		/**
		 * Set the used XmppStringprep.
		 *
		 * @param xmppStringprep the XmppStringprep to use.
		 * @return a reference to this builder.
		 */
		public Builder withXmppStringprep(XmppStringprep xmppStringprep) {
			this.xmppStringprep = Objects.requireNonNull(xmppStringprep, "xmppStringprep");
			return this;
		}

		/**
		 * Build a JxmppContext.
		 *
		 * @return a newly build JxmppContext.
		 */
		public JxmppContext build() {
			return new JxmppContext(this);
		}
	}
}
