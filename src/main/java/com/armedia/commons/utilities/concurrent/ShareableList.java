package com.armedia.commons.utilities.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;

public class ShareableList<ELEMENT> extends ShareableCollection<ELEMENT> implements List<ELEMENT> {

	private final List<ELEMENT> list;

	public ShareableList(List<ELEMENT> list) {
		this(ShareableCollection.extractLock(list), list);
	}

	public ShareableList(ShareableLockable lockable, List<ELEMENT> list) {
		this(BaseShareableLockable.extractLock(lockable), list);
	}

	public ShareableList(ReadWriteLock rwLock, List<ELEMENT> list) {
		super(rwLock, list);
		this.list = list;
	}

	@Override
	public boolean addAll(int index, Collection<? extends ELEMENT> c) {
		Objects.requireNonNull(c, "Must provide a non-null Collection to add from");
		if (c.isEmpty()) { return false; }
		return mutexLocked(() -> this.list.addAll(index, c));
	}

	@Override
	public ELEMENT get(int index) {
		return shareLocked(() -> this.list.get(index));
	}

	@Override
	public ELEMENT set(int index, ELEMENT element) {
		return mutexLocked(() -> this.list.set(index, element));
	}

	@Override
	public void add(int index, ELEMENT element) {
		mutexLocked(() -> this.list.add(index, element));
	}

	@Override
	public ELEMENT remove(int index) {
		return mutexLocked(() -> this.list.remove(index));
	}

	@Override
	public int indexOf(Object o) {
		return shareLocked(() -> this.list.indexOf(o));
	}

	@Override
	public int lastIndexOf(Object o) {
		return shareLocked(() -> this.list.lastIndexOf(o));
	}

	@Override
	public ListIterator<ELEMENT> listIterator() {
		return shareLocked(() -> new ShareableListIterator<>(this, this.list.listIterator()));
	}

	@Override
	public ListIterator<ELEMENT> listIterator(int index) {
		return shareLocked(() -> new ShareableListIterator<>(this, this.list.listIterator(index)));
	}

	@Override
	public List<ELEMENT> subList(int fromIndex, int toIndex) {
		return shareLocked(() -> new ShareableList<>(this, this.list.subList(fromIndex, toIndex)));
	}
}