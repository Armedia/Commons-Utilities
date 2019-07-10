/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2019 Armedia, LLC
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

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;

public class ShareableSpliterator<E> extends BaseShareableLockable implements Spliterator<E> {

	private final Spliterator<E> spliterator;

	public ShareableSpliterator(Spliterator<E> spliterator) {
		this(ShareableLockable.extractShareableLock(spliterator), spliterator);
	}

	public ShareableSpliterator(ReadWriteLock rwLock, Spliterator<E> spliterator) {
		super(rwLock);
		this.spliterator = Objects.requireNonNull(spliterator, "Must provide a non-null backing spliterator");
	}

	public ShareableSpliterator(ShareableLockable lockable, Spliterator<E> spliterator) {
		super(lockable);
		this.spliterator = Objects.requireNonNull(spliterator, "Must provide a non-null backing spliterator");
	}

	@Override
	public boolean tryAdvance(Consumer<? super E> action) {
		Objects.requireNonNull(action, "Must provide an action to apply on advancement");
		return shareLocked(() -> this.spliterator.tryAdvance(action));
	}

	@Override
	public void forEachRemaining(Consumer<? super E> action) {
		Objects.requireNonNull(action, "Must provide an action to apply on iteration");
		shareLocked(() -> this.spliterator.forEachRemaining(action));
	}

	@Override
	public Spliterator<E> trySplit() {
		return shareLocked(() -> {
			Spliterator<E> it = this.spliterator.trySplit();
			return (it != null ? new ShareableSpliterator<>(this, it) : null);
		});
	}

	@Override
	public long estimateSize() {
		return shareLocked(this.spliterator::estimateSize);
	}

	@Override
	public long getExactSizeIfKnown() {
		return shareLocked(this.spliterator::getExactSizeIfKnown);
	}

	@Override
	public int characteristics() {
		return this.spliterator.characteristics();
	}

	@Override
	public boolean hasCharacteristics(int characteristics) {
		return this.spliterator.hasCharacteristics(characteristics);
	}

	@Override
	public Comparator<? super E> getComparator() {
		return this.spliterator.getComparator();
	}
}
