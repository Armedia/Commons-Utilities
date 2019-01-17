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
	protected Result findNext() throws Exception {
		if (this.it.hasNext()) { return found(this.it.next()); }
		return null;
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