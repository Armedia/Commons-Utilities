package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReadWriteLockedTest {

	@Test
	public void testGetReadLock() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
		final ReadWriteLockable rwl = new BaseReadWriteLockable(lock);

		Assertions.assertSame(readLock, rwl.getReadLock());
	}

	@Test
	public void testGetWriteLock() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final ReadWriteLockable rwl = new BaseReadWriteLockable(lock);

		Assertions.assertSame(writeLock, rwl.getWriteLock());
	}

	@Test
	public void testAcquireReadLock() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final ReadWriteLockable rwl = new BaseReadWriteLockable(lock);

		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertSame(lock.readLock(), rwl.acquireReadLock());
		Assertions.assertEquals(1, lock.getReadHoldCount());
		Assertions.assertFalse(rwl.getWriteLock().tryLock(),
			"Succeeded in acquiring the write lock while the read lock was held");
		Assertions.assertEquals(1, lock.getReadHoldCount());
		readLock.unlock();
		Assertions.assertEquals(0, lock.getReadHoldCount());

		Assertions.assertSame(lock.readLock(), rwl.getReadLock());
		Assertions.assertEquals(0, lock.getReadHoldCount());
		try {
			readLock.unlock();
			Assertions.fail("The read lock was not held but was unlocked");
		} catch (Exception e) {
			// All is well
		}

		Assertions.assertEquals(0, lock.getReadHoldCount());
		for (int i = 1; i <= 10; i++) {
			Assertions.assertNotNull(rwl.acquireReadLock(),
				String.format("Failed to acquire the reading lock on attempt # %d", i));
		}
		Assertions.assertEquals(10, lock.getReadHoldCount());
		Assertions.assertFalse(rwl.getWriteLock().tryLock(),
			"Succeeded in acquiring the write lock while the read lock was held");
		for (int i = 10; i > 0; i--) {
			try {
				readLock.unlock();
			} catch (Exception e) {
				Assertions.fail(String.format("Failed to release the reading lock on attempt # %d", i));
			}
		}
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertTrue(rwl.getWriteLock().tryLock(),
			"Failed to acquire the write lock while the read lock was not held");
		rwl.getWriteLock().unlock();
	}

	@Test
	public void testAcquireWriteLock() throws Exception {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final ReadWriteLockable rwl = new BaseReadWriteLockable(lock);

		Assertions.assertSame(lock.writeLock(), rwl.getWriteLock());

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertSame(lock.writeLock(), rwl.acquireWriteLock());
		Assertions.assertTrue(writeLock.isHeldByCurrentThread());
		writeLock.unlock();
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());

		try {
			writeLock.unlock();
			Assertions.fail("The write lock was not held but was unlocked");
		} catch (Exception e) {
			// All is well
		}

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		for (int i = 1; i <= 10; i++) {
			Assertions.assertNotNull(rwl.acquireWriteLock(),
				String.format("Failed to acquire the writing lock on attempt # %d", i));
			Assertions.assertTrue(writeLock.isHeldByCurrentThread());
		}
		Assertions.assertTrue(rwl.getReadLock().tryLock(),
			"Failed to acquire the read lock while the write lock was held");
		rwl.getReadLock().unlock();

		for (int i = 10; i > 0; i--) {
			Assertions.assertTrue(writeLock.isHeldByCurrentThread());
			try {
				writeLock.unlock();
			} catch (Exception e) {
				Assertions.fail(String.format("Failed to release the writing lock on attempt # %d", i));
			}
		}
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
	}

	@Test
	public void testReadLocked() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final ReadWriteLockable rwl = new BaseReadWriteLockable(lock);

		try {
			rwl.readLocked((Supplier<Object>) null);
			Assertions.fail("Did not fail with a null Supplier");
		} catch (NullPointerException e) {
		}

		try {
			rwl.readLocked((Runnable) null);
			Assertions.fail("Did not fail with a null Runnable");
		} catch (NullPointerException e) {
		}

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		ReadWriteLock ret = rwl.readLocked(() -> {
			// Prove that we're holding the read lock
			Assertions.assertFalse(writeLock.isHeldByCurrentThread());
			Assertions.assertFalse(writeLock.tryLock(), "Acquired the write lock while the read lock was held");
			return lock;
		});
		Assertions.assertSame(lock, ret);
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertTrue(writeLock.tryLock());
		writeLock.unlock();

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		rwl.readLocked(() -> {
			// Prove that we're holding the read lock
			Assertions.assertFalse(writeLock.isHeldByCurrentThread());
			Assertions.assertFalse(writeLock.tryLock(), "Acquired the write lock while the read lock was held");
		});
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertTrue(writeLock.tryLock());
		writeLock.unlock();
	}

	@Test
	public void testWriteLocked() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final ReadWriteLockable rwl = new BaseReadWriteLockable(lock);
		try {
			rwl.writeLocked((Supplier<Object>) null);
			Assertions.fail("Did not fail with a null Supplier");
		} catch (NullPointerException e) {
		}

		try {
			rwl.writeLocked((Runnable) null);
			Assertions.fail("Did not fail with a null Runnable");
		} catch (NullPointerException e) {
		}

		Assertions.assertTrue(writeLock.tryLock());
		Assertions.assertTrue(writeLock.isHeldByCurrentThread());
		writeLock.unlock();

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		ReadWriteLock ret = rwl.writeLocked(() -> {
			// Prove that we're holding the read lock
			Assertions.assertTrue(writeLock.isHeldByCurrentThread());
			return lock;
		});
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertSame(lock, ret);

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		rwl.writeLocked(() -> {
			Assertions.assertTrue(writeLock.isHeldByCurrentThread());
		});
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
	}

	@Test
	public void testReadUpgradable() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final ReadWriteLockable rwl = new BaseReadWriteLockable(lock);
		Object o = null;

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertTrue(writeLock.tryLock());
		Assertions.assertTrue(writeLock.isHeldByCurrentThread());
		writeLock.unlock();
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());

		Assertions.assertTrue(readLock.tryLock());
		Assertions.assertFalse(writeLock.tryLock());
		readLock.unlock();

		final AtomicInteger callCount = new AtomicInteger(0);
		final Object a = "Object-A";
		final Object b = "Object-B";

		{
			Supplier<Object> sup = null;
			Predicate<Object> pred = null;
			Function<Object, Object> map = null;

			try {
				o = rwl.readUpgradable(sup, pred, map);
				Assertions.fail("Did not fail with all-null parameters");
			} catch (NullPointerException e) {
				// All is well
			}

			sup = null;
			pred = Objects::nonNull;
			map = Function.identity();
			try {
				o = rwl.readUpgradable(sup, pred, map);
				Assertions.fail("Did not fail with null Supplier");
			} catch (NullPointerException e) {
				// All is well
			}

			sup = Object::new;
			pred = null;
			map = Function.identity();
			try {
				o = rwl.readUpgradable(sup, pred, map);
				Assertions.fail("Did not fail with null Predicate");
			} catch (NullPointerException e) {
				// All is well
			}

			sup = Object::new;
			pred = Objects::nonNull;
			map = null;
			try {
				o = rwl.readUpgradable(sup, pred, map);
				Assertions.fail("Did not fail with null mapper Function");
			} catch (NullPointerException e) {
				// All is well
			}

			sup = Object::new;
			pred = null;
			map = null;
			try {
				o = rwl.readUpgradable(sup, pred, map);
				Assertions.fail("Did not fail with null Predicate and mapper Function");
			} catch (NullPointerException e) {
				// All is well
			}

			sup = null;
			pred = Objects::nonNull;
			map = null;
			try {
				o = rwl.readUpgradable(sup, pred, map);
				Assertions.fail("Did not fail with null Supplier and mapper Function");
			} catch (NullPointerException e) {
				// All is well
			}

			sup = null;
			pred = null;
			map = Function.identity();
			try {
				o = rwl.readUpgradable(sup, pred, map);
				Assertions.fail("Did not fail with null Supplier and Predicate");
			} catch (NullPointerException e) {
				// All is well
			}

			sup = Object::new;
			pred = Objects::nonNull;
			map = Function.identity();
			o = rwl.readUpgradable(sup, pred, map);
		}

		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		callCount.set(0);
		o = rwl.readUpgradable(() -> {
			// This should happen with the read lock held
			Assertions.assertEquals(1, lock.getReadHoldCount());
			Assertions.assertFalse(writeLock.isHeldByCurrentThread());
			callCount.incrementAndGet();
			return a;
		}, (e) -> {
			return true;
		}, (e) -> {
			Assertions.fail("This should not have been called");
			return b;
		});
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertEquals(1, callCount.get());
		Assertions.assertSame(o, a);

		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		callCount.set(0);
		o = rwl.readUpgradable(() -> {
			// This should happen with the read lock held
			if (callCount.getAndIncrement() == 0) {
				Assertions.assertEquals(1, lock.getReadHoldCount());
				Assertions.assertFalse(writeLock.isHeldByCurrentThread());
			} else {
				Assertions.assertEquals(0, lock.getReadHoldCount());
				Assertions.assertTrue(writeLock.isHeldByCurrentThread());
			}
			return a;
		}, (e) -> {
			return callCount.get() > 1;
		}, (e) -> {
			Assertions.fail("This should not have been called");
			return b;
		});
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertEquals(2, callCount.get());
		Assertions.assertSame(o, a);

		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		callCount.set(0);
		o = rwl.readUpgradable(() -> {
			// This should happen with the read lock held
			callCount.incrementAndGet();
			return a;
		}, (e) -> {
			return false;
		}, (e) -> {
			Assertions.assertEquals(0, lock.getReadHoldCount());
			Assertions.assertTrue(writeLock.isHeldByCurrentThread());
			return b;
		});
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertEquals(2, callCount.get());
		Assertions.assertSame(o, b);

		{
			Supplier<Object> sup = null;
			Predicate<Object> pred = null;
			Consumer<Object> map = null;

			try {
				rwl.readUpgradable(sup, pred, map);
				Assertions.fail("Did not fail with all-null parameters");
			} catch (NullPointerException e) {
				// All is well
			}

			sup = null;
			pred = Objects::nonNull;
			map = Objects::hashCode;
			try {
				rwl.readUpgradable(sup, pred, map);
				Assertions.fail("Did not fail with null Supplier");
			} catch (NullPointerException e) {
				// All is well
			}

			sup = Object::new;
			pred = null;
			map = Objects::hashCode;
			try {
				rwl.readUpgradable(sup, pred, map);
				Assertions.fail("Did not fail with null Predicate");
			} catch (NullPointerException e) {
				// All is well
			}

			sup = Object::new;
			pred = Objects::nonNull;
			map = null;
			try {
				rwl.readUpgradable(sup, pred, map);
				Assertions.fail("Did not fail with null mapper Function");
			} catch (NullPointerException e) {
				// All is well
			}

			sup = Object::new;
			pred = null;
			map = null;
			try {
				rwl.readUpgradable(sup, pred, map);
				Assertions.fail("Did not fail with null Predicate and mapper Function");
			} catch (NullPointerException e) {
				// All is well
			}

			sup = null;
			pred = Objects::nonNull;
			map = null;
			try {
				rwl.readUpgradable(sup, pred, map);
				Assertions.fail("Did not fail with null Supplier and mapper Function");
			} catch (NullPointerException e) {
				// All is well
			}

			sup = null;
			pred = null;
			map = Objects::hashCode;
			try {
				rwl.readUpgradable(sup, pred, map);
				Assertions.fail("Did not fail with null Supplier and Predicate");
			} catch (NullPointerException e) {
				// All is well
			}

			sup = Object::new;
			pred = Objects::nonNull;
			map = Objects::hashCode;
			rwl.readUpgradable(sup, pred, map);
		}

		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		callCount.set(0);
		rwl.readUpgradable(() -> {
			// This should happen with the read lock held
			Assertions.assertEquals(1, lock.getReadHoldCount());
			Assertions.assertFalse(writeLock.isHeldByCurrentThread());
			callCount.incrementAndGet();
			return a;
		}, (e) -> {
			return true;
		}, (e) -> {
			Assertions.fail("This should not have been called");
		});
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertEquals(1, callCount.get());

		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		callCount.set(0);
		rwl.readUpgradable(() -> {
			// This should happen with the read lock held
			if (callCount.getAndIncrement() == 0) {
				Assertions.assertEquals(1, lock.getReadHoldCount());
				Assertions.assertFalse(writeLock.isHeldByCurrentThread());
			} else {
				Assertions.assertEquals(0, lock.getReadHoldCount());
				Assertions.assertTrue(writeLock.isHeldByCurrentThread());
			}
			return a;
		}, (e) -> {
			return callCount.get() > 1;
		}, (e) -> {
			Assertions.fail("This should not have been called");
		});
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertEquals(2, callCount.get());

		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		callCount.set(0);
		rwl.readUpgradable(() -> {
			// This should happen with the read lock held
			callCount.incrementAndGet();
			return a;
		}, (e) -> {
			return false;
		}, (e) -> {
			Assertions.assertEquals(0, lock.getReadHoldCount());
			Assertions.assertTrue(writeLock.isHeldByCurrentThread());
		});
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertEquals(2, callCount.get());
	}

}