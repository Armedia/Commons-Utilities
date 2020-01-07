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
package com.armedia.commons.utilities.function;

import java.io.IOException;
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

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.ConcurrentInitializer;
import org.apache.commons.lang3.tuple.Pair;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.Tools;

public class LazySupplierTest {

	private static final Supplier<String> FAIL_SUPP = () -> {
		Assertions.fail("This supplier should not have been invoked");
		return null;
	};

	@Test
	public void testLazySupplier() {
		LazySupplier<String> supplier = new LazySupplier<>();
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertNull(supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testLazySupplierSupplierOfT() {
		final String uuid = UUID.randomUUID().toString();
		final Supplier<String> uuidSupplier = () -> uuid;
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>(uuidSupplier);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>((Supplier<String>) null);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertNull(supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testLazySupplierT() {
		final String uuid = UUID.randomUUID().toString();
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>(uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>((String) null);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertNull(supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testLazySupplierSupplierOfTT() {
		String uuid = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		Supplier<String> uuidSupplier = () -> uuid2;
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>(null, null);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertNull(supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuidSupplier, null);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(null, uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuidSupplier, uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get(LazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testGet() {
		String uuid = UUID.randomUUID().toString();
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>();
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertNull(supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(() -> uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(() -> {
			throw new RuntimeException(uuid);
		});
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		try {
			supplier.get();
			Assertions.fail("Did not raise an explicit exception");
		} catch (Exception e) {
			Assertions.assertSame(uuid, e.getCause().getMessage());
		}
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		AtomicBoolean called = new AtomicBoolean(false);
		Assertions.assertSame(uuid, supplier.get(() -> {
			called.set(true);
			return uuid;
		}));
		Assertions.assertTrue(called.get());
	}

	@Test
	public void testGetSupplierOfT() {
		String uuid = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>();
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(() -> uuid));
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get(() -> uuid2));
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(null));
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>();
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		try {
			supplier.get(() -> {
				throw new RuntimeException(uuid);
			});
			Assertions.fail("Did not raise an explicit exception");
		} catch (Exception e) {
			Assertions.assertSame(uuid, e.getMessage());
		}
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		AtomicBoolean called = new AtomicBoolean(false);
		Assertions.assertSame(uuid, supplier.get(() -> {
			called.set(true);
			return uuid;
		}));
		Assertions.assertTrue(called.get());
	}

	@Test
	public void testFromSupplierSupplierOfT() {
		final String uuid = UUID.randomUUID().toString();
		final Supplier<String> uuidSupplier = () -> uuid;
		LazySupplier<String> supplier = null;

		supplier = LazySupplier.fromSupplier(uuidSupplier);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = LazySupplier.fromSupplier(null);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertNull(supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testFromSupplierSupplierOfTT() {
		String uuid = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		Supplier<String> uuidSupplier = () -> uuid2;
		LazySupplier<String> supplier = null;

		supplier = LazySupplier.fromSupplier(null, null);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertNull(supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = LazySupplier.fromSupplier(uuidSupplier, null);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = LazySupplier.fromSupplier(null, uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = LazySupplier.fromSupplier(uuidSupplier, uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get(LazySupplierTest.FAIL_SUPP));
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
					Assertions.assertFalse(S.isInitialized());
					String ret = S.await();
					Assertions.assertTrue(S.isInitialized());
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
			Assertions.assertNotNull(worker.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case BLOCKED:
					case WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assertions.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assertions.assertFalse(called.get());
			Assertions.assertNotNull(worker.get());
			Assertions.assertNotSame(Thread.currentThread(), worker.get());
			Assertions.assertSame(uuid.get(), supplier.get().get());
			Assertions.assertSame(uuid.get(), future.get());
			Assertions.assertTrue(future.isDone());
			Assertions.assertTrue(called.get());
			called.set(false);
			future = executor.submit(() -> {
				// First things first... await
				worker.set(Thread.currentThread());
				barrier.await();
				final LazySupplier<String> S = supplier.get();
				try {
					Assertions.assertTrue(S.isInitialized());
					return S.await();
				} finally {
					called.set(true);
				}
			});
			barrier.await();
			Assertions.assertSame(uuid.get(), future.get());
			Assertions.assertTrue(future.isDone());
			Assertions.assertTrue(called.get());

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(() -> {
				throw new RuntimeException(uuid.get());
			}));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assertions.assertNotNull(worker.get());
			Thread.State state = null;
			outer: while (true) {
				state = worker.get().getState();
				inner: switch (state) {
					case BLOCKED:
					case WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assertions.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assertions.assertFalse(called.get());
			try {
				supplier.get().get();
				Assertions.fail("Did not raise an exception");
			} catch (Throwable t) {
				Assertions.assertSame(uuid.get(), t.getMessage());
			}
			Assertions.assertNotSame(Thread.State.NEW, state);
			Assertions.assertNotSame(Thread.State.RUNNABLE, state);
			Assertions.assertNotSame(Thread.State.TERMINATED, state);
			Assertions.assertNotSame(Thread.State.TIMED_WAITING, state);
			Assertions.assertFalse(called.get());
			Assertions.assertFalse(future.isDone());
			supplier.get().get(uuid::get);
			Assertions.assertSame(uuid.get(), future.get());
			Assertions.assertTrue(called.get());

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(uuid.get()));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assertions.assertNotNull(worker.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case BLOCKED:
					case WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assertions.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assertions.assertFalse(called.get());
			Assertions.assertNotNull(worker.get());
			Assertions.assertNotSame(Thread.currentThread(), worker.get());

			worker.get().interrupt();
			try {
				future.get();
				Assertions.fail("Did not fail chaining the InterrupedException");
			} catch (ExecutionException e) {
				// Make sure we were interrupted
				Assertions.assertTrue(InterruptedException.class.isInstance(e.getCause()));
			}
			Assertions.assertTrue(future.isDone());
			Assertions.assertTrue(called.get());
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
					Assertions.assertFalse(S.isInitialized());
					String ret = S.awaitUninterruptibly();
					Assertions.assertTrue(S.isInitialized());
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
			Assertions.assertNotNull(worker.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case BLOCKED:
					case WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assertions.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assertions.assertFalse(called.get());
			Assertions.assertNotNull(worker.get());
			Assertions.assertNotSame(Thread.currentThread(), worker.get());
			Assertions.assertSame(uuid.get(), supplier.get().get());
			Assertions.assertSame(uuid.get(), future.get());
			Assertions.assertTrue(future.isDone());
			Assertions.assertTrue(called.get());
			called.set(false);
			future = executor.submit(() -> {
				// First things first... await
				worker.set(Thread.currentThread());
				barrier.await();
				final LazySupplier<String> S = supplier.get();
				try {
					Assertions.assertTrue(S.isInitialized());
					return S.awaitUninterruptibly();
				} finally {
					called.set(true);
				}
			});
			barrier.await();
			Assertions.assertSame(uuid.get(), future.get());
			Assertions.assertTrue(future.isDone());
			Assertions.assertTrue(called.get());

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(() -> {
				throw new RuntimeException(uuid.get());
			}));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assertions.assertNotNull(worker.get());
			Thread.State state = null;
			outer: while (true) {
				state = worker.get().getState();
				inner: switch (state) {
					case BLOCKED:
					case WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assertions.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assertions.assertFalse(called.get());
			try {
				supplier.get().get();
				Assertions.fail("Did not raise an exception");
			} catch (Throwable t) {
				Assertions.assertSame(uuid.get(), t.getMessage());
			}
			Assertions.assertNotSame(Thread.State.NEW, state);
			Assertions.assertNotSame(Thread.State.RUNNABLE, state);
			Assertions.assertNotSame(Thread.State.TERMINATED, state);
			Assertions.assertNotSame(Thread.State.TIMED_WAITING, state);
			Assertions.assertFalse(called.get());
			Assertions.assertFalse(future.isDone());
			supplier.get().get(uuid::get);
			Assertions.assertSame(uuid.get(), future.get());
			Assertions.assertTrue(called.get());

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(uuid.get()));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assertions.assertNotNull(worker.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case BLOCKED:
					case WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assertions.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assertions.assertFalse(called.get());
			Assertions.assertNotNull(worker.get());
			Assertions.assertNotSame(Thread.currentThread(), worker.get());

			worker.get().interrupt(); // This should now have no effect
			Thread.sleep(100);
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case BLOCKED:
					case WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assertions.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assertions.assertFalse(future.isDone());
			Assertions.assertFalse(called.get());
			Assertions.assertSame(uuid.get(), supplier.get().get());
			Assertions.assertSame(uuid.get(), future.get());
			Assertions.assertTrue(future.isDone());
			Assertions.assertTrue(called.get());
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
					Assertions.assertFalse(S.isInitialized());
					Pair<String, Boolean> ret = S.await(to.getLeft(), to.getRight());
					Assertions.assertTrue(S.isInitialized());
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
			Assertions.assertNotNull(worker.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case TIMED_WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assertions.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Thread.sleep(100);
			Assertions.assertFalse(called.get());
			Assertions.assertNotNull(worker.get());
			Assertions.assertNotSame(Thread.currentThread(), worker.get());
			Assertions.assertSame(uuid.get(), supplier.get().get());
			futureRet = future.get();
			Assertions.assertSame(uuid.get(), futureRet.getLeft());
			Assertions.assertFalse(futureRet.getRight());
			Assertions.assertTrue(future.isDone());
			Assertions.assertTrue(called.get());
			called.set(false);
			future = executor.submit(() -> {
				// First things first... await
				worker.set(Thread.currentThread());
				barrier.await();
				final Pair<Long, TimeUnit> to = timeout.get();
				final LazySupplier<String> S = supplier.get();
				try {
					Assertions.assertTrue(S.isInitialized());
					return S.await(to.getLeft(), to.getRight());
				} finally {
					called.set(true);
				}
			});
			barrier.await();
			futureRet = future.get();
			Assertions.assertSame(uuid.get(), futureRet.getLeft());
			Assertions.assertFalse(futureRet.getRight());
			Assertions.assertTrue(future.isDone());
			Assertions.assertTrue(called.get());

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(() -> {
				throw new RuntimeException(uuid.get());
			}));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assertions.assertNotNull(worker.get());
			Thread.State state = null;
			outer: while (true) {
				state = worker.get().getState();
				inner: switch (state) {
					case TIMED_WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assertions.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assertions.assertFalse(called.get());
			try {
				supplier.get().get();
				Assertions.fail("Did not raise an exception");
			} catch (Throwable t) {
				Assertions.assertSame(uuid.get(), t.getMessage());
			}
			Assertions.assertNotSame(Thread.State.NEW, state);
			Assertions.assertNotSame(Thread.State.RUNNABLE, state);
			Assertions.assertNotSame(Thread.State.TERMINATED, state);
			Assertions.assertNotSame(Thread.State.BLOCKED, state);
			Assertions.assertNotSame(Thread.State.WAITING, state);
			Assertions.assertFalse(called.get());
			Assertions.assertFalse(future.isDone());
			Thread.sleep(100);
			supplier.get().get(uuid::get);
			futureRet = future.get();
			Assertions.assertSame(uuid.get(), futureRet.getLeft());
			Assertions.assertFalse(futureRet.getRight());
			Assertions.assertTrue(called.get());

			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(uuid.get()));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assertions.assertNotNull(worker.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case TIMED_WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assertions.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assertions.assertFalse(called.get());
			Assertions.assertNotNull(worker.get());
			Assertions.assertNotSame(Thread.currentThread(), worker.get());

			worker.get().interrupt();
			try {
				future.get();
				Assertions.fail("Did not fail chaining the InterrupedException");
			} catch (ExecutionException e) {
				// Make sure we were interrupted
				Assertions.assertTrue(InterruptedException.class.isInstance(e.getCause()));
			}
			Assertions.assertTrue(future.isDone());
			Assertions.assertTrue(called.get());

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
					Assertions.assertFalse(S.isInitialized());
					Pair<String, Boolean> ret = S.await(to.getLeft(), to.getRight());
					Assertions.assertFalse(S.isInitialized());
					return ret;
				} finally {
					called.set(true);
				}
			});
			barrier.await();
			Assertions.assertNotNull(worker.get());
			Assertions.assertNotSame(Thread.currentThread(), worker.get());
			Assertions.assertFalse(called.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case TIMED_WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assertions.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			long now = System.nanoTime();
			Thread.sleep(2100);
			long duration = (System.nanoTime() - now);
			Assertions.assertTrue(called.get());
			Assertions.assertTrue(TimeUnit.NANOSECONDS.toSeconds(duration) >= 2);
			futureRet = future.get();
			Assertions.assertTrue(futureRet.getRight());
			Assertions.assertTrue(called.get());

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
					Assertions.assertFalse(S.isInitialized());
					Pair<String, Boolean> ret = S.awaitUntil(to);
					Assertions.assertTrue(S.isInitialized());
					return ret;
				} finally {
					called.set(true);
				}
			};

			timeout.set(new Date(System.currentTimeMillis() + 5000));
			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(uuid.get()));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assertions.assertNotNull(worker.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case TIMED_WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assertions.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Thread.sleep(100);
			Assertions.assertFalse(called.get());
			Assertions.assertNotNull(worker.get());
			Assertions.assertNotSame(Thread.currentThread(), worker.get());
			Assertions.assertSame(uuid.get(), supplier.get().get());
			futureRet = future.get();
			Assertions.assertSame(uuid.get(), futureRet.getLeft());
			Assertions.assertFalse(futureRet.getRight());
			Assertions.assertTrue(future.isDone());
			Assertions.assertTrue(called.get());
			called.set(false);
			future = executor.submit(() -> {
				// First things first... await
				worker.set(Thread.currentThread());
				barrier.await();
				final Date to = timeout.get();
				final LazySupplier<String> S = supplier.get();
				try {
					Assertions.assertTrue(S.isInitialized());
					return S.awaitUntil(to);
				} finally {
					called.set(true);
				}
			});
			barrier.await();
			futureRet = future.get();
			Assertions.assertSame(uuid.get(), futureRet.getLeft());
			Assertions.assertFalse(futureRet.getRight());
			Assertions.assertTrue(future.isDone());
			Assertions.assertTrue(called.get());

			timeout.set(new Date(System.currentTimeMillis() + 5000));
			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(() -> {
				throw new RuntimeException(uuid.get());
			}));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assertions.assertNotNull(worker.get());
			Thread.State state = null;
			outer: while (true) {
				state = worker.get().getState();
				inner: switch (state) {
					case TIMED_WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assertions.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assertions.assertFalse(called.get());
			try {
				supplier.get().get();
				Assertions.fail("Did not raise an exception");
			} catch (Throwable t) {
				Assertions.assertSame(uuid.get(), t.getMessage());
			}
			Assertions.assertNotSame(Thread.State.NEW, state);
			Assertions.assertNotSame(Thread.State.RUNNABLE, state);
			Assertions.assertNotSame(Thread.State.TERMINATED, state);
			Assertions.assertNotSame(Thread.State.BLOCKED, state);
			Assertions.assertNotSame(Thread.State.WAITING, state);
			Assertions.assertFalse(called.get());
			Assertions.assertFalse(future.isDone());
			Thread.sleep(100);
			supplier.get().get(uuid::get);
			futureRet = future.get();
			Assertions.assertSame(uuid.get(), futureRet.getLeft());
			Assertions.assertFalse(futureRet.getRight());
			Assertions.assertTrue(called.get());

			timeout.set(new Date(System.currentTimeMillis() + 5000));
			uuid.set(UUID.randomUUID().toString());
			supplier.set(new LazySupplier<>(uuid.get()));
			called.set(false);

			future = executor.submit(waiter);
			barrier.await();
			Assertions.assertNotNull(worker.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case TIMED_WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assertions.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			Assertions.assertFalse(called.get());
			Assertions.assertNotNull(worker.get());
			Assertions.assertNotSame(Thread.currentThread(), worker.get());

			worker.get().interrupt();
			try {
				future.get();
				Assertions.fail("Did not fail chaining the InterrupedException");
			} catch (ExecutionException e) {
				// Make sure we were interrupted
				Assertions.assertTrue(InterruptedException.class.isInstance(e.getCause()));
			}
			Assertions.assertTrue(future.isDone());
			Assertions.assertTrue(called.get());

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
					Assertions.assertFalse(S.isInitialized());
					Pair<String, Boolean> ret = S.awaitUntil(to);
					Assertions.assertFalse(S.isInitialized());
					return ret;
				} finally {
					called.set(true);
				}
			});
			barrier.await();
			Assertions.assertNotNull(worker.get());
			Assertions.assertNotSame(Thread.currentThread(), worker.get());
			Assertions.assertFalse(called.get());
			outer: while (true) {
				inner: switch (worker.get().getState()) {
					case TIMED_WAITING:
						// We're good! it's waiting
						break outer;

					case TERMINATED:
						Assertions.fail("The waiter thread died on us");
						break inner;

					default:
						break inner;
				}
			}
			long now = System.nanoTime();
			Thread.sleep(2100);
			long duration = (System.nanoTime() - now);
			Assertions.assertTrue(called.get());
			Assertions.assertTrue(TimeUnit.NANOSECONDS.toSeconds(duration) >= 2);
			futureRet = future.get();
			Assertions.assertTrue(futureRet.getRight());
			Assertions.assertTrue(called.get());

		} finally {
			executor.shutdownNow();
			executor.awaitTermination(1, TimeUnit.MINUTES);
		}
	}

	@Test
	public void testAsInitializer() throws ConcurrentException {
		final Supplier<UUID> supplier = EasyMock.createStrictMock(Supplier.class);
		LazySupplier<UUID> lazySupplier = null;
		ConcurrentInitializer<UUID> initializer = null;

		{
			lazySupplier = new LazySupplier<>();
			initializer = lazySupplier.asInitializer();
			Assertions.assertNotNull(initializer);
			Assertions.assertNull(initializer.get());
		}

		{
			final UUID uuid = UUID.randomUUID();
			lazySupplier = new LazySupplier<>(uuid);
			initializer = lazySupplier.asInitializer();
			Assertions.assertNotNull(initializer);
			Assertions.assertSame(uuid, initializer.get());
		}

		{
			final UUID uuid = UUID.randomUUID();
			EasyMock.reset(supplier);
			EasyMock.expect(supplier.get()).andReturn(uuid).once();
			EasyMock.replay(supplier);
			lazySupplier = new LazySupplier<>(supplier);
			initializer = lazySupplier.asInitializer();
			Assertions.assertNotNull(initializer);
			Assertions.assertSame(uuid, initializer.get());
			Assertions.assertSame(uuid, initializer.get());
			EasyMock.verify(supplier);
		}

		{
			lazySupplier = new LazySupplier<>(null, null);
			initializer = lazySupplier.asInitializer();
			Assertions.assertNotNull(initializer);
			Assertions.assertNull(initializer.get());
		}

		{
			final UUID uuid = UUID.randomUUID();
			EasyMock.reset(supplier);
			EasyMock.expect(supplier.get()).andReturn(uuid).once();
			EasyMock.replay(supplier);
			lazySupplier = new LazySupplier<>(supplier, null);
			initializer = lazySupplier.asInitializer();
			Assertions.assertNotNull(initializer);
			Assertions.assertSame(uuid, initializer.get());
			Assertions.assertSame(uuid, initializer.get());
			EasyMock.verify(supplier);
		}

		{
			final UUID uuid = UUID.randomUUID();
			lazySupplier = new LazySupplier<>(null, uuid);
			initializer = lazySupplier.asInitializer();
			Assertions.assertNotNull(initializer);
			Assertions.assertSame(uuid, initializer.get());
		}

		{
			final UUID uuid = UUID.randomUUID();
			EasyMock.reset(supplier);
			EasyMock.expect(supplier.get()).andReturn(null).once();
			EasyMock.replay(supplier);
			lazySupplier = new LazySupplier<>(supplier, uuid);
			initializer = lazySupplier.asInitializer();
			Assertions.assertNotNull(initializer);
			Assertions.assertNull(initializer.get());
			Assertions.assertNull(initializer.get());
			EasyMock.verify(supplier);
		}

		{
			final UUID uuidA = UUID.randomUUID();
			final UUID uuidB = UUID.randomUUID();
			EasyMock.reset(supplier);
			EasyMock.expect(supplier.get()).andReturn(uuidA).once();
			EasyMock.replay(supplier);
			lazySupplier = new LazySupplier<>(supplier, uuidB);
			initializer = lazySupplier.asInitializer();
			Assertions.assertNotNull(initializer);
			Assertions.assertSame(uuidA, initializer.get());
			Assertions.assertSame(uuidA, initializer.get());
			EasyMock.verify(supplier);
		}
	}

	@Test
	public void testFromInitializer() throws ConcurrentException {
		ConcurrentInitializer<UUID> initializer = EasyMock.createStrictMock(ConcurrentInitializer.class);
		LazySupplier<UUID> lazySupplier = null;

		{
			lazySupplier = LazySupplier.fromInitializer(null);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertNull(lazySupplier.get());
		}

		{
			lazySupplier = LazySupplier.fromInitializer(null, null);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertNull(lazySupplier.get());
		}

		{
			final UUID uuid = UUID.randomUUID();
			lazySupplier = LazySupplier.fromInitializer(null, uuid);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertSame(uuid, lazySupplier.get());
		}

		{
			EasyMock.reset(initializer);
			EasyMock.expect(initializer.get()).andReturn(null).once();
			EasyMock.replay(initializer);
			lazySupplier = LazySupplier.fromInitializer(initializer);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertNull(lazySupplier.get());
			Assertions.assertNull(lazySupplier.get());
			EasyMock.verify(initializer);
		}

		{
			final UUID uuid = UUID.randomUUID();
			EasyMock.reset(initializer);
			EasyMock.expect(initializer.get()).andReturn(uuid).once();
			EasyMock.replay(initializer);
			lazySupplier = LazySupplier.fromInitializer(initializer);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertSame(uuid, lazySupplier.get());
			Assertions.assertSame(uuid, lazySupplier.get());
			EasyMock.verify(initializer);
		}

		{
			final IOException first = new IOException("first");
			final IOException second = new IOException("second");
			EasyMock.reset(initializer);
			EasyMock.expect(initializer.get()).andThrow(new ConcurrentException(first)).once();
			EasyMock.expect(initializer.get()).andThrow(new ConcurrentException(second)).once();
			EasyMock.replay(initializer);
			lazySupplier = LazySupplier.fromInitializer(initializer);
			Assertions.assertNotNull(lazySupplier);
			try {
				lazySupplier.get();
				Assertions.fail("Did not cascade the raised exception");
			} catch (RuntimeException e) {
				ConcurrentException ce = Tools.cast(ConcurrentException.class, e.getCause());
				Assertions.assertNotNull(ce);
				Assertions.assertSame(first, ce.getCause());
			}
			try {
				lazySupplier.get();
				Assertions.fail("Did not cascade the raised exception");
			} catch (RuntimeException e) {
				ConcurrentException ce = Tools.cast(ConcurrentException.class, e.getCause());
				Assertions.assertNotNull(ce);
				Assertions.assertSame(second, ce.getCause());
			}
			EasyMock.verify(initializer);
		}

		{
			EasyMock.reset(initializer);
			EasyMock.expect(initializer.get()).andReturn(null).once();
			EasyMock.replay(initializer);
			lazySupplier = LazySupplier.fromInitializer(initializer, null);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertNull(lazySupplier.get());
			Assertions.assertNull(lazySupplier.get());
			EasyMock.verify(initializer);
		}

		{
			final UUID uuid = UUID.randomUUID();
			EasyMock.reset(initializer);
			EasyMock.expect(initializer.get()).andReturn(uuid).once();
			EasyMock.replay(initializer);
			lazySupplier = LazySupplier.fromInitializer(initializer, null);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertSame(uuid, lazySupplier.get());
			Assertions.assertSame(uuid, lazySupplier.get());
			EasyMock.verify(initializer);
		}

		{
			final IOException first = new IOException("first");
			final IOException second = new IOException("second");
			EasyMock.reset(initializer);
			EasyMock.expect(initializer.get()).andThrow(new ConcurrentException(first)).once();
			EasyMock.expect(initializer.get()).andThrow(new ConcurrentException(second)).once();
			EasyMock.replay(initializer);
			lazySupplier = LazySupplier.fromInitializer(initializer, null);
			Assertions.assertNotNull(lazySupplier);
			try {
				lazySupplier.get();
				Assertions.fail("Did not cascade the raised exception");
			} catch (RuntimeException e) {
				ConcurrentException ce = Tools.cast(ConcurrentException.class, e.getCause());
				Assertions.assertNotNull(ce);
				Assertions.assertSame(first, ce.getCause());
			}
			try {
				lazySupplier.get();
				Assertions.fail("Did not cascade the raised exception");
			} catch (RuntimeException e) {
				ConcurrentException ce = Tools.cast(ConcurrentException.class, e.getCause());
				Assertions.assertNotNull(ce);
				Assertions.assertSame(second, ce.getCause());
			}
			EasyMock.verify(initializer);
		}

		{
			final UUID uuid = UUID.randomUUID();
			EasyMock.reset(initializer);
			EasyMock.expect(initializer.get()).andReturn(null).once();
			EasyMock.replay(initializer);
			lazySupplier = LazySupplier.fromInitializer(initializer, uuid);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertNull(lazySupplier.get());
			Assertions.assertNull(lazySupplier.get());
			EasyMock.verify(initializer);
		}

		{
			final UUID uuidA = UUID.randomUUID();
			final UUID uuidB = UUID.randomUUID();
			EasyMock.reset(initializer);
			EasyMock.expect(initializer.get()).andReturn(uuidA).once();
			EasyMock.replay(initializer);
			lazySupplier = LazySupplier.fromInitializer(initializer, uuidB);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertSame(uuidA, lazySupplier.get());
			Assertions.assertSame(uuidA, lazySupplier.get());
			EasyMock.verify(initializer);
		}

		{
			final UUID uuid = UUID.randomUUID();
			final IOException first = new IOException("first");
			final IOException second = new IOException("second");
			EasyMock.reset(initializer);
			EasyMock.expect(initializer.get()).andThrow(new ConcurrentException(first)).once();
			EasyMock.expect(initializer.get()).andThrow(new ConcurrentException(second)).once();
			EasyMock.replay(initializer);
			lazySupplier = LazySupplier.fromInitializer(initializer, uuid);
			Assertions.assertNotNull(lazySupplier);
			try {
				lazySupplier.get();
				Assertions.fail("Did not cascade the raised exception");
			} catch (RuntimeException e) {
				ConcurrentException ce = Tools.cast(ConcurrentException.class, e.getCause());
				Assertions.assertNotNull(ce);
				Assertions.assertSame(first, ce.getCause());
			}
			try {
				lazySupplier.get();
				Assertions.fail("Did not cascade the raised exception");
			} catch (RuntimeException e) {
				ConcurrentException ce = Tools.cast(ConcurrentException.class, e.getCause());
				Assertions.assertNotNull(ce);
				Assertions.assertSame(second, ce.getCause());
			}
			EasyMock.verify(initializer);
		}
	}
}
