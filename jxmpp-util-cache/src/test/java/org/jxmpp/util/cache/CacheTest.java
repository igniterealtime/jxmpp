/**
 *
 * Copyright 2014-2016 Florian Schmaus
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
package org.jxmpp.util.cache;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class CacheTest {

	@Test
	public void testMaxCacheSize() {
		LruCache<Integer, Object> cache = new LruCache<Integer, Object>(3);
		cache.put(1, new Object());
		cache.put(2, new Object());
		cache.put(3, new Object());
		cache.put(4, new Object());

		Object object = cache.lookup(4);
		assertNotNull(object);

		object = cache.lookup(1);
		assertNull(object);

		// '2' and '3' should still be in the Cache
		object = cache.lookup(2);
		assertNotNull(object);
		object = cache.lookup(3);
		assertNotNull(object);
	}

}
