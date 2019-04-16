package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.armedia.commons.utilities.function.CheckedBiConsumer;
import com.armedia.commons.utilities.function.CheckedBiFunction;
import com.armedia.commons.utilities.function.CheckedConsumer;
import com.armedia.commons.utilities.function.CheckedFunction;
import com.armedia.commons.utilities.function.CheckedPredicate;
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
 * @author diego
 *
 */
@FunctionalInterface
public interface ShareableLockable extends MutexLockable {

	public static final ReadWriteLock NULL_LOCK = null;

	/**
	 * <p>
	 * Return a reference to the write (exclusive) lock. Contrary to {@link #acquireMutexLock()}, no
	 * attempt is made to acquire the lock before returning it.
	 * </p>
	 *
	 * @return the write lock
	 */
	@Override
	public default Lock getMutexLock() {
		return getShareableLock().writeLock();
	}

	/**
	 * <p>
	 * Return a reference to the lock. The lock is already held when it's returned so this method
	 * may block while other threads hold the lock.
	 * </p>
	 * <p>
	 * If {@link #getShareableLock()} returns an instance of {@link ReentrantReadWriteLock}, this
	 * implementation performs a basic reentrant-deadlock check to make sure the caller does not
	 * already hold the read lock before attempting to acquire the write lock, since this is not
	 * allowed and will result in a deadlock. This checking only works {@link #getMutexLock()}
	 * returns the same lock obtained via {@link ReentrantReadWriteLock#writeLock()} (this is the
	 * default behavior if none of the other default method implementations are overridden).
	 * </p>
	 *
	 * @return the (held) mutex lock
	 */
	@Override
	default Lock acquireMutexLock() {
		Lock mutexLock = getMutexLock();
		ReadWriteLock rwl = getShareableLock();
		if ((mutexLock == rwl.writeLock()) && ReentrantReadWriteLock.class.isInstance(rwl)) {
			// Implement basic deadlock detection: we can only acquire the mutex
			// if we either already hold the write lock, or we don't hold any read locks
			ReentrantReadWriteLock rrwl = ReentrantReadWriteLock.class.cast(rwl);
			final int readCount = rrwl.getReadHoldCount();
			final int writeCount = rrwl.getWriteHoldCount();
			if ((writeCount == 0) && (readCount > 0)) { throw new LockDisallowedException(this, readCount); }
		}
		mutexLock.lock();
		return mutexLock;
	}

	/**
	 * <p>
	 * Returns the shareable (read-write) lock instance backing the whole construct
	 * </p>
	 *
	 * @return the shareable (read-write) lock instance
	 */
	public ReadWriteLock getShareableLock();

	/**
	 * <p>
	 * Return the shared (read) lock. Contrary to {@link #acquireSharedLock()}, no attempt is made
	 * to acquire the lock before returning it.
	 * </p>
	 *
	 * @return the write lock
	 */
	public default Lock getSharedLock() {
		return getShareableLock().readLock();
	}

	/**
	 * <p>
	 * Return the shared (read) lock. The lock is already held when it's returned so this method may
	 * block while other threads hold the mutex lock.
	 * </p>
	 *
	 * @return the (held) write lock
	 */
	public default Lock acquireSharedLock() {
		Lock ret = getSharedLock();
		ret.lock();
		return ret;
	}

	/**
	 * <p>
	 * Return the shared (read) lock, wrapped inside a {@link SharedAutoLock} instance for use in
	 * try-with-resources constructs. The lock is already held when it's returned so this method may
	 * block while other threads hold the mutex lock.
	 * </p>
	 *
	 * @return the (held) write lock
	 */
	public default SharedAutoLock autoSharedLock() {
		return new SharedAutoLock(this);
	}

