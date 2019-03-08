package com.armedia.commons.utilities.function;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.function.Supplier;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.ConcurrentInitializer;
import org.apache.commons.lang3.concurrent.ConcurrentRuntimeException;
import org.apache.commons.lang3.tuple.Pair;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.concurrent.BaseReadWriteLockable;

public class LazySupplier<T> extends BaseReadWriteLockable implements Supplier<T> {

	private final Condition condition;
	private final Supplier<T> defaultInitializer;
	private final ConcurrentInitializer<T> concurrentInitializer;
	private final T defaultValue;

	private volatile boolean initialized = false;
	private volatile T item = null;

	public LazySupplier() {
		this(null, null);
	}

	public LazySupplier(Supplier<T> defaultInitializer) {
		this(defaultInitializer, null);
	}

	public LazySupplier(T defaultValue) {
		this(null, defaultValue);
	}

	public LazySupplier(Supplier<T> defaultInitializer, T defaultValue) {
		this.defaultInitializer = defaultInitializer;
		this.defaultValue = defaultValue;
		this.concurrentInitializer = this::get;
		this.condition = getWriteLock().newCondition();
	}

	public boolean isDefaulted() {
		return readLocked(() -> isInitialized() && (this.defaultValue == this.item));
	}

	public boolean isInitialized() {
		return readLocked(() -> this.initialized);
	}

	public T await() throws InterruptedException {
		doubleCheckedLockedChecked(() -> !this.initialized, () -> {
			this.condition.await();
			this.condition.signal();
		});
		return this.item;
	}

	public T awaitUninterruptibly() {
		doubleCheckedLocked(() -> !this.initialized, () -> {
			this.condition.awaitUninterruptibly();
			this.condition.signal();
		});
		return this.item;
	}

	public Pair<T, Long> awaitNanos(long nanosTimeout) throws InterruptedException {
		final AtomicReference<Long> ret = new AtomicReference<>(null);
		doubleCheckedLockedChecked(() -> !this.initialized, () -> {
			ret.set(this.condition.awaitNanos(nanosTimeout));
			if (this.initialized) {
				this.condition.signal();
				ret.set(null);
			}
		});
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
		doubleCheckedLockedChecked(() -> !this.initialized, () -> {
			ret.set(this.condition.awaitUntil(deadline));
			if (ret.get()) {
				this.condition.signal();
			}
		});
		return Pair.of(this.item, !ret.get());
	}

	@Override
	public T get() {
		try {
			return get(this.defaultInitializer);
		} catch (Throwable t) {
			throw new RuntimeException(t.getMessage(), t);
		}
	}

	public ConcurrentInitializer<T> asInitializer() {
		return this.concurrentInitializer;
	}

	public T get(Supplier<T> init) {
		doubleCheckedLocked(() -> !this.initialized, () -> {
			Supplier<T> initializer = Tools.coalesce(init, this.defaultInitializer);
			if (initializer != null) {
				this.item = initializer.get();
			} else {
				this.item = this.defaultValue;
			}
			this.initialized = true;
			this.condition.signal();
		});
		return this.item;
	}

	public static <T> LazySupplier<T> fromSupplier(Supplier<T> defaultInitializer) {
		return LazySupplier.fromSupplier(defaultInitializer, null);
	}

	public static <T> LazySupplier<T> fromSupplier(Supplier<T> defaultInitializer, T defaultValue) {
		return new LazySupplier<>((defaultInitializer != null ? () -> defaultInitializer.get() : null), defaultValue);
	}

	public static <T> LazySupplier<T> fromInitializer(ConcurrentInitializer<T> defaultInitializer) {
		return LazySupplier.fromInitializer(defaultInitializer, null);
	}

	public static <T> LazySupplier<T> fromInitializer(ConcurrentInitializer<T> defaultInitializer, T defaultValue) {
		return new LazySupplier<>(defaultInitializer != null ? () -> {
			try {
				return defaultInitializer.get();
			} catch (ConcurrentException e) {
				throw new ConcurrentRuntimeException(e.getMessage(), e);
			}
		} : null, defaultValue);
	}
}