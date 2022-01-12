/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2022 Armedia, LLC
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
package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import com.armedia.commons.utilities.Tools;

public final class MutexAutoLock implements AutoCloseable {

	private static final Runnable NOOP = () -> {
	};

	private final Lock lock;
	private final Runnable preLock;
	private boolean locked;

	public MutexAutoLock(MutexLockable lock) {
		this(lock, null);
	}

	MutexAutoLock(MutexLockable lock, Runnable preLock) {
		this.lock = Objects.requireNonNull(lock, "Must provide a MutexLockable instance").acquireMutexLock();
		this.preLock = Tools.coalesce(preLock, MutexAutoLock.NOOP);
		this.locked = true;
	}

	public Condition newCondition() {
		return this.lock.newCondition();
	}

	@Override
	public void close() {
		if (this.locked) {
			try {
				this.preLock.run();
			} finally {
				this.lock.unlock();
				this.locked = false;
			}
		}
	}
}
