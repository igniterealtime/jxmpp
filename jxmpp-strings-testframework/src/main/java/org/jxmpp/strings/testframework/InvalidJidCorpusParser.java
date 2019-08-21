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

public class InvalidJidCorpusParser extends JidCorpusParser<InvalidJid> {

	private static final InvalidJidCorpusParboiledParser PARSER = Parboiled.createParser(InvalidJidCorpusParboiledParser.class);

	/**
	 * Construct a new valid JID corpus parser.
	 *
	 * @param input the string to parse.
	 */
	public InvalidJidCorpusParser(String input) {
		super(input, false);
	}

	/**
	 * Construct a new valid JID corpus parser.
	 *
	 * @param input the string to parse.
	 * @param tracing if tracing of the parser should be enabled.
	 */
	public InvalidJidCorpusParser(String input, boolean tracing) {
		super(input, tracing);
	}

	@Override
	protected Rule getParserRootRule() {
		return PARSER.Corpus();
	}

	@BuildParseTree
	static class InvalidJidCorpusParboiledParser extends BaseParser<Object> {

		Rule NoEndOfRecordText() {
			// TODO: Not sure if this does what it should do: Match text until the end of record sequence "<RS> <NL>" is
			// encountered.
			return ZeroOrMore(TestNot(String(END_OF_RECORD)), ANY);
		}

		Rule TextLine() {
			return ZeroOrMore(NoneOf("\n"));
		}

		Rule InvalidJidEntry() {
			return Sequence(
					InvalidJidHeader(),
					InvalidJid(),
					push(new InvalidJid((String) pop()))
				);
		}

		Rule InvalidJidHeader() {
			return String("invalid jid:\n");
		}

		Rule InvalidJid() {
			return Sequence(
					// Match and push the unnormalized JID.
					NoEndOfRecordText(),
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
					InvalidJidEntry(),
					CommentLine()
				);
		}

		Rule Corpus() {
			return ZeroOrMore(Entry());
		}
	}

}
