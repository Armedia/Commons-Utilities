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

public class ReadWriteMap<KEY, VALUE> extends BaseReadWriteLockable implements Map<KEY, VALUE> {

	protected final Map<KEY, VALUE> map;
	protected final Set<KEY> keys;
	protected final Set<Map.Entry<KEY, VALUE>> entries;
	protected final Collection<VALUE> values;

	public ReadWriteMap(Map<KEY, VALUE> map) {
		this(ReadWriteLockable.NULL_LOCK, map);
	}

	public ReadWriteMap(ReadWriteLockable lockable, Map<KEY, VALUE> map) {
		this(BaseReadWriteLockable.extractLock(lockable), map);
	}

	public ReadWriteMap(ReadWriteLock rwLock, Map<KEY, VALUE> map) {
		super(rwLock);
		this.map = Objects.requireNonNull(map, "Must provide a non-null backing map");
		this.keys = new ReadWriteSet<>(this, map.keySet());
		this.entries = new ReadWriteSet<>(this, map.entrySet());
		this.values = new ReadWriteCollection<>(this, map.values());
	}

	@Override
	public int size() {
		return readLocked(this.map::size);
	}

	@Override
	public boolean isEmpty() {
		return readLocked(this.map::isEmpty);
	}

	@Override
	public boolean containsKey(Object key) {
		return readLocked(() -> this.map.containsKey(key));
	}

	@Override
	public boolean containsValue(Object value) {
		return readLocked(() -> this.map.containsValue(value));
	}

	@Override
	public VALUE get(Object key) {
		return readLocked(() -> this.map.get(key));
	}

	@Override
	public VALUE put(KEY key, VALUE value) {
		return writeLocked(() -> this.map.put(key, value));
	}

	@Override
	public VALUE remove(Object key) {
		return writeLocked(() -> this.map.remove(key));
	}

	@Override
	public void putAll(Map<? extends KEY, ? extends VALUE> m) {
		Objects.requireNonNull(m, "Must provide a non-null map to put elements from");
		writeLocked(() -> this.map.putAll(m));
	}

	@Override
	public void clear() {
		writeLocked(this.map::clear);
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
		return readLocked(() -> {
			if (o == null) { return false; }
			if (o == this) { return true; }
			if (!Map.class.isInstance(o)) { return false; }
			Map<?, ?> m = Map.class.cast(o);
			if (this.map.size() != m.size()) { return false; }
			return this.map.equals(o);
		});
	}

	@Override
	public int hashCode() {
		return readLocked(() -> Tools.hashTool(this, null, this.map));
	}

	@Override
	public VALUE getOrDefault(Object key, VALUE defaultValue) {
		return readLocked(() -> this.map.getOrDefault(key, defaultValue));
	}

	@Override
	public void forEach(BiConsumer<? super KEY, ? super VALUE> action) {
		readLocked(() -> this.map.forEach(action));
	}

	@Override
	public void replaceAll(BiFunction<? super KEY, ? super VALUE, ? extends VALUE> function) {
		writeLocked(() -> this.map.replaceAll(function));
	}

	@Override
	public VALUE putIfAbsent(KEY key, VALUE value) {
		return readLockedUpgradable(() -> this.map.get(key), Objects::isNull, (e) -> {
			this.map.put(key, value);
			return null;
		});
	}

	@Override
	public boolean remove(Object key, Object value) {
		return writeLocked(() -> this.map.remove(key, value));
	}

	@Override
	public boolean replace(KEY key, VALUE oldValue, VALUE newValue) {
		return writeLocked(() -> this.map.replace(key, oldValue, newValue));
	}

	@Override
	public VALUE replace(KEY key, VALUE value) {
		return writeLocked(() -> this.map.replace(key, value));
	}

	@Override
	public VALUE computeIfAbsent(KEY key, Function<? super KEY, ? extends VALUE> mappingFunction) {
		Objects.requireNonNull(mappingFunction, "Must provide a non-null mapping function");
		return readLockedUpgradable(() -> this.map.get(key), Objects::isNull, (V) -> {
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
		return readLockedUpgradable(() -> this.map.get(key), Objects::nonNull, (V) -> {
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
		return writeLocked(() -> this.map.compute(key, remappingFunction));
	}

	@Override
	public VALUE merge(KEY key, VALUE value,
		BiFunction<? super VALUE, ? super VALUE, ? extends VALUE> remappingFunction) {
		Objects.requireNonNull(remappingFunction, "Must provide a non-null remapping function");
		return writeLocked(() -> this.map.merge(key, value, remappingFunction));
	}
}