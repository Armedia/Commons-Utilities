package com.armedia.commons.utilities.function;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

public class LazySupplierTest {

	private static final CheckedSupplier<String> FAIL_SUPP = () -> {
		Assert.fail("This supplier should not have been invoked");
		return null;
	};

	@Test
	public void testLazySupplier() {
		LazySupplier<String> supplier = new LazySupplier<>();
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertNull(supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testLazySupplierCheckedSupplierOfT() {
		final String uuid = UUID.randomUUID().toString();
		final CheckedSupplier<String> uuidSupplier = () -> uuid;
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>(uuidSupplier);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>((CheckedSupplier<String>) null);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertNull(supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testLazySupplierT() {
		final String uuid = UUID.randomUUID().toString();
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>(uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>((String) null);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertNull(supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testLazySupplierCheckedSupplierOfTT() {
		String uuid = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		CheckedSupplier<String> uuidSupplier = () -> uuid2;
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>(null, null);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertNull(supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuidSupplier, null);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(null, uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuidSupplier, uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get(LazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testGet() {
		String uuid = UUID.randomUUID().toString();
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>();
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertNull(supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(() -> uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(() -> {
			throw new Exception(uuid);
		});
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		try {
			supplier.get();
			Assert.fail("Did not raise an explicit exception");
		} catch (Exception e) {
			Assert.assertSame(uuid, e.getCause().getMessage());
		}
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		AtomicBoolean called = new AtomicBoolean(false);
		Assert.assertSame(uuid, supplier.get(() -> {
			called.set(true);
			return uuid;
		}));
		Assert.assertTrue(called.get());
	}

	@Test
	public void testGetSupplierOfT() {
		String uuid = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>();
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(() -> uuid));
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get(() -> uuid2));
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(null));
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>();
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		try {
			supplier.get(() -> {
				throw new RuntimeException(uuid);
			});
			Assert.fail("Did not raise an explicit exception");
		} catch (Exception e) {
			Assert.assertSame(uuid, e.getCause().getMessage());
		}
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		AtomicBoolean called = new AtomicBoolean(false);
		Assert.assertSame(uuid, supplier.get(() -> {
			called.set(true);
			return uuid;
		}));
		Assert.assertTrue(called.get());
	}

	@Test
	public void testGetChecked() throws Exception {
		String uuid = UUID.randomUUID().toString();
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>();
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertNull(supplier.getChecked());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.getChecked());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(() -> uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.getChecked());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(() -> {
			throw new Exception(uuid);
		});
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		try {
			supplier.getChecked();
			Assert.fail("Did not raise an explicit exception");
		} catch (Exception e) {
			Assert.assertSame(uuid, e.getMessage());
		}
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		AtomicBoolean called = new AtomicBoolean(false);
		Assert.assertSame(uuid, supplier.get(() -> {
			called.set(true);
			return uuid;
		}));
		Assert.assertTrue(called.get());
	}

	@Test
	public void testGetCheckedCheckedSupplierOfT() throws Exception {
		String uuid = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>();
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(() -> uuid));
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get(() -> uuid2));
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(null));
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>();
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		try {
			supplier.getChecked(() -> {
				throw new Exception(uuid);
			});
			Assert.fail("Did not raise an explicit exception");
		} catch (Exception e) {
			Assert.assertSame(uuid, e.getMessage());
		}
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		AtomicBoolean called = new AtomicBoolean(false);
		Assert.assertSame(uuid, supplier.get(() -> {
			called.set(true);
			return uuid;
		}));
		Assert.assertTrue(called.get());
	}

	@Test
	public void testFromSupplierSupplierOfT() {
		final String uuid = UUID.randomUUID().toString();
		final Supplier<String> uuidSupplier = () -> uuid;
		LazySupplier<String> supplier = null;

		supplier = LazySupplier.fromSupplier(uuidSupplier);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = LazySupplier.fromSupplier(null);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertNull(supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testFromSupplierSupplierOfTT() {
		String uuid = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		Supplier<String> uuidSupplier = () -> uuid2;
		LazySupplier<String> supplier = null;

		supplier = LazySupplier.fromSupplier(null, null);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertNull(supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = LazySupplier.fromSupplier(uuidSupplier, null);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = LazySupplier.fromSupplier(null, uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = LazySupplier.fromSupplier(uuidSupplier, uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get(LazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testAwait() throws Exception {
		final CyclicBarrier barrier = new CyclicBarrier(2);
		final AtomicReference<LazySupplier<String>> supplier = new AtomicReference<>();
		final AtomicReference<String> uuid = new AtomicReference<>();
		final AtomicBoolean called = new AtomicBoolean(false);
		final AtomicReference<Thread> worker = new AtomicReference<>();
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			Future<String> future = null;
			final Callable<String> waiter = () -> {
				// First things first... await
				worker.set(Thread.currentThread());
				barrier.await();
				final LazySupplier<String> S = supplier.get();
				try {
					Assert.assertFalse(S.isInitialized());
					String ret = S.await();
					Assert.assertTrue(S.isInitialized());
					return ret;
				} finally {
					called.set(true);
				}
			};

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(uuid.get()));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assert.assertNotNull(worker.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case BLOCKED:
					case WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assert.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assert.assertFalse(called.get());
			Assert.assertNotNull(worker.get());
			Assert.assertNotSame(Thread.currentThread(), worker.get());
			Assert.assertSame(uuid.get(), supplier.get().get());
			Assert.assertSame(uuid.get(), future.get());
			Assert.assertTrue(future.isDone());
			Assert.assertTrue(called.get());

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(() -> {
				throw new Exception(uuid.get());
			}));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assert.assertNotNull(worker.get());
			Thread.State state = null;
			outer: while (true) {
				state = worker.get().getState();
				inner: switch (state) {
					case BLOCKED:
					case WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assert.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assert.assertFalse(called.get());
			try {
				supplier.get().getChecked();
				Assert.fail("Did not raise an exception");
			} catch (Throwable t) {
				Assert.assertSame(uuid.get(), t.getMessage());
			}
			Assert.assertNotSame(Thread.State.NEW, state);
			Assert.assertNotSame(Thread.State.RUNNABLE, state);
			Assert.assertNotSame(Thread.State.TERMINATED, state);
			Assert.assertNotSame(Thread.State.TIMED_WAITING, state);
			Assert.assertFalse(called.get());
			Assert.assertFalse(future.isDone());
			supplier.get().get(uuid::get);
			Assert.assertSame(uuid.get(), future.get());
			Assert.assertTrue(called.get());

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(uuid.get()));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assert.assertNotNull(worker.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case BLOCKED:
					case WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assert.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assert.assertFalse(called.get());
			Assert.assertNotNull(worker.get());
			Assert.assertNotSame(Thread.currentThread(), worker.get());

			worker.get().interrupt();
			try {
				future.get();
				Assert.fail("Did not fail chaining the InterrupedException");
			} catch (ExecutionException e) {
				// Make sure we were interrupted
				Assert.assertTrue(InterruptedException.class.isInstance(e.getCause()));
			}
			Assert.assertTrue(future.isDone());
			Assert.assertTrue(called.get());
		} finally {
			executor.shutdownNow();
			executor.awaitTermination(1, TimeUnit.MINUTES);
		}
	}

	@Test
	public void testAwaitUninterruptibly() throws Exception {
		final CyclicBarrier barrier = new CyclicBarrier(2);
		final AtomicReference<LazySupplier<String>> supplier = new AtomicReference<>();
		final AtomicReference<String> uuid = new AtomicReference<>();
		final AtomicBoolean called = new AtomicBoolean(false);
		final AtomicReference<Thread> worker = new AtomicReference<>();
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			Future<String> future = null;
			final Callable<String> waiter = () -> {
				// First things first... await
				worker.set(Thread.currentThread());
				barrier.await();
				final LazySupplier<String> S = supplier.get();
				try {
					Assert.assertFalse(S.isInitialized());
					String ret = S.awaitUninterruptibly();
					Assert.assertTrue(S.isInitialized());
					return ret;
				} finally {
					called.set(true);
				}
			};

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(uuid.get()));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assert.assertNotNull(worker.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case BLOCKED:
					case WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assert.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assert.assertFalse(called.get());
			Assert.assertNotNull(worker.get());
			Assert.assertNotSame(Thread.currentThread(), worker.get());
			Assert.assertSame(uuid.get(), supplier.get().get());
			Assert.assertSame(uuid.get(), future.get());
			Assert.assertTrue(future.isDone());
			Assert.assertTrue(called.get());

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(() -> {
				throw new Exception(uuid.get());
			}));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assert.assertNotNull(worker.get());
			Thread.State state = null;
			outer: while (true) {
				state = worker.get().getState();
				inner: switch (state) {
					case BLOCKED:
					case WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assert.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assert.assertFalse(called.get());
			try {
				supplier.get().getChecked();
				Assert.fail("Did not raise an exception");
			} catch (Throwable t) {
				Assert.assertSame(uuid.get(), t.getMessage());
			}
			Assert.assertNotSame(Thread.State.NEW, state);
			Assert.assertNotSame(Thread.State.RUNNABLE, state);
			Assert.assertNotSame(Thread.State.TERMINATED, state);
			Assert.assertNotSame(Thread.State.TIMED_WAITING, state);
			Assert.assertFalse(called.get());
			Assert.assertFalse(future.isDone());
			supplier.get().get(uuid::get);
			Assert.assertSame(uuid.get(), future.get());
			Assert.assertTrue(called.get());

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(uuid.get()));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assert.assertNotNull(worker.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case BLOCKED:
					case WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assert.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assert.assertFalse(called.get());
			Assert.assertNotNull(worker.get());
			Assert.assertNotSame(Thread.currentThread(), worker.get());

			worker.get().interrupt(); // This should now have no effect
			Thread.sleep(100);
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case BLOCKED:
					case WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assert.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assert.assertFalse(future.isDone());
			Assert.assertFalse(called.get());
			Assert.assertSame(uuid.get(), supplier.get().get());
			Assert.assertSame(uuid.get(), future.get());
			Assert.assertTrue(future.isDone());
			Assert.assertTrue(called.get());
		} finally {
			executor.shutdownNow();
			executor.awaitTermination(1, TimeUnit.MINUTES);
		}
	}

	@Test
	public void testAwaitLongTimeUnit() throws Exception {
		final CyclicBarrier barrier = new CyclicBarrier(2);
		final AtomicReference<LazySupplier<String>> supplier = new AtomicReference<>();
		final AtomicReference<String> uuid = new AtomicReference<>();
		final AtomicBoolean called = new AtomicBoolean(false);
		final AtomicReference<Thread> worker = new AtomicReference<>();
		final AtomicReference<Pair<Long, TimeUnit>> timeout = new AtomicReference<>();
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		Pair<String, Boolean> futureRet = null;
		try {
			Future<Pair<String, Boolean>> future = null;
			final Callable<Pair<String, Boolean>> waiter = () -> {
				// First things first... await
				worker.set(Thread.currentThread());
				barrier.await();
				final Pair<Long, TimeUnit> to = timeout.get();
				final LazySupplier<String> S = supplier.get();
				try {
					Assert.assertFalse(S.isInitialized());
					Pair<String, Boolean> ret = S.await(to.getLeft(), to.getRight());
					Assert.assertTrue(S.isInitialized());
					return ret;
				} finally {
					called.set(true);
				}
			};

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(uuid.get()));
			called.set(false);
			timeout.set(Pair.of(10L, TimeUnit.SECONDS));

			future = executor.submit(waiter);
			barrier.await();
			Assert.assertNotNull(worker.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case TIMED_WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assert.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Thread.sleep(100);
			Assert.assertFalse(called.get());
			Assert.assertNotNull(worker.get());
			Assert.assertNotSame(Thread.currentThread(), worker.get());
			Assert.assertSame(uuid.get(), supplier.get().get());
			futureRet = future.get();
			Assert.assertSame(uuid.get(), futureRet.getLeft());
			Assert.assertFalse(futureRet.getRight());
			Assert.assertTrue(future.isDone());
			Assert.assertTrue(called.get());

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(() -> {
				throw new Exception(uuid.get());
			}));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assert.assertNotNull(worker.get());
			Thread.State state = null;
			outer: while (true) {
				state = worker.get().getState();
				inner: switch (state) {
					case TIMED_WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assert.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assert.assertFalse(called.get());
			try {
				supplier.get().getChecked();
				Assert.fail("Did not raise an exception");
			} catch (Throwable t) {
				Assert.assertSame(uuid.get(), t.getMessage());
			}
			Assert.assertNotSame(Thread.State.NEW, state);
			Assert.assertNotSame(Thread.State.RUNNABLE, state);
			Assert.assertNotSame(Thread.State.TERMINATED, state);
			Assert.assertNotSame(Thread.State.BLOCKED, state);
			Assert.assertNotSame(Thread.State.WAITING, state);
			Assert.assertFalse(called.get());
			Assert.assertFalse(future.isDone());
			Thread.sleep(100);
			supplier.get().get(uuid::get);
			futureRet = future.get();
			Assert.assertSame(uuid.get(), futureRet.getLeft());
			Assert.assertFalse(futureRet.getRight());
			Assert.assertTrue(called.get());

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(uuid.get()));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assert.assertNotNull(worker.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case TIMED_WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assert.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assert.assertFalse(called.get());
			Assert.assertNotNull(worker.get());
			Assert.assertNotSame(Thread.currentThread(), worker.get());

			worker.get().interrupt();
			try {
				future.get();
				Assert.fail("Did not fail chaining the InterrupedException");
			} catch (ExecutionException e) {
				// Make sure we were interrupted
				Assert.assertTrue(InterruptedException.class.isInstance(e.getCause()));
			}
			Assert.assertTrue(future.isDone());
			Assert.assertTrue(called.get());

			timeout.set(Pair.of(1L, TimeUnit.SECONDS));
			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(uuid.get()));
			called.set(false);

			future = executor.submit(() -> {
				// First things first... await
				worker.set(Thread.currentThread());
				barrier.await();
				final Pair<Long, TimeUnit> to = timeout.get();
				final LazySupplier<String> S = supplier.get();
				try {
					Assert.assertFalse(S.isInitialized());
					Pair<String, Boolean> ret = S.await(to.getLeft(), to.getRight());
					Assert.assertFalse(S.isInitialized());
					return ret;
				} finally {
					called.set(true);
				}
			});
			barrier.await();
			Assert.assertNotNull(worker.get());
			Assert.assertNotSame(Thread.currentThread(), worker.get());
			Assert.assertFalse(called.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case TIMED_WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assert.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			long now = System.nanoTime();
			Thread.sleep(2100);
			long duration = (System.nanoTime() - now);
			Assert.assertTrue(called.get());
			Assert.assertTrue(TimeUnit.NANOSECONDS.toSeconds(duration) >= 2);
			futureRet = future.get();
			Assert.assertTrue(futureRet.getRight());
			Assert.assertTrue(called.get());

		} finally {
			executor.shutdownNow();
			executor.awaitTermination(1, TimeUnit.MINUTES);
		}
	}

	@Test
	public void testAwaitUntil() throws Exception {
		final CyclicBarrier barrier = new CyclicBarrier(2);
		final AtomicReference<LazySupplier<String>> supplier = new AtomicReference<>();
		final AtomicReference<String> uuid = new AtomicReference<>();
		final AtomicBoolean called = new AtomicBoolean(false);
		final AtomicReference<Thread> worker = new AtomicReference<>();
		final AtomicReference<Date> timeout = new AtomicReference<>();
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		Pair<String, Boolean> futureRet = null;
		try {
			Future<Pair<String, Boolean>> future = null;
			final Callable<Pair<String, Boolean>> waiter = () -> {
				// First things first... await
				worker.set(Thread.currentThread());
				barrier.await();
				final Date to = timeout.get();
				final LazySupplier<String> S = supplier.get();
				try {
					Assert.assertFalse(S.isInitialized());
					Pair<String, Boolean> ret = S.awaitUntil(to);
					Assert.assertTrue(S.isInitialized());
					return ret;
				} finally {
					called.set(true);
				}
			};

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(uuid.get()));
			called.set(false);
			timeout.set(new Date(System.currentTimeMillis() + 1000));

			future = executor.submit(waiter);
			barrier.await();
			Assert.assertNotNull(worker.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case TIMED_WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assert.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Thread.sleep(100);
			Assert.assertFalse(called.get());
			Assert.assertNotNull(worker.get());
			Assert.assertNotSame(Thread.currentThread(), worker.get());
			Assert.assertSame(uuid.get(), supplier.get().get());
			futureRet = future.get();
			Assert.assertSame(uuid.get(), futureRet.getLeft());
			Assert.assertFalse(futureRet.getRight());
			Assert.assertTrue(future.isDone());
			Assert.assertTrue(called.get());

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(() -> {
				throw new Exception(uuid.get());
			}));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assert.assertNotNull(worker.get());
			Thread.State state = null;
			outer: while (true) {
				state = worker.get().getState();
				inner: switch (state) {
					case TIMED_WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assert.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assert.assertFalse(called.get());
			try {
				supplier.get().getChecked();
				Assert.fail("Did not raise an exception");
			} catch (Throwable t) {
				Assert.assertSame(uuid.get(), t.getMessage());
			}
			Assert.assertNotSame(Thread.State.NEW, state);
			Assert.assertNotSame(Thread.State.RUNNABLE, state);
			Assert.assertNotSame(Thread.State.TERMINATED, state);
			Assert.assertNotSame(Thread.State.BLOCKED, state);
			Assert.assertNotSame(Thread.State.WAITING, state);
			Assert.assertFalse(called.get());
			Assert.assertFalse(future.isDone());
			Thread.sleep(100);
			supplier.get().get(uuid::get);
			futureRet = future.get();
			Assert.assertSame(uuid.get(), futureRet.getLeft());
			Assert.assertFalse(futureRet.getRight());
			Assert.assertTrue(called.get());

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(uuid.get()));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assert.assertNotNull(worker.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case TIMED_WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assert.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assert.assertFalse(called.get());
			Assert.assertNotNull(worker.get());
			Assert.assertNotSame(Thread.currentThread(), worker.get());

			worker.get().interrupt();
			try {
				future.get();
				Assert.fail("Did not fail chaining the InterrupedException");
			} catch (ExecutionException e) {
				// Make sure we were interrupted
				Assert.assertTrue(InterruptedException.class.isInstance(e.getCause()));
			}
			Assert.assertTrue(future.isDone());
			Assert.assertTrue(called.get());

			timeout.set(new Date(System.currentTimeMillis() + 1000));
			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(uuid.get()));
			called.set(false);

			future = executor.submit(() -> {
				// First things first... await
				worker.set(Thread.currentThread());
				barrier.await();
				final Date to = timeout.get();
				final LazySupplier<String> S = supplier.get();
				try {
					Assert.assertFalse(S.isInitialized());
					Pair<String, Boolean> ret = S.awaitUntil(to);
					Assert.assertFalse(S.isInitialized());
					return ret;
				} finally {
					called.set(true);
				}
			});
			barrier.await();
			Assert.assertNotNull(worker.get());
			Assert.assertNotSame(Thread.currentThread(), worker.get());
			Assert.assertFalse(called.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case TIMED_WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assert.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			long now = System.nanoTime();
			Thread.sleep(2100);
			long duration = (System.nanoTime() - now);
			Assert.assertTrue(called.get());
			Assert.assertTrue(TimeUnit.NANOSECONDS.toSeconds(duration) >= 2);
			futureRet = future.get();
			Assert.assertTrue(futureRet.getRight());
			Assert.assertTrue(called.get());

		} finally {
			executor.shutdownNow();
			executor.awaitTermination(1, TimeUnit.MINUTES);
		}
	}
}