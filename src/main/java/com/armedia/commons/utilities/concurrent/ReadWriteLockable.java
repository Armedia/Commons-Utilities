package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

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

	public default <E> E readUpgradable(Supplier<E> checker, Predicate<E> decision, Function<E, E> writeBlock) {
		Objects.requireNonNull(checker, "Must provide a non-null checker");
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");

		final Lock readLock = acquireReadLock();
		try {
			E e = checker.get();
			if (decision.test(e)) { return e; }

			readLock.unlock();
			final Lock writeLock = acquireWriteLock();
			try {
				e = checker.get();
				if (!decision.test(e)) {
					e = writeBlock.apply(e);
				}
				readLock.lock();
				return e;
			} finally {
				writeLock.unlock();
			}
		} finally {
			readLock.unlock();
		}
	}

	public default <E> void readUpgradable(Supplier<E> checker, Predicate<E> decision, Consumer<E> writeBlock) {
		Objects.requireNonNull(checker, "Must provide a non-null checker");
		Objects.requireNonNull(decision, "Must provide a non-null decision");
		Objects.requireNonNull(writeBlock, "Must provide a non-null writeBlock");

		final Lock readLock = acquireReadLock();
		try {
			E e = checker.get();
			if (decision.test(e)) { return; }

			readLock.unlock();
			final Lock writeLock = acquireWriteLock();
			try {
				e = checker.get();
				if (!decision.test(e)) {
					writeBlock.accept(e);
				}
				readLock.lock();
			} finally {
				writeLock.unlock();
			}
		} finally {
			readLock.unlock();
		}
	}

	public default void doubleCheckedLocked(BooleanSupplier test, Runnable calculator) {
		Objects.requireNonNull(test, "Must provide a non-null test");
		Objects.requireNonNull(calculator, "Must provide a non-null calculator");

		if (test.getAsBoolean()) {
			final Lock writeLock = acquireWriteLock();
			try {
				if (test.getAsBoolean()) {
					calculator.run();
				}
			} finally {
				writeLock.unlock();
			}
		}
	}

	public default <E> E doubleCheckedLocked(Supplier<E> checker, Predicate<E> test, Supplier<E> calculator) {
		Objects.requireNonNull(checker, "Must provide a non-null checker");
		Objects.requireNonNull(test, "Must provide a non-null test");
		Objects.requireNonNull(calculator, "Must provide a non-null calculator");

		E localRef = checker.get();
		if (test.test(localRef)) {
			final Lock writeLock = acquireWriteLock();
			try {
				localRef = checker.get();
				if (test.test(localRef)) {
					localRef = calculator.get();
				}
			} finally {
				writeLock.unlock();
			}
		}
		return localRef;
	}

	public default <EX extends Throwable> void doubleCheckedLockedChecked(CheckedSupplier<Boolean, EX> test,
		CheckedRunnable<EX> calculator) throws EX {
		Objects.requireNonNull(test, "Must provide a non-null test");
		Objects.requireNonNull(calculator, "Must provide a non-null calculator");

		if (test.get()) {
			final Lock writeLock = acquireWriteLock();
			try {
				if (test.get()) {
					calculator.runChecked();
				}
			} finally {
				writeLock.unlock();
			}
		}
	}

	public default <E, EX extends Throwable> E doubleCheckedLockedChecked(CheckedSupplier<E, EX> checker,
		CheckedPredicate<E, EX> test, CheckedSupplier<E, EX> calculator) throws EX {
		Objects.requireNonNull(checker, "Must provide a non-null checker");
		Objects.requireNonNull(test, "Must provide a non-null test");
		Objects.requireNonNull(calculator, "Must provide a non-null calculator");

		E localRef = checker.getChecked();
		if (test.testChecked(localRef)) {
			final Lock writeLock = acquireWriteLock();
			try {
				localRef = checker.getChecked();
				if (test.testChecked(localRef)) {
					localRef = calculator.getChecked();
				}
			} finally {
				writeLock.unlock();
			}
		}
		return localRef;
	}

}