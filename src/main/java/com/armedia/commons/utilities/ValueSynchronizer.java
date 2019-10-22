/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2019 Armedia, LLC
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
/**
 *
 */

package com.armedia.commons.utilities;

import java.util.Objects;

/**
 *
 *
 */
public class ValueSynchronizer<V extends Object> {
	private V v = null;

	public ValueSynchronizer() {
		this(null);
	}

	public ValueSynchronizer(V v) {
		this.v = v;
	}

	public synchronized V set(V v) {
		V old = this.v;
		this.v = v;
		// Only wake up a single thread waiting
		notify();
		return old;
	}

	public synchronized V get() {
		return this.v;
	}

	public synchronized V wait(boolean equals, V v) throws InterruptedException {
		boolean waited = false;
		while (equals == Objects.equals(this.v, v)) {
			waited = true;
			wait();
		}
		// Wake up the next thread waiting
		if (waited) {
			notify();
		}
		return v;
	}

	public synchronized V waitFor(V v) throws InterruptedException {
		return wait(false, v);
	}

	public synchronized V waitWhile(V v) throws InterruptedException {
		return wait(true, v);
	}
}
