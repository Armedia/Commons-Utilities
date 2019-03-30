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

import com.armedia.commons.utilities.concurrent.ShareableLockable;

/**
 * @author drivera@armedia.com
 *
 */
public class LockingValueStorage<T> extends SimpleValueStorage<T> implements ShareableLockable {

	private final ReadWriteLock rwLock;

	public LockingValueStorage() {
		this(false, null);
	}

	public LockingValueStorage(boolean ordered) {
		this(ordered, null);
	}

	public LockingValueStorage(ReadWriteLock rwLock) {
		this(false, rwLock);
	}

	public LockingValueStorage(boolean ordered, ReadWriteLock rwLock) {
		super(ordered);
		this.rwLock = (rwLock != null ? rwLock : new ReentrantReadWriteLock());
	}

	@Override
	public ReadWriteLock getShareableLock() {
		return this.rwLock;
	}

	@Override
	public T setValue(String name, T value) {
		return mutexLocked(() -> super.setValue(name, value));
	}

	@Override
	public T getValue(String name) {
		return shareLocked(() -> super.getValue(name));
	}

	@Override
	public boolean hasValue(String name) {
		return shareLocked(() -> super.hasValue(name));
	}

	@Override
	public Set<String> getValueNames() {
		return shareLocked(() -> super.getValueNames());
	}

	@Override
	public T clearValue(String name) {
		return mutexLocked(() -> super.clearValue(name));
	}

	@Override
	public void clearAllValues() {
		mutexLocked(() -> super.clearAllValues());
	}
}