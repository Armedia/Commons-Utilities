/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2019 Armedia, LLC
 * %%
 * This file is part of the Caliente software.
 * 
 * If the software was purchased under a paid Caliente license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * 
 * Caliente is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Caliente is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Caliente. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 *******************************************************************************/
package com.armedia.commons.utilities.concurrent;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.function.LazySupplier;

public class ShareableMap<KEY, VALUE> extends BaseShareableLockable implements Map<KEY, VALUE> {

	protected final Map<KEY, VALUE> map;
	protected final LazySupplier<Set<Map.Entry<KEY, VALUE>>> entries;
	protected final LazySupplier<Set<KEY>> keys;
	protected final LazySupplier<Collection<VALUE>> values;

	public ShareableMap(Map<KEY, VALUE> map) {
		this(ShareableLockable.extractShareableLock(map), map);
	}

	public ShareableMap(ShareableLockable lockable, Map<KEY, VALUE> map) {
		this(ShareableLockable.extractShareableLock(lockable), map);
	}

	public ShareableMap(ReadWriteLock rwLock, Map<KEY, VALUE> map) {
		super(rwLock);
		this.map = Objects.requireNonNull(map, "Must provide a non-null backing map");
		this.keys = new LazySupplier<>(() -> shareLocked(() -> new ShareableSet<>(this, map.keySet())));
		this.entries = new LazySupplier<>(() -> shareLocked(() -> new ShareableSet<>(this, map.entrySet())));
		this.values = new LazySupplier<>(() -> shareLocked(() -> new ShareableCollection<>(this, map.values())));
	}

	@Override
	public int size() {
		return shareLocked(this.map::size);
	}

	@Override
	public boolean isEmpty() {
		return shareLocked(this.map::isEmpty);
	}

	@Override
	public boolean containsKey(Object key) {
		return shareLocked(() -> this.map.containsKey(key));
	}

	@Override
	public boolean containsValue(Object value) {
		return shareLocked(() -> this.map.containsValue(value));
	}

	@Override
	public VALUE get(Object key) {
		return shareLocked(() -> this.map.get(key));
	}

	@Override
	public VALUE put(KEY key, VALUE value) {
		return mutexLocked(() -> this.map.put(key, value));
	}

	@Override
	public VALUE remove(Object key) {
		return mutexLocked(() -> this.map.remove(key));
	}

	@Override
	public void putAll(Map<? extends KEY, ? extends VALUE> m) {
		Objects.requireNonNull(m, "Must provide a non-null map to put elements from");
		mutexLocked(() -> this.map.putAll(m));
	}

	@Override
	public void clear() {
		mutexLocked(this.map::clear);
	}

	@Override
	public Set<KEY> keySet() {
		return this.keys.get();
	}

	@Override
	public Collection<VALUE> values() {
		return this.values.get();
	}

	@Override
	public Set<Entry<KEY, VALUE>> entrySet() {
		return this.entries.get();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) { return false; }
		if (o == this) { return true; }
		Map<?, ?> other = Tools.cast(Map.class, o);
		if (other == null) { return false; }
		try (SharedAutoLock lock = autoSharedLock()) {
			if (this.map.size() != other.size()) { return false; }
			return this.map.equals(other);
		}
	}

	@Override
	public int hashCode() {
		return shareLocked(() -> Tools.hashTool(this, null, this.map));
	}

	@Override
	public VALUE getOrDefault(Object key, VALUE defaultValue) {
		return shareLocked(() -> this.map.getOrDefault(key, defaultValue));
	}

	@Override
	public void forEach(BiConsumer<? super KEY, ? super VALUE> action) {
		Objects.requireNonNull(action);
		shareLocked(() -> this.map.forEach(action));
	}

	@Override
	public void replaceAll(BiFunction<? super KEY, ? super VALUE, ? extends VALUE> function) {
		Objects.requireNonNull(function);
		mutexLocked(() -> this.map.replaceAll(function));
	}

	@Override
	public VALUE putIfAbsent(KEY key, VALUE value) {
		return shareLockedUpgradable(() -> this.map.get(key), Objects::isNull, (e) -> {
			this.map.put(key, value);
			return null;
		});
	}

	@Override
	public boolean remove(Object key, Object value) {
		return mutexLocked(() -> this.map.remove(key, value));
	}

	@Override
	public boolean replace(KEY key, VALUE oldValue, VALUE newValue) {
		return mutexLocked(() -> this.map.replace(key, oldValue, newValue));
	}

	@Override
	public VALUE replace(KEY key, VALUE value) {
		return mutexLocked(() -> this.map.replace(key, value));
	}

	@Override
	public VALUE computeIfAbsent(KEY key, Function<? super KEY, ? extends VALUE> mappingFunction) {
		Objects.requireNonNull(mappingFunction, "Must provide a non-null mapping function");
		return shareLockedUpgradable(() -> this.map.get(key), Objects::isNull, (V) -> {
			V = mappingFunction.apply(key);
			if (V != null) {
				this.map.put(key, V);
			}
			return V;
		});
	}

	@Override
	public VALUE computeIfPresent(KEY key, BiFunction<? super KEY, ? super VALUE, ? extends VALUE> remappingFunction) {
		Objects.requireNonNull(remappingFunction, "Must provide a non-null remapping function");
		return shareLockedUpgradable(() -> this.map.get(key), Objects::nonNull, (V) -> {
			V = remappingFunction.apply(key, V);
			if (V != null) {
				this.map.put(key, V);
			} else {
				this.map.remove(key);
			}
			return V;
		});
	}
}
