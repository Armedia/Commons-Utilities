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
package com.armedia.commons.utilities;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.function.Consumer;

import com.armedia.commons.utilities.concurrent.BaseShareableLockable;
import com.armedia.commons.utilities.concurrent.MutexAutoLock;
import com.armedia.commons.utilities.concurrent.SharedAutoLock;

/**
 * <p>
 * This class provides a simple mechanism to implement a simple producer-consumer pattern with
 * read-write lock synchronization semantics. That means that a mutex lock will only be acquired
 * when necessary, maximizing read concurrency.
 * </p>
 * <p>
 * However, this class differs from the typical producer-consumer pattern in that you can only set
 * the value once, though it can be read multiple times. The object's state can be cleared so that
 * another value can be stored inside.
 * </p>
 * <p>
 * The class supports {@code null} as a valid value, as well as checking to see if the value is
 * already set.
 * </p>
 *
 * @author diego.rivera@armedia.com
 *
 * @param <T>
 */
public class DelayedSupplier<T> extends BaseShareableLockable implements Consumer<T> {

	private volatile boolean set = false;
	private T value = null;
	private final Condition condition;

	/**
	 * <p>
	 * Construct a new, uninitialized instance.
	 * </p>
	 */
	public DelayedSupplier() {
		this.condition = super.newMutexCondition();
		this.set = false;
		this.value = null;
	}

	/**
	 * <p>
	 * Construct a new, initialized instance that already has a value set.
	 * </p>
	 *
	 * @param value
	 *            the value to set
	 */
	public DelayedSupplier(T value) {
		this.condition = super.newMutexCondition();
		this.set = true;
		this.value = value;
	}

	/**
	 * <p>
	 * Clear the currently-stored value.
	 * </p>
	 */
	public final void clear() {
		try (MutexAutoLock lock = mutexAutoLock()) {
			this.set = false;
			this.value = null;
		}
	}

	/**
	 * <p>
	 * Returns {@code true} if the value has been set (i.e. if {@link #get()} or
	 * {@link #get(long, TimeUnit)} would block if called), {@code false} otherwise.
	 * </p>
	 *
	 * @return {@code true} if the value has been set, {@code false} otherwise
	 */
	public final boolean isSet() {
		return shareLocked(() -> this.set);
	}

	/**
	 * <p>
	 * Simple method to support the {@link Consumer} interface.
	 * </p>
	 */
	@Override
	public final void accept(T t) {
		set(t);
	}

	/**
	 * <p>
	 * Sets the value, and signals to any threads waiting on {@link #get()} or
	 * {@link #get(long, TimeUnit)} that the value has been set.
	 * </p>
	 *
	 * @param t
	 *            the value to set
	 * @throws IllegalStateException
	 *             if the value is already set
	 */
	public final void set(T t) {
		try (MutexAutoLock lock = mutexAutoLock()) {
			if (this.set) { throw new IllegalStateException("The value has already been submitted"); }
			this.set = true;
			this.value = t;
			this.condition.signal();
		}
	}

	/**
	 * <p>
	 * Returns the stored value, or blocks forever waiting for another thread to set it via
	 * {@link #set(Object)}. Identical to invoking {@link #get(long, TimeUnit) get(0, null)}
	 * </p>
	 *
	 * @return the value
	 * @throws InterruptedException
	 *             if the thread is interrupted while waiting for the value to be set
	 */
	public final T get() throws InterruptedException {
		try {
			return get(0, null);
		} catch (TimeoutException e) {
			throw new RuntimeException("Shouldn't be timing out - this waits forever!");
		}
	}

	/**
	 * <p>
	 * Returns the stored value, or blocks for the given time interval waiting for another thread to
	 * set it via {@link #set(Object)}. If {@code timeUnit} is {@code null}, or {@code amount} is
	 * less than or equal to zero, this method blocks indefinitely.
	 * </p>
	 *
	 * @param amount
	 * @param timeUnit
	 * @return the value
	 * @throws InterruptedException
	 *             if the thread is interrupted while waiting for the value to be set
	 * @throws TimeoutException
	 *             if the specified time interval has detectably elapsed, but no value has been set
	 */
	public final T get(long amount, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
		try (SharedAutoLock shared = sharedAutoLock()) {
			if (!this.set) {
				try (MutexAutoLock lock = shared.upgrade()) {
					if (!this.set) {
						try {
							if ((timeUnit == null) || (amount <= 0)) {
								this.condition.await();
							} else {
								if (!this.condition.await(amount, timeUnit)) {
									throw new TimeoutException(
										String.format("The interval of %d %s has expired", amount, timeUnit));
								}
							}
						} finally {
							this.condition.signal();
						}
					}
				}
			}
			return this.value;
		}
	}
}
