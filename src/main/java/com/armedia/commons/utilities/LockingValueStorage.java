/**
 * *******************************************************************
 *
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS. REPRODUCTION OF ANY PORTION
 * OF THE SOURCE CODE, CONTAINED HEREIN, OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE, IS
 * STRICTLY PROHIBITED.
 *
 * Confidential Property of Armedia LLC. (c) Copyright Armedia LLC 2011. All Rights reserved.
 *
 * *******************************************************************
 */
package com.armedia.commons.utilities;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author drivera@armedia.com
 *
 */
public class LockingValueStorage<T> extends SimpleValueStorage<T> {

	private final ReadWriteLock rwLock;

	public LockingValueStorage() {
		this(false, false);
	}

	public LockingValueStorage(boolean ordered) {
		this(ordered, false);
	}

	public LockingValueStorage(boolean ordered, boolean fairLock) {
		super(ordered);
		this.rwLock = new ReentrantReadWriteLock(fairLock);
	}

	public LockingValueStorage(boolean ordered, ReadWriteLock rwLock) {
		super(ordered);
		if (rwLock == null) { throw new IllegalArgumentException("Must provide a ReadWriteLock instance"); }
		this.rwLock = rwLock;
	}

	@Override
	public T setValue(String name, T value) {
		final Lock l = this.rwLock.writeLock();
		l.lock();
		try {
			return super.setValue(name, value);
		} finally {
			l.unlock();
		}
	}

	@Override
	public T getValue(String name) {
		final Lock l = this.rwLock.readLock();
		l.lock();
		try {
			return super.getValue(name);
		} finally {
			l.unlock();
		}
	}

	@Override
	public boolean hasValue(String name) {
		final Lock l = this.rwLock.readLock();
		l.lock();
		try {
			return super.hasValue(name);
		} finally {
			l.unlock();
		}
	}

	@Override
	public Set<String> getValueNames() {
		final Lock l = this.rwLock.readLock();
		l.lock();
		try {
			return super.getValueNames();
		} finally {
			l.unlock();
		}
	}

	@Override
	public T clearValue(String name) {
		final Lock l = this.rwLock.writeLock();
		l.lock();
		try {
			return super.clearValue(name);
		} finally {
			l.unlock();
		}
	}

	@Override
	public void clearAllValues() {
		final Lock l = this.rwLock.writeLock();
		l.lock();
		try {
			super.clearAllValues();
		} finally {
			l.unlock();
		}
	}
}