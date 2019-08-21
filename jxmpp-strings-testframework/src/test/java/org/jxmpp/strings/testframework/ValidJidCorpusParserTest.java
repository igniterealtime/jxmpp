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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ValidJidCorpusParserTest {

	public static final char US = ValidJidCorpusParser.ASCII_UNIT_SEPARATOR_UNICODE_INFORMATION_SEPARATOR_ONE;
	public static final char RS = ValidJidCorpusParser.ASCII_RECORD_SEPARATOR_UNICODE_INFORMATION_SEPARATOR_TWO;

	@Test
	public void parseTestAllParts() {
		final String unnormalizedJid = "user@example.org/resource";
		final String normalizedLocalpart = "user";
		final String normalizedDomainpart = "example.org";
		final String normalizedResourcepart = "resource";

		testValidJidCorpusParser(unnormalizedJid, normalizedLocalpart, normalizedDomainpart, normalizedResourcepart);
	}

	@Test
	public void parseTestEmptyResourcepart() {
		final String unnormalizedJid = "user@example.org";
		final String normalizedLocalpart = "user";
		final String normalizedDomainpart = "example.org";
		final String normalizedResourcepart = "";

		testValidJidCorpusParser(unnormalizedJid, normalizedLocalpart, normalizedDomainpart, normalizedResourcepart);
	}

	@Test
	public void parseTestEmptyLocalpart() {
		final String unnormalizedJid = "example.org/resource";
		final String normalizedLocalpart = "";
		final String normalizedDomainpart = "example.org";
		final String normalizedResourcepart = "resource";

		testValidJidCorpusParser(unnormalizedJid, normalizedLocalpart, normalizedDomainpart, normalizedResourcepart);
	}

	@Test
	public void parseTestEmptyLocalpartEmptyResourcepart() {
		final String unnormalizedJid = "example.org";
		final String normalizedLocalpart = "";
		final String normalizedDomainpart = "example.org";
		final String normalizedResourcepart = "";

		testValidJidCorpusParser(unnormalizedJid, normalizedLocalpart, normalizedDomainpart, normalizedResourcepart);
	}

	@Test
	public void parseTestWithCommentLine() {
		String jid = createJidEntry("user@domain/resource", "user", "domain", "resource");
		String input = "Comment Line\n" + jid;

		ValidJidCorpusParser parser = new ValidJidCorpusParser(input);
		List<ValidJid> parsedValidJids = parser.parse();
		assertEquals(1, parsedValidJids.size());
	}

	private static void testValidJidCorpusParser(String unnormalizedJid, String normalizedLocalpart,
			String normalizedDomainpart, String normalizedResourcepart) {
		String jidEntry = createJidEntry(unnormalizedJid, normalizedLocalpart, normalizedDomainpart,
				normalizedResourcepart);

		ValidJidCorpusParser validJidCorpusParser = new ValidJidCorpusParser(jidEntry);
		List<ValidJid> parsedValidJids = validJidCorpusParser.parse();

		assertEquals(1, parsedValidJids.size());

		ValidJid validJid = parsedValidJids.get(0);
		assertEquals(unnormalizedJid, validJid.unnormalizedJid);
		assertEquals(normalizedLocalpart, validJid.localpart);
		assertEquals(normalizedDomainpart, validJid.domainpart);
		assertEquals(normalizedResourcepart, validJid.resourcepart);
	}

	private static String createJidEntry(String unnormalizedJid, String normalizedLocalpart, String normalizedDomainpart, String normalizedResourcepart) {
		return "jid:\n"
				+ unnormalizedJid + ValidJidCorpusParser.END_OF_RECORD
				+ normalizedLocalpart + US + normalizedDomainpart + US + normalizedResourcepart + RS + '\n';
	}
}
