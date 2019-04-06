package com.armedia.commons.utilities.concurrent;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.function.CheckedRunnable;
import com.armedia.commons.utilities.function.CheckedSupplier;

public class BaseMutexLockableTest {

	@Test
	public void testConstructor() {
		final ReentrantLock lock = new ReentrantLock();
		BaseMutexLockable rwl = null;

		rwl = new BaseMutexLockable();
		Assertions.assertNotNull(rwl.getMutexLock());
		Assertions.assertNotSame(lock, rwl.getMutexLock());

		rwl = new BaseMutexLockable(MutexLockable.NULL_LOCK);
		Assertions.assertNotNull(rwl.getMutexLock());
		Assertions.assertNotSame(lock, rwl.getMutexLock());

		Assertions.assertThrows(NullPointerException.class, () -> new BaseMutexLockable((ShareableLockable) null));

		Assertions.assertNotNull(rwl);
		Lock other = rwl.getMutexLock();
		rwl = new BaseMutexLockable(rwl);
		Assertions.assertNotNull(rwl.getMutexLock());
		Assertions.assertSame(other, rwl.getMutexLock());

		rwl = new BaseMutexLockable(lock);
		Assertions.assertNotNull(rwl.getMutexLock());
		Assertions.assertSame(lock, rwl.getMutexLock());
	}

	@Test
	public void testGetWriteLock() {
		final ReentrantLock lock = new ReentrantLock();
		final BaseMutexLockable rwl = new BaseMutexLockable(lock);

		Assertions.assertSame(lock, rwl.getMutexLock());
	}

	@Test
	public void testGetAutoWriteLock() {
		final ReentrantLock lock = new ReentrantLock();
		final BaseMutexLockable rwl = new BaseMutexLockable(lock);

		AutoLock autoLock = rwl.getAutoMutexLock();
		Assertions.assertSame(lock, autoLock.getLock());
		Assertions.assertFalse(lock.isLocked());
		Assertions.assertFalse(lock.isHeldByCurrentThread());
	}

	@Test
	public void testAcquireWriteLock() throws Exception {
		final ReentrantLock lock = new ReentrantLock();
		final BaseMutexLockable rwl = new BaseMutexLockable(lock);

		Assertions.assertSame(lock, rwl.getMutexLock());

		Assertions.assertFalse(lock.isHeldByCurrentThread());
		Assertions.assertSame(lock, rwl.acquireMutexLock());
		Assertions.assertTrue(lock.isHeldByCurrentThread());
		lock.unlock();
		Assertions.assertFalse(lock.isHeldByCurrentThread());

		Assertions.assertThrows(RuntimeException.class, () -> lock.unlock(),
			"The write lock was not held but was unlocked");

		Assertions.assertFalse(lock.isHeldByCurrentThread());
		for (int i = 1; i <= 10; i++) {
			Assertions.assertNotNull(rwl.acquireMutexLock(),
				String.format("Failed to acquire the writing lock on attempt # %d", i));
			Assertions.assertTrue(lock.isHeldByCurrentThread());
		}

		for (int i = 10; i > 0; i--) {
			Assertions.assertTrue(lock.isHeldByCurrentThread());
			Assertions.assertTrue(lock.isLocked());
			try {
				lock.unlock();
			} catch (Exception e) {
				Assertions.fail(String.format("Failed to release the writing lock on attempt # %d", i));
			}
		}
		Assertions.assertFalse(lock.isHeldByCurrentThread());
		Assertions.assertFalse(lock.isLocked());
	}

