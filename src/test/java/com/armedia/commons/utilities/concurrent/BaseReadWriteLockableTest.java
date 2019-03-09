package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.function.CheckedConsumer;
import com.armedia.commons.utilities.function.CheckedFunction;
import com.armedia.commons.utilities.function.CheckedPredicate;
import com.armedia.commons.utilities.function.CheckedRunnable;
import com.armedia.commons.utilities.function.CheckedSupplier;

public class BaseReadWriteLockableTest {

	@Test
	public void testConstructor() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		BaseReadWriteLockable rwl = null;

		rwl = new BaseReadWriteLockable();
		Assertions.assertNotNull(rwl.getMainLock());
		Assertions.assertNotSame(lock, rwl.getMainLock());

		rwl = new BaseReadWriteLockable(ReadWriteLockable.NULL_LOCK);
		Assertions.assertNotNull(rwl.getMainLock());
		Assertions.assertNotSame(lock, rwl.getMainLock());

		Assertions.assertThrows(NullPointerException.class, () -> new BaseReadWriteLockable((ReadWriteLockable) null));

		Assertions.assertNotNull(rwl);
		ReadWriteLock other = rwl.getMainLock();
		rwl = new BaseReadWriteLockable(rwl);
		Assertions.assertNotNull(rwl.getMainLock());
		Assertions.assertSame(other, rwl.getMainLock());

