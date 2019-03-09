package com.armedia.commons.utilities.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;

public class ReadWriteList<ELEMENT> extends ReadWriteCollection<ELEMENT> implements List<ELEMENT> {

	private final List<ELEMENT> list;

	public ReadWriteList(List<ELEMENT> list) {
		this(ReadWriteLockable.NULL_LOCK, list);
	}

	public ReadWriteList(ReadWriteLockable lockable, List<ELEMENT> list) {
		this(BaseReadWriteLockable.extractLock(lockable), list);
	}

	public ReadWriteList(ReadWriteLock rwLock, List<ELEMENT> list) {
		super(rwLock, list);
		this.list = list;
	}

	@Override
	public boolean addAll(int index, Collection<? extends ELEMENT> c) {
		Objects.requireNonNull(c, "Must provide a non-null Collection to add from");
		if (c.isEmpty()) { return false; }
		return writeLocked(() -> this.list.addAll(index, c));
	}

	@Override
	public ELEMENT get(int index) {
		return readLocked(() -> this.list.get(index));
	}

	@Override
	public ELEMENT set(int index, ELEMENT element) {
		return writeLocked(() -> this.list.set(index, element));
	}

	@Override
	public void add(int index, ELEMENT element) {
		writeLocked(() -> this.list.add(index, element));
	}

	@Override
	public ELEMENT remove(int index) {
		return writeLocked(() -> this.list.remove(index));
	}

	@Override
	public int indexOf(Object o) {
		return readLocked(() -> this.list.indexOf(o));
	}

	@Override
	public int lastIndexOf(Object o) {
		return readLocked(() -> this.list.lastIndexOf(o));
	}

	@Override
	public ListIterator<ELEMENT> listIterator() {
		return readLocked(() -> new ReadWriteListIterator<>(getMainLock(), this.list.listIterator()));
	}

	@Override
	public ListIterator<ELEMENT> listIterator(int index) {
		return readLocked(() -> new ReadWriteListIterator<>(getMainLock(), this.list.listIterator(index)));
	}

	@Override
	public List<ELEMENT> subList(int fromIndex, int toIndex) {
		return readLocked(() -> new ReadWriteList<>(getMainLock(), this.list.subList(fromIndex, toIndex)));
	}
}