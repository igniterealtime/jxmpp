/**
 *
 * Copyright 2018 Florian Schmaus
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.jxmpp.xml.splitter.XmlPrettyPrinter.PrettyPrintedXmlPartCallback;

public class XmlPrettyPrinterTest {

	private static class StringHolder {
		public String string;
	}

	@Test
	public void simplePrettyPrintTest() throws IOException {
		String inputXml = "<o><i a='v'>t</i></o>";
		String expectedPrettyPrintedXml =
				"<o>\n" +
				"  <i a='v'>\n" +
				"    t\n" +
				"  </i>\n" +
				"</o>";
		// Create an XmlPrettyPrinter.Builder with the default settings.
		XmlPrettyPrinter.Builder xmlPrettyPrinteBuilder = XmlPrettyPrinter.builder();

		xmlPrettyPrintTest(expectedPrettyPrintedXml, inputXml, xmlPrettyPrinteBuilder);
	}

	@Test
	public void simplePrettyPrintTestAttributeIndent() throws IOException {
		String inputXml = "<o><i a='v'>t</i></o>";
		String expectedPrettyPrintedXml =
				"<o>\n" +
				"  <i \n" +
				"   a='v'>\n" +
				"    t\n" +
				"  </i>\n" +
				"</o>";
		XmlPrettyPrinter.Builder xmlPrettyPrinteBuilder = XmlPrettyPrinter
				.builder()
				.setAttributeIndent(1);

		xmlPrettyPrintTest(expectedPrettyPrintedXml, inputXml, xmlPrettyPrinteBuilder);
	}

	@Test
	public void simplePrettyPrintTestTabIndent() throws IOException {
		String inputXml = "<o><i a='v'>t</i></o>";
		String expectedPrettyPrintedXml =
				"<o>\n" +
				"\t<i a='v'>\n" +
				"\t\tt\n" +
				"\t</i>\n" +
				"</o>";
		XmlPrettyPrinter.Builder xmlPrettyPrinteBuilder = XmlPrettyPrinter
				.builder()
				.setTabWidth(2);

		xmlPrettyPrintTest(expectedPrettyPrintedXml, inputXml, xmlPrettyPrinteBuilder);
	}

	private static void xmlPrettyPrintTest(String expectedPrettyPrintedXml, String inputXml,
			XmlPrettyPrinter.Builder xmlPrettyPrinterBuilder) throws IOException {
		final StringHolder stringHolder = new StringHolder();
		final XmlPrettyPrinter xmlPrettyPrinter = xmlPrettyPrinterBuilder.setPartCallback(
				new PrettyPrintedXmlPartCallback() {
					@Override
					public void onPrettyPrintedXmlPart(StringBuilder part) {
						stringHolder.string = part.toString();
					}
				}).build();

		XmlSplitter xmlSplitter = new XmlSplitter(1024, new CompleteElementCallback() {
			@Override
			public void onCompleteElement(String completeElement) {
			}
		}, xmlPrettyPrinter);

		try {
			xmlSplitter.append(inputXml);
		} finally {
			xmlSplitter.close();
		}

		assertEquals(expectedPrettyPrintedXml, stringHolder.string);
	}
}
