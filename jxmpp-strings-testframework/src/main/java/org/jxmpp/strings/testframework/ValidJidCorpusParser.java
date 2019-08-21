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

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;

public class ValidJidCorpusParser extends JidCorpusParser<ValidJid> {

	private static final ValidJidCorpusParboiledParser PARSER = Parboiled.createParser(ValidJidCorpusParboiledParser.class);

	/**
	 * Construct a new valid JID corpus parser.
	 *
	 * @param input the string to parse.
	 */
	public ValidJidCorpusParser(String input) {
		super(input, false);
	}

	/**
	 * Construct a new valid JID corpus parser.
	 *
	 * @param input the string to parse.
	 * @param tracing if tracing of the parser should be enabled.
	 */
	public ValidJidCorpusParser(String input, boolean tracing) {
		super(input, tracing);
	}

	@Override
	protected Rule getParserRootRule() {
		return PARSER.Corpus();
	}

	@BuildParseTree
	static class ValidJidCorpusParboiledParser extends BaseParser<Object> {

		Rule NoCtrlCharText() {
			return ZeroOrMore(NoneOf(CONTROL_CHARACTERS));
		}

		Rule NoCtrlCharNoNewLineText() {
			return ZeroOrMore(NoneOf(CONTROL_CHARACTERS_AND_NEWLINE));
		}

		Rule TextLine() {
			return ZeroOrMore(NoneOf("\n"));
		}

		Rule JidEntry() {
			return Sequence(
					JidHeader(),
					UnnormalizedJid(),
					NormalizedJid(),
					swap4(),
					push(new ValidJid((String) pop(), (String) pop(), (String) pop(), (String) pop()))
				);
		}

		Rule JidHeader() {
			return String("jid:\n");
		}

		Rule UnnormalizedJid() {
			return Sequence(
					// Match and push the unnormalized JID.
					NoCtrlCharNoNewLineText(),
					push(match()),
					String(END_OF_RECORD)
				);
		}

		Rule NormalizedJid() {
			return Sequence(
					// Match and push localpart.
					NoCtrlCharText(),
					push(match()),
					String(ASCII_UNIT_SEPARATOR_UNICODE_INFORMATION_SEPARATOR_ONE),
					// Match and push domainpart.
					NoCtrlCharText(),
					push(match()),
					String(ASCII_UNIT_SEPARATOR_UNICODE_INFORMATION_SEPARATOR_ONE),
					// Match and push resourcepart.
					NoCtrlCharText(),
					push(match()),
					String(END_OF_RECORD)
				);
		}

		Rule CommentLine() {
			return Sequence(
					TextLine(),
					String("\n")
				);
		}

		Rule Entry() {
			return FirstOf(
					JidEntry(),
					CommentLine()
				);
		}

		Rule Corpus() {
			return ZeroOrMore(Entry());
		}
	}

}
