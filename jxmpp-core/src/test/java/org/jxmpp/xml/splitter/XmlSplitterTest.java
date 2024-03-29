/**
 *
 * Copyright © 2015-2018 Florian Schmaus
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
package org.jxmpp.xml.splitter;


import java.io.IOException;

import org.junit.Test;

public class XmlSplitterTest extends XmlSplitterTestUtil {

	@Test
	public void simpleTest() throws IOException {
		String element = "<ele att='foo'>bar</ele>";
		xmlSplitterTest(element);
	}

	/**
	 * Test a space after the name of an empty element. Spaces are allowed
	 * according to XML § 3.1 [44].
	 *
	 * @throws IOException if an exception occurs
	 */
	@Test
	public void emptyElementTest() throws IOException {
		xmlSplitterTest("<ele att='foo'><empty /></ele>");
	}

	@Test
	public void attributeValueWithRightAngleBracketTest() throws IOException {
		xmlSplitterTest("<x a='/>'>c</x>");
	}
}
