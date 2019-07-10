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

import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.function.CheckedRunnable;
import com.armedia.commons.utilities.function.CheckedSupplier;
import com.armedia.commons.utilities.function.CheckedTools;

/**
 * <p>
 * This interface exists as a behavior template to facilitate the implementation of recurring
 * boilerplate read-write-lockable code. The objective is to simplify the development of
 * high-throughput, high-concurrency applications that leverage read-write locking, while
 * simultaneously reducing the risk of programming errors in lock management.
 * </p>
 * <p>
 * This class provides behaviors that support lock upgrading (from read lock to write lock), as well
 * as double-check locking semantics (for initializers, for instance).
 * </p>
 *
 *
 *
 */
@FunctionalInterface
public interface MutexLockable {

	public static final Lock NULL_LOCK = null;

	public static Lock extractMutexLock(Object o) {
		Objects.requireNonNull(o, "Must provide a non-null Object from which to extract the Mutex lock");
		MutexLockable l = Tools.cast(MutexLockable.class, o);
		if (l != null) { return l.getMutexLock(); }
		return Tools.cast(Lock.class, o);
	}

	/**
	 * <p>
	 * Returns the mutex lock. Contrary to {@link #acquireMutexLock()}, no attempt is made to
	 * acquire the lock before returning it.
	 * </p>
	 *
	 * @return the mutex lock
	 */
	public Lock getMutexLock();

	/**
	 * <p>
	 * Returns a new {@link Condition} instance based on the mutex lock.
	 * </p>
	 *
	 * @return a new {@link Condition} instance based on the mutex lock
	 */
	public default Condition newMutexCondition() {
		return getMutexLock().newCondition();
	}

	/**
	 * <p>
	 * Return the mutex lock. The lock is already held when it's returned so this method may block
	 * while other threads hold the lock.
	 * </p>
	 *
	 * @return the held mutex lock
	 */
	public default Lock acquireMutexLock() {
		Lock ret = getMutexLock();
		ret.lock();
		return ret;
	}

	/**
	 * <p>
	 * Return the mutex lock, wrapped inside an {@link MutexAutoLock} instance for use in
	 * try-with-resources constructs. The lock is already held when it's returned so this method may
	 * block while other threads hold the lock.
	 * </p>
	 *
	 * @return the held mutex lock, wrapped inside an {@link MutexAutoLock}
	 */
	public default MutexAutoLock autoMutexLock() {
		return new MutexAutoLock(this);
	}

	/**
	 * <p>
	 * Execute the given operation within the context of an exclusive (write) lock, returning the
	 * result of {@link Supplier#get()}. The lock is acquired and released automatically.
	 * </p>
	 *
	 * @param operation
	 * @throws NullPointerException
	 *             if {@code operation} is {@code null}
	 */
	public default <E> E mutexLocked(Supplier<E> operation) {
		return mutexLocked(CheckedTools.check(operation));
	}

	/**
	 * <p>
	 * Execute the given operation within the context of an exclusive (write) lock, and return the
	 * result of {@link CheckedSupplier#getChecked()}. The lock is acquired and released
	 * automatically. Any raised exceptions are cascaded upward.
	 * </p>
	 *
	 * @param operation
	 * @throws NullPointerException
	 *             if {@code operation} is {@code null}
	 */
	public default <E, EX extends Throwable> E mutexLocked(CheckedSupplier<E, EX> operation) throws EX {
		Objects.requireNonNull(operation, "Must provide a non-null operation to invoke");
		try (MutexAutoLock lock = autoMutexLock()) {
			return operation.getChecked();
		}
	}

	/**
	 * <p>
	 * Execute the given operation within the context of an exclusive (write) lock. The lock is
	 * acquired and released automatically.
	 * </p>
	 *
	 * @param operation
	 * @throws NullPointerException
	 *             if {@code operation} is {@code null}
	 */
	public default void mutexLocked(Runnable operation) {
		Objects.requireNonNull(operation, "Must provide an operation to run");
		mutexLocked(CheckedTools.check(operation));
	}

	/**
	 * <p>
	 * Execute the given operation within the context of an exclusive (write) lock. The lock is
	 * acquired and released automatically. Any raised exceptions are cascaded upward.
	 * </p>
	 *
	 * @param operation
	 * @throws NullPointerException
	 *             if {@code operation} is {@code null}
	 */
	public default <EX extends Throwable> void mutexLocked(CheckedRunnable<EX> operation) throws EX {
		Objects.requireNonNull(operation, "Must provide a non-null operation to invoke");
		mutexLocked(() -> {
			operation.runChecked();
			return null;
		});
	}
}
