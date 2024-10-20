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
package org.jxmpp.util.cache.jmh;

import java.util.Random;

import org.jxmpp.util.cache.LruCache;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
public class LruCacheBenchmark {

    private static final int CACHE_SIZE = 10000;

    private final LruCache<Integer, Integer> cache = new LruCache<>(CACHE_SIZE);

    private final Random initialSeedGenerator = new Random(10072015);

    @State(Scope.Thread)
    public static class ThreadState {
        Random random;

        /**
         * Setup the thread local state.
         * @param benchmark the current benchmark instance
         */
        @Setup(Level.Iteration)
        public void setup(LruCacheBenchmark benchmark) {
            int threadSeed = benchmark.initialSeedGenerator.nextInt();
            random = new Random(threadSeed);
        }

        /**
         * Tear down the thread local state.
         */
        @TearDown(Level.Iteration)
        public void tearDown() {
            random = null;
        }
    }

    /**
     * Simple sample JMH benchmark for the LruCache which measures the throughput.
     *
     * @param blackhole the JMH blackhole
     * @param threadState the current thread state
     */
    @Benchmark
    public void measureThroughput(Blackhole blackhole, ThreadState threadState) {
        Integer key = threadState.random.nextInt();
        Integer value = key + 1;

        Integer previous = cache.put(key, value);
        blackhole.consume(previous);
    }
}
