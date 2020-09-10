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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.TypeConversionException;

@Command(name = "test-xmpp-strings")
public class StringsTestframeworkMain implements Callable<Integer> {

	@Option(names = {"-s", "--stringprepper"})
	List<XmppStringPrepper> xmppStringPreppers;

	@Override
	public Integer call() throws IOException {
		StringsTestframework.Configuration.Builder configurationBuilder = StringsTestframework.Configuration.builder();
		if (xmppStringPreppers == null) {
			configurationBuilder.withAllKnownXmppStringpreppers();
		} else {
			configurationBuilder.addXmppStringPreppers(xmppStringPreppers);
		}

		StringsTestframework.Configuration configuration = configurationBuilder.build();

		StringsTestframework stringsTestframework = new StringsTestframework(configuration);

		StringsTestframework.Result result = stringsTestframework.runTests();

		Writer out = new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
		result.writeResults(out);
		out.flush();

		int exitcode = result.wasSuccessful ? 0 : 1;
		return exitcode;
	}

	/**
	 * Run the jXMPP Strings Testframework.
	 *
	 * @param args arguments to the framework (currently none).
	 * @throws IOException if an I/O error occurs.
	 */
	public static void main(String[] args) throws IOException {
		// For some yet unknown reason, the arguments passed with the test-xmpp-strings python script using gradle's
		// '--arg' arrive as one argument. That is
		// ./test-xmpp-strings -s simple
		// yields
		// args.length == 1
		// args[0].equals("-s simple")
		// Even though gradle has in JavaExec.setArgsString(String)
		// setArgs(Arrays.asList(Commandline.translateCommandline(args)));
		if (args.length == 1) {
			args = org.apache.tools.ant.types.Commandline.translateCommandline(args[0]);
		}

		CommandLine commandLine = new CommandLine(new StringsTestframeworkMain());
		commandLine.registerConverter(XmppStringPrepper.class, s -> {
			XmppStringPrepper xmppStringPrepper = XmppStringPrepper.lookup(s);
			if (xmppStringPrepper == null) {
				List<XmppStringPrepper> knownXmppStringPreppers = XmppStringPrepper.getKnownXmppStringpreppers();

				String message = "XMPP string prepper named '" + s + "' unknown. Known preppers: "
						+ knownXmppStringPreppers.stream()
							.map(XmppStringPrepper::getName)
							.collect(Collectors.joining(", "));
				throw new TypeConversionException(message);
			}
			return xmppStringPrepper;
		});
		commandLine.setPosixClusteredShortOptionsAllowed(false);

		int exitcode = commandLine.execute(args);
		System.exit(exitcode);
	}
}
