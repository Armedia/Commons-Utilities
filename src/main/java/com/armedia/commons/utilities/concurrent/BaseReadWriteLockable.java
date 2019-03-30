package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
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

	private final ReadWriteLock rwLock;

	/**
	 * <p>
	 * Create a new instance using a {@link ReentrantReadWriteLock} at its core.
	 * </p>
	 */
	public BaseReadWriteLockable() {
		this(ReadWriteLockable.NULL_LOCK);
	}

	public BaseReadWriteLockable(ReadWriteLockable lockable) {
		this(BaseReadWriteLockable.extractLock(lockable));
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
	public final ReadWriteLock getLock() {
		return this.rwLock;
	}

	protected static ReadWriteLock extractLock(ReadWriteLockable lockable) {
		return Objects.requireNonNull(lockable, "Must provide a non-null ReadWriteLockable instance").getLock();
	}
}