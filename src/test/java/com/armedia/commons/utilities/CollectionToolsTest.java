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
package com.armedia.commons.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 *
 */
public class CollectionToolsTest implements GoodService {

	@Test
	public void testConstructor() {
		new CollectionTools();
	}

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

		a = new ArrayList<>(Arrays.asList(listA));
		b = new ArrayList<>(Arrays.asList(listA));
		Assertions.assertEquals(0, CollectionTools.addUnique(a, b));
		Assertions.assertArrayEquals(a.toArray(), b.toArray());

		a = new ArrayList<>(Arrays.asList(listB));
		b = new ArrayList<>(Arrays.asList(listA));
		Assertions.assertEquals(3, CollectionTools.addUnique(a, b));
		Assertions.assertArrayEquals(listB, b.toArray());

		a = new ArrayList<>(Arrays.asList(listC));
		b = new ArrayList<>(Arrays.asList(listA));
		Assertions.assertEquals(listC.length, CollectionTools.addUnique(a, b));

		a = new ArrayList<>(Arrays.asList(listD));
		b = new ArrayList<>(Arrays.asList(listA));
		Assertions.assertEquals(1, CollectionTools.addUnique(a, b));

		Set<String> s = new TreeSet<>();
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
		b = new ArrayList<>();
		CollectionTools.addUnique(s, b);
		Assertions.assertEquals(s.size(), b.size());
		Object[] sorted = b.toArray();
		Arrays.sort(sorted);
		Assertions.assertArrayEquals(s.toArray(), sorted);

		{
			List<String> B = b;
			Assertions.assertThrows(IllegalArgumentException.class, () -> CollectionTools.addUnique(null, B));
			Assertions.assertThrows(IllegalArgumentException.class, () -> CollectionTools.addUnique(B, null));
			Assertions.assertThrows(IllegalArgumentException.class, () -> CollectionTools.addUnique(null, null));
		}

		a = Collections.emptyList();
		Assertions.assertEquals(0, CollectionTools.addUnique(a, b));
	}
}
