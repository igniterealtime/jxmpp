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

import java.util.Locale;

public enum XmppAddressParttype {

	localpart,
	domainpart,
	resourcepart,
	;

	final String capitalizedName;

	XmppAddressParttype() {
		String name = name();
		capitalizedName = name.substring(0, 1).toUpperCase(Locale.US) + name.substring(1);
	}

	/**
	 * Get the capitalized name of this XMPP address part.
	 *
	 * @return the capitalized name of this part.
	 */
	public String getCapitalizedName() {
		return capitalizedName;
	}
}
