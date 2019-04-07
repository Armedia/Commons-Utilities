package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
import java.util.concurrent.locks.Lock;

public final class AutoLock implements AutoCloseable {

	private final Lock lock;
	private boolean locked;

	AutoLock(Lock lock) {
		this.lock = Objects.requireNonNull(lock, "Must provide a lock instance");
		this.locked = true;
	}

	void unlock() {
		this.lock.unlock();
	}

	void lock() {
		this.lock.lock();
	}

	Lock getLock() {
		return this.lock;
	}

	@Override
	public void close() {
		if (this.locked) {
			this.lock.unlock();
			this.locked = false;
		}
	}
}