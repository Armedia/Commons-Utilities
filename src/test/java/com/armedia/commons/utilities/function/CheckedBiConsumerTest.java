/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2019 Armedia, LLC
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
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckedBiConsumerTest {

	@Test
	public void testAcceptChecked() throws Throwable {
		CheckedBiConsumer<String, Number, Throwable> c = null;

		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Number> number = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		c = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
		};
		c.acceptChecked(string.get(), number.get());

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		c = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			throw thrown;
		};
		try {
			c.acceptChecked(string.get(), number.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (Throwable t) {
			Assertions.assertSame(thrown, t);
		}
	}

	@Test
	public void testAccept() {
		CheckedBiConsumer<String, Number, Throwable> c = null;

		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Number> number = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		c = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
		};
		c.accept(string.get(), number.get());

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		c = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			throw thrown;
		};
		try {
			c.accept(string.get(), number.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (RuntimeException e) {
			Assertions.assertNotSame(thrown, e);
			Assertions.assertEquals(thrown.getMessage(), e.getMessage());
			Assertions.assertSame(thrown, e.getCause());
		}
	}

	@Test
	public void testAndThen() throws Throwable {
		CheckedBiConsumer<String, Number, Throwable> a = null;
		CheckedBiConsumer<String, Number, Throwable> b = null;

		final List<String> callers = new ArrayList<>();
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Number> number = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		a = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("a");
		};

		b = a.andThen((s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("b");
		});
		b.acceptChecked(string.get(), number.get());
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			callers.clear();
			string.set(UUID.randomUUID().toString());
			number.set(Math.random());
			a = (s, n) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				callers.add("a");
				throw thrown;
			};
			b = a.andThen((s, n) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				callers.add("b");
			});
			try {
				b.acceptChecked(string.get(), number.get());
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
			string.set(UUID.randomUUID().toString());
			number.set(Math.random());
			a = (s, n) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				callers.add("a");
			};
			b = a.andThen((s, n) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				callers.add("b");
				throw thrown;
			});
			try {
				b.acceptChecked(string.get(), number.get());
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
			CheckedBiConsumer<String, Number, Throwable> n = (x, y) -> {
			};

			Assertions.assertThrows(NullPointerException.class, () -> n.andThen(null));
		}
	}

}
