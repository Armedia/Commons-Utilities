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

import java.util.Collection;
import java.util.Iterator;

/**
 *
 *
 */
public class BasicIndexedIterator<T> implements IndexedIterator<T> {

	private final Iterator<T> it;
	private boolean removed = false;
	private int max = -1;
	private int currentIndex = -1;
	private T currentElement = null;

	public BasicIndexedIterator(Collection<T> c) {
		if (c == null) { throw new IllegalArgumentException("Collection may not be null"); }
		this.it = c.iterator();
		this.max = c.size();
	}

	@Override
	public int getMax() {
		return this.max;
	}

	@Override
	public boolean hasNext() {
		return this.it.hasNext();
	}

	@Override
	public T next() {
		this.currentElement = this.it.next();
		this.currentIndex++;
		this.removed = false;
		return this.currentElement;
	}

	@Override
	public void remove() {
		this.it.remove();
		this.currentElement = null;
		this.removed = true;
		this.currentIndex--;
		this.max--;
	}

	@Override
	public int currentIndex() {
		return (!this.removed ? this.currentIndex : -1);
	}

	@Override
	public boolean wasRemoved() {
		return this.removed;
	}

	@Override
	public T current() {
		if ((this.currentIndex < 0) && !this.removed) {
			throw new IllegalStateException("next() must be called first");
		}
		return this.currentElement;
	}
}
