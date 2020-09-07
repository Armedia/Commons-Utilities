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

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.armedia.commons.utilities.concurrent.BaseShareableLockable;
import com.armedia.commons.utilities.concurrent.MutexAutoLock;

public final class MutableBox<V> extends BaseShareableLockable implements Supplier<V>, Consumer<V> {

	private V value = null;

	private MutableBox(V value) {
		this.value = value;
	}

	@Override
	public V get() {
		return shareLocked(() -> this.value);
	}

	@Override
	public void accept(V value) {
		mutexLocked(() -> this.value = value);
	}

	public void set(V value) {
		accept(value);
	}

	public <M> M map(Function<V, M> mapper) {
		Objects.requireNonNull(mapper, "Must provide a mapper function");
		return mapper.apply(get());
	}

	public void recompute(Function<V, V> computer) {
		Objects.requireNonNull(computer, "Must provide a computation function");
		try (MutexAutoLock lock = autoMutexLock()) {
			set(computer.apply(get()));
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this, get());
	}

	@Override
	public boolean equals(Object o) {
		if (!Tools.baseEquals(this, o)) { return false; }
		MutableBox<?> other = MutableBox.class.cast(o);
		return Objects.equals(get(), other.get());
	}

	public static <V> MutableBox<V> of(V value) {
		return new MutableBox<>(value);
	}
}