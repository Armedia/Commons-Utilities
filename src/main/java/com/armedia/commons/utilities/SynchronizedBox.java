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
package com.armedia.commons.utilities;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.function.Predicate;

import com.armedia.commons.utilities.concurrent.BaseShareableLockable;
import com.armedia.commons.utilities.concurrent.MutexAutoLock;

/**
 * <p>
 * This is a simple, thread-safe synchronized value that allows semi-atomic modification operations,
 * and wait operations. The internal locking is carried out using methods from
 * {@link BaseShareableLockable} for maximum performance and concurrency. It also tracks the value
 * for the last time the value was changed, in nanoseconds.
 * </p>
 */
public final class SynchronizedBox<V> extends BaseShareableLockable {

	private final Condition changed;
	private final Instant created;
	private Instant lastChange = null;
	private volatile V value = null;

	/**
	 * <p>
	 * Create a new value with the starting value of 0. Identical to invoking
	 * {@link #SynchronizedBox(Object) new SynchronizedBox(null)}.
	 * </p>
	 */
	public SynchronizedBox() {
		this(null);
	}

	/**
	 * <p>
	 * Create a new value with the given starting value
	 * </p>
	 *
	 * @param start
	 *            the starting value for the value.
	 */
	public SynchronizedBox(V start) {
		this.value = start;
		this.created = Instant.now();
		this.lastChange = this.created;
		this.changed = getMutexLock().newCondition();
	}

	/**
	 * <p>
	 * Returns the {@link Instant} at which the object was created.
	 *
	 * @return the Instant at which the object was created, in nanoseconds
	 */
	public Instant getCreated() {
		return this.created;
	}

	/**
	 * <p>
	 * Returns the {@link Instant} at which the object was last changed.
	 * </p>
	 *
	 * @return the Instant at which the object was last changed, in nanoseconds
	 */
	public Instant getLastChanged() {
		return shareLocked(() -> this.lastChange);
	}

	/**
	 * <p>
	 * Returns {@code true} if the object's value has changed since its creation, {@code false}
	 * otherwise. This doesn't take into account instances where the value has been re-set to its
	 * original value after the fact. This only reflects if the value has varied in any way since
	 * its creation.
	 * </p>
	 *
	 * @return {@code true} if the object's value has changed since its creation, {@code false}
	 *         otherwise
	 */
	public boolean isChangedSinceCreation() {
		return shareLocked(() -> (!this.created.equals(this.lastChange)));
	}

	/**
	 * <p>
	 * Return the current value
	 * </p>
	 *
	 * @return the current value
	 */
	public V get() {
		return shareLocked(() -> this.value);
	}

	/**
	 * <p>
	 * Set the value to {@code newValue}, and return the previous value
	 * </p>
	 *
	 * @return the previous value
	 */
	public V set(final V newValue) {
		return shareLockedUpgradable(() -> this.value, (stored) -> (stored == newValue), (stored) -> {
			this.value = newValue;
			this.lastChange = Instant.now();
			this.changed.signal();
			return stored;
		});
	}

	/**
	 * <p>
	 * Wait until the given {@code predicate} evaluates to {@code true} using the box's value.
	 * Identical to invoking {@link #waitUntilMatches(Predicate, long, TimeUnit)
	 * waitUntil(predicate, 0, TimeUnit.SECONDS)}.
	 * </p>
	 *
	 * @param predicate
	 *            the predicate to use for checking if the wait is over
	 * @throws InterruptedException
	 */
	public void waitUntilMatches(final Predicate<V> predicate) throws InterruptedException {
		waitUntilMatches(predicate, 0, TimeUnit.SECONDS);
	}

	/**
	 * <p>
	 * Wait until the given {@code predicate} evaluates to {@code true} using the box's value, for
	 * up to the given timeout value. If the value of {@code timeout} is less than or equal to
	 * {@code 0}, then this method waits forever regardless of the value of {@code timeUnit}.
	 * </p>
	 *
	 * @param predicate
	 *            the predicate to use for checking if the wait is over
	 * @param timeout
	 *            the number of {@link TimeUnit TimeUnits} to wait for
	 * @param timeUnit
	 *            the {@link TimeUnit} for the wait timeout
	 * @throws InterruptedException
	 */
	public void waitUntilMatches(final Predicate<V> predicate, long timeout, TimeUnit timeUnit)
		throws InterruptedException {
		Objects.requireNonNull(predicate, "Must provide a predicate to check the value with");
		if (timeout > 0) {
			Objects.requireNonNull(timeUnit, "Must provide a TimeUnit for the waiting period");
		}
		try (MutexAutoLock lock = autoMutexLock()) {
			while (!predicate.test(this.value)) {
				if (timeout > 0) {
					this.changed.await(timeout, timeUnit);
				} else {
					this.changed.await();
				}
			}
			// Cascade the signal for anyone else waiting...
			this.changed.signal();
		}
	}

	/**
	 * <p>
	 * Wait until the value's value changes. Identical to invoking
	 * {@link #waitUntilChanged(long, TimeUnit) waitForChange(0, TimeUnit.SECONDS)}.
	 * </p>
	 *
	 * @return the new value after the detected change.
	 * @throws InterruptedException
	 */
	public V waitUntilChanged() throws InterruptedException {
		return waitUntilChanged(0, TimeUnit.SECONDS);
	}

	/**
	 * <p>
	 * Wait until the value's value changes, for up to the given timeout value. If the value of
	 * {@code timeout} is less than or equal to {@code 0}, then this method waits forever regardless
	 * of the value of {@code timeUnit}.
	 * </p>
	 *
	 * @param timeout
	 *            the number of {@link TimeUnit TimeUnits} to wait for
	 * @param timeUnit
	 *            the {@link TimeUnit} for the wait timeout
	 * @return the new value after the detected change.
	 * @throws InterruptedException
	 */
	public V waitUntilChanged(long timeout, TimeUnit timeUnit) throws InterruptedException {
		if (timeout > 0) {
			Objects.requireNonNull(timeUnit, "Must provide a TimeUnit for the waiting period");
		}
		try (MutexAutoLock lock = autoMutexLock()) {
			if (timeout > 0) {
				this.changed.await(timeout, timeUnit);
			} else {
				this.changed.await();
			}
			try {
				return this.value;
			} finally {
				// Cascade the signal for anyone else waiting...
				this.changed.signal();
			}
		}
	}

	/**
	 * <p>
	 * Blocks (using {@link Object#wait()}) until the given number of changes are counted. If
	 * {@code count} is less than or equal to 0, it will return immediately without blocking, with
	 * the current value of the counter.
	 * </p>
	 *
	 * @param count
	 * @return the value of the counter at the last change counted
	 * @throws InterruptedException
	 */
	public V waitUntilChangeCount(int count) throws InterruptedException {
		if (count <= 0) { return get(); }
		V ret = null;
		for (int i = 0; i < count; i++) {
			ret = waitUntilChanged();
		}
		return ret;
	}

	@Override
	public String toString() {
		return String.format("SynchronizedBox [created=%s, lastChange=%s, value=%s]", this.created, this.lastChange,
			this.value);
	}
}
