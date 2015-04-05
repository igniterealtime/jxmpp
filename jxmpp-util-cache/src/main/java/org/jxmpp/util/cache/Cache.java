/**
 *
 * Copyright © 2014-2015 Florian Schmaus
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

public interface Cache<K, V> {

	/**
	 * Put a value in the cache.
	 *
	 * @param key the key of the value.
	 * @param value the value.
	 * @return the previous value or {@code null}.
	 */
	public V put(K key, V value);

	/**
	 * Returns the value of the specified key, or {@code null}.
	 *
	 * @param key the key.
	 * @return the value found in the cache, or {@code null}.
	 */
	public V get(Object key);

	/**
	 * Return the maximum cache Size.
	 *
	 * @return the maximum cache size.
	 */
	public int getMaxCacheSize();

	/**
	 * Set the maximum cache size.
	 * @param size the new maximum cache size.
	 */
	public void setMaxCacheSize(int size);
}
