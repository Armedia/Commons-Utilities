package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
import java.util.concurrent.locks.Lock;

public final class SharedAutoLock implements AutoCloseable {

	private final ShareableLockable source;
	private final Lock lock;
	private final Runnable preMutexClose;
	private boolean locked;

	public SharedAutoLock(ShareableLockable source) {
		this.source = Objects.requireNonNull(source, "Must provide a ShareableLockable instance");
		this.lock = this.source.acquireSharedLock();
		this.locked = true;
		this.preMutexClose = () -> {
			// Re-acquire the shared lock
			this.lock.lock();
			this.locked = true;
		};
	}

	public MutexAutoLock upgrade() {
		// We can't upgrade if we don't hold the lock
		if (!this.locked) { throw new IllegalStateException("Can't upgrade if we don't currently hold the lock"); }
		this.lock.unlock();
		this.locked = false;
		boolean ok = false;
		try {
			MutexAutoLock ret = new MutexAutoLock(this.source, this.preMutexClose);
			ok = true;
			return ret;
		} finally {
			// If we failed, then try to re-acquire the read lock
			if (!ok) {
				this.lock.lock();
				this.locked = true;
			}
		}
	}

	@Override
	public void close() {
		if (this.locked) {
			this.lock.unlock();
			this.locked = false;
		}
	}
}