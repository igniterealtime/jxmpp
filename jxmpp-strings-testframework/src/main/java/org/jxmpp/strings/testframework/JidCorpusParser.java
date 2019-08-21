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

import java.util.ArrayList;
import java.util.List;

import org.parboiled.Rule;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.parserunners.TracingParseRunner;
import org.parboiled.support.ParsingResult;

public abstract class JidCorpusParser<J> {

	public static final char ASCII_LINE_FEED_UNICODE_END_OF_LINE = '\n';
	public static final char ASCII_RECORD_SEPARATOR_UNICODE_INFORMATION_SEPARATOR_TWO = '\u001E';
	public static final char ASCII_UNIT_SEPARATOR_UNICODE_INFORMATION_SEPARATOR_ONE = '\u001F';
	public static final String END_OF_RECORD;

	static {
		char[] endOfRecordChars = {ASCII_RECORD_SEPARATOR_UNICODE_INFORMATION_SEPARATOR_TWO,
				ASCII_LINE_FEED_UNICODE_END_OF_LINE};
		END_OF_RECORD = new String(endOfRecordChars);
	}

	protected static final char[] CONTROL_CHARACTERS = new char[] {
			ASCII_RECORD_SEPARATOR_UNICODE_INFORMATION_SEPARATOR_TWO,
			ASCII_UNIT_SEPARATOR_UNICODE_INFORMATION_SEPARATOR_ONE,
	};

	protected static final char[] CONTROL_CHARACTERS_AND_NEWLINE;

	static {
		CONTROL_CHARACTERS_AND_NEWLINE = new char[CONTROL_CHARACTERS.length + 1];
		System.arraycopy(CONTROL_CHARACTERS, 0, CONTROL_CHARACTERS_AND_NEWLINE, 0, CONTROL_CHARACTERS.length);
		CONTROL_CHARACTERS_AND_NEWLINE[CONTROL_CHARACTERS_AND_NEWLINE.length - 1] = '\n';
	}

	private final String input;
	private final boolean enableParserTracing;

	ParsingResult<J> parsingResult;
	private List<J> parsedJids;

	/**
	 * Construct a new valid JID corpus parser.
	 *
	 * @param input the string to parse.
	 */
	protected JidCorpusParser(String input) {
		this(input, false);
	}

	/**
	 * Construct a new valid JID corpus parser.
	 *
	 * @param input the string to parse.
	 * @param tracing if tracing of the parser should be enabled.
	 */
	protected JidCorpusParser(String input, boolean tracing) {
		this.input = input;
		this.enableParserTracing = tracing;
	}

	/**
	 * Parse the input.
	 *
	 * @return a list of parsed {@link ValidJid}s.
	 */
	public List<J> parse() {
		final ParseRunner<J> runner;
		if (enableParserTracing) {
			runner = getTracingParseRunner();
		} else {
			runner = getReportingParserRunner();
		}

		parsingResult = runner.run(input);
		parsedJids = getValidJidsFrom(parsingResult);
		return parsedJids;
	}

	protected abstract Rule getParserRootRule();

	private ReportingParseRunner<J> getReportingParserRunner() {
		Rule parserRootRule = getParserRootRule();
		ReportingParseRunner<J> runner = new ReportingParseRunner<>(parserRootRule);
		return runner;
	}

	private TracingParseRunner<J> getTracingParseRunner() {
		Rule parserRootRule = getParserRootRule();
		TracingParseRunner<J> runner = new TracingParseRunner<>(parserRootRule);
		return runner;
	}

	private static <J> List<J> getValidJidsFrom(ParsingResult<J> parsingResult) {
		final List<J> result = new ArrayList<>(parsingResult.valueStack.size());
		parsingResult.valueStack.forEach(e -> result.add(e));
		return result;
	}

}