	@Test
	public void testAcquireAutoWriteLock() throws Exception {
		final ReentrantLock lock = new ReentrantLock();
		final BaseMutexLockable rwl = new BaseMutexLockable(lock);

		Assertions.assertSame(lock, rwl.getMutexLock());
		Assertions.assertFalse(lock.isLocked());
		Assertions.assertFalse(lock.isHeldByCurrentThread());

		try (AutoLock auto = rwl.autoMutexLock()) {
			Assertions.assertSame(lock, auto.getLock());
			Assertions.assertTrue(lock.isLocked());
			Assertions.assertTrue(lock.isHeldByCurrentThread());
		}
		Assertions.assertFalse(lock.isLocked());
		Assertions.assertFalse(lock.isHeldByCurrentThread());

		Assertions.assertThrows(RuntimeException.class, () -> lock.unlock(),
			"The write lock was not held but was unlocked");

		Assertions.assertFalse(lock.isHeldByCurrentThread());
		List<AutoLock> autoLocks = new LinkedList<>();
		for (int i = 1; i <= 10; i++) {
			AutoLock l = rwl.autoMutexLock();
			Assertions.assertNotNull(l, String.format("Failed to acquire the writing lock on attempt # %d", i));
			Assertions.assertSame(lock, l.getLock());
			Assertions.assertTrue(lock.isHeldByCurrentThread());
			autoLocks.add(l);
		}

		int i = 0;
		for (AutoLock l : autoLocks) {
			Assertions.assertTrue(lock.isHeldByCurrentThread());
			try {
				l.close();
				i++;
			} catch (Exception e) {
				Assertions.fail(String.format("Failed to release the writing lock on attempt # %d", i));
			}
		}
		Assertions.assertFalse(lock.isLocked());
		Assertions.assertFalse(lock.isHeldByCurrentThread());
	}

	@Test
	public void testWriteLocked() {
		final ReentrantLock lock = new ReentrantLock();
		final BaseMutexLockable rwl = new BaseMutexLockable(lock);

		Assertions.assertThrows(NullPointerException.class,
			() -> rwl.mutexLocked((CheckedSupplier<Object, Exception>) null), "Did not fail with a null Supplier");
		Assertions.assertThrows(NullPointerException.class, () -> rwl.mutexLocked((CheckedRunnable<Exception>) null),
			"Did not fail with a null Runnable");

		Assertions.assertTrue(lock.tryLock());
		Assertions.assertTrue(lock.isHeldByCurrentThread());
		lock.unlock();

		Assertions.assertFalse(lock.isHeldByCurrentThread());
		Lock ret = rwl.mutexLocked(() -> {
			// Prove that we're holding the read lock
			Assertions.assertTrue(lock.isHeldByCurrentThread());
			return lock;
		});
		Assertions.assertFalse(lock.isHeldByCurrentThread());
		Assertions.assertSame(lock, ret);

		Assertions.assertFalse(lock.isHeldByCurrentThread());
		final RuntimeException ex = new RuntimeException();
		try {
			rwl.mutexLocked(() -> {
				// Prove that we're holding the read lock
				Assertions.assertTrue(lock.isHeldByCurrentThread());
				throw ex;
			});
			Assertions.fail("Did not cascade the raised exception");
		} catch (Throwable t) {
			Assertions.assertSame(ex, t);
		}
		Assertions.assertFalse(lock.isHeldByCurrentThread());
		Assertions.assertSame(lock, ret);

		Assertions.assertFalse(lock.isHeldByCurrentThread());
		rwl.mutexLocked(() -> Assertions.assertTrue(lock.isHeldByCurrentThread()));
		Assertions.assertFalse(lock.isHeldByCurrentThread());

		{
			Object a = new Object();
			Supplier<Object> operation = () -> {
				// Prove that we're holding the read lock
				Assertions.assertEquals(1, lock.getHoldCount());
				Assertions.assertTrue(lock.isHeldByCurrentThread());
				Assertions.assertTrue(lock.isLocked());
				return a;
			};
			Assertions.assertFalse(lock.isHeldByCurrentThread());
			Assertions.assertFalse(lock.isLocked());
			Assertions.assertSame(a, rwl.mutexLocked(operation));
			Assertions.assertFalse(lock.isHeldByCurrentThread());
			Assertions.assertFalse(lock.isLocked());
		}
		{
			Runnable operation = () -> {
				// Prove that we're holding the read lock
				Assertions.assertTrue(lock.isLocked());
				Assertions.assertTrue(lock.isHeldByCurrentThread());
			};
			Assertions.assertFalse(lock.isHeldByCurrentThread());
			Assertions.assertFalse(lock.isLocked());
			rwl.mutexLocked(operation);
			Assertions.assertFalse(lock.isLocked());
			Assertions.assertFalse(lock.isHeldByCurrentThread());
		}
	}
}