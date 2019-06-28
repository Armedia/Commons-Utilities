/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (c) 2010 - 2019 Armedia LLC
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

import java.util.ListIterator;
import java.util.concurrent.locks.ReadWriteLock;

public class ShareableListIterator<ELEMENT> extends ShareableIterator<ELEMENT> implements ListIterator<ELEMENT> {

	private final ListIterator<ELEMENT> iterator;

	public ShareableListIterator(ListIterator<ELEMENT> iterator) {
		this(ShareableLockable.extractShareableLock(iterator), iterator);
	}

	public ShareableListIterator(ShareableLockable lockable, ListIterator<ELEMENT> iterator) {
		this(ShareableLockable.extractShareableLock(lockable), iterator);
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