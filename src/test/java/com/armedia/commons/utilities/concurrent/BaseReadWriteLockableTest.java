package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.function.CheckedPredicate;
import com.armedia.commons.utilities.function.CheckedRunnable;
import com.armedia.commons.utilities.function.CheckedSupplier;

public class BaseReadWriteLockableTest {

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

		Assertions.assertThrows(NullPointerException.class, () -> rwl.readLocked((Supplier<Object>) null),
			"Did not fail with a null Supplier");

		Assertions.assertThrows(NullPointerException.class, () -> rwl.readLocked((Runnable) null),
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
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertSame(lock, ret);
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
	}

	@Test
	public void testReadLockedChecked() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final BaseReadWriteLockable rwl = new BaseReadWriteLockable(lock);

		Assertions.assertThrows(NullPointerException.class,
			() -> rwl.readLockedChecked((CheckedSupplier<Object, Exception>) null),
			"Did not fail with a null Supplier");

		Assertions.assertThrows(NullPointerException.class,
			() -> rwl.readLockedChecked((CheckedRunnable<Exception>) null), "Did not fail with a null Runnable");

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertEquals(0, lock.getReadHoldCount());
		ReadWriteLock ret = rwl.readLockedChecked(() -> {
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
			rwl.readLockedChecked(() -> {
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
		rwl.readLockedChecked(() -> {
			// Prove that we're holding the read lock
			Assertions.assertEquals(1, lock.getReadHoldCount());
			Assertions.assertFalse(writeLock.isHeldByCurrentThread());
			Assertions.assertFalse(writeLock.tryLock(), "Acquired the write lock while the read lock was held");
		});
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertTrue(writeLock.tryLock());
		writeLock.unlock();
	}

	@Test
	public void testWriteLocked() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final BaseReadWriteLockable rwl = new BaseReadWriteLockable(lock);

		Assertions.assertThrows(NullPointerException.class, () -> rwl.writeLocked((Supplier<Object>) null),
			"Did not fail with a null Supplier");
		Assertions.assertThrows(NullPointerException.class, () -> rwl.writeLocked((Runnable) null),
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
		rwl.writeLocked(() -> Assertions.assertTrue(writeLock.isHeldByCurrentThread()));
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
	}

	@Test
	public void testWriteLockedChecked() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final BaseReadWriteLockable rwl = new BaseReadWriteLockable(lock);

		Assertions.assertThrows(NullPointerException.class,
			() -> rwl.writeLockedChecked((CheckedSupplier<Object, Exception>) null),
			"Did not fail with a null Supplier");
		Assertions.assertThrows(NullPointerException.class,
			() -> rwl.writeLockedChecked((CheckedRunnable<Exception>) null), "Did not fail with a null Runnable");

		Assertions.assertTrue(writeLock.tryLock());
		Assertions.assertTrue(writeLock.isHeldByCurrentThread());
		writeLock.unlock();

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		ReadWriteLock ret = rwl.writeLockedChecked(() -> {
			// Prove that we're holding the read lock
			Assertions.assertTrue(writeLock.isHeldByCurrentThread());
			return lock;
		});
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertSame(lock, ret);

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		final RuntimeException ex = new RuntimeException();
		try {
			rwl.writeLockedChecked(() -> {
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
		rwl.writeLockedChecked(() -> Assertions.assertTrue(writeLock.isHeldByCurrentThread()));
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
	}

	@Test
	public void testReadUpgradable() {
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
			final Supplier<Object> sup = null;
			final Predicate<Object> pred = null;
			final Function<Object, Object> map = null;

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(sup, pred, map),
				"Did not fail with all-null parameters");

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(sup, Objects::nonNull, Function.identity()),
				"Did not fail with null Supplier");

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(Object::new, pred, Function.identity()), "Did not fail with null Predicate");

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(Object::new, Objects::nonNull, map), "Did not fail with null mapper Function");

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(Object::new, pred, map),
				"Did not fail with null Predicate and mapper Function");

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(sup, Objects::nonNull, map),
				"Did not fail with null Supplier and mapper Function");

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(sup, pred, Function.identity()),
				"Did not fail with null Supplier and Predicate");

			o = rwl.readUpgradable(Object::new, Objects::nonNull, Function.identity());
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
		}, (e) -> true, (e) -> {
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
		}, (e) -> callCount.get() > 1, (e) -> {
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
		}, (e) -> false, (e) -> {
			Assertions.assertEquals(0, lock.getReadHoldCount());
			Assertions.assertTrue(writeLock.isHeldByCurrentThread());
			return b;
		});
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertEquals(2, callCount.get());
		Assertions.assertSame(o, b);

		{
			final Supplier<Object> sup = null;
			final Predicate<Object> pred = null;
			final Consumer<Object> map = null;

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(sup, pred, map),
				"Did not fail with all-null parameters");

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(sup, Objects::nonNull, Objects::nonNull), "Did not fail with null Supplier");

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(Object::new, pred, Objects::nonNull), "Did not fail with null Predicate");

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.readUpgradable(Object::new, Objects::nonNull, map), "Did not fail with null mapper Function");

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(Object::new, pred, map),
				"Did not fail with null Predicate and mapper Function");

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(sup, Objects::nonNull, map),
				"Did not fail with null Supplier and mapper Function");

			Assertions.assertThrows(NullPointerException.class, () -> rwl.readUpgradable(sup, pred, Objects::nonNull),
				"Did not fail with null Supplier and Predicate");

			rwl.readUpgradable(Object::new, Objects::nonNull, Objects::nonNull);
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
		}, (e) -> callCount.get() > 1, (e) -> {
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
		}, (e) -> false, (e) -> {
			Assertions.assertEquals(0, lock.getReadHoldCount());
			Assertions.assertTrue(writeLock.isHeldByCurrentThread());
		});
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertEquals(2, callCount.get());
	}

	@Test
	public void testDoubleCheckedLocked() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final BaseReadWriteLockable rwl = new BaseReadWriteLockable(lock);

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertTrue(writeLock.tryLock());
		Assertions.assertTrue(writeLock.isHeldByCurrentThread());
		writeLock.unlock();
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());

		{
			final BooleanSupplier sup = null;
			final Runnable calc = null;

			Assertions.assertThrows(NullPointerException.class, () -> rwl.doubleCheckedLocked(sup, calc));
			Assertions.assertThrows(NullPointerException.class, () -> rwl.doubleCheckedLocked(() -> true, calc));
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.doubleCheckedLocked(sup, () -> Function.identity()));
			rwl.doubleCheckedLocked(() -> true, () -> Function.identity());
		}

		{
			final Supplier<Object> check = null;
			final Predicate<Object> test = null;
			final Supplier<Object> calc = null;

			Assertions.assertThrows(NullPointerException.class, () -> rwl.doubleCheckedLocked(check, test, calc));
			Assertions.assertThrows(NullPointerException.class, () -> rwl.doubleCheckedLocked(check, test, () -> null));
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.doubleCheckedLocked(check, Objects::nonNull, calc));
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.doubleCheckedLocked(check, Objects::nonNull, () -> null));
			Assertions.assertThrows(NullPointerException.class, () -> rwl.doubleCheckedLocked(Object::new, test, calc));
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.doubleCheckedLocked(Object::new, test, () -> null));
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.doubleCheckedLocked(Object::new, Objects::nonNull, calc));
			rwl.doubleCheckedLocked(Object::new, Objects::nonNull, () -> null);
		}

		final Object a = "Object-A";
		final Object b = "Object-B";

		final AtomicBoolean initialized = new AtomicBoolean(false);
		final AtomicBoolean invoked = new AtomicBoolean(false);

		initialized.set(false);
		invoked.set(false);
		Assertions.assertFalse(initialized.get());
		Assertions.assertFalse(invoked.get());

		rwl.doubleCheckedLocked(() -> !initialized.get(), () -> invoked.set(true));
		Assertions.assertFalse(initialized.get());
		Assertions.assertTrue(invoked.get());

		initialized.set(true);
		invoked.set(false);
		rwl.doubleCheckedLocked(() -> !initialized.get(), () -> invoked.set(true));
		Assertions.assertFalse(invoked.get());

		initialized.set(false);
		invoked.set(false);
		Assertions.assertFalse(initialized.get());
		Assertions.assertFalse(invoked.get());

		Object ret = rwl.doubleCheckedLocked(() -> b, (e) -> !initialized.get(), () -> {
			invoked.set(true);
			return a;
		});
		Assertions.assertFalse(initialized.get());
		Assertions.assertTrue(invoked.get());
		Assertions.assertSame(a, ret);

		initialized.set(true);
		invoked.set(false);
		ret = rwl.doubleCheckedLocked(() -> b, (e) -> !initialized.get(), () -> {
			invoked.set(true);
			return a;
		});
		Assertions.assertFalse(invoked.get());
		Assertions.assertSame(b, ret);

	}

	@Test
	public void testDoubleCheckedLockedChecked() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final BaseReadWriteLockable rwl = new BaseReadWriteLockable(lock);

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertTrue(writeLock.tryLock());
		Assertions.assertTrue(writeLock.isHeldByCurrentThread());
		writeLock.unlock();
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		{
			final CheckedSupplier<Boolean, Exception> sup = null;
			final CheckedRunnable<Exception> calc = null;

			Assertions.assertThrows(NullPointerException.class, () -> rwl.doubleCheckedLockedChecked(sup, calc));
			Assertions.assertThrows(NullPointerException.class, () -> rwl.doubleCheckedLockedChecked(() -> true, calc));
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.doubleCheckedLockedChecked(sup, () -> Function.identity()));
			rwl.doubleCheckedLocked(() -> true, () -> Function.identity());
		}

		{
			final CheckedSupplier<Object, Exception> check = null;
			final CheckedPredicate<Object, Exception> test = null;
			final CheckedSupplier<Object, Exception> calc = null;

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.doubleCheckedLockedChecked(check, test, calc));
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.doubleCheckedLockedChecked(check, test, () -> null));
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.doubleCheckedLockedChecked(check, Objects::nonNull, calc));
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.doubleCheckedLockedChecked(check, Objects::nonNull, () -> null));
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.doubleCheckedLockedChecked(Object::new, test, calc));
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.doubleCheckedLockedChecked(Object::new, test, () -> null));
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.doubleCheckedLockedChecked(Object::new, Objects::nonNull, calc));
			rwl.doubleCheckedLockedChecked(Object::new, Objects::nonNull, () -> null);
		}

		final Object a = "Object-A";
		final Object b = "Object-B";

		final AtomicBoolean initialized = new AtomicBoolean(false);
		final AtomicBoolean invoked = new AtomicBoolean(false);

		initialized.set(false);
		invoked.set(false);
		Assertions.assertFalse(initialized.get());
		Assertions.assertFalse(invoked.get());

		rwl.doubleCheckedLockedChecked(() -> !initialized.get(), () -> invoked.set(true));
		Assertions.assertFalse(initialized.get());
		Assertions.assertTrue(invoked.get());

		initialized.set(true);
		invoked.set(false);
		rwl.doubleCheckedLockedChecked(() -> !initialized.get(), () -> invoked.set(true));
		Assertions.assertFalse(invoked.get());

		initialized.set(false);
		invoked.set(false);
		Assertions.assertFalse(initialized.get());
		Assertions.assertFalse(invoked.get());

		Object ret = rwl.doubleCheckedLockedChecked(() -> b, (e) -> !initialized.get(), () -> {
			invoked.set(true);
			return a;
		});
		Assertions.assertFalse(initialized.get());
		Assertions.assertTrue(invoked.get());
		Assertions.assertSame(a, ret);

		initialized.set(true);
		invoked.set(false);
		ret = rwl.doubleCheckedLockedChecked(() -> b, (e) -> !initialized.get(), () -> {
			invoked.set(true);
			return a;
		});
		Assertions.assertFalse(invoked.get());
		Assertions.assertSame(b, ret);

	}
}