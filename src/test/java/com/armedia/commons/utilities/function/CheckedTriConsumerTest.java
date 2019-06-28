/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (C) 2013 - 2019 Armedia
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

public class CheckedTriConsumerTest {

	@Test
	public void testAcceptChecked() throws Throwable {
		CheckedTriConsumer<String, Double, UUID, Throwable> c = null;

		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);
		final AtomicReference<UUID> uuid = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		uuid.set(UUID.randomUUID());
		c = (s, n, u) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(uuid.get(), u);
		};
		c.acceptChecked(string.get(), number.get(), uuid.get());

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		uuid.set(UUID.randomUUID());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		c = (s, n, u) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(uuid.get(), u);
			throw thrown;
		};
		try {
			c.acceptChecked(string.get(), number.get(), uuid.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (Throwable t) {
			Assertions.assertSame(thrown, t);
		}
	}

	@Test
	public void testAccept() {
		CheckedTriConsumer<String, Double, UUID, Throwable> c = null;

		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);
		final AtomicReference<UUID> uuid = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		uuid.set(UUID.randomUUID());
		c = (s, n, u) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(uuid.get(), u);
		};
		c.accept(string.get(), number.get(), uuid.get());

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		uuid.set(UUID.randomUUID());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		c = (s, n, u) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(uuid.get(), u);
			throw thrown;
		};
		try {
			c.accept(string.get(), number.get(), uuid.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (RuntimeException e) {
			Assertions.assertNotSame(thrown, e);
			Assertions.assertEquals(thrown.getMessage(), e.getMessage());
			Assertions.assertSame(thrown, e.getCause());
		}
	}

	@Test
	public void testAndThen() throws Throwable {
		CheckedTriConsumer<String, Double, UUID, Throwable> a = null;
		CheckedTriConsumer<String, Double, UUID, Throwable> b = null;

		final List<String> callers = new ArrayList<>();
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);
		final AtomicReference<UUID> uuid = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		uuid.set(UUID.randomUUID());
		a = (s, n, u) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(uuid.get(), u);
			callers.add("a");
		};
		b = a.andThen((s, n, u) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(uuid.get(), u);
			callers.add("b");
		});
		b.acceptChecked(string.get(), number.get(), uuid.get());
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			callers.clear();
			string.set(UUID.randomUUID().toString());
			number.set(Math.random());
			uuid.set(UUID.randomUUID());
			a = (s, n, u) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				Assertions.assertSame(uuid.get(), u);
				callers.add("a");
				throw thrown;
			};
			b = a.andThen((s, n, u) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				Assertions.assertSame(uuid.get(), u);
				callers.add("b");
			});
			try {
				b.acceptChecked(string.get(), number.get(), uuid.get());
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
			uuid.set(UUID.randomUUID());
			a = (s, n, u) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				Assertions.assertSame(uuid.get(), u);
				callers.add("a");
			};
			b = a.andThen((s, n, u) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				Assertions.assertSame(uuid.get(), u);
				callers.add("b");
				throw thrown;
			});
			try {
				b.acceptChecked(string.get(), number.get(), uuid.get());
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
			CheckedTriConsumer<String, Double, UUID, Throwable> n = (x, y, z) -> {
			};

			Assertions.assertThrows(NullPointerException.class, () -> n.andThen(null));
		}
	}

}
