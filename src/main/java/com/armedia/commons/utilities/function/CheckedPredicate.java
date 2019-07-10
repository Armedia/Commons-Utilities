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
import java.util.function.Predicate;

@FunctionalInterface
public interface CheckedPredicate<T, EX extends Throwable> extends Predicate<T> {

	public boolean testChecked(T t) throws EX;

	@Override
	public default boolean test(T t) {
		try {
			return testChecked(t);
		} catch (Throwable thrown) {
			throw new RuntimeException(thrown.getMessage(), thrown);
		}
	}

	default CheckedPredicate<T, EX> and(CheckedPredicate<? super T, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (t) -> testChecked(t) && other.testChecked(t);
	}

	@Override
	default CheckedPredicate<T, EX> negate() {
		return (t) -> !testChecked(t);
	}

	default CheckedPredicate<T, EX> or(CheckedPredicate<? super T, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (t) -> testChecked(t) || other.testChecked(t);
	}

	default CheckedPredicate<T, EX> xor(CheckedPredicate<? super T, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (t) -> testChecked(t) != other.testChecked(t);
	}

}
