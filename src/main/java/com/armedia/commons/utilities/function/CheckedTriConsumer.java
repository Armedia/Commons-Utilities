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
package com.armedia.commons.utilities.function;

import java.util.Objects;

@FunctionalInterface
public interface CheckedTriConsumer<T, U, V, EX extends Throwable> extends TriConsumer<T, U, V> {

	public void acceptChecked(T t, U u, V v) throws EX;

	@Override
	public default void accept(T t, U u, V v) {
		try {
			acceptChecked(t, u, v);
		} catch (Throwable thrown) {
			throw new RuntimeException(thrown.getMessage(), thrown);
		}
	}

	default CheckedTriConsumer<T, U, V, EX> andThen(
		CheckedTriConsumer<? super T, ? super U, ? super V, ? extends EX> after) {
		Objects.requireNonNull(after);
		return (l, m, r) -> {
			acceptChecked(l, m, r);
			after.acceptChecked(l, m, r);
		};
	}

}
