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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.armedia.commons.utilities.concurrent.ReadWriteLockable;

/**
 * @author drivera@armedia.com
 *
 */
public class LockingValueStorage<T> extends SimpleValueStorage<T> implements ReadWriteLockable {

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
	public ReadWriteLock getMainLock() {
		return this.rwLock;
	}

	@Override
	public T setValue(String name, T value) {
		return writeLocked(() -> super.setValue(name, value));
	}

	@Override
	public T getValue(String name) {
		return readLocked(() -> super.getValue(name));
	}

	@Override
	public boolean hasValue(String name) {
		return readLocked(() -> super.hasValue(name));
	}

	@Override
	public Set<String> getValueNames() {
		return readLocked(() -> super.getValueNames());
	}

	@Override
	public T clearValue(String name) {
		return writeLocked(() -> super.clearValue(name));
	}

	@Override
	public void clearAllValues() {
		writeLocked(() -> super.clearAllValues());
	}
}