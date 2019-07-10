/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2019 Armedia, LLC
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

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.annotation.XmlTransient;

/**
 * <p>
 * This class exists as a simple concrete implementation of {@link ShareableLockable}, for
 * convenience.
 * </p>
 *
 *
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
		this(ShareableLockable.extractShareableLock(lockable));
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
}
