package com.armedia.commons.utilities;

import java.util.Iterator;

public final class CloseableIteratorWrapper<E> extends CloseableIterator<E> {

	private static final Runnable NOOP = () -> {
	};

	private final Runnable closer;
	private final Iterator<E> it;

	public CloseableIteratorWrapper(Iterator<E> it) {
		this(it, null);
	}

	public CloseableIteratorWrapper(Iterator<E> it, Runnable closer) {
		if (it == null) { throw new IllegalArgumentException("Must provide a non-null iterator"); }
		this.it = it;
		this.closer = Tools.coalesce(closer, CloseableIteratorWrapper.NOOP);
	}

	public CloseableIteratorWrapper(Iterable<E> c) {
		this(c, null);
	}

	public CloseableIteratorWrapper(Iterable<E> c, Runnable closer) {
		if (c == null) { throw new IllegalArgumentException("Must provide an iterable object to iterate over"); }
		this.it = c.iterator();
		this.closer = Tools.coalesce(closer, CloseableIteratorWrapper.NOOP);
	}

	@Override
	protected Result findNext() throws Exception {
		return (this.it.hasNext() ? found(this.it.next()) : null);
	}

	@Override
	public final void remove(E element) {
		this.it.remove();
	}

	@Override
	protected void doClose() {
		this.closer.run();
	}
}