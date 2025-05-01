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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckedRunnableTest {

	@Test
	public void testRunChecked() throws Throwable {
		CheckedRunnable<Throwable> c = null;

		final AtomicLong callCount = new AtomicLong(0);

		c = () -> callCount.incrementAndGet();
		callCount.set(0);
		c.runChecked();
		Assertions.assertEquals(1, callCount.get());

		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		c = () -> {
			callCount.incrementAndGet();
			throw thrown;
		};
		callCount.set(0);
		try {
			c.runChecked();
			Assertions.fail("Did not raise the cascaded exception");
		} catch (Throwable t) {
			Assertions.assertSame(thrown, t);
		}
		Assertions.assertEquals(1, callCount.get());
	}

	@Test
	public void testRun() {
		CheckedRunnable<Throwable> c = null;

		final AtomicLong callCount = new AtomicLong(0);

		c = () -> callCount.incrementAndGet();
		callCount.set(0);
		c.run();
		Assertions.assertEquals(1, callCount.get());

		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		c = () -> {
			callCount.incrementAndGet();
			throw thrown;
		};
		callCount.set(0);
		try {
			c.run();
			Assertions.fail("Did not raise the cascaded exception");
		} catch (RuntimeException e) {
			Assertions.assertNotSame(thrown, e);
			Assertions.assertEquals(thrown.getMessage(), e.getMessage());
			Assertions.assertSame(thrown, e.getCause());
		}
		Assertions.assertEquals(1, callCount.get());
	}

	@Test
	public void testAndThenChecked() throws Throwable {
		CheckedRunnable<Throwable> a = null;
		CheckedRunnable<Throwable> b = null;

		final List<String> callers = new ArrayList<>();

		a = () -> callers.add("a");
		b = a.andThen((Runnable) () -> callers.add("b"));
		callers.clear();
		b.runChecked();
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			callers.clear();
			a = () -> {
				callers.add("a");
				throw thrown;
			};
			b = a.andThen((Runnable) () -> callers.add("b"));
			try {
				b.runChecked();
				Assertions.fail("Did not raise the chained exception");
			} catch (Throwable t) {
				Assertions.assertSame(thrown, t);
			}
			Assertions.assertEquals(1, callers.size());
			Assertions.assertArrayEquals(new String[] {
				"a",
			}, callers.toArray());
		}

		{
			final RuntimeException thrown = new RuntimeException(UUID.randomUUID().toString());
			callers.clear();
			a = () -> callers.add("a");
			b = a.andThen((Runnable) () -> {
				callers.add("b");
				throw thrown;
			});
			try {
				b.runChecked();
				Assertions.fail("Did not raise the chained exception");
			} catch (Throwable t) {
				Assertions.assertSame(thrown, t);
			}
			Assertions.assertEquals(2, callers.size());
			Assertions.assertArrayEquals(new String[] {
				"a", "b"
			}, callers.toArray());
		}

		{
			CheckedRunnable<Throwable> n = () -> {
			};

			Assertions.assertThrows(NullPointerException.class, () -> n.andThen((Runnable) null));
		}
	}

	@Test
	public void testAndThen() throws Throwable {
		CheckedRunnable<Throwable> a = null;
		CheckedRunnable<Throwable> b = null;

		final List<String> callers = new ArrayList<>();

		a = () -> callers.add("a");
		b = a.andThen(() -> callers.add("b"));
		callers.clear();
		b.runChecked();
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			callers.clear();
			a = () -> {
				callers.add("a");
				throw thrown;
			};
			b = a.andThen(() -> callers.add("b"));
			try {
				b.runChecked();
				Assertions.fail("Did not raise the chained exception");
			} catch (Throwable t) {
				Assertions.assertSame(thrown, t);
			}
			Assertions.assertEquals(1, callers.size());
			Assertions.assertArrayEquals(new String[] {
				"a",
			}, callers.toArray());
		}

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			callers.clear();
			a = () -> callers.add("a");
			b = a.andThen(() -> {
				callers.add("b");
				throw thrown;
			});
			try {
				b.runChecked();
				Assertions.fail("Did not raise the chained exception");
			} catch (Throwable t) {
				Assertions.assertSame(thrown, t);
			}
			Assertions.assertEquals(2, callers.size());
			Assertions.assertArrayEquals(new String[] {
				"a", "b"
			}, callers.toArray());
		}

		{
			CheckedRunnable<Throwable> n = () -> {
			};

			Assertions.assertThrows(NullPointerException.class, () -> n.andThen(null));
		}
	}
}
