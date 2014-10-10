/**
 * *******************************************************************
 * 
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS.
 * REPRODUCTION OF ANY PORTION OF THE SOURCE CODE, CONTAINED HEREIN,
 * OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE,
 * IS STRICTLY PROHIBITED.
 * 
 * Confidential Property of Armedia LLC.
 * (c) Copyright Armedia LLC 2011.
 * All Rights reserved.
 * 
 * *******************************************************************
 */
package com.armedia.commons.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.armedia.commons.utilities.BasicIndexedIterator;
import com.armedia.commons.utilities.IndexedIterator;

/**
 * @author drivera@armedia.com
 * 
 */
public class BasicIndexedIteratorTest {

	@Test
	public void testBasicIndexedIterator() {
		Collection<String> c = null;
		try {
			new BasicIndexedIterator<Object>(null);
			Assert.fail("Failed to raise a IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		c = new ArrayList<String>();
		for (int i = 0; i < 100; i++) {
			c.add(String.valueOf(i));
		}
		IndexedIterator<String> it = new BasicIndexedIterator<String>(c);
		Assert.assertEquals(it.getMax(), c.size());
		Assert.assertFalse(it.wasRemoved());
		Assert.assertEquals(-1, it.currentIndex());
		Assert.assertTrue(it.hasNext());
	}

	@Test
	public void testGetMax() {
		Collection<Integer> c = new ArrayList<Integer>();
		for (int i = 0; i < 100; i++) {
			c.add(Integer.valueOf(i));
		}
		IndexedIterator<Integer> it = new BasicIndexedIterator<Integer>(c);
		for (int i = 1;; i++) {
			if (!it.hasNext()) {
				break;
			}
			Integer str = it.next();
			String msg = String.format("Failed on iteration %d (v = %d)", i, str);
			int prevSize = c.size();
			Assert.assertEquals(msg, prevSize, it.getMax());
			boolean removed = ((i % 5) == 0);
			if (!removed) {
				continue;
			}
			it.remove();
			Assert.assertEquals(msg, c.size(), it.getMax());
			Assert.assertEquals(msg, prevSize - 1, c.size());
			Assert.assertTrue(msg, it.wasRemoved());
			Assert.assertEquals(msg, -1, it.currentIndex());
		}
	}

	@Test
	public void testNext() {
		Collection<String> c = new ArrayList<String>();
		for (int i = 0; i < 100; i++) {
			c.add(UUID.randomUUID().toString());
		}
		Iterator<String> a = c.iterator();
		IndexedIterator<String> b = new BasicIndexedIterator<String>(c);
		while (a.hasNext() && b.hasNext()) {
			Assert.assertEquals(a.next(), b.next());
		}
		Assert.assertEquals(a.hasNext(), b.hasNext());
	}

	@Test
	public void testCurrent() {
		Collection<String> A = new ArrayList<String>();
		Collection<String> B = new ArrayList<String>();
		for (int i = 0; i < 100; i++) {
			String str = UUID.randomUUID().toString();
			A.add(str);
			B.add(str);
		}
		Iterator<String> a = A.iterator();
		IndexedIterator<String> b = new BasicIndexedIterator<String>(B);
		try {
			b.current();
			Assert.fail("Failed to raise an IllegalStateException");
		} catch (IllegalStateException e) {
			// all is well
		}
		for (int i = 0; a.hasNext() && b.hasNext(); i++) {
			String n = a.next();
			Assert.assertEquals(n, b.next());
			Assert.assertEquals(n, b.current());
			boolean removed = ((i % 5) == 0);
			if (!removed) {
				continue;
			}
			a.remove();
			b.remove();
			Assert.assertNull(b.current());
		}
		Assert.assertEquals(a.hasNext(), b.hasNext());
	}

	@Test
	public void testCurrentIndex() {
		Collection<Integer> c = new ArrayList<Integer>();
		for (int i = 0; i < 100; i++) {
			c.add(Integer.valueOf(i));
		}
		IndexedIterator<Integer> it = new BasicIndexedIterator<Integer>(c);
		int removedCount = 0;
		for (int i = 0;; i++) {
			if (!it.hasNext()) {
				break;
			}
			Integer current = it.next();
			String msg = String.format("Failed on iteration %d (v = %d)", i, current);
			Assert.assertEquals(msg, i - removedCount, it.currentIndex());
			Assert.assertEquals(msg, Integer.valueOf(i), current);
			boolean removed = ((i % 5) == 0);
			if (!removed) {
				continue;
			}
			it.remove();
			removedCount++;
			Assert.assertTrue(msg, it.wasRemoved());
			Assert.assertEquals(msg, -1, it.currentIndex());
		}
	}

	@Test
	public void testRemove() {
		Collection<Integer> c = new ArrayList<Integer>();
		for (int i = 0; i < 100; i++) {
			c.add(Integer.valueOf(i));
		}

		IndexedIterator<Integer> it = new BasicIndexedIterator<Integer>(c);
		try {
			it.remove();
			Assert.fail("Failed to explode when removing without fetching");
		} catch (IllegalStateException t) {
			// This is intended to explode...so this is ok
		}
		Assert.assertTrue(it.hasNext());
		Assert.assertNotNull(it.next());
		it.remove();
	}
}