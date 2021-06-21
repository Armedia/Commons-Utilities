/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2021 Armedia, LLC
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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.function.CheckedBiConsumer;
import com.armedia.commons.utilities.function.CheckedBiFunction;
import com.armedia.commons.utilities.function.CheckedConsumer;
import com.armedia.commons.utilities.function.CheckedFunction;
import com.armedia.commons.utilities.function.CheckedPredicate;
import com.armedia.commons.utilities.function.CheckedRunnable;
import com.armedia.commons.utilities.function.CheckedSupplier;

public class BaseShareableLockableTest {

	@Test
	public void testConstructor() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		BaseShareableLockable rwl = null;

		rwl = new BaseShareableLockable();
		Assertions.assertNotNull(rwl.getShareableLock());
		Assertions.assertNotSame(lock, rwl.getShareableLock());

		rwl = new BaseShareableLockable(ShareableLockable.NULL_LOCK);
		Assertions.assertNotNull(rwl.getShareableLock());
		Assertions.assertNotSame(lock, rwl.getShareableLock());

		Assertions.assertThrows(NullPointerException.class, () -> new BaseShareableLockable((ShareableLockable) null));

		Assertions.assertNotNull(rwl);
		ReadWriteLock other = rwl.getShareableLock();
		rwl = new BaseShareableLockable(rwl);
		Assertions.assertNotNull(rwl.getShareableLock());
		Assertions.assertSame(other, rwl.getShareableLock());

