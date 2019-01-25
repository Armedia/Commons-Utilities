package com.armedia.commons.utilities;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.ConcurrentInitializer;

public class LazyInitializer<T> implements ConcurrentInitializer<T> {

	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final Condition condition = this.rwLock.writeLock().newCondition();
	private final Supplier<T> defaultInitializer;
	private final T defaultValue;

	private volatile boolean initialized = false;
	private volatile T item = null;

	public LazyInitializer() {
		this(null, null);
	}

	public LazyInitializer(Supplier<T> defaultInitializer) {
		this(defaultInitializer, null);
	}

	public LazyInitializer(T defaultValue) {
		this(null, defaultValue);
	}

	public LazyInitializer(Supplier<T> defaultInitializer, T defaultValue) {
		this.defaultInitializer = defaultInitializer;
		this.defaultValue = defaultValue;
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

	public void await() throws InterruptedException {
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
	}

	public void awaitUninterruptibly() {
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
	}

	public long awaitNanos(long nanosTimeout) throws InterruptedException {
		if (!this.initialized) {
			this.rwLock.writeLock().lock();
			try {
				if (!this.initialized) {
					long ret = this.condition.awaitNanos(nanosTimeout);
					this.condition.signal();
					return ret;
				}
			} finally {
				this.rwLock.writeLock().unlock();
			}
		}
		return 0;
	}

	public boolean await(long time, TimeUnit unit) throws InterruptedException {
		return (awaitNanos(unit.toNanos(time)) > 0);
	}

	public boolean awaitUntil(Date deadline) throws InterruptedException {
		if (!this.initialized) {
			this.rwLock.writeLock().lock();
			try {
				if (!this.initialized) {
					boolean ret = this.condition.awaitUntil(deadline);
					this.condition.signal();
					return ret;
				}
			} finally {
				this.rwLock.writeLock().unlock();
			}
		}
		return false;
	}

	@Override
	public T get() throws ConcurrentException {
		return get(this.defaultInitializer);
	}

	public T get(Supplier<T> initializer) throws ConcurrentException {
		if (!this.initialized) {
			this.rwLock.writeLock().lock();
			try {
				if (!this.initialized) {
					initializer = Tools.coalesce(initializer, this.defaultInitializer);
					if (initializer != null) {
						try {
							this.item = initializer.get();
						} catch (Throwable e) {
							throw new ConcurrentException(e);
						}
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
}