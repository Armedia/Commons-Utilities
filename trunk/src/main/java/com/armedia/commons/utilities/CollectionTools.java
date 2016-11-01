/**
 * *******************************************************************
 * 
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS. REPRODUCTION OF ANY PORTION
 * OF THE SOURCE CODE, CONTAINED HEREIN, OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE, IS
 * STRICTLY PROHIBITED.
 * 
 * Confidential Property of Armedia LLC. (c) Copyright Armedia LLC 2007. All Rights reserved.
 * 
 * *******************************************************************
 */
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
		Set<T> s = new HashSet<T>();
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