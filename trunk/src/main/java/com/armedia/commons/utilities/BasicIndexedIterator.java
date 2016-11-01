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

import java.util.Collection;
import java.util.Iterator;

/**
 * @author drivera@armedia.com
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
		if ((this.currentIndex < 0) && !this.removed) { throw new IllegalStateException("next() must be called first"); }
		return this.currentElement;
	}
}