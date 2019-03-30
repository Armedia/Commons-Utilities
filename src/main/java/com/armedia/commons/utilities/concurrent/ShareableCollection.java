package com.armedia.commons.utilities.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.armedia.commons.utilities.Tools;

public class ShareableCollection<ELEMENT> extends BaseShareableLockable implements Collection<ELEMENT> {

	private final Collection<ELEMENT> c;

	public ShareableCollection(Collection<ELEMENT> c) {
		this(ShareableLockable.NULL_LOCK, c);
	}

	public ShareableCollection(ShareableLockable lockable, Collection<ELEMENT> c) {
		this(BaseShareableLockable.extractLock(lockable), c);
	}

	public ShareableCollection(ReadWriteLock rwLock, Collection<ELEMENT> c) {
		super(rwLock);
		this.c = Objects.requireNonNull(c, "Must provide a non-null backing Collection");
	}

	@Override
	public void forEach(Consumer<? super ELEMENT> action) {
		Objects.requireNonNull(action, "Must provide a non-null action to apply");
		shareLocked(() -> this.c.forEach(action));
	}

	@Override
	public int size() {
		return shareLocked(this.c::size);
	}

	@Override
	public boolean isEmpty() {
		return shareLocked(this.c::isEmpty);
	}

	@Override
	public boolean contains(Object o) {
		return shareLocked(() -> this.c.contains(o));
	}

	@Override
	public Iterator<ELEMENT> iterator() {
		return new ShareableIterator<>(this, this.c.iterator());
	}

	@Override
	public Object[] toArray() {
		return shareLocked(() -> this.c.toArray());
	}

	@Override
	public <T> T[] toArray(T[] a) {
		Objects.requireNonNull(a, "Must provide a non-null Array instance");
		return shareLocked(() -> this.c.toArray(a));
	}

	@Override
	public boolean add(ELEMENT e) {
		return mutexLocked(() -> this.c.add(e));
	}

	@Override
	public boolean remove(Object o) {
		return mutexLocked(() -> this.c.remove(o));
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		Objects.requireNonNull(c, "Must provide a non-null collection to check against");
		if (c.isEmpty()) { return true; }
		return mutexLocked(() -> this.c.containsAll(c));
	}

	@Override
	public boolean addAll(Collection<? extends ELEMENT> c) {
		Objects.requireNonNull(c, "Must provide a non-null collection to add from");
		if (c.isEmpty()) { return false; }
		return mutexLocked(() -> this.c.addAll(c));
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		Objects.requireNonNull(c, "Must provide a non-null collection to retain from");
		if (c.isEmpty()) { return false; }
		return mutexLocked(() -> this.c.retainAll(c));
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		Objects.requireNonNull(c, "Must provide a non-null collection to remove from");
		if (c.isEmpty()) { return false; }
		return mutexLocked(() -> this.c.removeAll(c));
	}

	@Override
	public void clear() {
		mutexLocked(this.c::clear);
	}

	@Override
	public boolean equals(Object o) {
		return shareLocked(() -> {
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
		return shareLocked(() -> Tools.hashTool(this, null, this.c));
	}

	@Override
	public boolean removeIf(Predicate<? super ELEMENT> filter) {
		Objects.requireNonNull(filter, "Must provide a non-null filter to search with");
		final Lock readLock = acquireSharedLock();
		Lock writeLock = null;
		try {
			try {
				for (ELEMENT e : this.c) {
					if (filter.test(e)) {
						// Ok so we need to upgrade to a write lock
						if (writeLock == null) {
							readLock.unlock();
							writeLock = acquireMutexLock();
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
		return shareLocked(() -> new ShareableSpliterator<>(this, this.c.spliterator()));
	}
}