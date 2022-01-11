/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2022 Armedia, LLC
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
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckedFunctionTest {

	@Test
	public void testApplyChecked() throws Throwable {
		CheckedFunction<String, UUID, Throwable> f = null;

		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<UUID> uuid = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		uuid.set(UUID.randomUUID());
		f = (s) -> {
			Assertions.assertSame(string.get(), s);
			return uuid.get();
		};
		Assertions.assertSame(uuid.get(), f.applyChecked(string.get()));

		string.set(UUID.randomUUID().toString());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		f = (s) -> {
			Assertions.assertSame(string.get(), s);
			throw thrown;
		};
		try {
			f.applyChecked(string.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (Throwable t) {
			Assertions.assertSame(thrown, t);
		}
	}

	@Test
	public void testApply() {
		CheckedFunction<String, UUID, Throwable> f = null;

		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<UUID> uuid = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		uuid.set(UUID.randomUUID());
		f = (s) -> {
			Assertions.assertSame(string.get(), s);
			return uuid.get();
		};
		Assertions.assertSame(uuid.get(), f.apply(string.get()));

		string.set(UUID.randomUUID().toString());
		uuid.set(UUID.randomUUID());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		f = (s) -> {
			Assertions.assertSame(string.get(), s);
			throw thrown;
		};
		try {
			f.apply(string.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (RuntimeException e) {
			Assertions.assertNotSame(thrown, e);
			Assertions.assertEquals(thrown.getMessage(), e.getMessage());
			Assertions.assertSame(thrown, e.getCause());
		}
	}

	@Test
	public void testCheckedIdentity() {
		CheckedFunction<String, String, Throwable> f = null;

		final AtomicReference<String> string = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		f = CheckedFunction.checkedIdentity();
		Assertions.assertSame(string.get(), f.apply(string.get()));

		{
			CheckedFunction<String, String, Throwable> a = CheckedFunction.checkedIdentity();
			CheckedFunction<Long, Long, Throwable> b = CheckedFunction.checkedIdentity();
			Assertions.assertSame(a, b);
		}
	}

	@Test
	public void testAndThen() throws Throwable {
		CheckedFunction<String, UUID, Throwable> a = null;
		CheckedFunction<String, Long, Throwable> b = null;

		final List<String> callers = new ArrayList<>();
		final Random r = new Random(System.nanoTime());
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<UUID> uuid = new AtomicReference<>(null);
		final AtomicReference<Long> l = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		uuid.set(UUID.randomUUID());
		l.set(r.nextLong());
		a = (s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("a");
			return uuid.get();
		};

		b = a.andThen((u) -> {
			Assertions.assertSame(uuid.get(), u);
			callers.add("b");
			return l.get();
		});
		Assertions.assertSame(l.get(), b.applyChecked(string.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			callers.clear();
			string.set(UUID.randomUUID().toString());
			uuid.set(UUID.randomUUID());
			l.set(r.nextLong());
			a = (s) -> {
				Assertions.assertSame(string.get(), s);
				callers.add("a");
				throw thrown;
			};
			b = a.andThen((u) -> {
				Assertions.assertSame(uuid.get(), u);
				callers.add("b");
				return l.get();
			});
			try {
				b.applyChecked(string.get());
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
			uuid.set(UUID.randomUUID());
			l.set(r.nextLong());
			a = (s) -> {
				Assertions.assertSame(string.get(), s);
				callers.add("a");
				return uuid.get();
			};
			b = a.andThen((u) -> {
				Assertions.assertSame(uuid.get(), u);
				callers.add("b");
				throw thrown;
			});
			try {
				b.applyChecked(string.get());
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
			CheckedFunction<String, UUID, Throwable> n = (x) -> null;
			Assertions.assertThrows(NullPointerException.class, () -> n.andThen(null));
		}
	}

	@Test
	public void testCompose() throws Throwable {
		CheckedFunction<String, UUID, Throwable> a = null;
		CheckedFunction<Long, UUID, Throwable> b = null;

		final List<String> callers = new ArrayList<>();
		final Random r = new Random(System.nanoTime());
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<UUID> uuid = new AtomicReference<>(null);
		final AtomicReference<Long> l = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		uuid.set(UUID.randomUUID());
		l.set(r.nextLong());
		a = (s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("a");
			return uuid.get();
		};
		b = a.compose((lv) -> {
			Assertions.assertSame(l.get(), lv);
			callers.add("b");
			return string.get();
		});
		Assertions.assertSame(uuid.get(), b.applyChecked(l.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"b", "a"
		}, callers.toArray());

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			callers.clear();
			string.set(UUID.randomUUID().toString());
			uuid.set(UUID.randomUUID());
			l.set(r.nextLong());
			a = (s) -> {
				Assertions.assertSame(string.get(), s);
				callers.add("a");
				throw thrown;
			};
			b = a.compose((lv) -> {
				Assertions.assertSame(l.get(), lv);
				callers.add("b");
				return string.get();
			});
			try {
				b.applyChecked(l.get());
				Assertions.fail("Did not raise the chained exception");
			} catch (Throwable t) {
				Assertions.assertSame(thrown, t);
			}
			Assertions.assertEquals(2, callers.size());
			Assertions.assertArrayEquals(new String[] {
				"b", "a"
			}, callers.toArray());
		}

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			callers.clear();
			string.set(UUID.randomUUID().toString());
			uuid.set(UUID.randomUUID());
			l.set(r.nextLong());
			a = (s) -> {
				Assertions.assertSame(string.get(), s);
				callers.add("a");
				return uuid.get();
			};
			b = a.compose((lv) -> {
				Assertions.assertSame(l.get(), lv);
				callers.add("b");
				throw thrown;
			});
			try {
				b.applyChecked(l.get());
				Assertions.fail("Did not raise the chained exception");
			} catch (Throwable t) {
				Assertions.assertSame(thrown, t);
			}
			Assertions.assertEquals(1, callers.size());
			Assertions.assertArrayEquals(new String[] {
				"b"
			}, callers.toArray());
		}

		{
			CheckedFunction<String, UUID, Throwable> n = (x) -> null;
			Assertions.assertThrows(NullPointerException.class, () -> n.compose(null));
		}
	}
}