	/**
	 * <p>
	 * Execute the given operation within the context of a shared (read) lock, returning the result
	 * of {@link Supplier#get()}. The lock is acquired and released automatically.
	 * </p>
	 *
	 * @param operation
	 * @throws NullPointerException
	 *             if {@code operation} is {@code null}
	 */
	public default <E> E shareLocked(Supplier<E> operation) {
		Objects.requireNonNull(operation, "Must provide an operation to run");
		return shareLocked(() -> operation.get());
	}

	/**
	 * <p>
	 * Execute the given operation within the context of a shared (read) lock, returning the result
	 * of {@link CheckedSupplier#getChecked()}. The lock is acquired and released automatically. Any
	 * raised exceptions are cascaded upward.
	 * </p>
	 *
	 * @param operation
	 * @throws NullPointerException
	 *             if {@code operation} is {@code null}
	 */
	public default <E, EX extends Throwable> E shareLocked(CheckedSupplier<E, EX> operation) throws EX {
		Objects.requireNonNull(operation, "Must provide a non-null operation to invoke");
		try (SharedAutoLock l = autoSharedLock()) {
			return operation.getChecked();
		}
	}

	/**
	 * <p>
	 * Execute the given operation within the context of a shared (read) lock. The lock is acquired
	 * and released automatically.
	 * </p>
	 *
	 * @param operation
	 * @throws NullPointerException
	 *             if {@code operation} is {@code null}
	 */
	public default void shareLocked(Runnable operation) {
		Objects.requireNonNull(operation, "Must provide an operation to run");
		shareLocked(() -> operation.run());
	}

	/**
	 * <p>
	 * Execute the given operation within the context of a shared (read) lock. The lock is acquired
	 * and released automatically. Any raised exceptions are cascaded upward.
	 * </p>
	 *
	 * @param operation
	 * @throws NullPointerException
	 *             if {@code operation} is {@code null}
	 */
	public default <EX extends Throwable> void shareLocked(CheckedRunnable<EX> operation) throws EX {
		Objects.requireNonNull(operation, "Must provide a non-null operation to invoke");
		shareLocked(() -> {
			operation.run();
			return null;
		});
	}

	/**
	 * <p>
	 * Implements a simple double-checked locking algorithm where the read lock is used to first
	 * ensure a clean read, and an optional upgrade to a write lock is performed if it's determined
	 * that this is needed in order to proceed with a specific operation.
	 * </p>
	 * <p>
	 * The {@link Supplier} decision parameter supplies a Boolean decision value that's analyzed as
	 * follows: if the value is {@link Boolean#FALSE} or {@code null}, then this means that there is
	 * no need to execute the writeBlock. Otherwise (if the value is {@link Boolean#TRUE}), then the
	 * read lock is released and a write lock is acquired. Once the write lock is held, the
	 * {@code decision} value is re-fetched in case another thread varied the conditions, and
	 * re-analyzed as described above. If the check again returns {@link Boolean#TRUE}, then the
	 * ({@link Runnable writeBlock}) is executed.
	 * </p>
	 * <p>
	 * Regardless of the outcome, all acquired locks are released. This method may be invoked while
	 * the write lock is held, but not the read lock as this would cause a deadlock.
	 * </p>
	 *
	 * @param decision
	 * @param writeBlock
	 * @returns the value returned by the {@code writeBlock} if it was executed, or {@code null} if
	 *          it wasn't.
	 */
	public default <E> E shareLockedUpgradable(Supplier<Boolean> decision,
		Function<Supplier<Condition>, E> writeBlock) {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedPredicate<E, RuntimeException> newDecision = (e) -> decision.get() == Boolean.TRUE;
		final CheckedBiFunction<E, Supplier<Condition>, E, RuntimeException> newWriteBlock = (e, c) -> writeBlock
			.apply(c);
		return shareLockedUpgradable(null, newDecision, newWriteBlock);
	}

	public default <E> E shareLockedUpgradable(Supplier<Boolean> decision, Supplier<E> writeBlock) {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedPredicate<E, RuntimeException> newDecision = (e) -> decision.get() == Boolean.TRUE;
		final CheckedBiFunction<E, Supplier<Condition>, E, RuntimeException> newWriteBlock = (e, c) -> writeBlock.get();
		return shareLockedUpgradable(null, newDecision, newWriteBlock);
	}

