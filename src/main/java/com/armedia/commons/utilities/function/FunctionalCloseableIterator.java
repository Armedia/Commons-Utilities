/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2022 Armedia, LLC
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
package com.armedia.commons.utilities.function;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.armedia.commons.utilities.CloseableIterator;

/**
 * <p>
 * Utility class to build a {@link CloseableIterator} using functional interfaces. All interface
 * contracts are plain, except the {@code seeker}: this function must obey a specific contract in
 * order to properly return results:
 * </p>
 * <ul>
 * <li>Any value returned - including {@code null} - will be returned as a valid iterator value</li>
 * <li>To signal the end of iteration, a {@link NoSuchElementException} should be raised. This
 * exception will be gracefully handled by the overarching framework.</li>
 * </ul>
 *
 * @author diego
 *
 * @param <E>
 */
public class FunctionalCloseableIterator<E> extends CloseableIterator<E> {

	public static class Builder<E> {
		private CheckedSupplier<Boolean, ? extends Exception> initializer = null;
		private final CheckedSupplier<E, ? extends Exception> seeker;
		private Consumer<E> remover = null;
		private CheckedRunnable<? extends Exception> closer = null;

		public <EX extends Exception> Builder(CheckedSupplier<E, EX> seeker) {
			this.seeker = Objects.requireNonNull(seeker, "Must provide a seeker method");
		}

		public Builder<E> withInitializer(CheckedSupplier<Boolean, ? extends Exception> initializer) {
			this.initializer = initializer;
			return this;
		}

		public Builder<E> withInitializer(Supplier<Boolean> initializer) {
			if (initializer == null) {
				this.initializer = null;
			} else {
				this.initializer = CheckedTools.check(initializer);
			}
			return this;
		}

		public CheckedSupplier<Boolean, ? extends Exception> initializer() {
			return this.initializer;
		}

		public Builder<E> withRemover(Consumer<E> remover) {
			this.remover = remover;
			return this;
		}

		public Consumer<E> remover() {
			return this.remover;
		}

		public Builder<E> withCloser(CheckedRunnable<? extends Exception> closer) {
			this.closer = closer;
			return this;
		}

		public Builder<E> withCloser(Runnable closer) {
			if (this.initializer == null) {
				this.closer = null;
			} else {
				this.closer = CheckedTools.check(closer);
			}
			return this;
		}

		public CheckedRunnable<? extends Exception> closer() {
			return this.closer;
		}

		public CloseableIterator<E> build() {
			return new FunctionalCloseableIterator<>(this.initializer, this.seeker, this.remover, this.closer);
		}
	}

	private final CheckedSupplier<Boolean, ? extends Exception> initializer;
	private final CheckedSupplier<E, ? extends Exception> seeker;
	private final Consumer<E> remover;
	private final CheckedRunnable<? extends Exception> closer;

	public FunctionalCloseableIterator(CheckedSupplier<E, ? extends Exception> seeker) {
		this(null, seeker, null, null);
	}

	public FunctionalCloseableIterator(CheckedSupplier<E, ? extends Exception> seeker,
		CheckedRunnable<? extends Exception> closer) {
		this(null, seeker, null, closer);
	}

	public FunctionalCloseableIterator(CheckedSupplier<Boolean, ? extends Exception> initializer,
		CheckedSupplier<E, ? extends Exception> seeker, CheckedRunnable<? extends Exception> closer) {
		this(initializer, seeker, null, closer);
	}

	public FunctionalCloseableIterator(CheckedSupplier<Boolean, ? extends Exception> initializer,
		CheckedSupplier<E, ? extends Exception> seeker, Consumer<E> remover,
		CheckedRunnable<? extends Exception> closer) {
		this.initializer = initializer;
		this.seeker = seeker;
		this.remover = null;
		this.closer = closer;
	}

	@Override
	protected boolean initialize() throws Exception {
		if (this.initializer != null) { return (this.initializer.getChecked() != Boolean.FALSE); }
		return true;
	}

	@Override
	protected Result findNext() throws Exception {
		try {
			return found(this.seeker.get());
		} catch (Exception e) {
			if (NoSuchElementException.class.isInstance(e)) { return null; }
			throw e;
		}
	}

	@Override
	protected void remove(E current) {
		if (this.remover == null) { throw new UnsupportedOperationException(); }
		this.remover.accept(current);
	}

	@Override
	protected void doClose() throws Exception {
		if (this.closer != null) {
			this.closer.run();
		}
	}
}
