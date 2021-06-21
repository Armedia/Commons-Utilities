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

import java.util.Iterator;

/**
 * This is an extension to the classical {@link Iterator} interface that adds three methods that
 * will assist in tracking the iterator's progress.
 *
 *
 *
 */
public interface IndexedIterator<T> extends Iterator<T> {

	/**
	 * Returns {@code true} if the last element fetched from the iterator was removed by invoking
	 * {@link #remove()}, {@code false} otherwise. If the first element hasn't been fetched (by
	 * invoking {@link #next()}), this method will evidently return {@code false} (since, naturally,
	 * remove() could not have been called yet).
	 *
	 * @return {@code true} if the last element fetched from the iterator was removed by invoking
	 *         {@link #remove()}, {@code false} otherwise
	 */
	public boolean wasRemoved();

	/**
	 * <p>
	 * Return the 0-based index of the last element fetched by invoking {@link #next()}. The first
	 * invocation of next() will result in this method returning 0, after the second invocation this
	 * method will return 1, and so on.
	 * </p>
	 * <p>
	 * If {@link #remove()} is invoked, this method will return -1 until the following invocation of
	 * next(). At that point, it will return the adjusted index for the newly fetched element,
	 * accounting for any previously-removed element(s).
	 * </p>
	 *
	 * @return the 0-based index of the last element fetched by invoking {@link #next()}
	 */
	public int currentIndex();

	/**
	 * Returns the last element returned by this iterator, or {@code null} if it was removed via an
	 * call to {@link #remove()}. If no element has been fetched, an {@link IllegalStateException}
	 * will be raised.
	 *
	 * @return the last element returned by this iterator, or {@code null} if it was removed via an
	 *         call to {@link #remove()}
	 */
	public T current();

	/**
	 * Returns the total number of elements this iterator can span over. If {@link #remove()} is
	 * invoked, this method will adjust (reduce) the returned number accordingly, and automatically.
	 *
	 * @return the total number of elements this iterator can span over
	 */
	public int getMax();
}
