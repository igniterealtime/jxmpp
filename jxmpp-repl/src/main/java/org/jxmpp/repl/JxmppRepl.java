/**
 *
 * Copyright 2017-2023 Florian Schmaus
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
package org.jxmpp.repl;

import org.jxmpp.stringprep.icu4j.Icu4jXmppStringprep;
import org.jxmpp.stringprep.libidn.LibIdnXmppStringprep;

/**
 * Supporting class for JXMPP's REPL.
 *
 */
public class JxmppRepl {

	private JxmppRepl() {
	}

	/**
	 * Initializes the REPL.
	 */
	public static void init() {
	}

	/**
	 * Use ICU4J as stringprep facility.
	 */
	public static void useIcu4J() {
		Icu4jXmppStringprep.setup();
	}

	/**
	 * Use LibIDN as stringprep facility.
	 */
	public static void useLibidn() {
		LibIdnXmppStringprep.setup();
	}
}
