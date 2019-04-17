package com.armedia.commons.utilities.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("resource")
class MutexAutoLockTest {

	@Test
	void testConstructor() {
		final MutexLockable mutex = new BaseMutexLockable();
		final Runnable runnable = () -> Assertions.fail("This method should not be called");
		Assertions.assertThrows(NullPointerException.class, () -> new MutexAutoLock(null));
		new MutexAutoLock(mutex);

		Assertions.assertThrows(NullPointerException.class, () -> new MutexAutoLock(null, null));
		Assertions.assertThrows(NullPointerException.class, () -> new MutexAutoLock(null, runnable));
		new MutexAutoLock(mutex, null);
		new MutexAutoLock(mutex, runnable);
	}

	@Test
	void testAutoClose() {
		final ReentrantLock lock = new ReentrantLock();
		final MutexLockable mutex = new BaseMutexLockable(lock);
		final AtomicInteger runnableCalled = new AtomicInteger(0);
		final Runnable runnable = () -> {
			runnableCalled.incrementAndGet();
			Assertions.assertTrue(lock.isLocked());
			Assertions.assertTrue(lock.isHeldByCurrentThread());
			Assertions.assertEquals(1, lock.getHoldCount());
		};

		Assertions.assertFalse(lock.isLocked());
		Assertions.assertFalse(lock.isHeldByCurrentThread());
		Assertions.assertEquals(0, lock.getHoldCount());
		try (MutexAutoLock autoLock = new MutexAutoLock(mutex)) {
			Assertions.assertTrue(lock.isLocked());
			Assertions.assertTrue(lock.isHeldByCurrentThread());
			Assertions.assertEquals(1, lock.getHoldCount());
		}

		Assertions.assertFalse(lock.isLocked());
		Assertions.assertFalse(lock.isHeldByCurrentThread());
		Assertions.assertEquals(0, lock.getHoldCount());
		try (MutexAutoLock autoLock = new MutexAutoLock(mutex, null)) {
			Assertions.assertTrue(lock.isLocked());
			Assertions.assertTrue(lock.isHeldByCurrentThread());
			Assertions.assertEquals(1, lock.getHoldCount());
		}

		runnableCalled.set(0);
		Assertions.assertEquals(0, runnableCalled.get());
		Assertions.assertFalse(lock.isLocked());
		Assertions.assertFalse(lock.isHeldByCurrentThread());
		Assertions.assertEquals(0, lock.getHoldCount());
		try (MutexAutoLock autoLock = new MutexAutoLock(mutex, runnable)) {
			Assertions.assertEquals(0, runnableCalled.get());
			Assertions.assertTrue(lock.isLocked());
			Assertions.assertTrue(lock.isHeldByCurrentThread());
			Assertions.assertEquals(1, lock.getHoldCount());
		}
		Assertions.assertEquals(1, runnableCalled.get());
		Assertions.assertFalse(lock.isLocked());
		Assertions.assertFalse(lock.isHeldByCurrentThread());
		Assertions.assertEquals(0, lock.getHoldCount());
	}

	@Test
	void testNewCondition() throws Exception {
		final ReentrantLock lock = new ReentrantLock();
		final MutexLockable mutex = new BaseMutexLockable(lock);
		MutexAutoLock autoLock = new MutexAutoLock(mutex);
		autoLock.close();
		final Condition c = autoLock.newCondition();
		Assertions.assertNotNull(c);

		Assertions.assertFalse(lock.isHeldByCurrentThread());
		Assertions.assertFalse(lock.isLocked());

		final CyclicBarrier barrier = new CyclicBarrier(2);
		final ExecutorService thread = Executors.newSingleThreadExecutor();
		final Callable<Void> test = () -> {
			barrier.await();
			Assertions.assertFalse(lock.isHeldByCurrentThread());
			Assertions.assertFalse(lock.isLocked());
			lock.lock();
			try {
				Assertions.assertTrue(lock.isHeldByCurrentThread());
				Assertions.assertTrue(lock.isLocked());
				Assertions.assertEquals(1, lock.getHoldCount());
				barrier.await();
				c.await();
				return null;
			} finally {
				lock.unlock();
			}
		};

		try {
			final Future<Void> future = thread.submit(test);
			barrier.await(100, TimeUnit.MILLISECONDS);
			barrier.await(100, TimeUnit.MILLISECONDS);
			Assertions.assertTrue(lock.isLocked());
			Assertions.assertFalse(lock.isHeldByCurrentThread());
			Assertions.assertFalse(future.isDone());
			Thread.sleep(100);
			Assertions.assertTrue(lock.tryLock());
			try {
				c.signal();
			} finally {
				lock.unlock();
			}
			future.get(100, TimeUnit.MILLISECONDS);
		} finally {
			thread.shutdownNow();
		}
	}

