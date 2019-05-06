package com.armedia.commons.utilities;

import java.util.Iterator;
import java.util.Objects;

public final class CloseableIteratorWrapper<E> extends CloseableIterator<E> {

	private static final Runnable NOOP = () -> {
	};

	private final Runnable closer;
	private final Iterator<E> it;

	public CloseableIteratorWrapper(Iterator<E> it) {
		this(it, null);
	}

	public CloseableIteratorWrapper(Iterator<E> it, Runnable closer) {
		this.it = Objects.requireNonNull(it, "Must provide a non-null iterator");
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