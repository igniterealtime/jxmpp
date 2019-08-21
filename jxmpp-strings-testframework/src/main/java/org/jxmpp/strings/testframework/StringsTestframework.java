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

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.function.BiConsumer;

import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.parboiled.common.FileUtils;

public class StringsTestframework {

	private static final ExecutorService EXECUTOR = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private final Configuration configuration;

	private final Phaser phaser = new Phaser(0);

	private List<ValidJidTestresult.Successful> validJidSuccessfulTestresults;
	private List<ValidJidTestresult.Failed> validJidFailedTestresults;

	public List<InvalidJidTestresult.Successful> invalidJidSuccessfulTestresults;
	public List<InvalidJidTestresult.Failed> invalidJidFailedTestresults;

	/**
	 * Construct a new instance of the test framework with the default configuration.
	 */
	public StringsTestframework() {
		this(Configuration.builder().withAllKnownXmppStringpreppers().build());
	}

	/**
	 * Construct a new instance of the test framework with the given configuration.
	 *
	 * @param configuration the configuration to use.
	 */
	public StringsTestframework(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Run the test framework.
	 * 
	 * @return <code>true</code> if there where no errors.
	 */
	public synchronized Result runTests() {
		phaser.register();

		{
			List<ValidJid> validJids = parseValidJids();
			final int numberOfValidJidTests = validJids.size() * configuration.xmppStringPreppers.size();
			validJidSuccessfulTestresults = Collections.synchronizedList(new ArrayList<>(numberOfValidJidTests));
			validJidFailedTestresults = Collections.synchronizedList(new ArrayList<>());
			executeForEach(configuration.xmppStringPreppers, validJids, this::testValidJids);
		}

		{
			List<InvalidJid> invalidJids = parseInvalidJids();
			final int numberofInvalidJidTests = invalidJids.size() * configuration.xmppStringPreppers.size();
			invalidJidSuccessfulTestresults = Collections.synchronizedList(new ArrayList<>(numberofInvalidJidTests));
			invalidJidFailedTestresults = Collections.synchronizedList(new ArrayList<>());
			executeForEach(configuration.xmppStringPreppers, invalidJids, this::testInvalidJids);
		}

		phaser.arriveAndAwaitAdvance();

		return new Result(validJidSuccessfulTestresults, validJidFailedTestresults, invalidJidSuccessfulTestresults,
				invalidJidFailedTestresults);
	}

	private <T,U> void executeForEach(Collection<? extends T> first, Collection<? extends U> second, BiConsumer<T, U> biConsumer) {
		int numberOfExecutes = first.size() * second.size();
		Phaser childPhaser = new Phaser(phaser, numberOfExecutes);
		for (T t : first) {
			for (U u : second) {
				EXECUTOR.execute(() -> {
					try {
						biConsumer.accept(t, u);
					} finally {
						childPhaser.arriveAndDeregister();
					}
				});
			}
		}
	}

	private void testValidJids(XmppStringPrepper xmppStringPrepper, ValidJid validJid) {
		Jid jid;
		try {
			jid = JidCreate.from(validJid.unnormalizedJid, xmppStringPrepper.context);
		} catch (XmppStringprepException e) {
			ValidJidTestresult.FailedBecauseException failed = new ValidJidTestresult.FailedBecauseException(xmppStringPrepper, validJid, e);
			validJidFailedTestresults.add(failed);
			return;
		}

		boolean domainpartMismatch = false, localpartMismatch = false, resourcepartMismatch = false;

		String domainpart = jid.getDomain().toString();
		if (!domainpart.equals(validJid.domainpart)) {
			domainpartMismatch = true;
		}

		if (!validJid.localpart.isEmpty()) {
			if (!jid.hasLocalpart()) {
				localpartMismatch = true;
			} else {
				String localpart = jid.getLocalpartOrThrow().toString();
				if (!localpart.equals(validJid.localpart)) {
					localpartMismatch = true;
				}
			}
		}

		if (!validJid.resourcepart.isEmpty()) {
			if (jid.hasNoResource()) {
				resourcepartMismatch = true;
			} else {
				String resourcepart = jid.getResourceOrThrow().toString();
				if (!resourcepart.equals(validJid.resourcepart)) {
					resourcepartMismatch = true;
				}
			}
		}

		if (domainpartMismatch || localpartMismatch || resourcepartMismatch) {
			ValidJidTestresult.FailedBecauseMismatch failed = new ValidJidTestresult.FailedBecauseMismatch(xmppStringPrepper, validJid, jid, domainpartMismatch, localpartMismatch, resourcepartMismatch);
			validJidFailedTestresults.add(failed);
		} else {
			ValidJidTestresult.Successful successful = new ValidJidTestresult.Successful(xmppStringPrepper, validJid, jid);
			validJidSuccessfulTestresults.add(successful);
		}
	}

	private void testInvalidJids(XmppStringPrepper xmppStringPrepper, InvalidJid invalidJid) {
		Jid jid;
		try {
			jid = JidCreate.from(invalidJid.invalidJid, xmppStringPrepper.context);
		} catch (XmppStringprepException e) {
			InvalidJidTestresult.Successful successful = new InvalidJidTestresult.Successful(xmppStringPrepper, invalidJid, e);
			invalidJidSuccessfulTestresults.add(successful);
			return;
		}

		InvalidJidTestresult.Failed failed = new InvalidJidTestresult.Failed(xmppStringPrepper, invalidJid, jid);
		invalidJidFailedTestresults.add(failed);
	}

	static List<ValidJid> parseValidJids() {
		String mainValidJids = FileUtils.readAllTextFromResource("xmpp-strings/jids/valid/main");
		ValidJidCorpusParser parser = new ValidJidCorpusParser(mainValidJids);
		List<ValidJid> validJids = parser.parse();
		return validJids;
	}

	static List<InvalidJid> parseInvalidJids() {
		String mainInvalidJids = FileUtils.readAllTextFromResource("xmpp-strings/jids/invalid/main");
		InvalidJidCorpusParser parser = new InvalidJidCorpusParser(mainInvalidJids);
		List<InvalidJid> invalidJids = parser.parse();
		return invalidJids;
	}

	public static class Configuration {
		final List<XmppStringPrepper> xmppStringPreppers;

		private Configuration(Builder builder) {
			xmppStringPreppers = Collections.unmodifiableList(new ArrayList<>(builder.xmppStringPreppers));
		}

		/**
		 * Construct a new builder.
		 *
		 * @return a newly constructed builder.
		 */
		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {
			List<String> validJidCorpusResources = new ArrayList<>();
			List<XmppStringPrepper> xmppStringPreppers = new ArrayList<>();

			private Builder() {
			}

			/**
			 * Use the given XMPP String Prepper with the given name.
			 *
			 * @param xmppStringPrepperName the name of the XMPP String Prepper.
			 * @return an reference to this builder instance.
			 */
			public Builder withXmppStringPrepper(String xmppStringPrepperName) {
				XmppStringPrepper xmppStringPrepper = XmppStringPrepper.lookup(xmppStringPrepperName);
				if (xmppStringPrepper == null) {
					throw new IllegalArgumentException(
							"No XMPP stringprepper with the name '" + xmppStringPrepperName + "' known.");
				}

				xmppStringPreppers.add(xmppStringPrepper);
				return this;
			}

			/**
			 * Use all known XMPP String Preppers.
			 *
			 * @return an reference to this builder instance.
			 */
			public Builder withAllKnownXmppStringpreppers() {
				List<XmppStringPrepper> xmppStringPreppers = XmppStringPrepper.getKnownXmppStringpreppers();
				this.xmppStringPreppers.addAll(xmppStringPreppers);
				return this;
			}

			/**
			 * Add the given collection of XMPP String Preppers to the list of used preppers.
			 *
			 * @param xmppStringPreppers a collection of XMPP String Preppers.
			 * @return an reference to this builder instance.
			 */
			public Builder addXmppStringPreppers(Collection<? extends XmppStringPrepper> xmppStringPreppers) {
				this.xmppStringPreppers.addAll(xmppStringPreppers);
				return this;
			}

			/**
			 * Build the configuration.
			 *
			 * @return the build configuration.
			 */
			public Configuration build() {
				return new Configuration(this);
			}
		}
	}

	public static class Result {
		public final List<ValidJidTestresult.Successful> validJidSuccessfulTestresults;
		public final List<ValidJidTestresult.Failed> validJidFailedTestresults;

		public final List<InvalidJidTestresult.Successful> invalidJidSuccessfulTestresults;
		public final List<InvalidJidTestresult.Failed> invalidJidFailedTestresults;

		public final boolean wasSucccessful;
		public final int totalSuccessful;
		public final int totalFailed;

		public final ZonedDateTime time = ZonedDateTime.now();

		Result(List<ValidJidTestresult.Successful> validJidSuccessfulTestresults, List<ValidJidTestresult.Failed> validJidFailedTestresults, List<InvalidJidTestresult.Successful> invalidJidSuccessfulTestresults, List<InvalidJidTestresult.Failed> invalidJidFailedTestresults) {
			this.validJidSuccessfulTestresults = Collections.unmodifiableList(validJidSuccessfulTestresults);
			this.validJidFailedTestresults = Collections.unmodifiableList(validJidFailedTestresults);
			this.invalidJidSuccessfulTestresults = Collections.unmodifiableList(invalidJidSuccessfulTestresults);
			this.invalidJidFailedTestresults = Collections.unmodifiableList(invalidJidFailedTestresults);

			totalSuccessful = validJidSuccessfulTestresults.size() + invalidJidSuccessfulTestresults.size();
			totalFailed = validJidFailedTestresults.size() + invalidJidFailedTestresults.size();
			wasSucccessful = totalFailed == 0;
		}

		/**
		 * Write a report of the results to the given appendable.
		 *
		 * @param appendable the appendable to write to.
		 * @throws IOException any IOExceptions while writing.
		 */
		public void writeResults(Appendable appendable) throws IOException {
			appendable.append(
					"jXMPP Strings Testframework Result\n" +
					"===================================\n" +
					"Successful Tests: " + totalSuccessful + '\n' +
					"Failed Tests    : " + totalFailed + '\n'
					);
			if (!wasSucccessful) {
				appendable.append(
						"Some tests FAILED! :(\n");
				for (ValidJidTestresult.Failed failed : validJidFailedTestresults) {
					appendable.append(failed.toString()).append('\n');
				}
				for (InvalidJidTestresult.Failed failed : invalidJidFailedTestresults) {
					appendable.append(failed.toString()).append('\n');
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			try {
				writeResults(sb);
			} catch (IOException e) {
				throw new AssertionError(e);
			}
			return sb.toString();
		}
	}
}
