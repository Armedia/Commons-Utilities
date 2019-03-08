package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.armedia.commons.utilities.function.CheckedConsumer;
import com.armedia.commons.utilities.function.CheckedFunction;
import com.armedia.commons.utilities.function.CheckedPredicate;
import com.armedia.commons.utilities.function.CheckedRunnable;
import com.armedia.commons.utilities.function.CheckedSupplier;

@FunctionalInterface
public interface ReadWriteLockable {

	public ReadWriteLock getMainLock();

	public default Lock getReadLock() {
		return getMainLock().readLock();
	}

	public default Lock acquireReadLock() {
		Lock ret = getReadLock();
		ret.lock();
		return ret;
	}

	public default <E> E readLocked(Supplier<E> supplier) {
		Objects.requireNonNull(supplier, "Must provide a non-null supplier to invoke");
		return readLocked(() -> supplier.get());
	}

	public default void readLocked(Runnable operation) {
		Objects.requireNonNull(operation, "Must provide a non-null runnable to invoke");
		readLocked(() -> operation.run());
	}

	public default <E, EX extends Throwable> E readLocked(CheckedSupplier<E, EX> operation) throws EX {
		Objects.requireNonNull(operation, "Must provide a non-null supplier to invoke");
		final Lock l = acquireReadLock();
		try {
			return operation.getChecked();
		} finally {
			l.unlock();
		}
	}

	public default <EX extends Throwable> void readLocked(CheckedRunnable<EX> operation) throws EX {
		Objects.requireNonNull(operation, "Must provide a non-null supplier to invoke");
		final Lock l = acquireReadLock();
		try {
			operation.runChecked();
		} finally {
			l.unlock();
		}
	}

	public default Lock getWriteLock() {
		return getMainLock().writeLock();
	}

	public default Lock acquireWriteLock() {
		Lock ret = getWriteLock();
		ret.lock();
		return ret;
	}

	public default <E> E writeLocked(Supplier<E> supplier) {
		Objects.requireNonNull(supplier, "Must provide a non-null supplier to invoke");
		return writeLocked(() -> supplier.get());
	}

	public default void writeLocked(Runnable operation) {
		Objects.requireNonNull(operation, "Must provide a non-null runnable to invoke");
		writeLocked(() -> operation.run());
	}

	public default <E, EX extends Throwable> E writeLocked(CheckedSupplier<E, EX> operation) throws EX {
		Objects.requireNonNull(operation, "Must provide a non-null supplier to invoke");
		final Lock l = acquireWriteLock();
		try {
			return operation.getChecked();
		} finally {
			l.unlock();
		}
	}

	public default <EX extends Throwable> void writeLocked(CheckedRunnable<EX> operation) throws EX {
		Objects.requireNonNull(operation, "Must provide a non-null runnable to invoke");
		final Lock l = acquireWriteLock();
		try {
			operation.runChecked();
		} finally {
			l.unlock();
		}
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
	public default <E> E readUpgradable(Supplier<Boolean> decision, Supplier<E> writeBlock) {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedPredicate<E, RuntimeException> newDecision = (e) -> decision.get() == Boolean.TRUE;
		final CheckedFunction<E, E, RuntimeException> newWriteBlock = (e) -> writeBlock.get();
		return readUpgradable(null, newDecision, newWriteBlock);
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
	public default <E, EX extends Throwable> E readUpgradable(CheckedSupplier<Boolean, EX> decision,
		CheckedSupplier<E, EX> writeBlock) throws EX {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedPredicate<E, EX> newDecision = (e) -> decision.getChecked() == Boolean.TRUE;
		final CheckedFunction<E, E, EX> newWriteBlock = (e) -> writeBlock.getChecked();
		return readUpgradable(null, newDecision, newWriteBlock);
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
	public default <E> E readUpgradable(Supplier<E> checker, Predicate<E> decision, Function<E, E> writeBlock) {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedSupplier<E, RuntimeException> newChecker = (checker != null ? () -> checker.get() : null);
		final CheckedPredicate<E, RuntimeException> newDecision = (e) -> decision.test(e);
		final CheckedFunction<E, E, RuntimeException> newWriteBlock = (e) -> writeBlock.apply(e);
		return readUpgradable(newChecker, newDecision, newWriteBlock);
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
	public default <E, EX extends Throwable> E readUpgradable(CheckedSupplier<E, EX> checker,
		CheckedPredicate<E, EX> decision, CheckedFunction<E, E, EX> writeBlock) throws EX {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		if (checker == null) {
			checker = () -> null;
		}

		final Lock readLock = acquireReadLock();
		try {
			E e = checker.getChecked();
			if (decision.testChecked(e)) {
				readLock.unlock();
				final Lock writeLock = acquireWriteLock();
				try {
					try {
						e = checker.getChecked();
						if (decision.testChecked(e)) {
							e = writeBlock.applyChecked(e);
						}
					} finally {
						readLock.lock();
					}
				} finally {
					writeLock.unlock();
				}
			}
			return e;
		} finally {
			readLock.unlock();
		}
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
	public default <E> void readUpgradable(Supplier<Boolean> decision, Runnable writeBlock) {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedPredicate<E, RuntimeException> newDecision = (e) -> decision.get() == Boolean.TRUE;
		final CheckedConsumer<E, RuntimeException> newWriteBlock = (e) -> writeBlock.run();
		readUpgradable(null, newDecision, newWriteBlock);
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
	public default <E, EX extends Throwable> void readUpgradable(CheckedSupplier<Boolean, EX> decision,
		CheckedRunnable<EX> writeBlock) throws EX {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedPredicate<E, EX> newDecision = (e) -> decision.getChecked() == Boolean.TRUE;
		final CheckedConsumer<E, EX> newWriteBlock = (e) -> writeBlock.runChecked();
		readUpgradable(null, newDecision, newWriteBlock);
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
	public default <E> void readUpgradable(Supplier<E> checker, Predicate<E> decision, Consumer<E> writeBlock) {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedSupplier<E, RuntimeException> newChecker = (checker != null ? () -> checker.get() : null);
		final CheckedPredicate<E, RuntimeException> newDecision = (e) -> decision.test(e);
		final CheckedConsumer<E, RuntimeException> newWriteBlock = (e) -> writeBlock.accept(e);
		readUpgradable(newChecker, newDecision, newWriteBlock);
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
	public default <E, EX extends Throwable> void readUpgradable(CheckedSupplier<E, EX> checker,
		CheckedPredicate<E, EX> decision, CheckedConsumer<E, EX> writeBlock) throws EX {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		if (checker == null) {
			checker = () -> null;
		}

		final Lock readLock = acquireReadLock();
		try {
			E e = checker.getChecked();
			if (decision.testChecked(e)) {
				readLock.unlock();
				final Lock writeLock = acquireWriteLock();
				try {
					try {
						e = checker.getChecked();
						if (decision.testChecked(e)) {
							writeBlock.acceptChecked(e);
						}
					} finally {
						readLock.lock();
					}
				} finally {
					writeLock.unlock();
				}
			}
		} finally {
			readLock.unlock();
		}
	}
}