	/**
	 * <p>
	 * Implements a simple double-checked locking algorithm where the read lock is used to first
	 * ensure a clean read, and an optional upgrade to a write lock is performed if it's determined
	 * that this is needed in order to proceed with a specific operation.
	 * </p>
	 * <p>
	 * The {@link CheckedSupplier} decision parameter supplies a Boolean decision value that's
	 * analyzed as follows: if the value is {@link Boolean#FALSE} or {@code null}, then this means
	 * that there is no need to execute the writeBlock. Otherwise (if the value is
	 * {@link Boolean#TRUE}), then the read lock is released and a write lock is acquired. Once the
	 * write lock is held, the {@code decision} value is re-fetched in case another thread varied
	 * the conditions, and re-analyzed as described above. If the check again returns
	 * {@link Boolean#TRUE}, then the ({@link CheckedSupplier writeBlock}) is executed.
	 * </p>
	 * <p>
	 * Regardless of the outcome, all acquired locks are released. This method may be invoked while
	 * the write lock is held, but not the read lock as this would cause a deadlock.
	 * </p>
	 *
	 * @param decision
	 * @param writeBlock
	 * @returns the value returned by the {@code writeBlock} if it was executed, or {@code null} if
	 *          it wasn't.
	 * @throws EX
	 */
	public default <E, EX extends Throwable> E shareLockedUpgradable(CheckedSupplier<Boolean, EX> decision,
		CheckedFunction<Supplier<Condition>, E, EX> writeBlock) throws EX {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedPredicate<E, EX> newDecision = (e) -> decision.getChecked() == Boolean.TRUE;
		final CheckedBiFunction<E, Supplier<Condition>, E, EX> newWriteBlock = (e, c) -> writeBlock.applyChecked(c);
		return shareLockedUpgradable(null, newDecision, newWriteBlock);
	}

	public default <E, EX extends Throwable> E shareLockedUpgradable(CheckedSupplier<Boolean, EX> decision,
		CheckedSupplier<E, EX> writeBlock) throws EX {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedPredicate<E, EX> newDecision = (e) -> decision.getChecked() == Boolean.TRUE;
		final CheckedBiFunction<E, Supplier<Condition>, E, EX> newWriteBlock = (e, c) -> writeBlock.getChecked();
		return shareLockedUpgradable(null, newDecision, newWriteBlock);
	}

	/**
	 * <p>
	 * Implements a simple double-checked locking algorithm where the read lock is used to first
	 * ensure a clean read, and an optional upgrade to a write lock is performed if it's determined
	 * that this is needed in order to proceed with a specific operation.
	 * </p>
	 * <p>
	 * The {@link CheckedSupplier} parameter supplies a decision value that is then given to the
	 * {@link CheckedPredicate} for analysis. If the predicate returns {@code false}, then this
	 * means that there is no need to execute the {@code writeBlock}. Otherwise (if the predicate
	 * returns {@code true}), then the read lock is released and a write lock is acquired. Once the
	 * write lock is held, the above check is repeated in case another thread varied the conditions
	 * (i.e. first retrieve the value from {@code checker}, then evaluate the {@code predicate}
	 * again). If the check again returns {@code true}, then the ({@link Function writeBlock}) is
	 * executed.
	 * </p>
	 * <p>
	 * Regardless of the outcome, all acquired locks are released. This method may be invoked while
	 * the write lock is held, but not the read lock as this would cause a deadlock.
	 * </p>
	 *
	 * @param checker
	 * @param decision
	 * @param writeBlock
	 * @returns either the value returned by the {@code writeBlock} (if it was executed), or the
	 *          last value returned by the {@code checker} parameter.
	 */
	public default <E> E shareLockedUpgradable(Supplier<E> checker, Predicate<E> decision,
		BiFunction<E, Supplier<Condition>, E> writeBlock) {
		final CheckedSupplier<E, RuntimeException> newChecker = (checker != null ? CheckedTools.check(checker) : null);
		return shareLockedUpgradable(newChecker, CheckedTools.check(decision), CheckedTools.check(writeBlock));
	}

