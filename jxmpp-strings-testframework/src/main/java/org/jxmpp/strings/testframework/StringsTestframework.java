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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.parboiled.common.FileUtils;

public class StringsTestframework {

	private static final ExecutorService EXECUTOR = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private final Configuration configuration;

	private final Phaser phaser = new Phaser(0);

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

		List<ValidJid> validJids = parseValidJids();
		List<InvalidJid> invalidJids = parseInvalidJids();


		List<XmppStringPrepperState> xmppStringPreppersState = configuration.xmppStringPreppers
				.stream()
				.map(s -> new XmppStringPrepperState(s, validJids.size(), invalidJids.size()))
				.collect(Collectors.toList());

		executeForEach(xmppStringPreppersState, validJids, StringsTestframework::testValidJids);

		executeForEach(xmppStringPreppersState, invalidJids, StringsTestframework::testInvalidJids);

		phaser.arriveAndAwaitAdvance();

		return new Result(xmppStringPreppersState);
	}

	private <T, U> void executeForEach(List<? extends T> smallList, Collection<? extends U> bigCollection,
			BiConsumer<T, U> biConsumer) {
		// Create a childPhaser for every item in smallList.
		List<Phaser> childPhasers = smallList.stream()
				.map(__ -> new Phaser(phaser, bigCollection.size() + 1))
				.collect(Collectors.toList());

		for (U u : bigCollection) {
			for (int i = 0; i < smallList.size(); i++) {
				T t = smallList.get(i);
				Phaser phaser = childPhasers.get(i);

				EXECUTOR.execute(() -> {
					try {
						biConsumer.accept(t, u);
					} finally {
						phaser.arrive();
					}
				});
			}
		}

		childPhasers.forEach(p -> p.arrive());
	}

	private static void testValidJids(XmppStringPrepperState xmppStringPrepperState, ValidJid validJid) {
		XmppStringPrepper xmppStringPrepper = xmppStringPrepperState.xmppStringPrepper;
		Jid jid;

		long startNanos = System.nanoTime();
		try {
			jid = JidCreate.from(validJid.unnormalizedJid, xmppStringPrepper.context);
		} catch (XmppStringprepException e) {
			long stopNanos = System.nanoTime();
			ValidJidTestresult.FailedBecauseException failed = new ValidJidTestresult.FailedBecauseException(
					xmppStringPrepper, startNanos, stopNanos, validJid, e);
			xmppStringPrepperState.validJidFailedTestresults.add(failed);
			return;
		}
		long stopNanos = System.nanoTime();

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
			ValidJidTestresult.FailedBecauseMismatch failed = new ValidJidTestresult.FailedBecauseMismatch(
					xmppStringPrepper, startNanos, stopNanos, validJid, jid, domainpartMismatch, localpartMismatch,
					resourcepartMismatch);
			xmppStringPrepperState.validJidFailedTestresults.add(failed);
		} else {
			ValidJidTestresult.Successful successful = new ValidJidTestresult.Successful(xmppStringPrepper, startNanos,
					stopNanos, validJid, jid);
			xmppStringPrepperState.validJidSuccessfulTestresults.add(successful);
		}
	}

	private static void testInvalidJids(XmppStringPrepperState xmppStringPrepperState, InvalidJid invalidJid) {
		XmppStringPrepper xmppStringPrepper = xmppStringPrepperState.xmppStringPrepper;
		Jid jid;

		long startNanos = System.nanoTime();
		try {
			jid = JidCreate.from(invalidJid.invalidJid, xmppStringPrepper.context);
		} catch (XmppStringprepException e) {
			long stopNanos = System.nanoTime();
			InvalidJidTestresult.Successful successful = new InvalidJidTestresult.Successful(xmppStringPrepper,
					startNanos, stopNanos, invalidJid, e);
			xmppStringPrepperState.invalidJidSuccessfulTestresults.add(successful);
			return;
		}
		long stopNanos = System.nanoTime();

		InvalidJidTestresult.Failed failed = new InvalidJidTestresult.Failed(xmppStringPrepper, startNanos, stopNanos,
				invalidJid, jid);
		xmppStringPrepperState.invalidJidFailedTestresults.add(failed);
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

	private static class XmppStringPrepperState {

		private final XmppStringPrepper xmppStringPrepper;

		private final List<ValidJidTestresult.Successful> validJidSuccessfulTestresults;
		private final List<ValidJidTestresult.Failed> validJidFailedTestresults;

		private final List<InvalidJidTestresult.Successful> invalidJidSuccessfulTestresults;
		private final List<InvalidJidTestresult.Failed> invalidJidFailedTestresults;

		private XmppStringPrepperState(XmppStringPrepper xmppStringPrepper, int validJidCorpusSize,
				int invalidJidCorpusSize) {
			this.xmppStringPrepper = xmppStringPrepper;

			this.validJidSuccessfulTestresults = Collections.synchronizedList(new ArrayList<>(validJidCorpusSize));
			this.validJidFailedTestresults = Collections.synchronizedList(new ArrayList<>());

			this.invalidJidSuccessfulTestresults = Collections.synchronizedList(new ArrayList<>(invalidJidCorpusSize));
			this.invalidJidFailedTestresults = Collections.synchronizedList(new ArrayList<>());
		}

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

	public static class XmppStringPrepperResult implements Comparable<XmppStringPrepperResult> {

		public final XmppStringPrepper xmppStringPrepper;

		public final List<ValidJidTestresult.Successful> validJidSuccessfulTestresults;
		public final List<ValidJidTestresult.Failed> validJidFailedTestresults;

		public final List<InvalidJidTestresult.Successful> invalidJidSuccessfulTestresults;
		public final List<InvalidJidTestresult.Failed> invalidJidFailedTestresults;

		public final int totalSuccessful, totalFailed;

		private XmppStringPrepperResult(XmppStringPrepperState xmppStringPrepperState) {
			xmppStringPrepper = xmppStringPrepperState.xmppStringPrepper;

			validJidSuccessfulTestresults = Collections
					.unmodifiableList(new ArrayList<>(xmppStringPrepperState.validJidSuccessfulTestresults));
			validJidFailedTestresults = Collections
					.unmodifiableList(new ArrayList<>(xmppStringPrepperState.validJidFailedTestresults));

			invalidJidSuccessfulTestresults = Collections
					.unmodifiableList(new ArrayList<>(xmppStringPrepperState.invalidJidSuccessfulTestresults));
			invalidJidFailedTestresults = Collections
					.unmodifiableList(new ArrayList<>(xmppStringPrepperState.invalidJidFailedTestresults));

			totalSuccessful = validJidSuccessfulTestresults.size() + invalidJidSuccessfulTestresults.size();
			totalFailed = validJidFailedTestresults.size() + invalidJidFailedTestresults.size();
		}

		@Override
		public int compareTo(XmppStringPrepperResult o) {
			if (totalSuccessful != o.totalSuccessful) {
				return Integer.compare(totalSuccessful, o.totalSuccessful);
			}

			int myValidJidSuccessfulCount = validJidFailedTestresults.size();
			int otherValidJidSuccessfulCount = o.validJidSuccessfulTestresults.size();
			if (myValidJidSuccessfulCount != otherValidJidSuccessfulCount) {
				return Integer.compare(myValidJidSuccessfulCount, otherValidJidSuccessfulCount);
			}

			return 0;
		}

	}

	public static class Result {
		public final List<XmppStringPrepperResult> xmppStringPrepperResults;

		public final List<ValidJidTestresult.Successful> validJidSuccessfulTestresults;
		public final List<ValidJidTestresult.Failed> validJidFailedTestresults;

		public final List<InvalidJidTestresult.Successful> invalidJidSuccessfulTestresults;
		public final List<InvalidJidTestresult.Failed> invalidJidFailedTestresults;

		public final boolean wasSuccessful;
		public final int totalSuccessful;
		public final int totalFailed;

		public final ZonedDateTime time = ZonedDateTime.now();

		Result(List<XmppStringPrepperState> xmppStringPreppersState) {
			xmppStringPrepperResults = xmppStringPreppersState.stream()
					.map(s -> new XmppStringPrepperResult(s))
					.collect(Collectors.collectingAndThen(Collectors.toList(),
							results -> {
								Collections.sort(results);
								return Collections.unmodifiableList(results);
							}));

			validJidSuccessfulTestresults = gather(xmppStringPrepperResults, s -> s.validJidSuccessfulTestresults);
			validJidFailedTestresults = gather(xmppStringPrepperResults, s -> s.validJidFailedTestresults);

			invalidJidSuccessfulTestresults = gather(xmppStringPrepperResults, s -> s.invalidJidSuccessfulTestresults);
			invalidJidFailedTestresults = gather(xmppStringPrepperResults, s -> s.invalidJidFailedTestresults);

			totalSuccessful = validJidSuccessfulTestresults.size() + invalidJidSuccessfulTestresults.size();
			totalFailed = validJidFailedTestresults.size() + invalidJidFailedTestresults.size();
			wasSuccessful = totalFailed == 0;
		}

		private static <I,R> List<R> gather(Collection<I> inputCollection,
				Function<? super I, Collection<? extends R>> mapper) {
			return inputCollection.stream()
					.map(mapper)
					.flatMap(l -> l.stream())
					// TODO Use Collectors.toUnmodifiableList() once jXMPP is on Java 10 or higher.
					.collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
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
			if (!wasSuccessful) {
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
