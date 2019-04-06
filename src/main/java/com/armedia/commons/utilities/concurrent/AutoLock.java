package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
import java.util.concurrent.locks.Lock;

public final class AutoLock implements AutoCloseable {

	private final Lock lock;

	AutoLock(Lock lock) {
		this.lock = Objects.requireNonNull(lock, "Must provide a lock instance");
	}

	void unlock() {
		this.lock.unlock();
	}

	void lock() {
		this.lock.lock();
	}

	public Lock getLock() {
		return this.lock;
	}

	@Override
	public void close() {
		this.lock.unlock();
	}
}