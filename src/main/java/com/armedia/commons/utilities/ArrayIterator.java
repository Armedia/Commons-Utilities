/**
 * *******************************************************************
 * 
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS.
 * REPRODUCTION OF ANY PORTION OF THE SOURCE CODE, CONTAINED HEREIN,
 * OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE,
 * IS STRICTLY PROHIBITED.
 * 
 * Confidential Property of Armedia LLC.
 * (c) Copyright Armedia LLC 2011-2012.
 * All Rights reserved.
 * 
 * *******************************************************************
 */
package com.armedia.commons.utilities;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author drivera@armedia.com
 * 
 */
public class ArrayIterator<E> implements Iterator<E> {

	private int pos = 0;
	private final E[] e;

	public ArrayIterator(E[] e) {
		this.e = e;
	}

	@Override
	public boolean hasNext() {
		return this.pos < this.e.length;
	}

	@Override
	public E next() {
		if (!hasNext()) { throw new NoSuchElementException(); }
		return this.e[this.pos++];
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}