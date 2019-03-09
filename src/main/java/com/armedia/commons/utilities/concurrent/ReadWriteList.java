package com.armedia.commons.utilities.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

public class ReadWriteList<ELEMENT> extends ReadWriteCollection<ELEMENT> implements List<ELEMENT> {

	private final List<ELEMENT> list;

	public ReadWriteList(List<ELEMENT> list) {
		this(ReadWriteLockable.NULL_LOCK, list, null);
	}

	public ReadWriteList(ReadWriteLockable lockable, List<ELEMENT> list) {
		this(BaseReadWriteLockable.extractLock(lockable), list, null);
	}

	public ReadWriteList(ReadWriteLock rwLock, List<ELEMENT> list) {
		this(rwLock, list, null);
	}

	public ReadWriteList(List<ELEMENT> list, Function<ELEMENT, ELEMENT> canonicalizer) {
		this(ReadWriteLockable.NULL_LOCK, list, canonicalizer);
	}

	public ReadWriteList(ReadWriteLockable lockable, List<ELEMENT> list, Function<ELEMENT, ELEMENT> canonicalizer) {
		this(BaseReadWriteLockable.extractLock(lockable), list, canonicalizer);
	}

	public ReadWriteList(ReadWriteLock rwLock, List<ELEMENT> list, Function<ELEMENT, ELEMENT> canonicalizer) {
		super(rwLock, list, canonicalizer);
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
		ELEMENT E = canonicalize(element);
		return writeLocked(() -> this.list.set(index, E));
	}

	@Override
	public void add(int index, ELEMENT element) {
		ELEMENT E = canonicalize(element);
		writeLocked(() -> this.list.add(index, E));
	}

	@Override
	public ELEMENT remove(int index) {
		return writeLocked(() -> this.list.remove(index));
	}

	@Override
	public int indexOf(Object o) {
		ELEMENT E = canonicalizeObject(o);
		return readLocked(() -> this.list.indexOf(E));
	}

	@Override
	public int lastIndexOf(Object o) {
		ELEMENT E = canonicalizeObject(o);
		return readLocked(() -> this.list.lastIndexOf(E));
	}

	@Override
	public ListIterator<ELEMENT> listIterator() {
		return readLocked(
			() -> new ReadWriteListIterator<>(getMainLock(), this.list.listIterator(), this.canonicalizer));
	}

	@Override
	public ListIterator<ELEMENT> listIterator(int index) {
		return readLocked(
			() -> new ReadWriteListIterator<>(getMainLock(), this.list.listIterator(index), this.canonicalizer));
	}

	@Override
	public List<ELEMENT> subList(int fromIndex, int toIndex) {
		return readLocked(() -> new ReadWriteList<>(getMainLock(), this.list.subList(fromIndex, toIndex)));
	}
}