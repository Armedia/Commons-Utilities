/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2025 Armedia, LLC
 * %%
 * This file is part of the Caliente software.
 * 
 * If the software was purchased under a paid Caliente license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * 
 * Caliente is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Caliente is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Caliente. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 *******************************************************************************/
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

	private boolean initialized = false;

	private State state = State.WAITING;

	private E current = null;

	protected class Result {
		private final E value;

		private Result(E value) {
			this.value = value;
		}
	}

	protected final Result found(E value) {
		return new Result(value);
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
		if (!this.initialized) {
			try {
				// If initialization fails, we mark as closed
				if (!initialize()) {
					this.state = State.CLOSED;
					return false;
				}
				this.initialized = true;
			} catch (Exception e) {
				// Uh-oh...something went wrong, we need to abort!
				this.state = State.CLOSED;
				throw new RuntimeException("Failed to initialize the iterator", e);
			}
		}

		try {
			result = findNext();
		} catch (Exception e) {
			close();
			throw new RuntimeException("Failed to check for the next item in the iterator, closed automatically", e);
		}

		if (result != null) {
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
		this.state = State.FETCHED;
		return this.current;
	}

	/**
	 * <p>
	 * Perform any lazy initialization. If this method returns {@code false}, the iterator will be
	 * marked as closed and will be of no use.
	 * </p>
	 *
	 * @throws Exception
	 */
	protected boolean initialize() throws Exception {
		return true;
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
		if (this.state != State.FETCHED) { throw new IllegalStateException("No element to remove"); }
		remove(this.current);
		this.state = State.WAITING;
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
