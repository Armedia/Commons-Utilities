package com.armedia.commons.utilities.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.armedia.commons.utilities.Tools;

public class ReadWriteCollection<ELEMENT> extends BaseReadWriteLockable implements Collection<ELEMENT> {

	private final Collection<ELEMENT> c;
	protected final Function<ELEMENT, ELEMENT> canonicalizer;

	public ReadWriteCollection(Collection<ELEMENT> c) {
		this(ReadWriteLockable.NULL_LOCK, c, null);
	}

	public ReadWriteCollection(Collection<ELEMENT> c, Function<ELEMENT, ELEMENT> canonicalizer) {
		this(ReadWriteLockable.NULL_LOCK, c, canonicalizer);
	}

	public ReadWriteCollection(ReadWriteLockable lockable, Collection<ELEMENT> c) {
		this(lockable, c, null);
	}

	public ReadWriteCollection(ReadWriteLockable lockable, Collection<ELEMENT> c,
		Function<ELEMENT, ELEMENT> canonicalizer) {
		this(BaseReadWriteLockable.extractLock(lockable), c, canonicalizer);
	}

	public ReadWriteCollection(ReadWriteLock rwLock, Collection<ELEMENT> c) {
		this(rwLock, c, null);
	}

	public ReadWriteCollection(ReadWriteLock rwLock, Collection<ELEMENT> c, Function<ELEMENT, ELEMENT> canonicalizer) {
		super(rwLock);
		this.c = Objects.requireNonNull(c, "Must provide a non-null backing Collection");
		this.canonicalizer = (canonicalizer != null ? canonicalizer : Objects::requireNonNull);
	}

	protected final ELEMENT canonicalizeObject(Object o) {
		@SuppressWarnings("unchecked")
		ELEMENT e = (ELEMENT) o;
		return canonicalize(e);
	}

	protected final ELEMENT canonicalize(ELEMENT e) {
		return this.canonicalizer.apply(e);
	}

	@Override
	public void forEach(Consumer<? super ELEMENT> action) {
		Objects.requireNonNull(action, "Must provide a non-null action to apply");
		readLocked(() -> this.c.forEach(action));
	}

	@Override
	public int size() {
		return readLocked(this.c::size);
	}

	@Override
	public boolean isEmpty() {
		return readLocked(this.c::isEmpty);
	}

	@Override
	public boolean contains(Object o) {
		Object O = canonicalizeObject(o);
		return readLocked(() -> this.c.contains(O));
	}

	@Override
	public Iterator<ELEMENT> iterator() {
		return new ReadWriteIterator<>(getMainLock(), this.c.iterator());
	}

	@Override
	public Object[] toArray() {
		return readLocked(() -> this.c.toArray());
	}

	@Override
	public <T> T[] toArray(T[] a) {
		Objects.requireNonNull(a, "Must provide a non-null Array instance");
		return readLocked(() -> this.c.toArray(a));
	}

	@Override
	public boolean add(ELEMENT e) {
		ELEMENT E = canonicalize(e);
		return writeLocked(() -> this.c.add(E));
	}

	@Override
	public boolean remove(Object o) {
		Object O = canonicalizeObject(o);
		return writeLocked(() -> this.c.remove(O));
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		Objects.requireNonNull(c, "Must provide a non-null collection to check against");
		if (c.isEmpty()) { return true; }
		return writeLocked(() -> this.c.containsAll(c));
	}

	@Override
	public boolean addAll(Collection<? extends ELEMENT> c) {
		Objects.requireNonNull(c, "Must provide a non-null collection to add from");
		if (c.isEmpty()) { return false; }
		return writeLocked(() -> this.c.addAll(c));
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		Objects.requireNonNull(c, "Must provide a non-null collection to retain from");
		if (c.isEmpty()) { return false; }
		return writeLocked(() -> this.c.retainAll(c));
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		Objects.requireNonNull(c, "Must provide a non-null collection to remove from");
		if (c.isEmpty()) { return false; }
		return writeLocked(() -> this.c.removeAll(c));
	}

	@Override
	public void clear() {
		writeLocked(this.c::clear);
	}

	@Override
	public boolean equals(Object o) {
		return readLocked(() -> {
			if (o == null) { return false; }
			if (o == this) { return true; }
			if (!Set.class.isInstance(o)) { return false; }
			Set<?> s = Set.class.cast(o);
			if (this.c.size() != s.size()) { return false; }
			return this.c.equals(o);
		});
	}

	@Override
	public int hashCode() {
		return readLocked(() -> Tools.hashTool(this, null, this.c));
	}

	@Override
	public boolean removeIf(Predicate<? super ELEMENT> filter) {
		Objects.requireNonNull(filter, "Must provide a non-null filter to search with");
		final Lock readLock = acquireReadLock();
		Lock writeLock = null;
		try {
			try {
				for (ELEMENT e : this.c) {
					if (filter.test(e)) {
						// Ok so we need to upgrade to a write lock
						if (writeLock == null) {
							readLock.unlock();
							writeLock = acquireWriteLock();
						}
						this.c.remove(e);
					}
				}
				if (writeLock == null) { return false; }
				readLock.lock();
				return true;
			} finally {
				if (writeLock != null) {
					writeLock.unlock();
				}
			}
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public Spliterator<ELEMENT> spliterator() {
		return readLocked(() -> new ReadWriteSpliterator<>(getMainLock(), this.c.spliterator()));
	}
}