package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
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
		final Lock l = acquireReadLock();
		try {
			return supplier.get();
		} finally {
			l.unlock();
		}
	}

	public default <E, EX extends Throwable> E readLockedChecked(CheckedSupplier<E, EX> operation) throws EX {
		Objects.requireNonNull(operation, "Must provide a non-null supplier to invoke");
		final Lock l = acquireReadLock();
		try {
			return operation.getChecked();
		} finally {
			l.unlock();
		}
	}

	public default void readLocked(Runnable operation) {
		Objects.requireNonNull(operation, "Must provide a non-null runnable to invoke");
		final Lock l = acquireReadLock();
		try {
			operation.run();
		} finally {
			l.unlock();
		}
	}

	public default <EX extends Throwable> void readLockedChecked(CheckedRunnable<EX> operation) throws EX {
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
		final Lock l = acquireWriteLock();
		try {
			return supplier.get();
		} finally {
			l.unlock();
		}
	}

	public default <E, EX extends Throwable> E writeLockedChecked(CheckedSupplier<E, EX> operation) throws EX {
		Objects.requireNonNull(operation, "Must provide a non-null supplier to invoke");
		final Lock l = acquireWriteLock();
		try {
			return operation.getChecked();
		} finally {
			l.unlock();
		}
	}

	public default void writeLocked(Runnable operation) {
		Objects.requireNonNull(operation, "Must provide a non-null runnable to invoke");
		final Lock l = acquireWriteLock();
		try {
			operation.run();
		} finally {
			l.unlock();
		}
	}

	public default <EX extends Throwable> void writeLockedChecked(CheckedRunnable<EX> operation) throws EX {
		Objects.requireNonNull(operation, "Must provide a non-null runnable to invoke");
		final Lock l = acquireWriteLock();
		try {
			operation.runChecked();
		} finally {
			l.unlock();
		}
	}

	public default <E, EX extends Throwable> E readUpgradable(CheckedSupplier<Boolean, EX> decision,
		CheckedSupplier<E, EX> writeBlock) throws EX {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedPredicate<E, EX> newDecision = (e) -> Optional.of(decision.getChecked()).orElse(Boolean.FALSE);
		final CheckedFunction<E, E, EX> newWriteBlock = (e) -> writeBlock.getChecked();
		return readUpgradable(null, newDecision, newWriteBlock);
	}

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
			if (!decision.testChecked(e)) {
				readLock.unlock();
				final Lock writeLock = acquireWriteLock();
				try {
					e = checker.getChecked();
					try {
						if (!decision.testChecked(e)) {
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

	public default <E, EX extends Throwable> void readUpgradable(CheckedSupplier<Boolean, EX> decision,
		CheckedRunnable<EX> writeBlock) throws EX {
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");
		final CheckedPredicate<E, EX> newDecision = (e) -> Optional.of(decision.getChecked()).orElse(Boolean.FALSE);
		final CheckedConsumer<E, EX> newWriteBlock = (e) -> writeBlock.runChecked();
		readUpgradable(null, newDecision, newWriteBlock);
	}

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
			if (!decision.testChecked(e)) {
				readLock.unlock();
				final Lock writeLock = acquireWriteLock();
				try {
					e = checker.getChecked();
					try {
						if (!decision.testChecked(e)) {
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