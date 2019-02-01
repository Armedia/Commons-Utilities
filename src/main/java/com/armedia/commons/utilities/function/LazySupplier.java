package com.armedia.commons.utilities.function;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.ConcurrentInitializer;
import org.apache.commons.lang3.tuple.Pair;

import com.armedia.commons.utilities.Tools;

public class LazySupplier<T> implements Supplier<T>, CheckedSupplier<T> {

	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final Condition condition = this.rwLock.writeLock().newCondition();
	private final CheckedSupplier<T> defaultInitializer;
	private final ConcurrentInitializer<T> concurrentInitializer;
	private final T defaultValue;

	private volatile boolean initialized = false;
	private volatile T item = null;

	public LazySupplier() {
		this(null, null);
	}

	public LazySupplier(CheckedSupplier<T> defaultInitializer) {
		this(defaultInitializer, null);
	}

	public LazySupplier(T defaultValue) {
		this(null, defaultValue);
	}

	public LazySupplier(CheckedSupplier<T> defaultInitializer, T defaultValue) {
		this.defaultInitializer = defaultInitializer;
		this.defaultValue = defaultValue;
		this.concurrentInitializer = () -> {
			try {
				return getChecked();
			} catch (Exception e) {
				throw new ConcurrentException(e.getMessage(), e);
			}
		};
	}

	public boolean isDefaulted() {
		this.rwLock.readLock().lock();
		try {
			return isInitialized() && (this.defaultValue == this.item);
		} finally {
			this.rwLock.readLock().unlock();
		}
	}

	public boolean isInitialized() {
		this.rwLock.readLock().lock();
		try {
			return this.initialized;
		} finally {
			this.rwLock.readLock().unlock();
		}
	}

	public T await() throws InterruptedException {
		if (!this.initialized) {
			this.rwLock.writeLock().lock();
			try {
				if (!this.initialized) {
					this.condition.await();
					this.condition.signal();
				}
			} finally {
				this.rwLock.writeLock().unlock();
			}
		}
		return this.item;
	}

	public T awaitUninterruptibly() {
		if (!this.initialized) {
			this.rwLock.writeLock().lock();
			try {
				if (!this.initialized) {
					this.condition.awaitUninterruptibly();
					this.condition.signal();
				}
			} finally {
				this.rwLock.writeLock().unlock();
			}
		}
		return this.item;
	}

	public Pair<T, Long> awaitNanos(long nanosTimeout) throws InterruptedException {
		Long ret = null;
		if (!this.initialized) {
			this.rwLock.writeLock().lock();
			try {
				if (!this.initialized) {
					ret = this.condition.awaitNanos(nanosTimeout);
					if (this.initialized) {
						this.condition.signal();
						ret = null;
					}
				}
			} finally {
				this.rwLock.writeLock().unlock();
			}
		}
		return Pair.of(this.item, ret);
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
		boolean ret = true;
		if (!this.initialized) {
			this.rwLock.writeLock().lock();
			try {
				if (!this.initialized) {
					ret = this.condition.awaitUntil(deadline);
					if (ret) {
						this.condition.signal();
					}
				}
			} finally {
				this.rwLock.writeLock().unlock();
			}
		}
		return Pair.of(this.item, !ret);
	}

	@Override
	public T get() {
		try {
			return getChecked(this.defaultInitializer);
		} catch (Exception e) {
			throw new RuntimeException("Lazy initialization failed", e);
		}
	}

	public T get(Supplier<T> initializer) {
		try {
			return getChecked((initializer != null ? initializer::get : null));
		} catch (Exception e) {
			throw new RuntimeException("Lazy initialization failed", e);
		}
	}

	@Override
	public T getChecked() throws Exception {
		return getChecked(this.defaultInitializer);
	}

	public ConcurrentInitializer<T> asInitializer() {
		return this.concurrentInitializer;
	}

	public T getChecked(CheckedSupplier<T> initializer) throws Exception {
		boolean localInitialized = this.initialized;
		if (!localInitialized) {
			this.rwLock.writeLock().lock();
			try {
				localInitialized = this.initialized;
				if (!localInitialized) {
					initializer = Tools.coalesce(initializer, this.defaultInitializer);
					if (initializer != null) {
						this.item = initializer.getChecked();
					} else {
						this.item = this.defaultValue;
					}
					this.initialized = true;
					this.condition.signal();
				}
			} finally {
				this.rwLock.writeLock().unlock();
			}
		}
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
		return new LazySupplier<>((defaultInitializer != null ? () -> defaultInitializer.get() : null), defaultValue);
	}
}