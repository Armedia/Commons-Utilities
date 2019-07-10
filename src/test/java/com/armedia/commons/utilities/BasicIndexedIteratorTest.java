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
package com.armedia.commons.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 *
 */
public class BasicIndexedIteratorTest implements GoodService {

	@Test
	public void testBasicIndexedIterator() {
		Collection<String> c = null;
		Assertions.assertThrows(IllegalArgumentException.class, () -> new BasicIndexedIterator<>(null));
		c = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			c.add(String.valueOf(i));
		}
		IndexedIterator<String> it = new BasicIndexedIterator<>(c);
		Assertions.assertEquals(it.getMax(), c.size());
		Assertions.assertFalse(it.wasRemoved());
		Assertions.assertEquals(-1, it.currentIndex());
		Assertions.assertTrue(it.hasNext());
	}

	@Test
	public void testGetMax() {
		Collection<Integer> c = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			c.add(Integer.valueOf(i));
		}
		IndexedIterator<Integer> it = new BasicIndexedIterator<>(c);
		for (int i = 1;; i++) {
			if (!it.hasNext()) {
				break;
			}
			Integer str = it.next();
			String msg = String.format("Failed on iteration %d (v = %d)", i, str);
			int prevSize = c.size();
			Assertions.assertEquals(prevSize, it.getMax(), msg);
			boolean removed = ((i % 5) == 0);
			if (!removed) {
				continue;
			}
			it.remove();
			Assertions.assertEquals(c.size(), it.getMax(), msg);
			Assertions.assertEquals(prevSize - 1, c.size(), msg);
			Assertions.assertTrue(it.wasRemoved(), msg);
			Assertions.assertEquals(-1, it.currentIndex(), msg);
		}
	}

	@Test
	public void testNext() {
		Collection<String> c = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			c.add(UUID.randomUUID().toString());
		}
		Iterator<String> a = c.iterator();
		IndexedIterator<String> b = new BasicIndexedIterator<>(c);
		while (a.hasNext() && b.hasNext()) {
			Assertions.assertEquals(a.next(), b.next());
		}
		Assertions.assertEquals(a.hasNext(), b.hasNext());
	}

	@Test
	public void testCurrent() {
		Collection<String> A = new ArrayList<>();
		Collection<String> B = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			String str = UUID.randomUUID().toString();
			A.add(str);
			B.add(str);
		}
		Iterator<String> a = A.iterator();
		IndexedIterator<String> b = new BasicIndexedIterator<>(B);
		Assertions.assertThrows(IllegalStateException.class, () -> b.current());
		for (int i = 0; a.hasNext() && b.hasNext(); i++) {
			String n = a.next();
			Assertions.assertEquals(n, b.next());
			Assertions.assertEquals(n, b.current());
			boolean removed = ((i % 5) == 0);
			if (!removed) {
				continue;
			}
			a.remove();
			b.remove();
			Assertions.assertNull(b.current());
		}
		Assertions.assertEquals(a.hasNext(), b.hasNext());
	}

	@Test
	public void testCurrentIndex() {
		Collection<Integer> c = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			c.add(Integer.valueOf(i));
		}
		IndexedIterator<Integer> it = new BasicIndexedIterator<>(c);
		int removedCount = 0;
		for (int i = 0;; i++) {
			if (!it.hasNext()) {
				break;
			}
			Integer current = it.next();
			String msg = String.format("Failed on iteration %d (v = %d)", i, current);
			Assertions.assertEquals(i - removedCount, it.currentIndex(), msg);
			Assertions.assertEquals(Integer.valueOf(i), current, msg);
			boolean removed = ((i % 5) == 0);
			if (!removed) {
				continue;
			}
			it.remove();
			removedCount++;
			Assertions.assertTrue(it.wasRemoved(), msg);
			Assertions.assertEquals(-1, it.currentIndex(), msg);
		}
	}

	@Test
	public void testRemove() {
		Collection<Integer> c = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			c.add(Integer.valueOf(i));
		}

		IndexedIterator<Integer> it = new BasicIndexedIterator<>(c);
		Assertions.assertThrows(IllegalStateException.class, () -> it.remove());
		Assertions.assertTrue(it.hasNext());
		Assertions.assertNotNull(it.next());
		it.remove();
	}
}
