/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (c) 2010 - 2019 Armedia LLC
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
import java.util.concurrent.locks.Lock;

public final class SharedAutoLock implements AutoCloseable {

	private final ShareableLockable source;
	private final Lock lock;
	private final Runnable preMutexClose;
	private boolean locked;

	public SharedAutoLock(ShareableLockable source) {
		this.source = Objects.requireNonNull(source, "Must provide a ShareableLockable instance");
		this.lock = this.source.acquireSharedLock();
		this.locked = true;
		this.preMutexClose = () -> {
			// Re-acquire the shared lock
			this.lock.lock();
			this.locked = true;
		};
	}

	public MutexAutoLock upgrade() {
		// We can't upgrade if we don't hold the lock
		if (!this.locked) { throw new IllegalStateException("Can't upgrade if we don't currently hold the lock"); }
		this.lock.unlock();
		this.locked = false;
		boolean ok = false;
		try {
			MutexAutoLock ret = new MutexAutoLock(this.source, this.preMutexClose);
			ok = true;
			return ret;
		} finally {
			// If we failed, then try to re-acquire the read lock
			if (!ok) {
				this.lock.lock();
				this.locked = true;
			}
		}
	}

	@Override
	public void close() {
		if (this.locked) {
			this.lock.unlock();
			this.locked = false;
		}
	}
}