	public default <E> E shareLockedUpgradable(Supplier<E> checker, Predicate<E> decision, Function<E, E> writeBlock) {
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedSupplier<E, RuntimeException> newChecker = (checker != null ? CheckedTools.check(checker) : null);
		final CheckedBiFunction<E, Supplier<Condition>, E, RuntimeException> newWriteBlock = (e, c) -> writeBlock
			.apply(e);
		return shareLockedUpgradable(newChecker, CheckedTools.check(decision), newWriteBlock);
	}

	/**
	 * <p>
	 * Implements a simple double-checked locking algorithm where the read lock is used to first
	 * ensure a clean read, and an optional upgrade to a write lock is performed if it's determined
	 * that this is needed in order to proceed with a specific operation.
	 * </p>
	 * <p>
	 * The {@link CheckedSupplier} parameter supplies a decision value that is then given to the
	 * {@link CheckedPredicate} for analysis. If the predicate returns {@code false}, then this
	 * means that there is no need to execute the {@code writeBlock}. Otherwise (if the predicate
	 * returns {@code true}), then the read lock is released and a write lock is acquired. Once the
	 * write lock is held, the above check is repeated in case another thread varied the conditions
	 * (i.e. first retrieve the value from {@code checker}, then evaluate the {@code predicate}
	 * again). If the check again returns {@code true}, then the ({@link CheckedSupplier
	 * writeBlock}) is executed.
	 * </p>
	 * <p>
	 * Regardless of the outcome, all acquired locks are released. This method may be invoked while
	 * the write lock is held, but not the read lock as this would cause a deadlock.
	 * </p>
	 *
	 * @param checker
	 * @param decision
	 * @param writeBlock
	 * @returns either the value returned by the {@code writeBlock} (if it was executed), or the
	 *          last value returned by the {@code checker} parameter.
	 * @throws EX
	 */
	public default <E, EX extends Throwable> E shareLockedUpgradable(CheckedSupplier<E, EX> checker,
		CheckedPredicate<E, EX> decision, CheckedBiFunction<E, Supplier<Condition>, E, EX> writeBlock) throws EX {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		if (checker == null) {
			checker = () -> null;
		}

		try (SharedAutoLock s = autoSharedLock()) {
			E e = checker.getChecked();
			if (decision.testChecked(e)) {
				try (MutexAutoLock m = s.upgrade()) {
					e = checker.getChecked();
					if (decision.testChecked(e)) {
						e = writeBlock.applyChecked(e, m::newCondition);
					}
				}
			}
			return e;
		}
	}

	public default <E, EX extends Throwable> E shareLockedUpgradable(CheckedSupplier<E, EX> checker,
		CheckedPredicate<E, EX> decision, CheckedFunction<E, E, EX> writeBlock) throws EX {
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedBiFunction<E, Supplier<Condition>, E, EX> newWriteBlock = (e, c) -> writeBlock.applyChecked(e);
		return shareLockedUpgradable(checker, decision, newWriteBlock);
	}

