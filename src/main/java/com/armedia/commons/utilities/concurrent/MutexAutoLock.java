package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import com.armedia.commons.utilities.Tools;

public final class MutexAutoLock implements AutoCloseable {

	private static final Runnable NOOP = () -> {
	};

	private final Lock lock;
	private final Runnable preLock;
	private boolean locked;

	public MutexAutoLock(MutexLockable lock) {
		this(lock, null);
	}

	MutexAutoLock(MutexLockable lock, Runnable preLock) {
		this.lock = Objects.requireNonNull(lock, "Must provide a MutexLockable instance").acquireMutexLock();
		this.preLock = Tools.coalesce(preLock, MutexAutoLock.NOOP);
		this.locked = true;
	}

	public Condition newCondition() {
		return this.lock.newCondition();
	}

	@Override
	public void close() {
		if (this.locked) {
			try {
				this.preLock.run();
			} finally {
				this.lock.unlock();
				this.locked = false;
			}
		}
	}
}