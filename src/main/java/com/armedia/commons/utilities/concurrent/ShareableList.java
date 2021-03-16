/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2021 Armedia, LLC
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.UnaryOperator;

public class ShareableList<ELEMENT> extends ShareableCollection<ELEMENT> implements List<ELEMENT> {

	private final List<ELEMENT> list;

	public ShareableList(List<ELEMENT> list) {
		this(ShareableLockable.extractShareableLock(list), list);
	}

	public ShareableList(ShareableLockable lockable, List<ELEMENT> list) {
		this(ShareableLockable.extractShareableLock(lockable), list);
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

	@Override
	public void replaceAll(UnaryOperator<ELEMENT> operator) {
		Objects.requireNonNull(operator, "Must provide a non-null operator");
		try (MutexAutoLock lock = autoMutexLock()) {
			final ListIterator<ELEMENT> li = listIterator();
			while (li.hasNext()) {
				li.set(operator.apply(li.next()));
			}
		}
	}

	@Override
	public void sort(Comparator<? super ELEMENT> c) {
		Objects.requireNonNull(c, "Must provide a non-null comparator");
		try (MutexAutoLock lock = autoMutexLock()) {
			ELEMENT[] a = toArray(ShareableCollection.noElements());
			Arrays.sort(a, c);
			ListIterator<ELEMENT> i = listIterator();
			for (ELEMENT e : a) {
				i.next();
				i.set(e);
			}
		}
	}
}
