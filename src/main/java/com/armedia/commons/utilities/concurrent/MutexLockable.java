package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import com.armedia.commons.utilities.function.CheckedRunnable;
import com.armedia.commons.utilities.function.CheckedSupplier;

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
 * @author diego
 *
 */
@FunctionalInterface
public interface MutexLockable {

	public static final Lock NULL_LOCK = null;

	/**
	 * <p>
	 * Returns the mutex lock
	 * </p>
	 *
	 * @return the mutex lock
	 */
	public Lock getMutexLock();

	/**
	 * <p>
	 * Return a reference to the lock. The lock is already held when it's returned so this method
	 * may block while other threads hold the lock.
	 * </p>
	 *
	 * @return the (held) mutex lock
	 */
	public default Lock acquireMutexLock() {
		Lock ret = getMutexLock();
		ret.lock();
		return ret;
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
		Objects.requireNonNull(operation, "Must provide an operation to run");
		return mutexLocked(() -> operation.get());
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
		final Lock l = acquireMutexLock();
		try {
			return operation.getChecked();
		} finally {
			l.unlock();
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
		mutexLocked(() -> operation.run());
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
		final Lock l = acquireMutexLock();
		try {
			operation.runChecked();
		} finally {
			l.unlock();
		}
	}
}