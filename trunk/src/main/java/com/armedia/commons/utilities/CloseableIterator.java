package com.armedia.commons.utilities;

import java.io.Closeable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class CloseableIterator<E> implements Closeable, Iterator<E> {

	private static enum State {
		//
		WAITING, READY, FETCHED, CLOSED,
		//
		;
	}

	private State state = State.WAITING;

	private E current = null;

	private void assertOpen() {
		if (this.state == State.CLOSED) { throw new NoSuchElementException("This iterator is already closed"); }
	}

	@Override
	public final boolean hasNext() {
		if (this.state == State.CLOSED) { return false; }
		if (this.state == State.READY) { return true; }

		// Either WAITING or FETCHED, we need to check to see if we have a next element
		final boolean ret = checkNext();
		if (ret) {
			this.state = State.READY;
		} else {
			close();
		}
		return ret;
	}

	@Override
	public final E next() {
		assertOpen();

		if (!hasNext()) {
			close();
		}
		try {
			this.current = getNext();
			this.state = State.FETCHED;
			return this.current;
		} catch (Exception e) {
			close();
			throw new RuntimeException("Failed to fetch the next item in the iterator, closed automatically", e);
		}
	}

	/**
	 * <p>
	 * Test to see whether more elements are available. This is analogous to the traditional
	 * {@link Iterator#hasNext()} method.
	 * </p>
	 *
	 * @return {@code true} if there's another element to iterate over, {@code false} otherwise.
	 */
	protected abstract boolean checkNext();

	/**
	 * <p>
	 * Retrieve the next element in the collection, or raise a {@link NoSuchElementException} if no
	 * element exists. This method is analogous to the traditional {@link Iterator#next()} method.
	 * </p>
	 *
	 * @return the next element in the iteration
	 * @throws NoSuchElementException
	 *             if no further elements exist
	 * @throws Exception
	 *             any exception raised by the actual seek process. This will cause the iterator to
	 *             be closed
	 */
	protected abstract E getNext() throws Exception;

	@Override
	public final void remove() {
		assertOpen();
		if (this.state != State.FETCHED) { throw new IllegalStateException("No element to remove"); }
		try {
			remove(this.current);
		} finally {
			this.state = State.WAITING;
		}
	}

	protected void remove(E current) {
		throw new UnsupportedOperationException("Remove not supported for this iterator");
	}

	@Override
	public final void close() {
		if (this.state == State.CLOSED) { return; }
		try {
			doClose();
		} finally {
			this.state = State.CLOSED;
		}
	}

	protected abstract void doClose();
}