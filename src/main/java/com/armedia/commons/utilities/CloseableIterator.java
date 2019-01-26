package com.armedia.commons.utilities;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public abstract class CloseableIterator<E> implements AutoCloseable, Iterator<E> {

	private static enum State {
		//
		WAITING, READY, FETCHED, CLOSED,
		//
		;
	}

	private State state = State.WAITING;

	private E current = null;

	protected class Result {
		private final E value;
		private final boolean found;

		private Result(E value, boolean found) {
			this.value = value;
			this.found = found;
		}
	}

	protected final Result found(E value) {
		return new Result(value, true);
	}

	private void assertOpen() {
		if (this.state == State.CLOSED) { throw new NoSuchElementException("This iterator is already closed"); }
	}

	@Override
	public final boolean hasNext() {
		if (this.state == State.CLOSED) { return false; }
		if (this.state == State.READY) { return true; }

		// Either WAITING or FETCHED, we need to check to see if we have a next element
		final Result result;
		try {
			result = findNext();
		} catch (Exception e) {
			close();
			throw new RuntimeException("Failed to check for the next item in the iterator, closed automatically", e);
		}

		if ((result != null) && result.found) {
			this.current = result.value;
			this.state = State.READY;
			return true;
		}

		close();
		return false;
	}

	@Override
	public final E next() {
		assertOpen();

		if (!hasNext()) { throw new NoSuchElementException(); }

		try {
			this.state = State.FETCHED;
			return this.current;
		} catch (Exception e) {
			close();
			throw new RuntimeException("Failed to fetch the next item in the iterator, closed automatically", e);
		}
	}

	/**
	 * <p>
	 * Seek the next element in the collection, and return it (via {@link #found(Object)}), or
	 * return a "not found" result (by returning {@code null}). This way {@code null}-values are
	 * supported as valid. This method will be invoked at most once per element in the collection.
	 * As soon as nothing ({@code null}) is returned, the iterator will be closed (via
	 * {@link #close()}).
	 * </p>
	 *
	 * @return {@code true} if there's another element to iterate over, {@code false} otherwise.
	 */
	protected abstract Result findNext() throws Exception;

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
		} catch (Exception e) {
			// Do nothing...log it?
		} finally {
			this.state = State.CLOSED;
		}
	}

	protected abstract void doClose() throws Exception;

	protected Stream<E> configureStream(Stream<E> stream) {
		// Make sure we add the close handler
		return stream.onClose(this::close);
	}

	public Stream<E> stream() {
		return configureStream(StreamTools.of(this));
	}

	public Stream<E> stream(boolean parallel) {
		return configureStream(StreamTools.of(this, parallel));
	}

	public Stream<E> stream(int characteristics) {
		return configureStream(StreamTools.of(this, characteristics));
	}

	public Stream<E> stream(int characteristics, boolean parallel) {
		return configureStream(StreamTools.of(this, characteristics, parallel));
	}
}