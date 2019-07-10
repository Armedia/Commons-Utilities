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
package com.armedia.commons.utilities.function;

import java.util.Objects;

@FunctionalInterface
public interface CheckedTriPredicate<T, U, V, EX extends Throwable> extends TriPredicate<T, U, V> {

	public boolean testChecked(T t, U u, V v) throws EX;

	@Override
	public default boolean test(T t, U u, V v) {
		try {
			return testChecked(t, u, v);
		} catch (Throwable thrown) {
			throw new RuntimeException(thrown.getMessage(), thrown);
		}
	}

	default CheckedTriPredicate<T, U, V, EX> and(
		CheckedTriPredicate<? super T, ? super U, ? super V, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (T t, U u, V v) -> testChecked(t, u, v) && other.testChecked(t, u, v);
	}

	@Override
	default CheckedTriPredicate<T, U, V, EX> negate() {
		return (T t, U u, V v) -> !testChecked(t, u, v);
	}

	default CheckedTriPredicate<T, U, V, EX> or(
		CheckedTriPredicate<? super T, ? super U, ? super V, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (T t, U u, V v) -> testChecked(t, u, v) || other.testChecked(t, u, v);
	}

	default CheckedTriPredicate<T, U, V, EX> xor(
		CheckedTriPredicate<? super T, ? super U, ? super V, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (T t, U u, V v) -> testChecked(t, u, v) != other.testChecked(t, u, v);
	}

}
