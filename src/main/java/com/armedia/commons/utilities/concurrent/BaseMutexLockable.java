package com.armedia.commons.utilities.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.annotation.XmlTransient;

/**
 * <p>
 * This class exists as a simple concrete implementation of {@link ShareableLockable}, for
 * convenience.
 * </p>
 *
 * @author diego
 *
 */
@XmlTransient
public class BaseMutexLockable implements MutexLockable {

	@XmlTransient
	private final Lock lock;

	/**
	 * <p>
	 * Create a new instance using a {@link ReentrantReadWriteLock} at its core.
	 * </p>
	 */
	public BaseMutexLockable() {
		this(MutexLockable.NULL_LOCK);
	}

	public BaseMutexLockable(MutexLockable lockable) {
		this(MutexLockable.extractMutexLock(lockable));
	}

	/**
	 * <p>
	 * Create a new instance. If the given lock is {@code null}, a new
	 * {@link ReentrantReadWriteLock} instance is created and used.
	 * </p>
	 */
	public BaseMutexLockable(Lock lock) {
		this.lock = (lock != null ? lock : new ReentrantLock());
	}

	@Override
	public final Lock getMutexLock() {
		return this.lock;
	}
}