package com.armedia.commons.utilities;

import java.io.Closeable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class CloseableIterator<E> implements Closeable, Iterator<E> {

	private boolean closed = false;
	private E previous = null;
	private E current = null;

	private void assertOpen() {
		if (this.closed) { throw new IllegalStateException("This iterator has already been closed"); }
	}

	@Override
	public final boolean hasNext() {
		assertOpen();
		boolean ret = (findNext() != null);
		if (!ret) {
			// If we've reached the last element, we close it
			close();
		}
		return ret;
	}

	@Override
	public final E next() {
		assertOpen();
		E ret = findNext();
		this.current = null;
		this.previous = ret;
		if (ret == null) { throw new NoSuchElementException(); }
		return ret;
	}

	private final E findNext() {
		assertOpen();
		if (this.current == null) {
			boolean ok = false;
			try {
				this.current = seek();
				ok = true;
			} catch (Throwable t) {
				throw new RuntimeException("Failed to find the next element", t);
			} finally {
				if (!ok) {
					close();
				}
			}
		}
		return this.current;
	}

	protected abstract E seek() throws Throwable;

	@Override
	public final void remove() {
		assertOpen();
		if (this.previous == null) { throw new IllegalStateException("No element to remove"); }
		remove(this.previous);
	}

	protected void remove(E next) {
		throw new UnsupportedOperationException("Remove not supported for this iterator");
	}

	@Override
	public final void close() {
		assertOpen();
		try {
			doClose();
		} finally {
			this.closed = true;
		}
	}

	protected abstract void doClose();
}