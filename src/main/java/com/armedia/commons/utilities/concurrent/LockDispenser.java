/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2020 Armedia, LLC
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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 *
 *
 */
public final class LockDispenser<K, C> {

	/**
	 * <p>
	 * Deprecated in favor of {@link #synchronizableObject()}
	 * </p>
	 *
	 * @return the same as {@link #synchronizableObject()}
	 */
	@Deprecated
	public static <K> LockDispenser<K, Object> getBasic() {
		return LockDispenser.basic();
	}

	public static <K> LockDispenser<K, Object> basic() {
		return LockDispenser.synchronizableObject();
	}

	public static <K> LockDispenser<K, Object> synchronizableObject() {
		return new LockDispenser<>((k) -> new Object());
	}

	public static <K> LockDispenser<K, ReentrantLock> reentrantLock() {
		return new LockDispenser<>((k) -> new ReentrantLock());
	}

	public static <K> LockDispenser<K, MutexLockable> mutexLockable() {
		return new LockDispenser<>((k) -> new BaseMutexLockable());
	}

	public static <K> LockDispenser<K, ReentrantReadWriteLock> reentrantReadWriteLock() {
		return new LockDispenser<>((k) -> new ReentrantReadWriteLock());
	}

	public static <K> LockDispenser<K, ShareableLockable> shareableLockable() {
		return new LockDispenser<>((k) -> new BaseShareableLockable());
	}

	private class LockBox {
		private final K key;

		private Reference<C> lock = null;

		private LockBox(K key) {
			this.key = key;
		}

		private synchronized C get() {
			if ((this.lock == null) || (this.lock.get() == null)) {
				C lock = LockDispenser.this.lockBuilder.apply(this.key);
				if (lock == null) {
					throw new RuntimeException("The LockBuilder must always return a non-null lock object instance");
				}
				this.lock = LockDispenser.this.referenceBuilder.apply(lock);
				if (this.lock == null) {
					throw new RuntimeException(
						"The ReferenceBuilder must always return a non-null Reference<> instance");
				}
			}
			return this.lock.get();
		}
	}

	private final Function<K, C> lockBuilder;
	private final Function<C, Reference<C>> referenceBuilder;
	private final ConcurrentMap<K, LockBox> locks = new ConcurrentHashMap<>();

	public LockDispenser(Function<K, C> lockBuilder) {
		this(lockBuilder, WeakReference::new);
	}

	public LockDispenser(Function<K, C> lockBuilder, Function<C, Reference<C>> referenceBuilder) {
		this.lockBuilder = Objects.requireNonNull(lockBuilder, "Must provide a non-null LockBuilder instance");
		this.referenceBuilder = Objects.requireNonNull(referenceBuilder,
			"Must provide a non-null ReferenceBuilder instance");
	}

	private LockBox newBox(final K key) {
		return new LockBox(key);
	}

	public C getLock(final K key) {
		return ConcurrentTools.createIfAbsent(this.locks, key, this::newBox).get();
	}
}
