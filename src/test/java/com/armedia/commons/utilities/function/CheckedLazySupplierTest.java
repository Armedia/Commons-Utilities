/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2025 Armedia, LLC
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

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.ConcurrentInitializer;
import org.apache.commons.lang3.tuple.Pair;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckedLazySupplierTest {

	private static final CheckedSupplier<String, Exception> FAIL_SUPP = () -> {
		Assertions.fail("This supplier should not have been invoked");
		return null;
	};

	@Test
	public void testCheckedLazySupplier() {
		CheckedLazySupplier<String, Exception> supplier = new CheckedLazySupplier<>();
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertNull(supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertNull(supplier.get(CheckedLazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testCheckedLazySupplierCheckedSupplierOfT() {
		final String uuid = UUID.randomUUID().toString();
		final CheckedSupplier<String, Exception> uuidSupplier = () -> uuid;
		CheckedLazySupplier<String, Exception> supplier = null;

		supplier = new CheckedLazySupplier<>(uuidSupplier);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = new CheckedLazySupplier<>((CheckedSupplier<String, Exception>) null);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertNull(supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertNull(supplier.get(CheckedLazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testCheckedLazySupplierT() {
		final String uuid = UUID.randomUUID().toString();
		CheckedLazySupplier<String, Exception> supplier = null;

		supplier = new CheckedLazySupplier<>(uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = new CheckedLazySupplier<>((String) null);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertNull(supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertNull(supplier.get(CheckedLazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testCheckedLazySupplierCheckedSupplierOfTT() {
		String uuid = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		CheckedSupplier<String, Exception> uuidSupplier = () -> uuid2;
		CheckedLazySupplier<String, Exception> supplier = null;

		supplier = new CheckedLazySupplier<>(null, null);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertNull(supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertNull(supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = new CheckedLazySupplier<>(uuidSupplier, null);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = new CheckedLazySupplier<>(null, uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = new CheckedLazySupplier<>(uuidSupplier, uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testGet() {
		String uuid = UUID.randomUUID().toString();
		CheckedLazySupplier<String, Exception> supplier = null;

		supplier = new CheckedLazySupplier<>();
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertNull(supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertNull(supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = new CheckedLazySupplier<>(uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = new CheckedLazySupplier<>(() -> uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = new CheckedLazySupplier<>(() -> {
			throw new Exception(uuid);
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
		CheckedLazySupplier<String, Exception> supplier = null;

		supplier = new CheckedLazySupplier<>();
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(() -> uuid));
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = new CheckedLazySupplier<>(uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get(() -> uuid2));
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = new CheckedLazySupplier<>(uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(null));
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = new CheckedLazySupplier<>();
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		try {
			supplier.get(() -> {
				throw new RuntimeException(uuid);
			});
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
	public void testGetChecked() throws Exception {
		String uuid = UUID.randomUUID().toString();
		CheckedLazySupplier<String, Exception> supplier = null;

		supplier = new CheckedLazySupplier<>();
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertNull(supplier.getChecked());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertNull(supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = new CheckedLazySupplier<>(uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.getChecked());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = new CheckedLazySupplier<>(() -> uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.getChecked());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = new CheckedLazySupplier<>(() -> {
			throw new Exception(uuid);
		});
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		try {
			supplier.getChecked();
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
	public void testGetCheckedSupplierOfT() throws Exception {
		String uuid = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		CheckedLazySupplier<String, Exception> supplier = null;

		supplier = new CheckedLazySupplier<>();
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(() -> uuid));
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = new CheckedLazySupplier<>(uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get(() -> uuid2));
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = new CheckedLazySupplier<>(uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(null));
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = new CheckedLazySupplier<>();
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		try {
			supplier.getChecked(() -> {
				throw new Exception(uuid);
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
		final CheckedSupplier<String, Exception> uuidSupplier = () -> uuid;
		CheckedLazySupplier<String, Exception> supplier = null;

		supplier = CheckedLazySupplier.from(uuidSupplier);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = CheckedLazySupplier.from(null);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertNull(supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertNull(supplier.get(CheckedLazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testFromSupplierSupplierOfTT() {
		String uuid = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		CheckedSupplier<String, Exception> uuidSupplier = () -> uuid2;
		CheckedLazySupplier<String, Exception> supplier = null;

		supplier = CheckedLazySupplier.from(null, null);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertNull(supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertNull(supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = CheckedLazySupplier.from(uuidSupplier, null);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = CheckedLazySupplier.from(null, uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertTrue(supplier.isDefaulted());
		Assertions.assertSame(uuid, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));

		supplier = CheckedLazySupplier.from(uuidSupplier, uuid);
		Assertions.assertFalse(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get());
		Assertions.assertTrue(supplier.isInitialized());
		Assertions.assertFalse(supplier.isDefaulted());
		Assertions.assertSame(uuid2, supplier.get(CheckedLazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testAwait() throws Exception {
		final CyclicBarrier barrier = new CyclicBarrier(2);
		final AtomicReference<CheckedLazySupplier<String, Exception>> supplier = new AtomicReference<>();
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
				final CheckedLazySupplier<String, Exception> S = supplier.get();
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
			supplier.set(new CheckedLazySupplier<>(uuid.get()));
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
				final CheckedLazySupplier<String, Exception> S = supplier.get();
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
			supplier.set(new CheckedLazySupplier<>(() -> {
				throw new Exception(uuid.get());
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
				supplier.get().getChecked();
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
			supplier.set(new CheckedLazySupplier<>(uuid.get()));
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
		final AtomicReference<CheckedLazySupplier<String, Exception>> supplier = new AtomicReference<>();
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
				final CheckedLazySupplier<String, Exception> S = supplier.get();
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
			supplier.set(new CheckedLazySupplier<>(uuid.get()));
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
				final CheckedLazySupplier<String, Exception> S = supplier.get();
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
			supplier.set(new CheckedLazySupplier<>(() -> {
				throw new Exception(uuid.get());
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
				supplier.get().getChecked();
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
			supplier.set(new CheckedLazySupplier<>(uuid.get()));
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
		final AtomicReference<CheckedLazySupplier<String, Exception>> supplier = new AtomicReference<>();
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
				final CheckedLazySupplier<String, Exception> S = supplier.get();
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
			supplier.set(new CheckedLazySupplier<>(uuid.get()));
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
				final CheckedLazySupplier<String, Exception> S = supplier.get();
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
			supplier.set(new CheckedLazySupplier<>(() -> {
				throw new Exception(uuid.get());
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
				supplier.get().getChecked();
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
			supplier.set(new CheckedLazySupplier<>(uuid.get()));
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
			supplier.set(new CheckedLazySupplier<>(uuid.get()));
			called.set(false);

			future = executor.submit(() -> {
				// First things first... await
				worker.set(Thread.currentThread());
				barrier.await();
				final Pair<Long, TimeUnit> to = timeout.get();
				final CheckedLazySupplier<String, Exception> S = supplier.get();
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
		final AtomicReference<CheckedLazySupplier<String, Exception>> supplier = new AtomicReference<>();
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
				final CheckedLazySupplier<String, Exception> S = supplier.get();
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
			supplier.set(new CheckedLazySupplier<>(uuid.get()));
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
				final CheckedLazySupplier<String, Exception> S = supplier.get();
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
			supplier.set(new CheckedLazySupplier<>(() -> {
				throw new Exception(uuid.get());
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
				supplier.get().getChecked();
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
			supplier.set(new CheckedLazySupplier<>(uuid.get()));
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
			supplier.set(new CheckedLazySupplier<>(uuid.get()));
			called.set(false);

			future = executor.submit(() -> {
				// First things first... await
				worker.set(Thread.currentThread());
				barrier.await();
				final Date to = timeout.get();
				final CheckedLazySupplier<String, Exception> S = supplier.get();
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
	public void testAsInitializer() throws Exception {
		final CheckedSupplier<UUID, Exception> supplier = EasyMock.createStrictMock(CheckedSupplier.class);
		CheckedLazySupplier<UUID, Exception> lazySupplier = null;
		ConcurrentInitializer<UUID> initializer = null;

		{
			lazySupplier = new CheckedLazySupplier<>();
			initializer = lazySupplier.asInitializer();
			Assertions.assertNotNull(initializer);
			Assertions.assertNull(initializer.get());
		}

		{
			final UUID uuid = UUID.randomUUID();
			lazySupplier = new CheckedLazySupplier<>(uuid);
			initializer = lazySupplier.asInitializer();
			Assertions.assertNotNull(initializer);
			Assertions.assertSame(uuid, initializer.get());
		}

		{
			final UUID uuid = UUID.randomUUID();
			EasyMock.reset(supplier);
			EasyMock.expect(supplier.getChecked()).andReturn(uuid).once();
			EasyMock.replay(supplier);
			lazySupplier = new CheckedLazySupplier<>(supplier);
			initializer = lazySupplier.asInitializer();
			Assertions.assertNotNull(initializer);
			Assertions.assertSame(uuid, initializer.get());
			Assertions.assertSame(uuid, initializer.get());
			EasyMock.verify(supplier);
		}

		{
			final Exception first = new Exception("first");
			final Exception second = new Exception("second");
			EasyMock.reset(supplier);
			EasyMock.expect(supplier.getChecked()).andThrow(first).once();
			EasyMock.expect(supplier.getChecked()).andThrow(second).once();
			EasyMock.replay(supplier);
			lazySupplier = new CheckedLazySupplier<>(supplier);
			initializer = lazySupplier.asInitializer();
			Assertions.assertNotNull(initializer);
			try {
				initializer.get();
			} catch (ConcurrentException ce) {
				Assertions.assertSame(first, ce.getCause());
			}
			try {
				initializer.get();
			} catch (ConcurrentException ce) {
				Assertions.assertSame(second, ce.getCause());
			}
			EasyMock.verify(supplier);
		}

		{
			lazySupplier = new CheckedLazySupplier<>(null, null);
			initializer = lazySupplier.asInitializer();
			Assertions.assertNotNull(initializer);
			Assertions.assertNull(initializer.get());
		}

		{
			final UUID uuid = UUID.randomUUID();
			EasyMock.reset(supplier);
			EasyMock.expect(supplier.getChecked()).andReturn(uuid).once();
			EasyMock.replay(supplier);
			lazySupplier = new CheckedLazySupplier<>(supplier, null);
			initializer = lazySupplier.asInitializer();
			Assertions.assertNotNull(initializer);
			Assertions.assertSame(uuid, initializer.get());
			Assertions.assertSame(uuid, initializer.get());
			EasyMock.verify(supplier);
		}

		{
			final UUID uuid = UUID.randomUUID();
			lazySupplier = new CheckedLazySupplier<>(null, uuid);
			initializer = lazySupplier.asInitializer();
			Assertions.assertNotNull(initializer);
			Assertions.assertSame(uuid, initializer.get());
		}

		{
			final UUID uuid = UUID.randomUUID();
			EasyMock.reset(supplier);
			EasyMock.expect(supplier.getChecked()).andReturn(null).once();
			EasyMock.replay(supplier);
			lazySupplier = new CheckedLazySupplier<>(supplier, uuid);
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
			EasyMock.expect(supplier.getChecked()).andReturn(uuidA).once();
			EasyMock.replay(supplier);
			lazySupplier = new CheckedLazySupplier<>(supplier, uuidB);
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
		CheckedLazySupplier<UUID, ConcurrentException> lazySupplier = null;

		{
			lazySupplier = CheckedLazySupplier.fromInitializer(null);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertNull(lazySupplier.getChecked());
		}

		{
			lazySupplier = CheckedLazySupplier.fromInitializer(null, null);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertNull(lazySupplier.getChecked());
		}

		{
			final UUID uuid = UUID.randomUUID();
			lazySupplier = CheckedLazySupplier.fromInitializer(null, uuid);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertSame(uuid, lazySupplier.getChecked());
		}

		{
			EasyMock.reset(initializer);
			EasyMock.expect(initializer.get()).andReturn(null).once();
			EasyMock.replay(initializer);
			lazySupplier = CheckedLazySupplier.fromInitializer(initializer);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertNull(lazySupplier.getChecked());
			Assertions.assertNull(lazySupplier.getChecked());
			EasyMock.verify(initializer);
		}

		{
			final UUID uuid = UUID.randomUUID();
			EasyMock.reset(initializer);
			EasyMock.expect(initializer.get()).andReturn(uuid).once();
			EasyMock.replay(initializer);
			lazySupplier = CheckedLazySupplier.fromInitializer(initializer);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertSame(uuid, lazySupplier.getChecked());
			Assertions.assertSame(uuid, lazySupplier.getChecked());
			EasyMock.verify(initializer);
		}

		{
			final IOException first = new IOException("first");
			final IOException second = new IOException("second");
			EasyMock.reset(initializer);
			EasyMock.expect(initializer.get()).andThrow(new ConcurrentException(first)).once();
			EasyMock.expect(initializer.get()).andThrow(new ConcurrentException(second)).once();
			EasyMock.replay(initializer);
			lazySupplier = CheckedLazySupplier.fromInitializer(initializer);
			Assertions.assertNotNull(lazySupplier);
			try {
				lazySupplier.getChecked();
				Assertions.fail("Did not cascade the raised exception");
			} catch (ConcurrentException ce) {
				Assertions.assertSame(first, ce.getCause());
			}
			try {
				lazySupplier.getChecked();
				Assertions.fail("Did not cascade the raised exception");
			} catch (ConcurrentException ce) {
				Assertions.assertSame(second, ce.getCause());
			}
			EasyMock.verify(initializer);
		}

		{
			EasyMock.reset(initializer);
			EasyMock.expect(initializer.get()).andReturn(null).once();
			EasyMock.replay(initializer);
			lazySupplier = CheckedLazySupplier.fromInitializer(initializer, null);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertNull(lazySupplier.getChecked());
			Assertions.assertNull(lazySupplier.getChecked());
			EasyMock.verify(initializer);
		}

		{
			final UUID uuid = UUID.randomUUID();
			EasyMock.reset(initializer);
			EasyMock.expect(initializer.get()).andReturn(uuid).once();
			EasyMock.replay(initializer);
			lazySupplier = CheckedLazySupplier.fromInitializer(initializer, null);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertSame(uuid, lazySupplier.getChecked());
			Assertions.assertSame(uuid, lazySupplier.getChecked());
			EasyMock.verify(initializer);
		}

		{
			final IOException first = new IOException("first");
			final IOException second = new IOException("second");
			EasyMock.reset(initializer);
			EasyMock.expect(initializer.get()).andThrow(new ConcurrentException(first)).once();
			EasyMock.expect(initializer.get()).andThrow(new ConcurrentException(second)).once();
			EasyMock.replay(initializer);
			lazySupplier = CheckedLazySupplier.fromInitializer(initializer, null);
			Assertions.assertNotNull(lazySupplier);
			try {
				lazySupplier.getChecked();
				Assertions.fail("Did not cascade the raised exception");
			} catch (ConcurrentException ce) {
				Assertions.assertSame(first, ce.getCause());
			}
			try {
				lazySupplier.getChecked();
				Assertions.fail("Did not cascade the raised exception");
			} catch (ConcurrentException ce) {
				Assertions.assertSame(second, ce.getCause());
			}
			EasyMock.verify(initializer);
		}

		{
			final UUID uuid = UUID.randomUUID();
			EasyMock.reset(initializer);
			EasyMock.expect(initializer.get()).andReturn(null).once();
			EasyMock.replay(initializer);
			lazySupplier = CheckedLazySupplier.fromInitializer(initializer, uuid);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertNull(lazySupplier.getChecked());
			Assertions.assertNull(lazySupplier.getChecked());
			EasyMock.verify(initializer);
		}

		{
			final UUID uuidA = UUID.randomUUID();
			final UUID uuidB = UUID.randomUUID();
			EasyMock.reset(initializer);
			EasyMock.expect(initializer.get()).andReturn(uuidA).once();
			EasyMock.replay(initializer);
			lazySupplier = CheckedLazySupplier.fromInitializer(initializer, uuidB);
			Assertions.assertNotNull(lazySupplier);
			Assertions.assertSame(uuidA, lazySupplier.getChecked());
			Assertions.assertSame(uuidA, lazySupplier.getChecked());
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
			lazySupplier = CheckedLazySupplier.fromInitializer(initializer, uuid);
			Assertions.assertNotNull(lazySupplier);
			try {
				lazySupplier.getChecked();
				Assertions.fail("Did not cascade the raised exception");
			} catch (ConcurrentException ce) {
				Assertions.assertSame(first, ce.getCause());
			}
			try {
				lazySupplier.getChecked();
				Assertions.fail("Did not cascade the raised exception");
			} catch (ConcurrentException ce) {
				Assertions.assertSame(second, ce.getCause());
			}
			EasyMock.verify(initializer);
		}
	}
}
