package com.armedia.commons.utilities.concurrent;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>
 * This class exists as a simple concrete implementation of {@link ReadWriteLockable}, for
 * convenience.
 * </p>
 *
 * @author diego
 *
 */
public class BaseReadWriteLockable implements ReadWriteLockable {

	protected final ReadWriteLock rwLock;

	/**
	 * <p>
	 * Create a new instance using a {@link ReentrantReadWriteLock} at its core.
	 * </p>
	 */
	public BaseReadWriteLockable() {
		this(null);
	}

	/**
	 * <p>
	 * Create a new instance. If the given lock is {@code null}, a new
	 * {@link ReentrantReadWriteLock} instance is created and used.
	 * </p>
	 */
	public BaseReadWriteLockable(ReadWriteLock rwLock) {
		this.rwLock = (rwLock != null ? rwLock : new ReentrantReadWriteLock());
	}

	@Override
	public final ReadWriteLock getMainLock() {
		return this.rwLock;
	}
}