		rwl = new BaseReadWriteLockable(lock);
		Assertions.assertNotNull(rwl.getMainLock());
		Assertions.assertSame(lock, rwl.getMainLock());
	}

	@Test
	public void testGetReadLock() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
		final BaseReadWriteLockable rwl = new BaseReadWriteLockable(lock);

		Assertions.assertSame(readLock, rwl.getReadLock());
	}

	@Test
	public void testGetWriteLock() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final BaseReadWriteLockable rwl = new BaseReadWriteLockable(lock);

		Assertions.assertSame(writeLock, rwl.getWriteLock());
	}

	@Test
	public void testAcquireReadLock() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final BaseReadWriteLockable rwl = new BaseReadWriteLockable(lock);

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
		Assertions.assertThrows(RuntimeException.class, () -> readLock.unlock(),
			"The read lock was not held but was unlocked");

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
		final BaseReadWriteLockable rwl = new BaseReadWriteLockable(lock);

		Assertions.assertSame(lock.writeLock(), rwl.getWriteLock());

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertSame(lock.writeLock(), rwl.acquireWriteLock());
		Assertions.assertTrue(writeLock.isHeldByCurrentThread());
		writeLock.unlock();
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());

		Assertions.assertThrows(RuntimeException.class, () -> writeLock.unlock(),
			"The write lock was not held but was unlocked");

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
		final BaseReadWriteLockable rwl = new BaseReadWriteLockable(lock);

		Assertions.assertThrows(NullPointerException.class,
			() -> rwl.readLocked((CheckedSupplier<Object, Exception>) null), "Did not fail with a null Supplier");

		Assertions.assertThrows(NullPointerException.class, () -> rwl.readLocked((CheckedRunnable<Exception>) null),
			"Did not fail with a null Runnable");

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertEquals(0, lock.getReadHoldCount());
		ReadWriteLock ret = rwl.readLocked(() -> {
			// Prove that we're holding the read lock
			Assertions.assertEquals(1, lock.getReadHoldCount());
			Assertions.assertFalse(writeLock.isHeldByCurrentThread());
			Assertions.assertFalse(writeLock.tryLock(), "Acquired the write lock while the read lock was held");
			return lock;
		});
		Assertions.assertSame(lock, ret);
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertTrue(writeLock.tryLock());
		writeLock.unlock();

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertEquals(0, lock.getReadHoldCount());
		final RuntimeException ex = new RuntimeException();
		try {
			rwl.readLocked(() -> {
				Assertions.assertEquals(1, lock.getReadHoldCount());
				throw ex;
			});
			Assertions.fail("Did not cascade the raised exception");
		} catch (Throwable t) {
			Assertions.assertSame(ex, t);
		}
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertTrue(writeLock.tryLock());
		writeLock.unlock();

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertEquals(0, lock.getReadHoldCount());
		rwl.readLocked(() -> {
			// Prove that we're holding the read lock
			Assertions.assertEquals(1, lock.getReadHoldCount());
			Assertions.assertFalse(writeLock.isHeldByCurrentThread());
			Assertions.assertFalse(writeLock.tryLock(), "Acquired the write lock while the read lock was held");
		});
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertTrue(writeLock.tryLock());
		writeLock.unlock();

		{
			Object a = new Object();
			Supplier<Object> operation = () -> {
				// Prove that we're holding the read lock
				Assertions.assertEquals(1, lock.getReadHoldCount());
				Assertions.assertFalse(writeLock.isHeldByCurrentThread());
				Assertions.assertFalse(writeLock.tryLock(), "Acquired the write lock while the read lock was held");
				return a;
			};
			Assertions.assertFalse(writeLock.isHeldByCurrentThread());
			Assertions.assertEquals(0, lock.getReadHoldCount());
			Assertions.assertSame(a, rwl.readLocked(operation));
			Assertions.assertEquals(0, lock.getReadHoldCount());
			Assertions.assertFalse(writeLock.isHeldByCurrentThread());
			Assertions.assertTrue(writeLock.tryLock());
			writeLock.unlock();
		}
		{
			Runnable operation = () -> {
				// Prove that we're holding the read lock
				Assertions.assertEquals(1, lock.getReadHoldCount());
				Assertions.assertFalse(writeLock.isHeldByCurrentThread());
				Assertions.assertFalse(writeLock.tryLock(), "Acquired the write lock while the read lock was held");
			};
			Assertions.assertFalse(writeLock.isHeldByCurrentThread());
			Assertions.assertEquals(0, lock.getReadHoldCount());
			rwl.readLocked(operation);
			Assertions.assertEquals(0, lock.getReadHoldCount());
			Assertions.assertFalse(writeLock.isHeldByCurrentThread());
			Assertions.assertTrue(writeLock.tryLock());
			writeLock.unlock();
		}
	}

	@Test
	public void testWriteLocked() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final BaseReadWriteLockable rwl = new BaseReadWriteLockable(lock);

		Assertions.assertThrows(NullPointerException.class,
			() -> rwl.writeLocked((CheckedSupplier<Object, Exception>) null), "Did not fail with a null Supplier");
		Assertions.assertThrows(NullPointerException.class, () -> rwl.writeLocked((CheckedRunnable<Exception>) null),
			"Did not fail with a null Runnable");

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
		final RuntimeException ex = new RuntimeException();
		try {
			rwl.writeLocked(() -> {
				// Prove that we're holding the read lock
				Assertions.assertTrue(writeLock.isHeldByCurrentThread());
				throw ex;
			});
			Assertions.fail("Did not cascade the raised exception");
		} catch (Throwable t) {
			Assertions.assertSame(ex, t);
		}
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertSame(lock, ret);

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		rwl.writeLocked(() -> Assertions.assertTrue(writeLock.isHeldByCurrentThread()));
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());

		{
			Object a = new Object();
			Supplier<Object> operation = () -> {
				// Prove that we're holding the read lock
				Assertions.assertEquals(0, lock.getReadHoldCount());
				Assertions.assertTrue(writeLock.isHeldByCurrentThread());
				return a;
			};
			Assertions.assertFalse(writeLock.isHeldByCurrentThread());
			Assertions.assertEquals(0, lock.getReadHoldCount());
			Assertions.assertSame(a, rwl.writeLocked(operation));
			Assertions.assertEquals(0, lock.getReadHoldCount());
			Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		}
		{
			Runnable operation = () -> {
				// Prove that we're holding the read lock
				Assertions.assertEquals(0, lock.getReadHoldCount());
				Assertions.assertTrue(writeLock.isHeldByCurrentThread());
			};
			Assertions.assertFalse(writeLock.isHeldByCurrentThread());
			Assertions.assertEquals(0, lock.getReadHoldCount());
			rwl.writeLocked(operation);
			Assertions.assertEquals(0, lock.getReadHoldCount());
			Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		}
	}

	@Test
	public void testReadUpgradable() throws Exception {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final BaseReadWriteLockable rwl = new BaseReadWriteLockable(lock);
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
			final CheckedSupplier<Object, Exception> nullSup = null;
			final CheckedSupplier<Object, Exception> sup = Object::new;
			final CheckedPredicate<Object, Exception> nullPred = null;
			final CheckedPredicate<Object, Exception> pred = (e) -> false;
			final CheckedFunction<Object, Object, Exception> nullMap = null;
			final CheckedFunction<Object, Object, Exception> map = (e) -> null;

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(nullSup, nullPred, nullMap),
				"Did not fail with all-null parameters");

			rwl.readUpgradable(nullSup, pred, map);

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(sup, nullPred, map),
				"Did not fail with null Predicate");

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(sup, Objects::nonNull, nullMap), "Did not fail with null mapper Function");

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(sup, nullPred, nullMap),
				"Did not fail with null Predicate and mapper Function");

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(nullSup, pred, nullMap),
				"Did not fail with null Supplier and mapper Function");

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(nullSup, nullPred, map),
				"Did not fail with null Supplier and Predicate");

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
		}, (e) -> false, (e) -> {
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
		}, (e) -> callCount.get() <= 1, (e) -> {
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
		}, (e) -> true, (e) -> {
			Assertions.assertEquals(0, lock.getReadHoldCount());
			Assertions.assertTrue(writeLock.isHeldByCurrentThread());
			return b;
		});
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertEquals(2, callCount.get());
		Assertions.assertSame(o, b);

		{
			final CheckedSupplier<Object, Exception> nullSup = null;
			final CheckedSupplier<Object, Exception> sup = Object::new;
			final CheckedPredicate<Object, Exception> nullPred = null;
			final CheckedPredicate<Object, Exception> pred = (e) -> false;
			final CheckedConsumer<Object, Exception> nullCons = null;
			final CheckedConsumer<Object, Exception> cons = (e) -> {
			};

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(nullSup, nullPred, nullCons),
				"Did not fail with all-null parameters");

			rwl.readUpgradable(nullSup, pred, cons);

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(sup, nullPred, cons),
				"Did not fail with null Predicate");

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(sup, Objects::nonNull, nullCons), "Did not fail with null mapper Function");

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(sup, nullPred, nullCons),
				"Did not fail with null Predicate and mapper Function");

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(nullSup, pred, nullCons),
				"Did not fail with null Supplier and mapper Function");

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(nullSup, nullPred, cons),
				"Did not fail with null Supplier and Predicate");

			rwl.readUpgradable(sup, pred, cons);
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
		}, (e) -> false, (e) -> {
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
		}, (e) -> callCount.get() <= 1, (e) -> {
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
		}, (e) -> true, (e) -> {
			Assertions.assertEquals(0, lock.getReadHoldCount());
			Assertions.assertTrue(writeLock.isHeldByCurrentThread());
		});
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertEquals(2, callCount.get());

		{
			callCount.set(0);
			final AtomicBoolean writeBlockInvoked = new AtomicBoolean(false);
			final Supplier<Boolean> nullDecision = null;
			final Supplier<Boolean> decision = () -> {
				callCount.incrementAndGet();
				return Boolean.TRUE;
			};
			final Supplier<Object> nullWriteBlock = null;
			final Supplier<Object> writeBlock = () -> {
				writeBlockInvoked.set(true);
				return a;
			};

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertSame(a, rwl.readUpgradable(decision, writeBlock));
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());

			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.readUpgradable(() -> {
				callCount.incrementAndGet();
				return Boolean.FALSE;
			}, writeBlock);
			Assertions.assertEquals(1, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.readUpgradable(() -> {
				callCount.incrementAndGet();
				return null;
			}, writeBlock);
			Assertions.assertEquals(1, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
		}

		{
			callCount.set(0);
			final AtomicBoolean writeBlockInvoked = new AtomicBoolean(false);
			final CheckedSupplier<Boolean, Exception> nullDecision = null;
			final CheckedSupplier<Boolean, Exception> decision = () -> {
				callCount.incrementAndGet();
				return Boolean.TRUE;
			};
			final CheckedSupplier<Object, Exception> nullWriteBlock = null;
			final CheckedSupplier<Object, Exception> writeBlock = () -> {
				writeBlockInvoked.set(true);
				return a;
			};

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertSame(a, rwl.readUpgradable(decision, writeBlock));
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());

			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.readUpgradable(() -> {
				callCount.incrementAndGet();
				return Boolean.FALSE;
			}, writeBlock);
			Assertions.assertEquals(1, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.readUpgradable(() -> {
				callCount.incrementAndGet();
				return null;
			}, writeBlock);
			Assertions.assertEquals(1, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
		}

		{
			callCount.set(0);
			final AtomicBoolean writeBlockInvoked = new AtomicBoolean(false);
			final Supplier<Object> nullChecker = null;
			final Supplier<Object> checker = () -> b;
			final Predicate<Object> nullDecision = null;
			final Predicate<Object> decision = (e) -> {
				callCount.incrementAndGet();
				return Boolean.TRUE;
			};
			final Function<Object, Object> nullWriteBlock = null;
			final Function<Object, Object> writeBlock = (e) -> {
				writeBlockInvoked.set(true);
				return a;
			};

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(nullChecker, nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(nullChecker, nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(nullChecker, decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertSame(a, rwl.readUpgradable(nullChecker, decision, writeBlock));
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(checker, nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(checker, nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(checker, decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertSame(a, rwl.readUpgradable(checker, decision, writeBlock));
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());
		}

		{
			callCount.set(0);
			final AtomicBoolean writeBlockInvoked = new AtomicBoolean(false);
			final Supplier<Boolean> nullDecision = null;
			final Supplier<Boolean> decision = () -> {
				callCount.incrementAndGet();
				return Boolean.TRUE;
			};
			final Supplier<Object> nullWriteBlock = null;
			final Runnable writeBlock = () -> writeBlockInvoked.set(true);

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			rwl.readUpgradable(decision, writeBlock);
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());

			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.readUpgradable(() -> {
				callCount.incrementAndGet();
				return Boolean.FALSE;
			}, writeBlock);
			Assertions.assertEquals(1, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.readUpgradable(() -> {
				callCount.incrementAndGet();
				return null;
			}, writeBlock);
			Assertions.assertEquals(1, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
		}

		{
			callCount.set(0);
			final AtomicBoolean writeBlockInvoked = new AtomicBoolean(false);
			final CheckedSupplier<Boolean, Exception> nullDecision = null;
			final CheckedSupplier<Boolean, Exception> decision = () -> {
				callCount.incrementAndGet();
				return Boolean.TRUE;
			};
			final CheckedRunnable<Exception> nullWriteBlock = null;
			final CheckedRunnable<Exception> writeBlock = () -> writeBlockInvoked.set(true);

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			rwl.readUpgradable(decision, writeBlock);
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());

			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.readUpgradable(() -> {
				callCount.incrementAndGet();
				return Boolean.FALSE;
			}, writeBlock);
			Assertions.assertEquals(1, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.readUpgradable(() -> {
				callCount.incrementAndGet();
				return null;
			}, writeBlock);
			Assertions.assertEquals(1, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
		}

		{
			callCount.set(0);
			final AtomicBoolean writeBlockInvoked = new AtomicBoolean(false);
			final Supplier<Object> nullChecker = null;
			final Supplier<Object> checker = () -> b;
			final Predicate<Object> nullDecision = null;
			final Predicate<Object> decision = (e) -> {
				callCount.incrementAndGet();
				return Boolean.TRUE;
			};
			final Consumer<Object> nullWriteBlock = null;
			final Consumer<Object> writeBlock = (e) -> writeBlockInvoked.set(true);

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(nullChecker, nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(nullChecker, nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(nullChecker, decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			rwl.readUpgradable(nullChecker, decision, writeBlock);
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(checker, nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(checker, nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(checker, decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			rwl.readUpgradable(checker, decision, writeBlock);
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());
		}
	}
}