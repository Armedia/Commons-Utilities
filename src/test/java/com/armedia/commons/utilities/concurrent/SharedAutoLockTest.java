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
class SharedAutoLockTest {

	@Test
	void testConstructor() {
		final ShareableLockable lock = new BaseShareableLockable();
		Assertions.assertThrows(NullPointerException.class, () -> new SharedAutoLock(null));
		new SharedAutoLock(lock);
	}

	@Test
	void testUpgrade() {
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
				} catch (LockDisallowedException e) {
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
	void testAutoClose() {
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
	void testClose() {
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
