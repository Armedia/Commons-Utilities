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
package com.armedia.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.armedia.commons.utilities.CollectionTools;

/**
 * @author drivera@armedia.com
 * 
 */
public class CollectionToolsTest {
	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CollectionTools#addUnique(java.util.Collection, java.util.Collection)}
	 * .
	 */
	@Test
	public void testAddUnique() {
		String[] listA = {
			"Rohcai0k", "aF4ohshu", "Aeph5ohn", "uZee9aev",
		};
		String[] listB = {
			"Rohcai0k", "aF4ohshu", "Aeph5ohn", "uZee9aev", "oi2Chei5", "aL7shahD", "ieCh3Ooc",

		};
		String[] listC = {
			"ieY8aep3", "raugahG6", "oJ6uxeiD", "oophae7G",
		};
		String[] listD = {
			"Rohcai0k", "aF4ohshu", "Aeph5ohn", "uZee9aev", "ieY8aep3",
		};
		List<String> a = null;
		List<String> b = null;

		a = new ArrayList<String>(Arrays.asList(listA));
		b = new ArrayList<String>(Arrays.asList(listA));
		Assert.assertEquals(0, CollectionTools.addUnique(a, b));
		Assert.assertArrayEquals(a.toArray(), b.toArray());

		a = new ArrayList<String>(Arrays.asList(listB));
		b = new ArrayList<String>(Arrays.asList(listA));
		Assert.assertEquals(3, CollectionTools.addUnique(a, b));
		Assert.assertArrayEquals(listB, b.toArray());

		a = new ArrayList<String>(Arrays.asList(listC));
		b = new ArrayList<String>(Arrays.asList(listA));
		Assert.assertEquals(listC.length, CollectionTools.addUnique(a, b));

		a = new ArrayList<String>(Arrays.asList(listD));
		b = new ArrayList<String>(Arrays.asList(listA));
		Assert.assertEquals(1, CollectionTools.addUnique(a, b));

		Set<String> s = new TreeSet<String>();
		for (String str : listA) {
			s.add(str);
		}
		for (String str : listB) {
			s.add(str);
		}
		for (String str : listC) {
			s.add(str);
		}
		for (String str : listD) {
			s.add(str);
		}
		b = new ArrayList<String>();
		CollectionTools.addUnique(s, b);
		Assert.assertEquals(s.size(), b.size());
		Object[] sorted = b.toArray();
		Arrays.sort(sorted);
		Assert.assertArrayEquals(s.toArray(), sorted);

		try {
			CollectionTools.addUnique(null, b);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// all is well
		}

		try {
			CollectionTools.addUnique(b, null);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// all is well
		}

		try {
			CollectionTools.addUnique(null, null);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// all is well
		}

		a = Collections.emptyList();
		Assert.assertEquals(0, CollectionTools.addUnique(a, b));
	}
}