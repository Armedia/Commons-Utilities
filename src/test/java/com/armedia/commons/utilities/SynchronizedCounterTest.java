/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2020 Armedia, LLC
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
package com.armedia.commons.utilities;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SynchronizedCounterTest {

	@Test
	public void testSynchronizedCounter() {
		SynchronizedCounter c = new SynchronizedCounter();
		Assertions.assertEquals(0, c.get());
	}

	public void testSynchronizedCounterLong() {
		SynchronizedCounter c = null;
		for (long i = -100; i <= 100; i++) {
			c = new SynchronizedCounter(i);
			Assertions.assertEquals(i, c.get());
		}
	}

	@Test
	public void testGetCreated() throws Exception {
		SynchronizedCounter c = null;
		Instant pre = null;
		Instant created = null;
		Instant post = null;

		pre = Instant.now();
		c = new SynchronizedCounter();
		created = c.getCreated();
		post = Instant.now();
		Assertions.assertTrue(pre.isBefore(post) || pre.equals(post));
		Assertions.assertTrue(pre.isBefore(created) || pre.equals(created));
		Assertions.assertTrue(post.isAfter(created) || post.equals(created));
	}

	@Test
	public void testGetLastChanged() throws Exception {
		SynchronizedCounter c = null;
		Instant pre = null;
		Instant lastChanged = null;
		Instant post = null;

		pre = Instant.now();
		c = new SynchronizedCounter();
		lastChanged = c.getLastChanged();
		post = Instant.now();

		Assertions.assertTrue(pre.isBefore(post) || pre.equals(post));
		Assertions.assertTrue(pre.isBefore(lastChanged) || pre.equals(lastChanged));
		Assertions.assertTrue(post.isAfter(lastChanged) || post.equals(lastChanged));

		for (int i = 0; i < 10; i++) {
			pre = Instant.now();
			c.addAndGet(1);
			lastChanged = c.getLastChanged();
			post = Instant.now();
			Assertions.assertTrue(pre.isBefore(post) || pre.equals(post));
			Assertions.assertTrue(pre.isBefore(lastChanged) || pre.equals(lastChanged));
			Assertions.assertTrue(post.isAfter(lastChanged) || post.equals(lastChanged));
		}
	}

	@Test
	public void testIsChangedSinceCreation() {
		SynchronizedCounter c = null;

		c = new SynchronizedCounter(0);
		Assertions.assertFalse(c.isChangedSinceCreation());
		c.addAndGet(1);
		Assertions.assertTrue(c.isChangedSinceCreation());

		c = new SynchronizedCounter(0);
		Assertions.assertFalse(c.isChangedSinceCreation());
		c.setAndGet(1);
		Assertions.assertTrue(c.isChangedSinceCreation());

		c = new SynchronizedCounter(0);
		Assertions.assertFalse(c.isChangedSinceCreation());
		c.recompute((l) -> 10L);
		Assertions.assertTrue(c.isChangedSinceCreation());

		c = new SynchronizedCounter(0);
		Assertions.assertFalse(c.isChangedSinceCreation());
		c.recomputeIfMatches((l) -> l == 0, (l) -> 100L);
		Assertions.assertTrue(c.isChangedSinceCreation());
	}

	@Test
	public void testSetAndGet() {
		SynchronizedCounter c = new SynchronizedCounter(0);
		Long prev = null;
		for (long i = -100; i <= 100; i++) {
			long v = c.setAndGet(i);
			if (prev != null) {
				Assertions.assertEquals(prev, v);
			}
			prev = i;
		}
		Assertions.assertEquals(prev, c.get());
	}

	@Test
	public void testSetIfMatches() {
		SynchronizedCounter c = null;
		for (long i = -100; i <= 100; i++) {
			c = new SynchronizedCounter(i);
			Assertions.assertEquals((i % 2) == 0, c.setIfMatches((l) -> ((l % 2) == 0), System.nanoTime()));
		}
	}

	@Test
	public void testRecomputeLongUnaryOperator() {
		SynchronizedCounter c = new SynchronizedCounter(0);
		for (long i = -100; i <= 100; i++) {
			final long v = i;
			Assertions.assertEquals(i, c.recompute((l) -> v));
		}
	}

	@Test
	public void testRecomputeIfMatches() {
		SynchronizedCounter c = null;
		for (long i = -100; i <= 100; i++) {
			c = new SynchronizedCounter(i);
			final long v = i;
			Assertions.assertEquals(i, c.recomputeIfMatches((l) -> (l == v), (l) -> v));
		}
		for (long i = -100; i <= 100; i++) {
			c = new SynchronizedCounter(i);
			Assertions.assertEquals(i, c.recomputeIfMatches((l) -> (l == System.nanoTime()), (l) -> System.nanoTime()));
		}
	}

	@Test
	public void testAdd() {
		for (int n = 0; n < 5; n++) {
			SynchronizedCounter c = new SynchronizedCounter(0);
			long v = 0;
			for (long i = -100; i <= 100; i++) {
				Assertions.assertEquals(v += n, c.addAndGet(n));
			}
		}
	}

	@Test
	public void testSubtract() {
		for (int n = 0; n < 5; n++) {
			SynchronizedCounter c = new SynchronizedCounter(0);
			long v = 0;
			for (long i = -100; i <= 100; i++) {
				Assertions.assertEquals(v -= n, c.subtractAndGet(n));
			}
		}
	}

	@Test
	public void testIncrement() {
		SynchronizedCounter c = new SynchronizedCounter(0);
		long v = 0;
		for (long i = -100; i <= 100; i++) {
			Assertions.assertEquals(++v, c.incrementAndGet());
		}
	}

	@Test
	public void testDecrement() {
		SynchronizedCounter c = new SynchronizedCounter(0);
		long v = 0;
		for (long i = -100; i <= 100; i++) {
			Assertions.assertEquals(--v, c.decrementAndGet());
		}
	}

	@Test
	public void testWaitUntilValueLong() throws Exception {
		SynchronizedCounter c = new SynchronizedCounter(0);
		final CyclicBarrier barrier = new CyclicBarrier(2);

		final AtomicBoolean success = new AtomicBoolean(false);
		final long now = System.nanoTime();
		CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
			try {
				barrier.await();
				c.waitUntilValue(now);
				success.set(true);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		});

		barrier.await();
		Thread.sleep(100);
		c.setAndGet(now);
		f.join();
		Assertions.assertTrue(success.get());
	}

	@Test
	public void testWaitUntilValueLongLongTimeUnit() throws Exception {
		SynchronizedCounter c = new SynchronizedCounter(0);
		final CyclicBarrier barrier = new CyclicBarrier(2);

		final long now = System.nanoTime();
		CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
			try {
				barrier.await();
				Assertions.assertTrue(c.waitUntilValue(now, 200, TimeUnit.MILLISECONDS));
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		});

		barrier.await();
		Thread.sleep(100);
		c.setAndGet(now);
		f.join();

		c.setAndGet(0);
		f = CompletableFuture.runAsync(() -> {
			try {
				barrier.await();
				Assertions.assertFalse(c.waitUntilValue(now, 10, TimeUnit.MILLISECONDS));
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		});

		barrier.await();
		Thread.sleep(100);
		c.setAndGet(now);
		f.join();
	}

	@Test
	public void testWaitUntilChanged() throws Exception {
		SynchronizedCounter c = new SynchronizedCounter(0);
		final CyclicBarrier barrier = new CyclicBarrier(2);
		final AtomicLong newValue = new AtomicLong(0);

		CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
			try {
				barrier.await();
				newValue.set(c.waitUntilChanged());
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		});

		barrier.await();
		Thread.sleep(100);
		final long now = System.nanoTime();
		c.setAndGet(now);
		f.join();
		Assertions.assertEquals(now, newValue.get());
	}

	@Test
	public void testWaitUntilChangedLongTimeUnit() throws Exception {
		SynchronizedCounter c = new SynchronizedCounter(0);
		final CyclicBarrier barrier = new CyclicBarrier(2);
		final AtomicLong newValue = new AtomicLong(0);

		CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
			try {
				barrier.await();
				newValue.set(c.waitUntilChanged(200, TimeUnit.MILLISECONDS));
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		});

		barrier.await();
		Thread.sleep(100);
		long now = System.nanoTime();
		c.setAndGet(now);
		f.join();
		Assertions.assertEquals(now, newValue.get());

		f = CompletableFuture.runAsync(() -> {
			try {
				barrier.await();
				newValue.set(c.waitUntilChanged(10, TimeUnit.MILLISECONDS));
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		});

		barrier.await();
		Thread.sleep(100);
		now = System.nanoTime();
		c.setAndGet(now);
		CompletableFuture<Void> f2 = f;
		try {
			f2.join();
			Assertions.fail("Did not timeout while waiting");
		} catch (CompletionException e) {
			Throwable t = e.getCause();
			Assertions.assertSame(RuntimeException.class, t.getClass());
			Assertions.assertSame(TimeoutException.class, t.getCause().getClass());
		}
	}

	@Test
	public void testToString() {
		new SynchronizedCounter(0).toString();
	}
}
