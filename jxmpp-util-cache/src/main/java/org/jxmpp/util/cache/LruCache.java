/**
 *
 * Copyright 2003-2005 Jive Software, 2014 Florian Schmaus
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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A specialized Map that is size-limited (using an LRU algorithm). The Map is
 * thread-safe.
 * 
 * @author Matt Tucker
 * @author Florian Schmaus
 */
public class LruCache<K, V> extends LinkedHashMap<K, V> implements Cache<K, V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4980809402073634607L;

	/**
     * The default initial value for the used data structures.
     */
    private static final int DEFAULT_INITIAL_SIZE = 50;

    /**
     * Maximum number of items the cache will hold.
     */
    private int maxCacheSize;

    /**
     * Maintain the number of cache hits and misses. A cache hit occurs every
     * time the get method is called and the cache contains the requested
     * object. A cache miss represents the opposite occurrence.
     * <p>
     * Keeping track of cache hits and misses lets one measure how efficient
     * the cache is; the higher the percentage of hits, the more efficient.
     */
    private final AtomicLong cacheHits = new AtomicLong();
    private final AtomicLong cacheMisses = new AtomicLong();

    /**
     * Create a new cache and specify the maximum size of for the cache in
     * bytes, and the maximum lifetime of objects.
     *
     * @param maxSize the maximum number of objects the cache will hold. -1
     *      means the cache has no max size.
     */
	public LruCache(int maxSize) {
		super(maxSize < DEFAULT_INITIAL_SIZE ? maxSize : DEFAULT_INITIAL_SIZE,
				0.75f, true);
        if (maxSize == 0) {
            throw new IllegalArgumentException("Max cache size cannot be 0.");
        }
        this.maxCacheSize = maxSize;
    }

	@Override
	protected final boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxCacheSize;
	}

    @Override
    public final synchronized V put(K key, V value) {
        return super.put(key, value);
    }

    @Override
    public final V get(Object key) {
		V cacheObject;
		synchronized (this) {
			cacheObject = super.get(key);
		}
		if (cacheObject == null) {
			// The object didn't exist in cache, so increment cache misses.
			cacheMisses.incrementAndGet();
			return null;
		}

        // The object exists in cache, so increment cache hits. Also, increment
        // the object's read count.
        cacheHits.incrementAndGet();

        return cacheObject;
    }

    @Override
    public final synchronized V remove(Object key) {
        return super.remove(key);
    }

    @Override
    public final void clear() {
		synchronized (this) {
			super.clear();
		}
        cacheHits.set(0);
        cacheMisses.set(0);
    }

    @Override
    public final synchronized int size() {
        return super.size();
    }

    @Override
    public final synchronized boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public final synchronized Collection<V> values() {
		return super.values();
    }

    @Override
    public final synchronized boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    @Override
    public final synchronized void putAll(Map<? extends K, ? extends V> m) {
		super.putAll(m);
    }

    @Override
    public final synchronized boolean containsValue(Object value) {
        return super.containsValue(value);
    }

    @Override
    public final synchronized Set<java.util.Map.Entry<K, V>> entrySet() {
		return super.entrySet();
    }

    @Override
    public final synchronized Set<K> keySet() {
    	return super.keySet();
    }

    public final long getCacheHits() {
        return cacheHits.longValue();
    }

    public final long getCacheMisses() {
        return cacheMisses.longValue();
    }

    @Override
    public final int getMaxCacheSize() {
        return maxCacheSize;
    }

    @Override
    public final void setMaxCacheSize(int maxCacheSize) {
		this.maxCacheSize = maxCacheSize;
	}
}
