package com.armedia.commons.utilities.concurrent;

import java.util.ListIterator;
import java.util.concurrent.locks.ReadWriteLock;

public class ShareableListIterator<ELEMENT> extends ShareableIterator<ELEMENT> implements ListIterator<ELEMENT> {

	private final ListIterator<ELEMENT> iterator;

	public ShareableListIterator(ListIterator<ELEMENT> iterator) {
		this(ShareableLockable.NULL_LOCK, iterator);
	}

	public ShareableListIterator(ShareableLockable lockable, ListIterator<ELEMENT> iterator) {
		this(BaseShareableLockable.extractLock(lockable), iterator);
	}

	public ShareableListIterator(ReadWriteLock rwLock, ListIterator<ELEMENT> iterator) {
		super(rwLock, iterator);
		this.iterator = iterator;
	}

	@Override
	public boolean hasPrevious() {
		return shareLocked(this.iterator::hasPrevious);
	}

	@Override
	public ELEMENT previous() {
		return shareLocked(this.iterator::previous);
	}

	@Override
	public int nextIndex() {
		return shareLocked(this.iterator::nextIndex);
	}

	@Override
	public int previousIndex() {
		return shareLocked(this.iterator::previousIndex);
	}

	@Override
	public void set(ELEMENT e) {
		mutexLocked(() -> this.iterator.set(e));
	}

	@Override
	public void add(ELEMENT e) {
		mutexLocked(() -> this.iterator.add(e));
	}
}