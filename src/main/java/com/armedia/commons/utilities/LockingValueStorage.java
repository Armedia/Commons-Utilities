/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2020 Armedia, LLC
 * %%
 * This file is part of the Caliente software.
 * 
 * If the software was purchased under a paid Caliente license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * 
 * Caliente is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Caliente is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Caliente. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 *******************************************************************************/
package com.armedia.commons.utilities;

import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.armedia.commons.utilities.concurrent.ShareableLockable;

/**
 *
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
