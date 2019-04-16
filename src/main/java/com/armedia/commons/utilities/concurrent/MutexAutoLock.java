package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class MutexAutoLock implements AutoCloseable {

	private final Lock lock;
	private boolean locked;

	MutexAutoLock(Lock lock) {
		this.lock = Objects.requireNonNull(lock, "Must provide a lock instance");
		this.locked = true;
	}

	public final Condition newCondition() {
		return this.lock.newCondition();
	}

	@Override
	public void close() {
		if (this.locked) {
			this.lock.unlock();
			this.locked = false;
		}
	}
}