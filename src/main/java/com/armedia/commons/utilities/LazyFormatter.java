/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2021 Armedia, LLC
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
import java.util.function.Supplier;

import com.armedia.commons.utilities.function.LazySupplier;

public final class LazyFormatter implements Supplier<String> {

	private final LazySupplier<String> value;

	private LazyFormatter(Supplier<String> str) {
		this.value = new LazySupplier<>(
			Objects.requireNonNull(str, "Must provide a non-null String Supplier instance"));
	}

	public static LazyFormatter of(String format, Object... args) {
		Objects.requireNonNull(format, "Must provide a non-null formatter string");
		if ((args == null) || (args.length == 0)) { return new LazyFormatter(() -> format); }
		return new LazyFormatter(() -> String.format(format, args));
	}

	public static LazyFormatter of(Supplier<?> message) {
		Objects.requireNonNull(message, "Must provide a message supplier");
		return new LazyFormatter(() -> String.valueOf(message.get()));
	}

	@Override
	public String get() {
		return toString();
	}

	@Override
	public String toString() {
		return this.value.get();
	}
}
