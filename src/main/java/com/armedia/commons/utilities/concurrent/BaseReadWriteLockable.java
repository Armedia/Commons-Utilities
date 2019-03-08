package com.armedia.commons.utilities.concurrent;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BaseReadWriteLockable implements ReadWriteLockable {

	protected final ReadWriteLock rwLock;

	public BaseReadWriteLockable() {
		this(null);
	}

	public BaseReadWriteLockable(ReadWriteLock rwLock) {
		this.rwLock = (rwLock != null ? rwLock : new ReentrantReadWriteLock());
	}

	@Override
	public final ReadWriteLock getMainLock() {
		return this.rwLock;
	}
}