/**
 * *******************************************************************
 *
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS. REPRODUCTION OF ANY PORTION
 * OF THE SOURCE CODE, CONTAINED HEREIN, OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE, IS
 * STRICTLY PROHIBITED.
 *
 * Confidential Property of Armedia LLC. (c) Copyright Armedia LLC 2011. All Rights reserved.
 *
 * *******************************************************************
 */
package com.armedia.commons.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author drivera@armedia.com
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