/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (c) 2010 - 2019 Armedia LLC
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

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class contains multiple static methods that facilitate the comparison of two objects. In the
 * cases where references are compared, null-valued references are properly handled (i.e. null !=
 * non-null, and null == null).
 *
 *
 *
 */
public class CompareUtil {

	public static <T extends Comparable<T>> int compare(T a, T b) {
		if (a == b) { return 0; }
		if (a == null) { return -1; }
		if (b == null) { return 1; }
		return CompareUtil.toUnitary(a.compareTo(b));
	}

	public static <K extends Comparable<K>, V extends Comparable<V>> int compare(Map<K, V> a, Map<K, V> b) {
		if (a == b) { return 0; }
		if (a == null) { return -1; }
		if (b == null) { return 1; }

		// Neither is null, so compare them...
		if (a.isEmpty() != b.isEmpty()) {
			// If only one is empty, then the empty one sorts ahead
			return (a.isEmpty() ? -1 : 1);
		} else {
			// If both are "equally empty", then if they're both empty,
			// we can shortcut the comparison
			if (a.isEmpty()) { return 0; }
		}

		// First, check just the keys - if there are differences there,
		// we need go no further
		Set<K> keys = new TreeSet<>();
		keys.addAll(a.keySet());
		keys.addAll(b.keySet());
		for (K k : keys) {
			// The one who contains the lower-sorting key, sorts earlier
			if (!a.containsKey(k)) { return 1; }
			if (!b.containsKey(k)) { return -1; }

			// Both have the same key, so compare the values - the one who
			// contains the lower value, sorts earlier
			V va = a.get(k);
			V vb = b.get(k);
			int v = CompareUtil.compare(va, vb);
			if (v != 0) { return v; }
		}

		// The maps are identical
		return 0;

	}

	public static int toUnitary(int value) {
		if (value == 0) { return value; }
		return (value < 0 ? -1 : 1);
	}
}