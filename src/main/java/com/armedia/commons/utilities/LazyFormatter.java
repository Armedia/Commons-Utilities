/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (c) 2010 - 2019 Armedia LLC
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

public final class LazyFormatter implements Supplier<String> {

	private final Supplier<String> supplier;
	private volatile String value = null;

	private LazyFormatter(Supplier<String> str) {
		this.supplier = Objects.requireNonNull(str, "Must provide a non-null String Supplier instance");
	}

	private LazyFormatter(String str) {
		this.value = Objects.requireNonNull(str, "Must provide a non-null String instance");
		this.supplier = null;
	}

	public static LazyFormatter lazyFormat(String format, Object... args) {
		Objects.requireNonNull(format, "Must provide a non-null formatter string");
		if ((args == null) || (args.length == 0)) { return LazyFormatter.lazyFormat(format); }
		return LazyFormatter.lazyFormat(() -> String.format(format, args));
	}

	public static LazyFormatter lazyFormat(Supplier<String> str) {
		return new LazyFormatter(str);
	}

	@Override
	public String get() {
		return toString();
	}

	@Override
	public String toString() {
		String localValue = this.value;
		if (localValue == null) {
			synchronized (this) {
				localValue = this.value;
				if (localValue == null) {
					this.value = localValue = this.supplier.get();
				}
			}
		}
		return localValue;
	}
}