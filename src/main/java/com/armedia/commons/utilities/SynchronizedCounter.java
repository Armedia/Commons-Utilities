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
package com.armedia.commons.utilities;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.function.Function;
import java.util.function.LongPredicate;
import java.util.function.LongUnaryOperator;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.armedia.commons.utilities.concurrent.BaseShareableLockable;
import com.armedia.commons.utilities.concurrent.MutexAutoLock;

/**
 * <p>
 * This is a simple, thread-safe synchronized value that allows semi-atomic modification operations,
 * and wait operations. The internal locking is carried out using methods from
 * {@link BaseShareableLockable} for maximum performance and concurrency. It also tracks the value
 * for the last time the value was changed, in nanoseconds.
 * </p>
 *
 *
 *
 */
public final class SynchronizedCounter extends BaseShareableLockable {
	private final Condition changed;
	private final Instant created;
	private long changes = 0;
	private Instant lastChange = null;
	private volatile long value = 0;

	/**
	 * <p>
	 * Create a new value with the starting value of 0. Identical to invoking
	 * {@link #SynchronizedCounter(long) new SynchronizedCounter(0)}.
	 * </p>
	 */
	public SynchronizedCounter() {
		this(0);
	}

	/**
	 * <p>
	 * Create a new value with the given starting value
	 * </p>
	 *
	 * @param start
	 *            the starting value for the value.
	 */
	public SynchronizedCounter(long start) {
		this.value = start;
		this.created = Instant.now();
		this.lastChange = this.created;
		this.changed = getMutexLock().newCondition();
	}

	/**
	 * <p>
	 * Returns the time at which the object was created, in nanoseconds (as returned by
	 * {@link System#nanoTime()}).
	 *
	 * @return the time at which the object was created, in nanoseconds
	 */
	public Instant getCreated() {
		return this.created;
	}

	/**
	 * <p>
	 * Returns the time at which the object was last changed, in nanoseconds (as returned by
	 * {@link System#nanoTime()}).
	 * </p>
	 *
	 * @return the time at which the object was last changed, in nanoseconds
	 */
	public Instant getLastChanged() {
		return shareLocked(() -> this.lastChange);
	}

	/**
	 * <p>
	 * Returns {@code true} if the object's value has changed since its creation, {@code false}
	 * otherwise. This doesn't take into account instances where the value's value has been re-set
	 * to its original value after the fact. This only reflects if the value's value has varied in
	 * any way since its creation.
	 * </p>
	 *
	 * @return {@code true} if the object's value has changed since its creation, {@code false}
	 *         otherwise
	 */
	public boolean isChangedSinceCreation() {
		return shareLocked(() -> ((this.changes != 0) || !this.created.equals(this.lastChange)));
	}

	/**
	 * <p>
	 * Return the value's current value
	 * </p>
	 *
	 * @return the value's current value
	 */
	public long get() {
		return shareLocked(() -> this.value);
	}

	/**
	 * <p>
	 * Set the value to {@code newValue}, and return the previous value
	 * </p>
	 *
	 * @return the previous value
	 */
	public long setAndGet(final long newValue) {
		return recompute((oldValue) -> (oldValue != newValue), (v) -> newValue).getLeft();
	}

	/**
	 * <p>
	 * Set the value to {@code newValue} if and only if the old value matches the given
	 * {@link LongPredicate#test(long) predicate}, and return {@code true} if the value was
	 * modified, {@code false} otherwise
	 * </p>
	 *
	 * @return {@code true} if the new value was set, {@code false} otherwise
	 */
	public boolean setIfMatches(LongPredicate predicate, final long newValue) {
		return recompute(predicate, (v) -> newValue).getMiddle();
	}

	/**
	 * <p>
	 * Calculate the new value based on the given {@link Function function}, and return a
	 * {@link Pair} that describes whether the value was {@link Pair#getLeft() recomputed}, and
	 * {@link Pair#getRight() the new value computed}.
	 * </p>
	 *
	 * @return the new value
	 */
	public long recompute(final LongUnaryOperator f) {
		return recomputeIfMatches((v) -> true, f);
	}

	/**
	 * <p>
	 * Calculate the new value based on the given {@link Function function}, but only if its current
	 * value matches the given {@link LongPredicate predicate}.
	 * </p>
	 *
	 * @return the new value
	 */
	public long recomputeIfMatches(LongPredicate predicate, final LongUnaryOperator f) {
		return recompute(predicate, f).getRight();
	}