	@Test
	void testClose() {
		final ReentrantLock lock = new ReentrantLock();
		final MutexLockable mutex = new BaseMutexLockable(lock);
		final AtomicInteger runnableCalled = new AtomicInteger(0);
		final Runnable runnable = () -> {
			runnableCalled.incrementAndGet();
			Assertions.assertTrue(lock.isLocked());
			Assertions.assertTrue(lock.isHeldByCurrentThread());
			Assertions.assertEquals(1, lock.getHoldCount());
		};

		Assertions.assertFalse(lock.isLocked());
		Assertions.assertFalse(lock.isHeldByCurrentThread());
		Assertions.assertEquals(0, lock.getHoldCount());
		MutexAutoLock autoLock = new MutexAutoLock(mutex);
		Assertions.assertTrue(lock.isLocked());
		Assertions.assertTrue(lock.isHeldByCurrentThread());
		Assertions.assertEquals(1, lock.getHoldCount());
		autoLock.close();

		Assertions.assertFalse(lock.isLocked());
		Assertions.assertFalse(lock.isHeldByCurrentThread());
		Assertions.assertEquals(0, lock.getHoldCount());
		autoLock = new MutexAutoLock(mutex, null);
		Assertions.assertTrue(lock.isLocked());
		Assertions.assertTrue(lock.isHeldByCurrentThread());
		Assertions.assertEquals(1, lock.getHoldCount());
		autoLock.close();

		runnableCalled.set(0);
		Assertions.assertEquals(0, runnableCalled.get());
		Assertions.assertFalse(lock.isLocked());
		Assertions.assertFalse(lock.isHeldByCurrentThread());
		Assertions.assertEquals(0, lock.getHoldCount());
		autoLock = new MutexAutoLock(mutex, runnable);
		Assertions.assertEquals(0, runnableCalled.get());
		Assertions.assertTrue(lock.isLocked());
		Assertions.assertTrue(lock.isHeldByCurrentThread());
		Assertions.assertEquals(1, lock.getHoldCount());
		autoLock.close();
		Assertions.assertEquals(1, runnableCalled.get());
		Assertions.assertFalse(lock.isLocked());
		Assertions.assertFalse(lock.isHeldByCurrentThread());
		Assertions.assertEquals(0, lock.getHoldCount());

		runnableCalled.set(0);
		Assertions.assertEquals(0, runnableCalled.get());
		Assertions.assertFalse(lock.isLocked());
		Assertions.assertFalse(lock.isHeldByCurrentThread());
		Assertions.assertEquals(0, lock.getHoldCount());
		autoLock = new MutexAutoLock(mutex, runnable);
		Assertions.assertEquals(0, runnableCalled.get());
		Assertions.assertTrue(lock.isLocked());
		Assertions.assertTrue(lock.isHeldByCurrentThread());
		Assertions.assertEquals(1, lock.getHoldCount());
		autoLock.close();
		// Make sure subsequent calls do nothing
		autoLock.close();
		Assertions.assertEquals(1, runnableCalled.get());
		Assertions.assertFalse(lock.isLocked());
		Assertions.assertFalse(lock.isHeldByCurrentThread());
		Assertions.assertEquals(0, lock.getHoldCount());
	}

}
