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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CollectionTools {

	/**
	 * Adds all the elements in the {@link Collection} {@code src} into the {@code target}
	 * Collection, ensuring that duplicates aren't added. The method uses an internal instance of
	 * {@link HashSet} to detect duplicity. Importantly, it will only add items to the target that
	 * don't already exist upon it. Items are added by way of the {@link Collection#add(Object)}
	 * method.
	 *
	 *
	 * @param src
	 *            The collection from which to add objects
	 * @param tgt
	 *            The collection to which they should be added
	 * @return The number of elements added which were not already in {@code tgt}
	 */
	public static <T> int addUnique(Collection<T> src, Collection<T> tgt) {
		if (src == null) { throw new IllegalArgumentException("Source may not be null"); }
		if (tgt == null) { throw new IllegalArgumentException("Target may not be null"); }
		if (src.isEmpty()) { return 0; }
		Set<T> s = new HashSet<>();
		s.addAll(tgt);
		int origSize = tgt.size();
		for (T t : src) {
			// Skip duplicates
			if (s.contains(t)) {
				continue;
			}
			tgt.add(t);
		}
		return tgt.size() - origSize;
	}
}
