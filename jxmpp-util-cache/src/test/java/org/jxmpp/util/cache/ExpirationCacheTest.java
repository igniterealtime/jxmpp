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

public class ExpirationCacheTest {

	@Test
	public void testExpirationTime() throws InterruptedException {
		// Create a Cache where the items expire after 500ms
		final int EXPIRATION_TIME = 500;
		ExpirationCache<Integer, Object> cache = new ExpirationCache<Integer, Object>(3, EXPIRATION_TIME);
		cache.put(1, new Object());
		cache.put(2, new Object());

		Thread.sleep(EXPIRATION_TIME + 1);
		Object object = cache.lookup(1);
		assertNull(object);

		object = cache.lookup(2);
		assertNull(object);

		cache.put(3, new Object());
		object = cache.lookup(3);
		assertNotNull(object);
	}

}
