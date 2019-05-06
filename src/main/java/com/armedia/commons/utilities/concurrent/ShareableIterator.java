package com.armedia.commons.utilities.concurrent;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;

public class ShareableIterator<E> extends BaseShareableLockable implements Iterator<E> {
	private final Iterator<E> iterator;

	public ShareableIterator(Iterator<E> iterator) {
		this(ShareableLockable.extractShareableLock(iterator), iterator);
	}

	public ShareableIterator(ShareableLockable lockable, Iterator<E> iterator) {
		this(ShareableLockable.extractShareableLock(lockable), iterator);
	}

	public ShareableIterator(ReadWriteLock rwLock, Iterator<E> iterator) {
		super(rwLock);
		this.iterator = Objects.requireNonNull(iterator, "Must provide a non-null Iterator to back this iterator");
	}

	@Override
	public boolean hasNext() {
		return shareLocked(this.iterator::hasNext);
	}

	@Override
	public E next() {
		return shareLocked(this.iterator::next);
	}

	@Override
	public void remove() {
		mutexLocked(this.iterator::remove);
	}

	@Override
	public void forEachRemaining(Consumer<? super E> action) {
		Objects.requireNonNull(action, "Must provide a non-null action to apply");
		shareLocked(() -> this.iterator.forEachRemaining(action));
	}
}