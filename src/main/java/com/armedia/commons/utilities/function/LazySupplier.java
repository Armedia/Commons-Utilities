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
package com.armedia.commons.utilities.function;

import java.util.function.Supplier;

public class LazySupplier<T> extends CheckedLazySupplier<T, RuntimeException> {

	public LazySupplier() {
		this(null, null);
	}

	public LazySupplier(Supplier<T> defaultInitializer) {
		this(defaultInitializer, null);
	}

	public LazySupplier(T defaultValue) {
		this(null, defaultValue);
	}

	@Override
	public T get() {
		return super.getChecked();
	}

	@Override
	public T get(Supplier<T> initializer) {
		return super.getChecked(initializer != null ? CheckedTools.check(initializer) : null);
	}

	public LazySupplier(Supplier<T> defaultInitializer, T defaultValue) {
		super(defaultInitializer != null ? CheckedTools.check(defaultInitializer) : null, defaultValue);
	}

	public static <T> LazySupplier<T> from(Supplier<T> defaultInitializer) {
		return LazySupplier.from(defaultInitializer, null);
	}

	public static <T> LazySupplier<T> from(Supplier<T> defaultInitializer, T defaultValue) {
		return new LazySupplier<>((defaultInitializer != null ? () -> defaultInitializer.get() : null), defaultValue);
	}
}
