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