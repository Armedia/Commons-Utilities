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
import java.util.function.BiPredicate;

@FunctionalInterface
public interface CheckedBiPredicate<T, U, EX extends Throwable> extends BiPredicate<T, U> {

	public boolean testChecked(T t, U u) throws EX;

	@Override
	public default boolean test(T t, U u) {
		try {
			return testChecked(t, u);
		} catch (Throwable thrown) {
			throw new RuntimeException(thrown.getMessage(), thrown);
		}
	}

	default CheckedBiPredicate<T, U, EX> and(CheckedBiPredicate<? super T, ? super U, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (T t, U u) -> testChecked(t, u) && other.testChecked(t, u);
	}

	@Override
	default CheckedBiPredicate<T, U, EX> negate() {
		return (T t, U u) -> !testChecked(t, u);
	}

	default CheckedBiPredicate<T, U, EX> or(CheckedBiPredicate<? super T, ? super U, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (T t, U u) -> testChecked(t, u) || other.testChecked(t, u);
	}

	default CheckedBiPredicate<T, U, EX> xor(CheckedBiPredicate<? super T, ? super U, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (T t, U u) -> testChecked(t, u) != other.testChecked(t, u);
	}

}
