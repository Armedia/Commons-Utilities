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

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DelayedSupplierTest {

	@Test
	public void testConstructors() throws Exception {
		{
			DelayedSupplier<Object> s = new DelayedSupplier<>();
			Assertions.assertFalse(s.isSet());
			Assertions.assertThrows(TimeoutException.class, () -> s.get(1, TimeUnit.MILLISECONDS));
		}
		{
			final Object o = new Object();
			DelayedSupplier<Object> s = new DelayedSupplier<>(o);
			Assertions.assertTrue(s.isSet());
			Assertions.assertSame(o, s.get());
		}
	}

	@Test
	public void testClear() throws Exception {
		final Object a = new Object();
		final Object b = new Object();

		DelayedSupplier<Object> s = new DelayedSupplier<>();
		Assertions.assertFalse(s.isSet());
		s.set(a);
		Assertions.assertTrue(s.isSet());
		Assertions.assertSame(a, s.get());
		Assertions.assertNotSame(b, s.get());
		s.clear();
		Assertions.assertFalse(s.isSet());
		s.set(b);
		Assertions.assertTrue(s.isSet());
		Assertions.assertSame(b, s.get());
		Assertions.assertNotSame(a, s.get());

		s = new DelayedSupplier<>(a);
		Assertions.assertTrue(s.isSet());
		Assertions.assertSame(a, s.get());
		Assertions.assertNotSame(b, s.get());
		s.clear();
		Assertions.assertFalse(s.isSet());
		s.set(b);
		Assertions.assertTrue(s.isSet());
		Assertions.assertSame(b, s.get());
		Assertions.assertNotSame(a, s.get());
	}

	@Test
	public void testIsSet() {
		DelayedSupplier<Object> s = new DelayedSupplier<>();
		Assertions.assertFalse(s.isSet());
		s.set(new Object());
		Assertions.assertTrue(s.isSet());

		s = new DelayedSupplier<>(new Object());
		Assertions.assertTrue(s.isSet());
	}

	@Test
	public void testAccept() {
		DelayedSupplier<Object> s = new DelayedSupplier<>();
		Assertions.assertFalse(s.isSet());
		s.accept(new Object());
		Assertions.assertTrue(s.isSet());
		Assertions.assertThrows(IllegalStateException.class, () -> s.accept(new Object()));
	}

	@Test
	public void testSet() {
		DelayedSupplier<Object> s = new DelayedSupplier<>();
		Assertions.assertFalse(s.isSet());
		s.set(new Object());
		Assertions.assertTrue(s.isSet());
		Assertions.assertThrows(IllegalStateException.class, () -> s.set(new Object()));
	}

	@Test
	public void testGet() throws Exception {
		final Object o1 = new Object();
		final DelayedSupplier<Object> s = new DelayedSupplier<>();
		Assertions.assertFalse(s.isSet());

		final CyclicBarrier barrier = new CyclicBarrier(3);

		final AtomicLong wA = new AtomicLong(0);
		final AtomicReference<Throwable> eA = new AtomicReference<>();
		final AtomicReference<Object> rA = new AtomicReference<>();
		final Thread tA = new Thread(() -> {
			try {
				barrier.await();
				final long start = System.nanoTime();
				try {
					rA.set(s.get());
				} finally {
					wA.set(System.nanoTime() - start);
				}
			} catch (Throwable t) {
				eA.set(t);
			}
		});
		tA.setDaemon(true);
		tA.start();

		final AtomicLong wB = new AtomicLong(0);
		final AtomicReference<Throwable> eB = new AtomicReference<>();
		final AtomicReference<Object> rB = new AtomicReference<>();
		final Thread tB = new Thread(() -> {
			try {
				barrier.await();
				final long start = System.nanoTime();
				try {
					rB.set(s.get());
				} finally {
					wB.set(System.nanoTime() - start);
				}
			} catch (Throwable t) {
				eB.set(t);
			}
		});
		tB.setDaemon(true);
		tB.start();

		// Wait for the threads to be blocked
		barrier.await();
		// Sleep a spell
		Thread.sleep(200);

		Assertions.assertSame(Thread.State.WAITING, tA.getState());
		Assertions.assertSame(Thread.State.WAITING, tB.getState());

		s.set(o1);
		tA.join(100);
		tB.join(100);
		Assertions.assertSame(o1, rA.get());
		Assertions.assertSame(o1, rB.get());
		Assertions.assertTrue(TimeUnit.NANOSECONDS.toMillis(wA.get()) >= 100);
		Assertions.assertTrue(TimeUnit.NANOSECONDS.toMillis(wB.get()) >= 100);
		Assertions.assertNull(eA.get());
		Assertions.assertNull(eB.get());
	}

	@Test
	public void testGetWithNullTimeouts() throws Exception {
		final Object o1 = new Object();
		final DelayedSupplier<Object> s = new DelayedSupplier<>();
		Assertions.assertFalse(s.isSet());

		final CyclicBarrier barrier = new CyclicBarrier(3);

		final AtomicLong wA = new AtomicLong(0);
		final AtomicReference<Throwable> eA = new AtomicReference<>();
		final AtomicReference<Object> rA = new AtomicReference<>();
		final Thread tA = new Thread(() -> {
			try {
				barrier.await();
				final long start = System.nanoTime();
				try {
					rA.set(s.get(0, TimeUnit.SECONDS));
				} finally {
					wA.set(System.nanoTime() - start);
				}
			} catch (Throwable t) {
				eA.set(t);
			}
		});
		tA.setDaemon(true);
		tA.start();

		final AtomicLong wB = new AtomicLong(0);
		final AtomicReference<Throwable> eB = new AtomicReference<>();
		final AtomicReference<Object> rB = new AtomicReference<>();
		final Thread tB = new Thread(() -> {
			try {
				barrier.await();
				final long start = System.nanoTime();
				try {
					rB.set(s.get(-1, null));
				} finally {
					wB.set(System.nanoTime() - start);
				}
			} catch (Throwable t) {
				eB.set(t);
			}
		});
		tB.setDaemon(true);
		tB.start();

		// Wait for the threads to be blocked
		barrier.await();
		// Sleep a spell
		Thread.sleep(200);

		Assertions.assertSame(Thread.State.WAITING, tA.getState());
		Assertions.assertSame(Thread.State.WAITING, tB.getState());

		s.set(o1);
		tA.join(100);
		tB.join(100);
		Assertions.assertSame(o1, rA.get());
		Assertions.assertSame(o1, rB.get());
		Assertions.assertTrue(TimeUnit.NANOSECONDS.toMillis(wA.get()) >= 100);
		Assertions.assertTrue(TimeUnit.NANOSECONDS.toMillis(wB.get()) >= 100);
		Assertions.assertNull(eA.get());
		Assertions.assertNull(eB.get());
	}

	@Test
	public void testGetTimeout() throws Exception {
		final Object o1 = new Object();
		final DelayedSupplier<Object> s = new DelayedSupplier<>();
		Assertions.assertFalse(s.isSet());

		final CyclicBarrier barrier = new CyclicBarrier(3);

		final AtomicLong wA = new AtomicLong(0);
		final AtomicReference<Throwable> eA = new AtomicReference<>();
		final AtomicReference<Object> rA = new AtomicReference<>();
		final Thread tA = new Thread(() -> {
			try {
				barrier.await();
				final long start = System.nanoTime();
				try {
					rA.set(s.get(100, TimeUnit.MILLISECONDS));
				} finally {
					wA.set(System.nanoTime() - start);
				}
			} catch (Throwable t) {
				eA.set(t);
			}
		});
		tA.setDaemon(true);
		tA.start();

		final AtomicLong wB = new AtomicLong(0);
		final AtomicReference<Throwable> eB = new AtomicReference<>();
		final AtomicReference<Object> rB = new AtomicReference<>();
		final Thread tB = new Thread(() -> {
			try {
				barrier.await();
				final long start = System.nanoTime();
				try {
					rB.set(s.get(100, TimeUnit.MILLISECONDS));
				} finally {
					wB.set(System.nanoTime() - start);
				}
			} catch (Throwable t) {
				eB.set(t);
			}
		});
		tB.setDaemon(true);
		tB.start();

		// Wait for the threads to be blocked
		barrier.await();
		// Sleep a spell to let the threads time out
		Thread.sleep(200);

		s.set(o1);
		tA.join(100);
		tB.join(100);
		Assertions.assertNull(rA.get());
		Assertions.assertNull(rB.get());
		Assertions.assertNotNull(eA.get());
		Assertions.assertNotNull(eB.get());
		Assertions.assertSame(TimeoutException.class, eA.get().getClass());
		Assertions.assertSame(TimeoutException.class, eB.get().getClass());
	}

	@Test
	public void testGetNoTimeout() throws Exception {
		final Object o1 = new Object();
		final DelayedSupplier<Object> s = new DelayedSupplier<>();
		Assertions.assertFalse(s.isSet());

		final CyclicBarrier barrier = new CyclicBarrier(3);

		final AtomicLong wA = new AtomicLong(0);
		final AtomicReference<Throwable> eA = new AtomicReference<>();
		final AtomicReference<Object> rA = new AtomicReference<>();
		final Thread tA = new Thread(() -> {
			try {
				barrier.await();
				final long start = System.nanoTime();
				try {
					rA.set(s.get(1000, TimeUnit.MILLISECONDS));
				} finally {
					wA.set(System.nanoTime() - start);
				}
			} catch (Throwable t) {
				eA.set(t);
			}
		});
		tA.setDaemon(true);
		tA.start();

		final AtomicLong wB = new AtomicLong(0);
		final AtomicReference<Throwable> eB = new AtomicReference<>();
		final AtomicReference<Object> rB = new AtomicReference<>();
		final Thread tB = new Thread(() -> {
			try {
				barrier.await();
				final long start = System.nanoTime();
				try {
					rB.set(s.get(1000, TimeUnit.MILLISECONDS));
				} finally {
					wB.set(System.nanoTime() - start);
				}
			} catch (Throwable t) {
				eB.set(t);
			}
		});
		tB.setDaemon(true);
		tB.start();

		// Wait for the threads to be blocked
		barrier.await();
		// Sleep a spell to let things settle down
		Thread.sleep(200);

		Assertions.assertSame(Thread.State.TIMED_WAITING, tA.getState());
		Assertions.assertSame(Thread.State.TIMED_WAITING, tB.getState());

		s.set(o1);
		tA.join(100);
		tB.join(100);
		Assertions.assertSame(o1, rA.get());
		Assertions.assertSame(o1, rB.get());
		Assertions.assertTrue(TimeUnit.NANOSECONDS.toMillis(wA.get()) >= 100);
		Assertions.assertTrue(TimeUnit.NANOSECONDS.toMillis(wB.get()) >= 100);
		Assertions.assertNull(eA.get());
		Assertions.assertNull(eB.get());
	}

}