		rwl = new BaseShareableLockable(lock);
		Assertions.assertNotNull(rwl.getShareableLock());
		Assertions.assertSame(lock, rwl.getShareableLock());
	}

	@Test
	public void testGetReadLock() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
		final BaseShareableLockable rwl = new BaseShareableLockable(lock);

		Assertions.assertSame(readLock, rwl.getSharedLock());
	}

	@Test
	public void testGetWriteLock() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final ShareableLockable rwl = new ShareableLockable() {
			@Override
			public ReadWriteLock getShareableLock() {
				return lock;
			}
		};

		Assertions.assertSame(writeLock, rwl.getMutexLock());
	}

	@Test
	public void testAcquireReadLock() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final BaseShareableLockable rwl = new BaseShareableLockable(lock);

		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertSame(lock.readLock(), rwl.acquireSharedLock());
		Assertions.assertEquals(1, lock.getReadHoldCount());
		Assertions.assertFalse(rwl.getMutexLock().tryLock(),
			"Succeeded in acquiring the write lock while the read lock was held");
		Assertions.assertEquals(1, lock.getReadHoldCount());
		readLock.unlock();
		Assertions.assertEquals(0, lock.getReadHoldCount());

		Assertions.assertSame(lock.readLock(), rwl.getSharedLock());
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertThrows(RuntimeException.class, () -> readLock.unlock(),
			"The read lock was not held but was unlocked");

		Assertions.assertEquals(0, lock.getReadHoldCount());
		for (int i = 1; i <= 10; i++) {
			Assertions.assertNotNull(rwl.acquireSharedLock(),
				String.format("Failed to acquire the reading lock on attempt # %d", i));
		}
		Assertions.assertEquals(10, lock.getReadHoldCount());
		Assertions.assertFalse(rwl.getMutexLock().tryLock(),
			"Succeeded in acquiring the write lock while the read lock was held");
		for (int i = 10; i > 0; i--) {
			try {
				readLock.unlock();
			} catch (Exception e) {
				Assertions.fail(String.format("Failed to release the reading lock on attempt # %d", i));
			}
		}
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertTrue(rwl.getMutexLock().tryLock(),
			"Failed to acquire the write lock while the read lock was not held");
		rwl.getMutexLock().unlock();
	}

	@Test
	public void testAcquireAutoReadLock() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final BaseShareableLockable rwl = new BaseShareableLockable(lock);

		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());

		try (SharedAutoLock auto = rwl.sharedAutoLock()) {
			Assertions.assertNotNull(auto);
			Assertions.assertEquals(1, lock.getReadHoldCount());
			Assertions.assertFalse(rwl.getMutexLock().tryLock(),
				"Succeeded in acquiring the write lock while the read lock was held");
			Assertions.assertEquals(1, lock.getReadHoldCount());
		}
		Assertions.assertEquals(0, lock.getReadHoldCount());

		Assertions.assertSame(lock.readLock(), rwl.getSharedLock());
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertThrows(RuntimeException.class, () -> readLock.unlock(),
			"The read lock was not held but was unlocked");

		Assertions.assertEquals(0, lock.getReadHoldCount());
		List<SharedAutoLock> autoLocks = new LinkedList<>();
		for (int i = 1; i <= 10; i++) {
			SharedAutoLock l = rwl.sharedAutoLock();
			Assertions.assertNotNull(l, String.format("Failed to acquire the reading lock on attempt # %d", i));
			autoLocks.add(l);
		}
		Assertions.assertEquals(10, lock.getReadHoldCount());
		Assertions.assertFalse(rwl.getMutexLock().tryLock(),
			"Succeeded in acquiring the write lock while the read lock was held");
		int i = 1;
		for (SharedAutoLock l : autoLocks) {
			try {
				l.close();
				i++;
			} catch (Exception e) {
				Assertions.fail(String.format("Failed to release the reading lock on attempt # %d", i));
			}
		}
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertTrue(rwl.getMutexLock().tryLock(),
			"Failed to acquire the write lock while the read lock was not held");
		rwl.getMutexLock().unlock();
	}

	@Test
	public void testAcquireWriteLock() throws Exception {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final BaseShareableLockable rwl = new BaseShareableLockable(lock);

		Assertions.assertSame(lock.writeLock(), rwl.getMutexLock());

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertSame(lock.writeLock(), rwl.acquireMutexLock());
		Assertions.assertTrue(writeLock.isHeldByCurrentThread());
		writeLock.unlock();
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());

		Assertions.assertThrows(RuntimeException.class, () -> writeLock.unlock(),
			"The write lock was not held but was unlocked");

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		for (int i = 1; i <= 10; i++) {
			Assertions.assertNotNull(rwl.acquireMutexLock(),
				String.format("Failed to acquire the writing lock on attempt # %d", i));
			Assertions.assertTrue(writeLock.isHeldByCurrentThread());
		}
		Assertions.assertTrue(rwl.getSharedLock().tryLock(),
			"Failed to acquire the read lock while the write lock was held");
		rwl.getSharedLock().unlock();

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
	public void testAcquireAutoWriteLock() throws Exception {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final BaseShareableLockable rwl = new BaseShareableLockable(lock);

		Assertions.assertSame(lock.writeLock(), rwl.getMutexLock());
		Assertions.assertFalse(lock.isWriteLocked());
		Assertions.assertFalse(lock.isWriteLockedByCurrentThread());

		try (MutexAutoLock auto = rwl.mutexAutoLock()) {
			Assertions.assertTrue(lock.isWriteLocked());
			Assertions.assertTrue(lock.isWriteLockedByCurrentThread());
		}
		Assertions.assertFalse(lock.isWriteLocked());
		Assertions.assertFalse(lock.isWriteLockedByCurrentThread());

		Assertions.assertThrows(RuntimeException.class, () -> writeLock.unlock(),
			"The write lock was not held but was unlocked");

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		List<MutexAutoLock> autoLocks = new LinkedList<>();
		for (int i = 1; i <= 10; i++) {
			MutexAutoLock l = rwl.mutexAutoLock();
			Assertions.assertNotNull(l, String.format("Failed to acquire the writing lock on attempt # %d", i));
			Assertions.assertTrue(writeLock.isHeldByCurrentThread());
			autoLocks.add(l);
		}
		Assertions.assertTrue(rwl.getSharedLock().tryLock(),
			"Failed to acquire the read lock while the write lock was held");
		rwl.getSharedLock().unlock();

		int i = 0;
		for (MutexAutoLock l : autoLocks) {
			Assertions.assertTrue(writeLock.isHeldByCurrentThread());
			try {
				l.close();
				i++;
			} catch (Exception e) {
				Assertions.fail(String.format("Failed to release the writing lock on attempt # %d", i));
			}
		}
		Assertions.assertFalse(lock.isWriteLocked());
		Assertions.assertFalse(lock.isWriteLockedByCurrentThread());
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
			Lock sl = rwl.acquireSharedLock();
			Assertions.assertEquals(1, lock.getReadHoldCount());
			Assertions.assertSame(readLock, sl);
			Assertions.assertThrows(LockUpgradeDeadlockException.class, () -> rwl.acquireMutexLock());
			try {
				rwl.acquireMutexLock();
			} catch (LockUpgradeDeadlockException e) {
				Assertions.assertSame(rwl, e.getTarget());
				Assertions.assertEquals(1, e.getReadHoldCount());

			}
			Assertions.assertEquals(0, lock.getWriteHoldCount());
			sl.unlock();
			Assertions.assertEquals(0, lock.getReadHoldCount());
			Assertions.assertEquals(0, lock.getWriteHoldCount());
			Lock ml = rwl.acquireMutexLock();
			Assertions.assertSame(writeLock, ml);
			Assertions.assertEquals(1, lock.getWriteHoldCount());
			ml.unlock();
			Assertions.assertEquals(0, lock.getWriteHoldCount());
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
	public void testReadLocked() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final BaseShareableLockable rwl = new BaseShareableLockable(lock);

		Assertions.assertThrows(NullPointerException.class,
			() -> rwl.shareLocked((CheckedSupplier<Object, Exception>) null), "Did not fail with a null Supplier");

		Assertions.assertThrows(NullPointerException.class, () -> rwl.shareLocked((CheckedRunnable<Exception>) null),
			"Did not fail with a null Runnable");

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertEquals(0, lock.getReadHoldCount());
		ReadWriteLock ret = rwl.shareLocked(() -> {
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
			rwl.shareLocked(() -> {
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
		rwl.shareLocked(() -> {
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
			Assertions.assertSame(a, rwl.shareLocked(operation));
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
			rwl.shareLocked(operation);
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
		final BaseShareableLockable rwl = new BaseShareableLockable(lock);

		Assertions.assertThrows(NullPointerException.class,
			() -> rwl.mutexLocked((CheckedSupplier<Object, Exception>) null), "Did not fail with a null Supplier");
		Assertions.assertThrows(NullPointerException.class, () -> rwl.mutexLocked((CheckedRunnable<Exception>) null),
			"Did not fail with a null Runnable");

		Assertions.assertTrue(writeLock.tryLock());
		Assertions.assertTrue(writeLock.isHeldByCurrentThread());
		writeLock.unlock();

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		ReadWriteLock ret = rwl.mutexLocked(() -> {
			// Prove that we're holding the read lock
			Assertions.assertTrue(writeLock.isHeldByCurrentThread());
			return lock;
		});
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		Assertions.assertSame(lock, ret);

		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		final RuntimeException ex = new RuntimeException();
		try {
			rwl.mutexLocked(() -> {
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
		rwl.mutexLocked(() -> Assertions.assertTrue(writeLock.isHeldByCurrentThread()));
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
			Assertions.assertSame(a, rwl.mutexLocked(operation));
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
			rwl.mutexLocked(operation);
			Assertions.assertEquals(0, lock.getReadHoldCount());
			Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		}
	}

	@Test
	public void testReadUpgradable() throws Exception {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
		final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		final BaseShareableLockable rwl = new BaseShareableLockable(lock);
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

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullSup, nullPred, nullMap), "Did not fail with all-null parameters");

			rwl.shareLockedUpgradable(nullSup, pred, map);

			Assertions.assertThrows(NullPointerException.class, () -> rwl.shareLockedUpgradable(sup, nullPred, map),
				"Did not fail with null Predicate");

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(sup, Objects::nonNull, nullMap),
				"Did not fail with null mapper Function");

			Assertions.assertThrows(NullPointerException.class, () -> rwl.shareLockedUpgradable(sup, nullPred, nullMap),
				"Did not fail with null Predicate and mapper Function");

			Assertions.assertThrows(NullPointerException.class, () -> rwl.shareLockedUpgradable(nullSup, pred, nullMap),
				"Did not fail with null Supplier and mapper Function");

			Assertions.assertThrows(NullPointerException.class, () -> rwl.shareLockedUpgradable(nullSup, nullPred, map),
				"Did not fail with null Supplier and Predicate");

			o = rwl.shareLockedUpgradable(sup, pred, map);
		}

		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		callCount.set(0);
		o = rwl.shareLockedUpgradable(() -> {
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
		o = rwl.shareLockedUpgradable(() -> {
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
		o = rwl.shareLockedUpgradable(() -> {
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

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullSup, nullPred, nullCons), "Did not fail with all-null parameters");

			rwl.shareLockedUpgradable(nullSup, pred, cons);

			Assertions.assertThrows(NullPointerException.class, () -> rwl.shareLockedUpgradable(sup, nullPred, cons),
				"Did not fail with null Predicate");

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(sup, Objects::nonNull, nullCons),
				"Did not fail with null mapper Function");

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(sup, nullPred, nullCons),
				"Did not fail with null Predicate and mapper Function");

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullSup, pred, nullCons),
				"Did not fail with null Supplier and mapper Function");

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullSup, nullPred, cons),
				"Did not fail with null Supplier and Predicate");

			rwl.shareLockedUpgradable(sup, pred, cons);
		}

		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(writeLock.isHeldByCurrentThread());
		callCount.set(0);
		rwl.shareLockedUpgradable(() -> {
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
		rwl.shareLockedUpgradable(() -> {
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
		rwl.shareLockedUpgradable(() -> {
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

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertSame(a, rwl.shareLockedUpgradable(decision, writeBlock));
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());

			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.shareLockedUpgradable(() -> {
				callCount.incrementAndGet();
				return Boolean.FALSE;
			}, writeBlock);
			Assertions.assertEquals(1, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.shareLockedUpgradable(() -> {
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

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertSame(a, rwl.shareLockedUpgradable(decision, writeBlock));
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());

			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.shareLockedUpgradable(() -> {
				callCount.incrementAndGet();
				return Boolean.FALSE;
			}, writeBlock);
			Assertions.assertEquals(1, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.shareLockedUpgradable(() -> {
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
				() -> rwl.shareLockedUpgradable(nullChecker, nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullChecker, nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullChecker, decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertSame(a, rwl.shareLockedUpgradable(nullChecker, decision, writeBlock));
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(checker, nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(checker, nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(checker, decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertSame(a, rwl.shareLockedUpgradable(checker, decision, writeBlock));
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

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			rwl.shareLockedUpgradable(decision, writeBlock);
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());

			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.shareLockedUpgradable(() -> {
				callCount.incrementAndGet();
				return Boolean.FALSE;
			}, writeBlock);
			Assertions.assertEquals(1, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.shareLockedUpgradable(() -> {
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

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			rwl.shareLockedUpgradable(decision, writeBlock);
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());

			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.shareLockedUpgradable(() -> {
				callCount.incrementAndGet();
				return Boolean.FALSE;
			}, writeBlock);
			Assertions.assertEquals(1, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.shareLockedUpgradable(() -> {
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
				() -> rwl.shareLockedUpgradable(nullChecker, nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullChecker, nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullChecker, decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			rwl.shareLockedUpgradable(nullChecker, decision, writeBlock);
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(checker, nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(checker, nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(checker, decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			rwl.shareLockedUpgradable(checker, decision, writeBlock);
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
			final Function<Supplier<Condition>, Object> nullWriteBlock = null;
			final Function<Supplier<Condition>, Object> writeBlock = (c) -> {
				writeBlockInvoked.set(true);
				Assertions.assertNotNull(c);
				Assertions.assertNotNull(c.get());
				return a;
			};

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertSame(a, rwl.shareLockedUpgradable(decision, writeBlock));
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());

			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.shareLockedUpgradable(() -> {
				callCount.incrementAndGet();
				return Boolean.FALSE;
			}, writeBlock);
			Assertions.assertEquals(1, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.shareLockedUpgradable(() -> {
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
			final CheckedFunction<Supplier<Condition>, Object, Exception> nullWriteBlock = null;
			final CheckedFunction<Supplier<Condition>, Object, Exception> writeBlock = (c) -> {
				writeBlockInvoked.set(true);
				Assertions.assertNotNull(c);
				Assertions.assertNotNull(c.get());
				return a;
			};

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertSame(a, rwl.shareLockedUpgradable(decision, writeBlock));
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());

			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.shareLockedUpgradable(() -> {
				callCount.incrementAndGet();
				return Boolean.FALSE;
			}, writeBlock);
			Assertions.assertEquals(1, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.shareLockedUpgradable(() -> {
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
			final BiFunction<Object, Supplier<Condition>, Object> nullWriteBlock = null;
			final BiFunction<Object, Supplier<Condition>, Object> writeBlock = (e, c) -> {
				writeBlockInvoked.set(true);
				Assertions.assertNotNull(c);
				Assertions.assertNotNull(c.get());
				return a;
			};

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullChecker, nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullChecker, nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullChecker, decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertSame(a, rwl.shareLockedUpgradable(nullChecker, decision, writeBlock));
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(checker, nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(checker, nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(checker, decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertSame(a, rwl.shareLockedUpgradable(checker, decision, writeBlock));
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
			final Consumer<Supplier<Condition>> nullWriteBlock = null;
			final Consumer<Supplier<Condition>> writeBlock = (c) -> {
				writeBlockInvoked.set(true);
				Assertions.assertNotNull(c);
				Assertions.assertNotNull(c.get());
			};

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			rwl.shareLockedUpgradable(decision, writeBlock);
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());

			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.shareLockedUpgradable(() -> {
				callCount.incrementAndGet();
				return Boolean.FALSE;
			}, writeBlock);
			Assertions.assertEquals(1, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.shareLockedUpgradable(() -> {
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
			final CheckedConsumer<Supplier<Condition>, Exception> nullWriteBlock = null;
			final CheckedConsumer<Supplier<Condition>, Exception> writeBlock = (c) -> {
				writeBlockInvoked.set(true);
				Assertions.assertNotNull(c);
				Assertions.assertNotNull(c.get());
			};

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			rwl.shareLockedUpgradable(decision, writeBlock);
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());

			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.shareLockedUpgradable(() -> {
				callCount.incrementAndGet();
				return Boolean.FALSE;
			}, writeBlock);
			Assertions.assertEquals(1, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			rwl.shareLockedUpgradable(() -> {
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
			final BiConsumer<Object, Supplier<Condition>> nullWriteBlock = null;
			final BiConsumer<Object, Supplier<Condition>> writeBlock = (e, c) -> {
				writeBlockInvoked.set(true);
				Assertions.assertNotNull(c);
				Assertions.assertNotNull(c.get());
			};

			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullChecker, nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullChecker, nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(nullChecker, decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			rwl.shareLockedUpgradable(nullChecker, decision, writeBlock);
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());
			callCount.set(0);
			writeBlockInvoked.set(false);
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(checker, nullDecision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(checker, nullDecision, writeBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			Assertions.assertThrows(NullPointerException.class,
				() -> rwl.shareLockedUpgradable(checker, decision, nullWriteBlock));
			Assertions.assertEquals(0, callCount.get());
			Assertions.assertFalse(writeBlockInvoked.get());
			rwl.shareLockedUpgradable(checker, decision, writeBlock);
			Assertions.assertEquals(2, callCount.get());
			Assertions.assertTrue(writeBlockInvoked.get());
		}
	}

	@Test
	public void testRunnableAlwaysChecked() throws Throwable {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final BaseShareableLockable rwl = new BaseShareableLockable(lock);
		final AtomicLong callCount = new AtomicLong(0);

		callCount.set(0);
		rwl.shareLocked(new CheckedRunnable<Throwable>() {
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
		Assertions.assertThrows(IOException.class, () -> rwl.shareLocked(new CheckedRunnable<IOException>() {
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
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final BaseShareableLockable rwl = new BaseShareableLockable(lock);
		final AtomicLong callCount = new AtomicLong(0);

		callCount.set(0);
		rwl.shareLocked(new CheckedSupplier<Object, Throwable>() {
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
		Assertions.assertThrows(IOException.class, () -> rwl.shareLocked(new CheckedSupplier<Object, IOException>() {
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

	@Test
	public void testAlwaysCheckedInComplexUpgradable() throws Throwable {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final BaseShareableLockable rwl = new BaseShareableLockable(lock);
		final AtomicLong predicateCount = new AtomicLong(0);
		final AtomicLong supplierCount = new AtomicLong(0);
		final AtomicLong workerCount = new AtomicLong(0);
		final AtomicReference<Object> uuid = new AtomicReference<>(null);

		Object expected = null;

		final CheckedSupplier<Object, Throwable> supplier = new CheckedSupplier<Object, Throwable>() {
			@Override
			public Object get() {
				Assertions.fail("CheckedSupplier.get() should never be called");
				return null;
			}

			@Override
			public Object getChecked() throws Throwable {
				Assertions.assertNotNull(uuid.get()); // for safety
				supplierCount.incrementAndGet();
				return uuid.get();
			}
		};

		final CheckedPredicate<Object, Throwable> predicate = new CheckedPredicate<Object, Throwable>() {
			@Override
			public boolean test(Object o) {
				Assertions.fail("CheckedPredicate.test() should never be called");
				return false;
			}

			@Override
			public boolean testChecked(Object o) throws Throwable {
				Assertions.assertNotNull(o);
				predicateCount.incrementAndGet();
				return true;
			}
		};

		supplierCount.set(0);
		predicateCount.set(0);
		workerCount.set(0);
		expected = UUID.randomUUID();
		uuid.set(expected);
		rwl.shareLockedUpgradable(supplier, predicate, new CheckedConsumer<Object, Throwable>() {
			@Override
			public void accept(Object o) {
				Assertions.fail("CheckedConsumer.accept() should never be called");
			}

			@Override
			public void acceptChecked(Object o) throws Throwable {
				Assertions.assertSame(uuid.get(), o);
				workerCount.incrementAndGet();
			}
		});
		Assertions.assertEquals(2, supplierCount.get());
		Assertions.assertEquals(2, predicateCount.get());
		Assertions.assertEquals(1, workerCount.get());

		supplierCount.set(0);
		predicateCount.set(0);
		workerCount.set(0);
		expected = UUID.randomUUID();
		uuid.set(expected);
		rwl.shareLockedUpgradable(supplier, predicate, new CheckedBiConsumer<Object, Supplier<Condition>, Throwable>() {
			@Override
			public void accept(Object o, Supplier<Condition> c) {
				Assertions.fail("CheckedConsumer.accept() should never be called");
			}

			@Override
			public void acceptChecked(Object o, Supplier<Condition> c) throws Throwable {
				Assertions.assertSame(uuid.get(), o);
				Assertions.assertNotNull(c);
				Assertions.assertNotNull(c.get());
				workerCount.incrementAndGet();
			}
		});
		Assertions.assertEquals(2, supplierCount.get());
		Assertions.assertEquals(2, predicateCount.get());
		Assertions.assertEquals(1, workerCount.get());

		supplierCount.set(0);
		predicateCount.set(0);
		workerCount.set(0);
		expected = UUID.randomUUID();
		uuid.set(expected);
		Assertions.assertSame(expected,
			rwl.shareLockedUpgradable(supplier, predicate, new CheckedFunction<Object, Object, Throwable>() {
				@Override
				public Object apply(Object o) {
					Assertions.fail("CheckedFunction.apply() should never be called");
					return null;
				}

				@Override
				public Object applyChecked(Object o) throws Throwable {
					Assertions.assertSame(uuid.get(), o);
					workerCount.incrementAndGet();
					return o;
				}
			}));
		Assertions.assertEquals(2, supplierCount.get());
		Assertions.assertEquals(2, predicateCount.get());
		Assertions.assertEquals(1, workerCount.get());

		supplierCount.set(0);
		predicateCount.set(0);
		workerCount.set(0);
		expected = UUID.randomUUID();
		uuid.set(expected);
		Assertions.assertSame(expected, rwl.shareLockedUpgradable(supplier, predicate,
			new CheckedBiFunction<Object, Supplier<Condition>, Object, Throwable>() {
				@Override
				public Object apply(Object o, Supplier<Condition> c) {
					Assertions.fail("CheckedFunction.apply() should never be called");
					return null;
				}

				@Override
				public Object applyChecked(Object o, Supplier<Condition> c) throws Throwable {
					Assertions.assertSame(uuid.get(), o);
					Assertions.assertNotNull(c);
					Assertions.assertNotNull(c.get());
					workerCount.incrementAndGet();
					return o;
				}
			}));
		Assertions.assertEquals(2, supplierCount.get());
		Assertions.assertEquals(2, predicateCount.get());
		Assertions.assertEquals(1, workerCount.get());
	}

	@Test
	public void testExtractShareableLock() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		final BaseShareableLockable rwl = new BaseShareableLockable(lock);
		Assertions.assertSame(lock, ShareableLockable.extractShareableLock(rwl));
		Assertions.assertThrows(NullPointerException.class, () -> ShareableLockable.extractShareableLock(null));
		Assertions.assertNull(ShareableLockable.extractShareableLock(new Object()));
		Assertions.assertSame(lock, ShareableLockable.extractShareableLock(lock));
	}
}
