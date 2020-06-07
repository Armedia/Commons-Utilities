/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2020 Armedia, LLC
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
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.armedia.commons.utilities.function.CheckedSupplier;
import com.armedia.commons.utilities.function.CheckedTools;

public final class CloseableIteratorWrapper<E> extends CloseableIterator<E> {

	private static <E> Supplier<Iterator<E>> wrap(Iterator<E> it) {
		Objects.requireNonNull(it, "Must provide a non-null Iterator");
		return () -> it;
	}

	private final CheckedSupplier<Iterator<E>, ? extends Exception> initializer;
	private final Runnable closer;
	private Iterator<E> it = null;

	public CloseableIteratorWrapper(Iterator<E> it) {
		this(it, null);
	}

	public CloseableIteratorWrapper(Iterator<E> it, Runnable closer) {
		this(CloseableIteratorWrapper.wrap(it), closer);
	}

	public CloseableIteratorWrapper(Supplier<Iterator<E>> initializer) {
		this(initializer, null);
	}

	public CloseableIteratorWrapper(Supplier<Iterator<E>> initializer, Runnable closer) {
		this(CheckedTools.check(initializer), closer);
	}

	public CloseableIteratorWrapper(Stream<E> stream) {
		this(Objects.requireNonNull(stream, "Must provide a non-null Stream")::iterator, stream::close);
	}

	public <EX extends Exception> CloseableIteratorWrapper(CheckedSupplier<Iterator<E>, EX> initializer) {
		this(initializer, null);
	}

	public <EX extends Exception> CloseableIteratorWrapper(CheckedSupplier<Iterator<E>, EX> initializer,
		Runnable closer) {
		this.initializer = Objects.requireNonNull(initializer, "Must provide a non-null iterator");
		this.closer = closer;
	}

	@Override
	protected void initialize() throws Exception {
		if (this.it == null) {
			this.it = this.initializer.getChecked();
		}
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
		if (this.closer != null) {
			this.closer.run();
		}
	}
}
