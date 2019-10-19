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

public class SynchronizedBoxTest {

	@Test
	public void testSynchronizedBox() {
		SynchronizedBox<Long> c = new SynchronizedBox<>();
		Assertions.assertNull(c.get());
	}

	public void testSynchronizedBoxLong() {
		SynchronizedBox<Long> c = null;
		for (long i = -100; i <= 100; i++) {
			c = new SynchronizedBox<>(i);
			Assertions.assertEquals(i, c.get());
		}
	}

	@Test
	public void testGetCreated() throws Exception {
		SynchronizedBox<Long> c = null;
		Instant pre = null;
		Instant created = null;
		Instant post = null;

		pre = Instant.now();
		c = new SynchronizedBox<>();
		created = c.getCreated();
		post = Instant.now();
		Assertions.assertTrue(pre.isBefore(post) || pre.equals(post));
		Assertions.assertTrue(pre.isBefore(created) || pre.equals(created));
		Assertions.assertTrue(post.isAfter(created) || post.equals(created));
	}

	@Test
	public void testGetLastChanged() throws Exception {
		SynchronizedBox<Long> c = null;
		Instant pre = null;
		Instant lastChanged = null;
		Instant post = null;

		pre = Instant.now();
		c = new SynchronizedBox<>();
		lastChanged = c.getLastChanged();
		post = Instant.now();

		Assertions.assertTrue(pre.isBefore(post) || pre.equals(post));
		Assertions.assertTrue(pre.isBefore(lastChanged) || pre.equals(lastChanged));
		Assertions.assertTrue(post.isAfter(lastChanged) || post.equals(lastChanged));

		for (int i = 0; i < 10; i++) {
			pre = Instant.now();
			c.setAndGet(System.nanoTime());
			lastChanged = c.getLastChanged();
			post = Instant.now();
			Assertions.assertTrue(pre.isBefore(post) || pre.equals(post));
			Assertions.assertTrue(pre.isBefore(lastChanged) || pre.equals(lastChanged));
			Assertions.assertTrue(post.isAfter(lastChanged) || post.equals(lastChanged));
		}
	}

	@Test
	public void testIsChangedSinceCreation() {
		SynchronizedBox<Long> c = null;

		c = new SynchronizedBox<>(0L);
		Assertions.assertFalse(c.isChangedSinceCreation());
		c.setAndGet(System.nanoTime());
		Assertions.assertTrue(c.isChangedSinceCreation());

		c = new SynchronizedBox<>(0L);
		Assertions.assertFalse(c.isChangedSinceCreation());
		c.setAndGet(System.currentTimeMillis());
		Assertions.assertTrue(c.isChangedSinceCreation());

		c = new SynchronizedBox<>(0L);
		Assertions.assertFalse(c.isChangedSinceCreation());
		c.recompute((l) -> 10L);
		Assertions.assertTrue(c.isChangedSinceCreation());

		c = new SynchronizedBox<>(0L);
		Assertions.assertFalse(c.isChangedSinceCreation());
		c.recomputeIfMatches((l) -> l == 0, (l) -> 100L);
		Assertions.assertTrue(c.isChangedSinceCreation());
	}

	@Test
	public void testSetAndGet() {
		SynchronizedBox<Long> c = new SynchronizedBox<>(0L);
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
		SynchronizedBox<Long> c = null;
		for (long i = -100; i <= 100; i++) {
			c = new SynchronizedBox<>(i);
			Assertions.assertEquals((i % 2) == 0, c.setIfMatches((l) -> ((l % 2) == 0), System.nanoTime()));
		}
	}

	@Test
	public void testRecomputeLongUnaryOperator() {
		SynchronizedBox<Long> c = new SynchronizedBox<>(0L);
		for (long i = -100; i <= 100; i++) {
			final long v = i;
			Assertions.assertEquals(i, c.recompute((l) -> v));
		}
	}

	@Test
	public void testRecomputeIfMatches() {
		SynchronizedBox<Long> c = null;
		for (long i = -100; i <= 100; i++) {
			c = new SynchronizedBox<>(i);
			final long v = i;
			Assertions.assertEquals(i, c.recomputeIfMatches((l) -> (l == v), (l) -> v));
		}
		for (long i = -100; i <= 100; i++) {
			c = new SynchronizedBox<>(i);
			Assertions.assertEquals(i, c.recomputeIfMatches((l) -> (l == System.nanoTime()), (l) -> System.nanoTime()));
		}
	}

	@Test
	public void testWaitUntilMatches() throws Exception {
		SynchronizedBox<Long> c = new SynchronizedBox<>(0L);
		final CyclicBarrier barrier = new CyclicBarrier(2);

		final AtomicBoolean success = new AtomicBoolean(false);
		final long now = System.nanoTime();
		CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
			try {
				barrier.await();
				c.waitUntilMatches((l) -> l == now);
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
		SynchronizedBox<Long> c = new SynchronizedBox<>(0L);
		final CyclicBarrier barrier = new CyclicBarrier(2);

		final long now = System.nanoTime();
		CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
			try {
				barrier.await();
				Assertions.assertEquals(now, c.waitUntilMatches((l) -> l == now, 200, TimeUnit.MILLISECONDS));
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		});

		barrier.await();
		Thread.sleep(100);
		c.setAndGet(now);
		f.join();

		c.setAndGet(0L);
		f = CompletableFuture.runAsync(() -> {
			try {
				barrier.await();
				c.waitUntilMatches((l) -> l == now, 10, TimeUnit.MILLISECONDS);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		});

		barrier.await();
		Thread.sleep(100);
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
	public void testWaitUntilChanged() throws Exception {
		SynchronizedBox<Long> c = new SynchronizedBox<>(0L);
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
		SynchronizedBox<Long> c = new SynchronizedBox<>(0L);
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
		new SynchronizedBox<>(0L).toString();
	}
}