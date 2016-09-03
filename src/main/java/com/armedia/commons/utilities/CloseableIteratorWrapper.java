package com.armedia.commons.utilities;

import java.util.Iterator;

public final class CloseableIteratorWrapper<E> extends CloseableIterator<E> {

	private final Iterator<E> it;

	public CloseableIteratorWrapper(Iterator<E> it) {
		if (it == null) { throw new IllegalArgumentException("Must provide a non-null iterator"); }
		this.it = it;
	}

	public CloseableIteratorWrapper(Iterable<E> c) {
		if (c == null) { throw new IllegalArgumentException("Must provide an iterable object to iterate over"); }
		this.it = c.iterator();
	}

	@Override
	protected boolean checkNext() {
		return this.it.hasNext();
	}

	@Override
	protected final E getNext() {
		return this.it.next();
	}

	@Override
	public final void remove(E element) {
		this.it.remove();
	}

	@Override
	protected void doClose() {
		// Do nothing... the underlying iterator supports nothing
	}
}