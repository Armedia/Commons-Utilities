/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2021 Armedia, LLC
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CloseableIteratorTest {

	static final Collection<Integer> ALL_CHARACTERISTICS;

	static {
		List<Integer> c = Arrays.asList(new Integer[] {
			Spliterator.CONCURRENT, Spliterator.DISTINCT, Spliterator.IMMUTABLE, Spliterator.NONNULL,
			Spliterator.ORDERED, Spliterator.SIZED, Spliterator.SORTED, Spliterator.SUBSIZED,
		});
		c.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return Tools.compare(o1, o2);
			}
		});
		List<Integer> ret = new ArrayList<>();
		for (int i = 0; i < (1 << c.size()); i++) {
			int val = 0;
			for (int b = 0; b < c.size(); b++) {
				int bit = (1 << b);
				if ((i & bit) != 0) {
					val |= bit;
				}
			}
			ret.add(val);
		}
		ALL_CHARACTERISTICS = Tools.freezeCollection(ret);
	}

	@Test
	public void testFound() {
		final UUID uuid = UUID.randomUUID();
		try (CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {
			@Override
			protected Result findNext() throws Exception {
				return null;
			}

			@Override
			protected void doClose() throws Exception {
			}
		}) {
			Assertions.assertNotNull(cit.found(null));
			Assertions.assertNotNull(cit.found(uuid));
		}
	}

	@Test
	public void testHasNext() {
		final UUID uuid = UUID.randomUUID();
		final Collection<UUID> c = Collections.singleton(uuid);
		try (CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {
			final Iterator<UUID> it = c.iterator();

			@Override
			protected Result findNext() throws Exception {
				if (this.it.hasNext()) { return found(this.it.next()); }
				return null;
			}

			@Override
			protected void doClose() throws Exception {
			}
		}) {
			Assertions.assertTrue(cit.hasNext());
			Assertions.assertSame(uuid, cit.next());
			Assertions.assertFalse(cit.hasNext());
		}

		CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {
			final Iterator<UUID> it = c.iterator();

			@Override
			protected Result findNext() throws Exception {
				if (this.it.hasNext()) { return found(this.it.next()); }
				return null;
			}

			@Override
			protected void doClose() throws Exception {
			}
		};
		cit.close();
		Assertions.assertFalse(cit.hasNext());

		final Exception thrown = new Exception(uuid.toString());
		cit = new CloseableIterator<UUID>() {
			@Override
			protected Result findNext() throws Exception {
				throw thrown;
			}

			@Override
			protected void doClose() throws Exception {
			}
		};
		try {
			cit.hasNext();
			Assertions.fail("Did not fail with an exception in findNext()");
		} catch (RuntimeException e) {
			Assertions.assertSame(thrown, e.getCause());
		}
		cit.close();
	}

	@Test
	public void testNext() {
		final UUID uuid = UUID.randomUUID();
		final Collection<UUID> c = Collections.singleton(uuid);
		try (CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {
			final Iterator<UUID> it = c.iterator();

			@Override
			protected Result findNext() throws Exception {
				if (this.it.hasNext()) { return found(this.it.next()); }
				return null;
			}

			@Override
			protected void doClose() throws Exception {
			}
		}) {
			Assertions.assertTrue(cit.hasNext());
			Assertions.assertSame(uuid, cit.next());
			Assertions.assertThrows(NoSuchElementException.class, () -> cit.next());
		}

		CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {
			final Iterator<UUID> it = c.iterator();

			@Override
			protected Result findNext() throws Exception {
				if (this.it.hasNext()) { return found(this.it.next()); }
				return null;
			}

			@Override
			protected void doClose() throws Exception {
			}
		};
		cit.close();
		Assertions.assertThrows(NoSuchElementException.class, () -> cit.next());
	}

	@Test
	public void testRemove() {
		final UUID uuid = UUID.randomUUID();
		final Collection<UUID> c = new ArrayList<>(Collections.singleton(uuid));
		try (CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {
			final Iterator<UUID> it = c.iterator();

			@Override
			protected Result findNext() throws Exception {
				if (this.it.hasNext()) { return found(this.it.next()); }
				return null;
			}

			@Override
			protected void remove(UUID current) {
				Assertions.assertSame(uuid, current);
			}

			@Override
			protected void doClose() throws Exception {
			}
		}) {
			Assertions.assertTrue(cit.hasNext());
			Assertions.assertSame(uuid, cit.next());
			cit.remove();
		}

		try (CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {
			final Iterator<UUID> it = c.iterator();

			@Override
			protected Result findNext() throws Exception {
				if (this.it.hasNext()) { return found(this.it.next()); }
				return null;
			}

			@Override
			protected void doClose() throws Exception {
			}
		}) {
			Assertions.assertTrue(cit.hasNext());
			Assertions.assertSame(uuid, cit.next());
			Assertions.assertThrows(UnsupportedOperationException.class, () -> cit.remove());
		}

		{
			CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {
				final Iterator<UUID> it = c.iterator();

				@Override
				protected Result findNext() throws Exception {
					if (this.it.hasNext()) { return found(this.it.next()); }
					return null;
				}

				@Override
				protected void doClose() throws Exception {
				}
			};
			Assertions.assertThrows(IllegalStateException.class, () -> cit.remove());
			cit.close();
		}

		{
			CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {
				final Iterator<UUID> it = c.iterator();

				@Override
				protected Result findNext() throws Exception {
					if (this.it.hasNext()) { return found(this.it.next()); }
					return null;
				}

				@Override
				protected void doClose() throws Exception {
				}
			};
			cit.close();
			Assertions.assertThrows(IllegalStateException.class, () -> cit.remove());
		}
	}

	@Test
	public void testClose() {
		try (CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {

			@Override
			protected Result findNext() throws Exception {
				return null;
			}

			@Override
			protected void doClose() throws Exception {
			}
		}) {
		}
		final Exception thrown = new Exception(UUID.randomUUID().toString());
		try (CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {

			@Override
			protected Result findNext() throws Exception {
				return null;
			}

			@Override
			protected void doClose() throws Exception {
				throw thrown;
			}
		}) {
		}
	}

	@Test
	public void testConfigureStream() {
		final UUID uuid = UUID.randomUUID();
		final Collection<UUID> c = new ArrayList<>(Collections.singleton(uuid));
		final AtomicInteger closeCalled = new AtomicInteger(0);
		try (CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {
			final Iterator<UUID> it = c.iterator();

			@Override
			protected Result findNext() throws Exception {
				if (this.it.hasNext()) { return found(this.it.next()); }
				return null;
			}

			@Override
			protected void remove(UUID current) {
				Assertions.assertSame(uuid, current);
			}

			@Override
			protected void doClose() throws Exception {
				closeCalled.incrementAndGet();
			}
		}) {
			ArrayList<UUID> l = new ArrayList<>();
			try (Stream<UUID> s = c.stream()) {
				cit.configureStream(s);
				s.forEach(l::add);
			}
			Assertions.assertNotEquals(0, closeCalled.get());
		}
	}

	@Test
	public void testStream() {
		final Collection<UUID> c = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			c.add(UUID.randomUUID());
		}
		final AtomicInteger closeCalled = new AtomicInteger(0);
		closeCalled.set(0);
		try (CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {
			final Iterator<UUID> it = c.iterator();

			@Override
			protected Result findNext() throws Exception {
				if (this.it.hasNext()) { return found(this.it.next()); }
				return null;
			}

			@Override
			protected void remove(UUID current) {
				this.it.remove();
			}

			@Override
			protected void doClose() throws Exception {
				closeCalled.incrementAndGet();
			}
		}) {
			ArrayList<UUID> l = new ArrayList<>();
			try (Stream<UUID> s = cit.stream()) {
				Assertions.assertFalse(s.isParallel());
				s.forEach(l::add);
			}
			Assertions.assertNotEquals(0, closeCalled.get());
			Assertions.assertEquals(c, l);
		}
	}

	@Test
	public void testStreamParallel() {
		final Collection<UUID> c = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			c.add(UUID.randomUUID());
		}
		final AtomicInteger closeCalled = new AtomicInteger(0);
		closeCalled.set(0);
		try (CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {
			final Iterator<UUID> it = c.iterator();

			@Override
			protected Result findNext() throws Exception {
				if (this.it.hasNext()) { return found(this.it.next()); }
				return null;
			}

			@Override
			protected void remove(UUID current) {
				this.it.remove();
			}

			@Override
			protected void doClose() throws Exception {
				closeCalled.incrementAndGet();
			}
		}) {
			ArrayList<UUID> l = new ArrayList<>();
			try (Stream<UUID> s = cit.stream(true)) {
				Assertions.assertTrue(s.isParallel());
				s.forEach(l::add);
			}
			Assertions.assertNotEquals(0, closeCalled.get());
			Assertions.assertEquals(c, l);
		}
		closeCalled.set(0);
		try (CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {
			final Iterator<UUID> it = c.iterator();

			@Override
			protected Result findNext() throws Exception {
				if (this.it.hasNext()) { return found(this.it.next()); }
				return null;
			}

			@Override
			protected void remove(UUID current) {
				this.it.remove();
			}

			@Override
			protected void doClose() throws Exception {
				closeCalled.incrementAndGet();
			}
		}) {
			ArrayList<UUID> l = new ArrayList<>();
			try (Stream<UUID> s = cit.stream(false)) {
				Assertions.assertFalse(s.isParallel());
				s.forEach(l::add);
			}
			Assertions.assertNotEquals(0, closeCalled.get());
			Assertions.assertEquals(c, l);
		}
	}

	@Test
	public void testStreamCharacteristics() {
		final Collection<UUID> c = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			c.add(UUID.randomUUID());
		}
		for (Integer characteristics : CloseableIteratorTest.ALL_CHARACTERISTICS) {
			final AtomicInteger closeCalled = new AtomicInteger(0);
			try (CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {
				final Iterator<UUID> it = c.iterator();

				@Override
				protected Result findNext() throws Exception {
					if (this.it.hasNext()) { return found(this.it.next()); }
					return null;
				}

				@Override
				protected void remove(UUID current) {
					this.it.remove();
				}

				@Override
				protected void doClose() throws Exception {
					closeCalled.incrementAndGet();
				}
			}) {
				ArrayList<UUID> l = new ArrayList<>();
				try (Stream<UUID> s = cit.stream(characteristics)) {
					Assertions.assertFalse(s.isParallel());
					s.forEach(l::add);
				}
				Assertions.assertNotEquals(0, closeCalled.get());
				Assertions.assertEquals(c, l);
			}
		}
	}

	@Test
	public void testStreamCharacteristicsParallel() {
		final Collection<UUID> c = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			c.add(UUID.randomUUID());
		}
		for (Integer characteristics : CloseableIteratorTest.ALL_CHARACTERISTICS) {
			final AtomicInteger closeCalled = new AtomicInteger(0);

			closeCalled.set(0);
			try (CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {
				final Iterator<UUID> it = c.iterator();

				@Override
				protected Result findNext() throws Exception {
					if (this.it.hasNext()) { return found(this.it.next()); }
					return null;
				}

				@Override
				protected void remove(UUID current) {
					this.it.remove();
				}

				@Override
				protected void doClose() throws Exception {
					closeCalled.incrementAndGet();
				}
			}) {
				ArrayList<UUID> l = new ArrayList<>();
				try (Stream<UUID> s = cit.stream(characteristics, true)) {
					Assertions.assertTrue(s.isParallel());
					s.forEach(l::add);
				}
				Assertions.assertNotEquals(0, closeCalled.get());
				Assertions.assertEquals(c, l);
			}
			closeCalled.set(0);
			try (CloseableIterator<UUID> cit = new CloseableIterator<UUID>() {
				final Iterator<UUID> it = c.iterator();

				@Override
				protected Result findNext() throws Exception {
					if (this.it.hasNext()) { return found(this.it.next()); }
					return null;
				}

				@Override
				protected void remove(UUID current) {
					this.it.remove();
				}

				@Override
				protected void doClose() throws Exception {
					closeCalled.incrementAndGet();
				}
			}) {
				ArrayList<UUID> l = new ArrayList<>();
				try (Stream<UUID> s = cit.stream(characteristics, false)) {
					Assertions.assertFalse(s.isParallel());
					s.forEach(l::add);
				}
				Assertions.assertNotEquals(0, closeCalled.get());
				Assertions.assertEquals(c, l);
			}
		}
	}

}
