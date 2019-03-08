package com.armedia.commons.utilities;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;

import com.armedia.commons.utilities.concurrent.BaseReadWriteLockable;

/**
 * <p>
 * This is a simple, thread-safe synchronized value that allows semi-atomic modification operations,
 * and wait operations. The internal locking is carried out using a {@link ReadWriteLock} for
 * maximum performance and concurrency. It also tracks the value for the last time the value was
 * changed, in nanoseconds.
 * </p>
 *
 * @author Diego Rivera &lt;diego.rivera@armedia.com&gt;
 *
 */
public final class SynchronizedCounter extends BaseReadWriteLockable {
	private final Condition changed;
	private final long created;
	private long lastChange = 0;
	private long value = 0;

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
		this.created = System.nanoTime();
		this.lastChange = this.created;
		this.changed = getWriteLock().newCondition();
	}

	/**
	 * <p>
	 * Returns the time at which the object was created, in nanoseconds (as returned by
	 * {@link System#nanoTime()}).
	 *
	 * @return the time at which the object was created, in nanoseconds
	 */
	public long getCreated() {
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
	public long getLastChanged() {
		return readLocked(() -> this.lastChange);
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
		return readLocked(() -> (this.lastChange != this.created));
	}

	/**
	 * <p>
	 * Return the value's current value
	 * </p>
	 *
	 * @return the value's current value
	 */
	public long get() {
		return readLocked(() -> this.value);
	}

	/**
	 * <p>
	 * Set the value's value to the new {@code value}, and return its current value
	 * </p>
	 *
	 * @return the value's current value
	 */
	public long set(long value) {
		return writeLocked(() -> {
			final long ret = this.value;
			this.value = value;
			if (value != ret) {
				// Only trigger the change if there actually was a change
				this.lastChange = System.nanoTime();
				this.changed.signal();
			}
			return ret;
		});
	}

	/**
	 * <p>
	 * Add the given {@code delta} to the current value. If the delta is 0, no change detected via
	 * {@link #waitForChange()} or {@link #waitUntil(long)}.
	 * </p>
	 *
	 * @param delta
	 *            the amount to add to the value.
	 * @return the new value after applying the delta
	 */
	public long add(long delta) {
		return writeLocked(() -> {
			long ret = (this.value += delta);
			if (delta != 0) {
				// Only trigger the change if there actually was a change
				this.lastChange = System.nanoTime();
				this.changed.signal();
			}
			return ret;
		});
	}

	/**
	 * <p>
	 * Subtract the given delta from the value. Identical to invoking {@link #add(long) add(-delta)}
	 * </p>
	 *
	 * @param delta
	 *            the amount to subtract from the value
	 * @return the new value after applying the delta.
	 */
	public long subtract(long delta) {
		return add(-delta);
	}

	/**
	 * <p>
	 * Add one to the value. Identical to invoking {@link #add(long) add(1)}
	 * </p>
	 *
	 * @return the new value after applying the change.
	 */
	public long increment() {
		return add(1);
	}

	/**
	 * <p>
	 * Subtract one from the value. Identical to invoking {@link #add(long) subtract(1)}
	 * </p>
	 *
	 * @return the new value after applying the change.
	 */
	public long decrement() {
		return subtract(1);
	}

	/**
	 * <p>
	 * Wait until the value's value matches the given {@code value}. Identical to invoking
	 * {@link #waitUntil(long, long, TimeUnit) waitUntil(value, 0, TimeUnit.SECONDS)}.
	 * </p>
	 *
	 * @param value
	 *            the value to wait for
	 * @throws InterruptedException
	 */
	public void waitUntil(final long value) throws InterruptedException {
		waitUntil(value, 0, TimeUnit.SECONDS);
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
	public void waitUntil(final long value, long timeout, TimeUnit timeUnit) throws InterruptedException {
		writeLockedChecked(() -> {
			while (value != this.value) {
				if (timeout > 0) {
					this.changed.await(timeout, timeUnit);
				} else {
					this.changed.await();
				}
			}
			// Cascade the signal for anyone else waiting...
			this.changed.signal();
		});
	}

	/**
	 * <p>
	 * Wait until the value's value changes. Identical to invoking
	 * {@link #waitForChange(long, TimeUnit) waitForChange(0, TimeUnit.SECONDS)}.
	 * </p>
	 *
	 * @return the new value after the detected change.
	 * @throws InterruptedException
	 */
	public long waitForChange() throws InterruptedException {
		return waitForChange(0, TimeUnit.SECONDS);
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
	public long waitForChange(long timeout, TimeUnit timeUnit) throws InterruptedException {
		return writeLockedChecked(() -> {
			if (timeout > 0) {
				this.changed.await(timeout, timeUnit);
			} else {
				this.changed.await();
			}
			final long ret = this.value;
			// Cascade the signal for anyone else waiting...
			this.changed.signal();
			return ret;
		});
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
	public long waitUntilChangeCount(int count) throws InterruptedException {
		if (count <= 0) { return get(); }
		long ret = 0;
		for (int i = 0; i < count; i++) {
			ret = waitForChange();
		}
		return ret;
	}

	@Override
	public String toString() {
		return String.format("SynchronizedCounter [created=%s, lastChange=%s, value=%s]", this.created, this.lastChange,
			this.value);
	}
}