	/**
	 * <p>
	 * Implements a simple double-checked locking algorithm where the read lock is used to first
	 * ensure a clean read, and an optional upgrade to a write lock is performed if it's determined
	 * that this is needed in order to proceed with a specific operation.
	 * </p>
	 * <p>
	 * The {@link Supplier} decision parameter supplies a Boolean decision value that's analyzed as
	 * follows: if the value is {@link Boolean#FALSE} or {@code null}, then this means that there is
	 * no need to execute the writeBlock. Otherwise (if the value is {@link Boolean#TRUE}), then the
	 * read lock is released and a write lock is acquired. Once the write lock is held, the
	 * {@code decision} value is re-fetched in case another thread varied the conditions, and
	 * re-analyzed as described above. If the check again returns {@link Boolean#TRUE}, then the
	 * ({@link Runnable writeBlock}) is executed.
	 * </p>
	 * <p>
	 * Regardless of the outcome, all acquired locks are released. This method may be invoked while
	 * the write lock is held, but not the read lock as this would cause a deadlock.
	 * </p>
	 *
	 * @param decision
	 * @param writeBlock
	 */
	public default <E> void shareLockedUpgradable(Supplier<Boolean> decision,
		Consumer<Supplier<Condition>> writeBlock) {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedPredicate<E, RuntimeException> newDecision = (e) -> decision.get() == Boolean.TRUE;
		final CheckedBiConsumer<E, Supplier<Condition>, RuntimeException> newWriteBlock = (e, c) -> writeBlock
			.accept(c);
		shareLockedUpgradable(null, newDecision, newWriteBlock);
	}

	public default <E> void shareLockedUpgradable(Supplier<Boolean> decision, Runnable writeBlock) {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedPredicate<E, RuntimeException> newDecision = (e) -> decision.get() == Boolean.TRUE;
		final CheckedBiConsumer<E, Supplier<Condition>, RuntimeException> newWriteBlock = (e, c) -> writeBlock.run();
		shareLockedUpgradable(null, newDecision, newWriteBlock);
	}

	/**
	 * <p>
	 * Implements a simple double-checked locking algorithm where the read lock is used to first
	 * ensure a clean read, and an optional upgrade to a write lock is performed if it's determined
	 * that this is needed in order to proceed with a specific operation.
	 * </p>
	 * <p>
	 * The {@link CheckedSupplier} decision parameter supplies a Boolean decision value that's
	 * analyzed as follows: if the value is {@link Boolean#FALSE} or {@code null}, then this means
	 * that there is no need to execute the writeBlock. Otherwise (if the value is
	 * {@link Boolean#TRUE}), then the read lock is released and a write lock is acquired. Once the
	 * write lock is held, the {@code decision} value is re-fetched in case another thread varied
	 * the conditions, and re-analyzed as described above. If the check again returns
	 * {@link Boolean#TRUE}, then the ({@link CheckedSupplier writeBlock}) is executed.
	 * </p>
	 * <p>
	 * Regardless of the outcome, all acquired locks are released. This method may be invoked while
	 * the write lock is held, but not the read lock as this would cause a deadlock.
	 * </p>
	 *
	 * @param decision
	 * @param writeBlock
	 * @throws EX
	 */
	public default <E, EX extends Throwable> void shareLockedUpgradable(CheckedSupplier<Boolean, EX> decision,
		CheckedConsumer<Supplier<Condition>, EX> writeBlock) throws EX {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedPredicate<E, EX> newDecision = (e) -> decision.getChecked() == Boolean.TRUE;
		final CheckedBiConsumer<E, Supplier<Condition>, EX> newWriteBlock = (e, c) -> writeBlock.acceptChecked(c);
		shareLockedUpgradable(null, newDecision, newWriteBlock);
	}

	public default <E, EX extends Throwable> void shareLockedUpgradable(CheckedSupplier<Boolean, EX> decision,
		CheckedRunnable<EX> writeBlock) throws EX {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedPredicate<E, EX> newDecision = (e) -> decision.getChecked() == Boolean.TRUE;
		final CheckedBiConsumer<E, Supplier<Condition>, EX> newWriteBlock = (e, c) -> writeBlock.runChecked();
		shareLockedUpgradable(null, newDecision, newWriteBlock);
	}

