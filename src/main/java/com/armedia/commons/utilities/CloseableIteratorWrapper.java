/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (C) 2013 - 2019 Armedia
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
