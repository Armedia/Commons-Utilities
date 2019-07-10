/*******************************************************************************
 * #%L
 * Armedia Caliente
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

public class LockUpgradeDeadlockException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final ShareableLockable target;
	private final int readCount;

	LockUpgradeDeadlockException(ShareableLockable target, int readCount) {
		super(String.format("Locking operation is not allowed: %d read locks held when requesting a write lock",
			readCount));
		this.target = target;
		this.readCount = readCount;
	}

	public ShareableLockable getTarget() {
		return this.target;
	}

	public int getReadHoldCount() {
		return this.readCount;
	}
}
