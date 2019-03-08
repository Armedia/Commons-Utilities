package com.armedia.commons.utilities.function;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.function.Supplier;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.ConcurrentInitializer;
import org.apache.commons.lang3.tuple.Pair;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.concurrent.BaseReadWriteLockable;

public class CheckedLazySupplier<T, EX extends Throwable> extends BaseReadWriteLockable
	implements Supplier<T>, CheckedSupplier<T, EX> {

	private final Condition condition;
	private final CheckedSupplier<T, EX> defaultInitializer;
	private final ConcurrentInitializer<T> concurrentInitializer;
	private final T defaultValue;

	private volatile boolean initialized = false;
	private volatile T item = null;

	public CheckedLazySupplier() {
		this(null, null);
	}

	public CheckedLazySupplier(CheckedSupplier<T, EX> defaultInitializer) {
		this(defaultInitializer, null);
	}

	public CheckedLazySupplier(T defaultValue) {
		this(null, defaultValue);
	}

	public CheckedLazySupplier(CheckedSupplier<T, EX> defaultInitializer, T defaultValue) {
		this.defaultInitializer = defaultInitializer;
		this.defaultValue = defaultValue;
		this.concurrentInitializer = () -> {
			try {
				return getChecked();
			} catch (Throwable t) {
				throw new ConcurrentException(t.getMessage(), t);
			}
		};
		this.condition = getWriteLock().newCondition();
	}

	public boolean isDefaulted() {
		return readLocked(() -> isInitialized() && (this.defaultValue == this.item));
	}

	public boolean isInitialized() {
		return readLocked(() -> this.initialized);
	}

	public T await() throws InterruptedException {
		if (!this.initialized) {
			writeLockedChecked(() -> {
				if (!this.initialized) {
					this.condition.await();
					this.condition.signal();
				}
			});
		}
		return this.item;
	}

	public T awaitUninterruptibly() {
		if (!this.initialized) {
			writeLocked(() -> {
				if (!this.initialized) {
					this.condition.awaitUninterruptibly();
					this.condition.signal();
				}
			});
		}
		return this.item;
	}

	public Pair<T, Long> awaitNanos(long nanosTimeout) throws InterruptedException {
		final AtomicReference<Long> ret = new AtomicReference<>(null);
		if (!this.initialized) {
			writeLockedChecked(() -> {
				if (!this.initialized) {
					ret.set(this.condition.awaitNanos(nanosTimeout));
					if (this.initialized) {
						this.condition.signal();
						ret.set(null);
					}
				}
			});
		}
		return Pair.of(this.item, ret.get());
	}

	public Pair<T, Boolean> await(long time, TimeUnit unit) throws InterruptedException {
		unit = Tools.coalesce(unit, TimeUnit.MILLISECONDS);
		Pair<T, Long> p = awaitNanos(unit.toNanos(time));
		if (p.getRight() != null) {
			return Pair.of(null, Boolean.TRUE);
		} else {
			return Pair.of(p.getLeft(), Boolean.FALSE);
		}
	}

	public Pair<T, Boolean> awaitUntil(Date deadline) throws InterruptedException {
		final AtomicBoolean ret = new AtomicBoolean(true);
		if (!this.initialized) {
			writeLockedChecked(() -> {
				if (!this.initialized) {
					ret.set(this.condition.awaitUntil(deadline));
					if (ret.get()) {
						this.condition.signal();
					}
				}
			});
		}
		return Pair.of(this.item, !ret.get());
	}

	@Override
	public T get() {
		try {
			return getChecked(this.defaultInitializer);
		} catch (Throwable t) {
			throw new RuntimeException("Lazy initialization failed", t);
		}
	}

	public T get(Supplier<T> initializer) {
		try {
			return getChecked((initializer != null ? initializer::get : null));
		} catch (Throwable t) {
			throw new RuntimeException("Lazy initialization failed", t);
		}
	}

	@Override
	public T getChecked() throws EX {
		return getChecked(this.defaultInitializer);
	}

	public ConcurrentInitializer<T> asInitializer() {
		return this.concurrentInitializer;
	}

	public T getChecked(CheckedSupplier<T, EX> initializer) throws EX {
		if (!this.initialized) {
			writeLockedChecked(() -> {
				if (!this.initialized) {
					CheckedSupplier<T, EX> init = Tools.coalesce(initializer, this.defaultInitializer);
					if (init != null) {
						this.item = init.getChecked();
					} else {
						this.item = this.defaultValue;
					}
					this.initialized = true;
					this.condition.signal();
				}
			});
		}
		return this.item;
	}

	public static <T, EX extends Throwable> CheckedLazySupplier<T, EX> fromSupplier(Supplier<T> defaultInitializer) {
		return CheckedLazySupplier.fromSupplier(defaultInitializer, null);
	}

	public static <T, EX extends Throwable> CheckedLazySupplier<T, EX> fromSupplier(Supplier<T> defaultInitializer,
		T defaultValue) {
		return new CheckedLazySupplier<>((defaultInitializer != null ? () -> defaultInitializer.get() : null),
			defaultValue);
	}

	public static <T> CheckedLazySupplier<T, ConcurrentException> fromInitializer(
		ConcurrentInitializer<T> defaultInitializer) {
		return CheckedLazySupplier.fromInitializer(defaultInitializer, null);
	}

	public static <T> CheckedLazySupplier<T, ConcurrentException> fromInitializer(
		ConcurrentInitializer<T> defaultInitializer, T defaultValue) {
		return new CheckedLazySupplier<>(defaultInitializer != null ? () -> defaultInitializer.get() : null,
			defaultValue);
	}
}