	/**
	 * <p>
	 * Does the actual work for the {@link #recompute(LongUnaryOperator)},
	 * {@link #recomputeIfMatches(LongPredicate, LongUnaryOperator)}, {@link #setAndGet(long)} and
	 * {@link #setIfMatches(LongPredicate, long)}, returning a triple whose components are:
	 * <ul>
	 * <li>{@link Triple#getLeft() Left}: the old value</li>
	 * <li>{@link Triple#getMiddle() Middle}: a flag indicating whether the value was recomputed or
	 * not</li>
	 * <li>{@link Triple#getRight() Right}: the new value</li>
	 * </ul>
	 *
	 * @param predicate
	 * @param f
	 * @return a Triple as described above
	 */
	protected Triple<Long, Boolean, Long> recompute(LongPredicate predicate, final LongUnaryOperator f) {
		Objects.requireNonNull(f, "Must provide a function to compute the new value with");
		Objects.requireNonNull(predicate, "Must provide a predicate to test the current value with");
		final AtomicReference<Long> old = new AtomicReference<>(null);
		final AtomicBoolean recomputed = new AtomicBoolean(false);
		final Predicate<Long> p = (v) -> predicate.test(v);
		final Long v = shareLockedUpgradable(() -> this.value, p, (oldValue) -> {
			old.set(oldValue);
			final long newValue = f.applyAsLong(oldValue.longValue());
			this.value = newValue;
			this.lastChange = Instant.now();
			this.changes++;
			this.changed.signal();
			recomputed.set(true);
			return newValue;
		});
		return Triple.of(old.get(), recomputed.get(), v);
	}

	/**
	 * <p>
	 * Add the given {@code delta} to the current value. If the delta is 0, no change detected via
	 * {@link #waitUntilChanged()} or {@link #waitUntilValue(long)}.
	 * </p>
	 *
	 * @param delta
	 *            the amount to add to the value.
	 * @return the new value after applying the delta
	 */
	public long addAndGet(long delta) {
		try (MutexAutoLock lock = autoMutexLock()) {
			long ret = (this.value += delta);
			if (delta != 0) {
				// Only trigger the change if there actually was a change
				this.lastChange = Instant.now();
				this.changes++;
				this.changed.signal();
			}
			return ret;
		}
	}

	/**
	 * <p>
	 * Subtract the given delta from the value. Identical to invoking {@link #addAndGet(long)
	 * add(-delta)}
	 * </p>
	 *
	 * @param delta
	 *            the amount to subtract from the value
	 * @return the new value after applying the delta.
	 */
	public long subtractAndGet(long delta) {
		return addAndGet(-delta);
	}

	/**
	 * <p>
	 * Add one to the value. Identical to invoking {@link #addAndGet(long) add(1)}
	 * </p>
	 *
	 * @return the new value after applying the change.
	 */
	public long incrementAndGet() {
		return addAndGet(1);
	}

	/**
	 * <p>
	 * Subtract one from the value. Identical to invoking {@link #addAndGet(long) subtract(1)}
	 * </p>
	 *
	 * @return the new value after applying the change.
	 */
	public long decrementAndGet() {
		return subtractAndGet(1);
	}

	/**
	 * <p>
	 * Wait until the value's value matches the given {@code value}. Identical to invoking
	 * {@link #waitUntilValue(long, long, TimeUnit) waitUntil(value, 0, TimeUnit.SECONDS)}.
	 * </p>
	 *
	 * @param value
	 *            the value to wait for
	 * @throws InterruptedException
	 */
	public void waitUntilValue(final long value) throws InterruptedException {
		waitUntilValue(value, 0, TimeUnit.SECONDS);
	}

	/**
	 * <p>
	 * Wait until the value's value matches the given {@code value}, for up to the given timeout
	 * value. If the value of {@code timeout} is less than or equal to {@code 0}, then this method
	 * waits forever regardless of the value of {@code timeUnit}.
	 * </p>
	 *
	 * @param value
	 *            the value to wait for
	 * @param timeout
	 *            the number of {@link TimeUnit TimeUnits} to wait for
	 * @param timeUnit
	 *            the {@link TimeUnit} for the wait timeout
	 * @throws InterruptedException
	 */
	public boolean waitUntilValue(final long value, long timeout, TimeUnit timeUnit) throws InterruptedException {
		if (timeout > 0) {
			Objects.requireNonNull(timeUnit, "Must provide a TimeUnit for the waiting period");
		}
		try (MutexAutoLock lock = autoMutexLock()) {
			boolean success = true;
			while (value != this.value) {
				if (timeout > 0) {
					if (!this.changed.await(timeout, timeUnit)) {
						success = false;
					}
				} else {
					this.changed.await();
				}
			}
			// Cascade the signal for anyone else waiting...
			this.changed.signal();
			return success;
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
	public long waitUntilChanged() throws InterruptedException {
		try {
			return waitUntilChanged(0, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			throw new RuntimeException("Unexpected timeout - should have waited forever", e);
		}
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
	 * @throws TimeoutException
	 */
	public long waitUntilChanged(long timeout, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
		if (timeout > 0) {
			Objects.requireNonNull(timeUnit, "Must provide a TimeUnit for the waiting period");
		}
		try (MutexAutoLock lock = autoMutexLock()) {
			if (timeout > 0) {
				if (!this.changed.await(timeout, timeUnit)) {
					throw new TimeoutException(
						String.format("Timed out waiting %d %s for the value to change", timeout, timeUnit));
				}
			} else {
				this.changed.await();
			}
			final long ret = this.value;
			// Cascade the signal for anyone else waiting...
			this.changed.signal();
			return ret;
		}
	}

	@Override
	public String toString() {
		return String.format("SynchronizedCounter [created=%s, lastChange=%s, value=%s]", this.created, this.lastChange,
			this.value);
	}
}
