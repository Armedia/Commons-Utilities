package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.annotation.XmlTransient;

import com.armedia.commons.utilities.Tools;

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
public class BaseShareableLockable extends BaseMutexLockable implements ShareableLockable {

	@XmlTransient
	private final ReadWriteLock rwLock;

	/**
	 * <p>
	 * Create a new instance using a {@link ReentrantReadWriteLock} at its core.
	 * </p>
	 */
	public BaseShareableLockable() {
		this(ShareableLockable.NULL_LOCK);
	}

	public BaseShareableLockable(ShareableLockable lockable) {
		this(BaseShareableLockable.extractLock(lockable));
	}

	/**
	 * <p>
	 * Create a new instance. If the given lock is {@code null}, a new
	 * {@link ReentrantReadWriteLock} instance is created and used.
	 * </p>
	 */
	public BaseShareableLockable(ReadWriteLock rwLock) {
		this(rwLock != null ? rwLock : new ReentrantReadWriteLock(), null);
	}

	/**
	 * <p>
	 * This constructor exists to facilitate inheritance from the superclass
	 * </p>
	 *
	 * @param rwLock
	 * @param marker
	 */
	private BaseShareableLockable(ReadWriteLock rwLock, Object marker) {
		super(rwLock.writeLock());
		this.rwLock = rwLock;
	}

	@Override
	public final ReadWriteLock getShareableLock() {
		return this.rwLock;
	}

	protected static ReadWriteLock extractLock(ShareableLockable lockable) {
		return Objects.requireNonNull(lockable, "Must provide a non-null ReadWriteLockable instance")
			.getShareableLock();
	}

	protected static ShareableLockable extractShareableLockable(Object o) {
		return Tools.cast(ShareableLockable.class, o);
	}
}