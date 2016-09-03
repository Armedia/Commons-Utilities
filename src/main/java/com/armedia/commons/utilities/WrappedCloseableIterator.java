package com.armedia.commons.utilities;

import java.util.Iterator;

public class WrappedCloseableIterator<E> extends CloseableIterator<E> {

	private final Iterator<E> it;

	public WrappedCloseableIterator(Iterator<E> it) {
		if (it == null) { throw new IllegalArgumentException("Must provide a non-null iterator"); }
		this.it = it;
	}

	@Override
	protected final E seek() throws Throwable {
		return this.it.next();
	}

	@Override
	protected void doClose() {
		// Do nothing...
	}
}