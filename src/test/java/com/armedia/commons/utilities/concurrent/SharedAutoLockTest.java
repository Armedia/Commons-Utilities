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

import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("resource")
public class SharedAutoLockTest {

	@Test
	public void testConstructor() {
		final ShareableLockable lock = new BaseShareableLockable();
		Assertions.assertThrows(NullPointerException.class, () -> new SharedAutoLock(null));
		new SharedAutoLock(lock);
	}

	@Test
	public void testUpgrade() {
		final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
		final ShareableLockable lock = new BaseShareableLockable(rwLock);
		try (SharedAutoLock basic = new SharedAutoLock(lock)) {
			Assertions.assertEquals(1, rwLock.getReadHoldCount());
			Assertions.assertEquals(1, rwLock.getReadLockCount());
			Assertions.assertFalse(rwLock.isWriteLocked());
			Assertions.assertFalse(rwLock.isWriteLockedByCurrentThread());
			Assertions.assertEquals(0, rwLock.getWriteHoldCount());
			try (MutexAutoLock upgraded = basic.upgrade()) {
				Assertions.assertEquals(0, rwLock.getReadHoldCount());
				Assertions.assertEquals(0, rwLock.getReadLockCount());
				Assertions.assertTrue(rwLock.isWriteLocked());
				Assertions.assertTrue(rwLock.isWriteLockedByCurrentThread());
				Assertions.assertEquals(1, rwLock.getWriteHoldCount());
			}
			Assertions.assertEquals(1, rwLock.getReadHoldCount());
			Assertions.assertEquals(1, rwLock.getReadLockCount());
			Assertions.assertFalse(rwLock.isWriteLocked());
			Assertions.assertFalse(rwLock.isWriteLockedByCurrentThread());
			Assertions.assertEquals(0, rwLock.getWriteHoldCount());
		}
		Assertions.assertEquals(0, rwLock.getReadHoldCount());
		Assertions.assertEquals(0, rwLock.getReadLockCount());
		Assertions.assertFalse(rwLock.isWriteLocked());
		Assertions.assertFalse(rwLock.isWriteLockedByCurrentThread());
		Assertions.assertEquals(0, rwLock.getWriteHoldCount());

		SharedAutoLock auto = new SharedAutoLock(lock);
		auto.close();
		Assertions.assertThrows(IllegalStateException.class, () -> auto.upgrade());
	}

	@Test
	public void testDeadlockDetection() throws Exception {
		final CyclicBarrier barrier = new CyclicBarrier(2);
		ExecutorService thread = Executors.newSingleThreadExecutor();
		Callable<Void> test = () -> {
			barrier.await();
			final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
			final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
			final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
			final BaseShareableLockable rwl = new BaseShareableLockable(lock);

			Assertions.assertFalse(writeLock.isHeldByCurrentThread());
			Assertions.assertEquals(0, lock.getReadHoldCount());

			readLock.lock();
			Assertions.assertEquals(1, lock.getReadHoldCount());
			Assertions.assertEquals(1, lock.getReadLockCount());
			Assertions.assertFalse(lock.isWriteLocked());
			Assertions.assertFalse(lock.isWriteLockedByCurrentThread());
			Assertions.assertEquals(0, lock.getWriteHoldCount());
			try (SharedAutoLock basic = new SharedAutoLock(rwl)) {
				Assertions.assertEquals(2, lock.getReadHoldCount());
				Assertions.assertEquals(2, lock.getReadLockCount());
				Assertions.assertFalse(lock.isWriteLocked());
				Assertions.assertFalse(lock.isWriteLockedByCurrentThread());
				Assertions.assertEquals(0, lock.getWriteHoldCount());
				try (MutexAutoLock upgraded = basic.upgrade()) {
					Assertions.fail("Did not fail on an obvious deadlock");
				} catch (LockUpgradeDeadlockException e) {
					Assertions.assertSame(rwl, e.getTarget());
					Assertions.assertEquals(1, e.getReadHoldCount());
				}
				Assertions.assertEquals(2, lock.getReadHoldCount());
				Assertions.assertEquals(2, lock.getReadLockCount());
				Assertions.assertFalse(lock.isWriteLocked());
				Assertions.assertFalse(lock.isWriteLockedByCurrentThread());
				Assertions.assertEquals(0, lock.getWriteHoldCount());
			}

			return null;
		};

		// We do this in a background thread to avoid waiting forever...
		try {
			final Future<Void> future = thread.submit(test);
			barrier.await();
			future.get(100, TimeUnit.MILLISECONDS);
		} finally {
			thread.shutdownNow();
		}
	}

	@Test
	public void testAutoClose() {
		final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
		final ShareableLockable lock = new BaseShareableLockable(rwLock);
		try (SharedAutoLock autoLock = new SharedAutoLock(lock)) {
			Assertions.assertEquals(1, rwLock.getReadHoldCount());
			Assertions.assertEquals(1, rwLock.getReadLockCount());
			Assertions.assertFalse(rwLock.isWriteLocked());
			Assertions.assertFalse(rwLock.isWriteLockedByCurrentThread());
			Assertions.assertEquals(0, rwLock.getWriteHoldCount());
		}
		Assertions.assertEquals(0, rwLock.getReadHoldCount());
		Assertions.assertEquals(0, rwLock.getReadLockCount());
		Assertions.assertFalse(rwLock.isWriteLocked());
		Assertions.assertFalse(rwLock.isWriteLockedByCurrentThread());
		Assertions.assertEquals(0, rwLock.getWriteHoldCount());
	}

	@Test
	public void testClose() {
		final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
		final ShareableLockable lock = new BaseShareableLockable(rwLock);
		SharedAutoLock autoLock = new SharedAutoLock(lock);
		Assertions.assertEquals(1, rwLock.getReadHoldCount());
		Assertions.assertEquals(1, rwLock.getReadLockCount());
		Assertions.assertFalse(rwLock.isWriteLocked());
		Assertions.assertFalse(rwLock.isWriteLockedByCurrentThread());
		Assertions.assertEquals(0, rwLock.getWriteHoldCount());
		autoLock.close();
		Assertions.assertEquals(0, rwLock.getReadHoldCount());
		Assertions.assertEquals(0, rwLock.getReadLockCount());
		Assertions.assertFalse(rwLock.isWriteLocked());
		Assertions.assertFalse(rwLock.isWriteLockedByCurrentThread());
		Assertions.assertEquals(0, rwLock.getWriteHoldCount());

		autoLock = new SharedAutoLock(lock);
		Assertions.assertEquals(1, rwLock.getReadHoldCount());
		Assertions.assertEquals(1, rwLock.getReadLockCount());
		Assertions.assertFalse(rwLock.isWriteLocked());
		Assertions.assertFalse(rwLock.isWriteLockedByCurrentThread());
		Assertions.assertEquals(0, rwLock.getWriteHoldCount());
		autoLock.close();
		// The 2nd call should do nothing
		autoLock.close();
		Assertions.assertEquals(0, rwLock.getReadHoldCount());
		Assertions.assertEquals(0, rwLock.getReadLockCount());
		Assertions.assertFalse(rwLock.isWriteLocked());
		Assertions.assertFalse(rwLock.isWriteLockedByCurrentThread());
		Assertions.assertEquals(0, rwLock.getWriteHoldCount());
	}

}