	/**
	 * <p>
	 * Implements a simple double-checked locking algorithm where the read lock is used to first
	 * ensure a clean read, and an optional upgrade to a write lock is performed if it's determined
	 * that this is needed in order to proceed with a specific operation.
	 * </p>
	 * <p>
	 * The {@link CheckedSupplier} parameter supplies a decision value that is then given to the
	 * {@link CheckedPredicate} for analysis. If the predicate returns {@code false}, then this
	 * means that there is no need to execute the {@code writeBlock}. Otherwise (if the predicate
	 * returns {@code true}), then the read lock is released and a write lock is acquired. Once the
	 * write lock is held, the above check is repeated in case another thread varied the conditions
	 * (i.e. first retrieve the value from {@code checker}, then evaluate the {@code predicate}
	 * again). If the check again returns {@code true}, then the ({@link CheckedSupplier
	 * writeBlock}) is executed.
	 * </p>
	 * <p>
	 * Regardless of the outcome, all acquired locks are released. This method may be invoked while
	 * the write lock is held, but not the read lock as this would cause a deadlock.
	 * </p>
	 *
	 * @param checker
	 * @param decision
	 * @param writeBlock
	 */
	public default <E> void shareLockedUpgradable(Supplier<E> checker, Predicate<E> decision,
		BiConsumer<E, Supplier<Condition>> writeBlock) {
		final CheckedSupplier<E, RuntimeException> newChecker = (checker != null ? CheckedTools.check(checker) : null);
		shareLockedUpgradable(newChecker, CheckedTools.check(decision), CheckedTools.check(writeBlock));
	}

	public default <E> void shareLockedUpgradable(Supplier<E> checker, Predicate<E> decision, Consumer<E> writeBlock) {
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedSupplier<E, RuntimeException> newChecker = (checker != null ? CheckedTools.check(checker) : null);
		CheckedBiConsumer<E, Supplier<Condition>, RuntimeException> newWriteBlock = (e, c) -> writeBlock.accept(e);
		shareLockedUpgradable(newChecker, CheckedTools.check(decision), newWriteBlock);
	}

	/**
	 * <p>
	 * Implements a simple double-checked locking algorithm where the read lock is used to first
	 * ensure a clean read, and an optional upgrade to a write lock is performed if it's determined
	 * that this is needed in order to proceed with a specific operation.
	 * </p>
	 * <p>
	 * The {@link CheckedSupplier} parameter supplies a decision value that is then given to the
	 * {@link CheckedPredicate} for analysis. If the predicate returns {@code false}, then this
	 * means that there is no need to execute the {@code writeBlock}. Otherwise (if the predicate
	 * returns {@code true}), then the read lock is released and a write lock is acquired. Once the
	 * write lock is held, the above check is repeated in case another thread varied the conditions
	 * (i.e. first retrieve the value from {@code checker}, then evaluate the {@code predicate}
	 * again). If the check again returns {@code true}, then the ({@link CheckedSupplier
	 * writeBlock}) is executed.
	 * </p>
	 * <p>
	 * Regardless of the outcome, all acquired locks are released. This method may be invoked while
	 * the write lock is held, but not the read lock as this would cause a deadlock.
	 * </p>
	 *
	 * @param checker
	 * @param decision
	 * @param writeBlock
	 * @throws EX
	 */
	public default <E, EX extends Throwable> void shareLockedUpgradable(CheckedSupplier<E, EX> checker,
		CheckedPredicate<E, EX> decision, CheckedBiConsumer<E, Supplier<Condition>, EX> writeBlock) throws EX {
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		CheckedBiFunction<E, Supplier<Condition>, E, EX> newWriteBlock = (e, c) -> {
			writeBlock.acceptChecked(e, c);
			return null;
		};
		shareLockedUpgradable(checker, decision, newWriteBlock);
	}

	public default <E, EX extends Throwable> void shareLockedUpgradable(CheckedSupplier<E, EX> checker,
		CheckedPredicate<E, EX> decision, CheckedConsumer<E, EX> writeBlock) throws EX {
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		CheckedBiFunction<E, Supplier<Condition>, E, EX> newWriteBlock = (e, c) -> {
			writeBlock.acceptChecked(e);
			return null;
		};
		shareLockedUpgradable(checker, decision, newWriteBlock);
	}
}