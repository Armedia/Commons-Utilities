package com.armedia.commons.utilities.concurrent;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
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

		try (MutexAutoLock auto = rwl.autoMutexLock()) {
			Assertions.assertTrue(lock.isLocked());
			Assertions.assertTrue(lock.isHeldByCurrentThread());
		}
		Assertions.assertFalse(lock.isLocked());
		Assertions.assertFalse(lock.isHeldByCurrentThread());

		Assertions.assertThrows(RuntimeException.class, () -> lock.unlock(),
			"The write lock was not held but was unlocked");

		Assertions.assertFalse(lock.isHeldByCurrentThread());
		List<MutexAutoLock> autoLocks = new LinkedList<>();
		for (int i = 1; i <= 10; i++) {
			MutexAutoLock l = rwl.autoMutexLock();
			Assertions.assertNotNull(l, String.format("Failed to acquire the writing lock on attempt # %d", i));
			Assertions.assertTrue(lock.isHeldByCurrentThread());
			autoLocks.add(l);
		}

		int i = 0;
		for (MutexAutoLock l : autoLocks) {
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
	public void testGetCondition() throws Exception {
		final ReentrantLock lock = new ReentrantLock();
		final BaseMutexLockable rwl = new BaseMutexLockable(lock);

		Assertions.assertSame(lock, rwl.getMutexLock());

		Condition c = rwl.newMutexCondition();
		Assertions.assertNotNull(c);
		Assertions.assertFalse(lock.isLocked());
		Assertions.assertFalse(lock.isHeldByCurrentThread());
		Assertions.assertThrows(Throwable.class, () -> c.await());

		try (MutexAutoLock auto = rwl.autoMutexLock()) {
			Assertions.assertTrue(lock.isLocked());
			Assertions.assertTrue(lock.isHeldByCurrentThread());
			Assertions.assertFalse(c.await(100, TimeUnit.MILLISECONDS));
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

	@Test
	public void testRunnableAlwaysChecked() throws Throwable {
		final ReentrantLock lock = new ReentrantLock();
		final BaseMutexLockable rwl = new BaseMutexLockable(lock);
		final AtomicLong callCount = new AtomicLong(0);

		callCount.set(0);
		rwl.mutexLocked(new CheckedRunnable<Throwable>() {
			@Override
			public void run() {
				Assertions.fail("CheckedRunnable.run() should never be called!");
			}

			@Override
			public void runChecked() throws Throwable {
				callCount.incrementAndGet();
			}
		});
		Assertions.assertEquals(1, callCount.get());

		callCount.set(0);
		Assertions.assertThrows(IOException.class, () -> rwl.mutexLocked(new CheckedRunnable<IOException>() {
			@Override
			public void run() {
				Assertions.fail("CheckedRunnable.run() should never be called!");
			}

			@Override
			public void runChecked() throws IOException {
				callCount.incrementAndGet();
				throw new IOException("This is a test exception");
			}
		}));
		Assertions.assertEquals(1, callCount.get());
	}

	@Test
	public void testSupplierAlwaysChecked() throws Throwable {
		final ReentrantLock lock = new ReentrantLock();
		final BaseMutexLockable rwl = new BaseMutexLockable(lock);
		final AtomicLong callCount = new AtomicLong(0);

		callCount.set(0);
		rwl.mutexLocked(new CheckedSupplier<Object, Throwable>() {
			@Override
			public Object get() {
				Assertions.fail("CheckedSupplier.get() should never be called!");
				return null;
			}

			@Override
			public Object getChecked() throws Throwable {
				callCount.incrementAndGet();
				return null;
			}
		});
		Assertions.assertEquals(1, callCount.get());

		callCount.set(0);
		Assertions.assertThrows(IOException.class, () -> rwl.mutexLocked(new CheckedSupplier<Object, IOException>() {
			@Override
			public Object get() {
				Assertions.fail("CheckedSupplier.get() should never be called!");
				return null;
			}

			@Override
			public Object getChecked() throws IOException {
				callCount.incrementAndGet();
				throw new IOException("This is a test exception");
			}
		}));
		Assertions.assertEquals(1, callCount.get());
	}
}