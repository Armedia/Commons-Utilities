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

public class ShareableMap<KEY, VALUE> extends BaseShareableLockable implements Map<KEY, VALUE> {

	protected final Map<KEY, VALUE> map;
	protected final Set<KEY> keys;
	protected final Set<Map.Entry<KEY, VALUE>> entries;
	protected final Collection<VALUE> values;

	protected static ReadWriteLock extractLock(Map<?, ?> m) {
		ShareableLockable l = Tools.cast(ShareableLockable.class, m);
		if (l != null) { return BaseShareableLockable.extractLock(l); }
		return null;
	}

	public ShareableMap(Map<KEY, VALUE> map) {
		this(ShareableMap.extractLock(map), map);
	}

	public ShareableMap(ShareableLockable lockable, Map<KEY, VALUE> map) {
		this(BaseShareableLockable.extractLock(lockable), map);
	}

	public ShareableMap(ReadWriteLock rwLock, Map<KEY, VALUE> map) {
		super(rwLock);
		this.map = Objects.requireNonNull(map, "Must provide a non-null backing map");
		this.keys = new ShareableSet<>(this, map.keySet());
		this.entries = new ShareableSet<>(this, map.entrySet());
		this.values = new ShareableCollection<>(this, map.values());
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
		return this.keys;
	}

	@Override
	public Collection<VALUE> values() {
		return this.values;
	}

	@Override
	public Set<Entry<KEY, VALUE>> entrySet() {
		return this.entries;
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
		shareLocked(() -> this.map.forEach(action));
	}

	@Override
	public void replaceAll(BiFunction<? super KEY, ? super VALUE, ? extends VALUE> function) {
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

	@Override
	public VALUE compute(KEY key, BiFunction<? super KEY, ? super VALUE, ? extends VALUE> remappingFunction) {
		Objects.requireNonNull(remappingFunction, "Must provide a non-null remapping function");
		return mutexLocked(() -> this.map.compute(key, remappingFunction));
	}

	@Override
	public VALUE merge(KEY key, VALUE value,
		BiFunction<? super VALUE, ? super VALUE, ? extends VALUE> remappingFunction) {
		Objects.requireNonNull(remappingFunction, "Must provide a non-null remapping function");
		return mutexLocked(() -> this.map.merge(key, value, remappingFunction));
	}
}