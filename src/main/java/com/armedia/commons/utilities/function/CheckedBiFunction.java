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
import java.util.function.BiFunction;

@FunctionalInterface
public interface CheckedBiFunction<T, U, R, EX extends Throwable> extends BiFunction<T, U, R> {

	public R applyChecked(T t, U u) throws EX;

	@Override
	public default R apply(T t, U u) {
		try {
			return applyChecked(t, u);
		} catch (Throwable thrown) {
			throw new RuntimeException(thrown.getMessage(), thrown);
		}
	}

	default <V> CheckedBiFunction<T, U, V, EX> andThen(CheckedFunction<? super R, ? extends V, ? extends EX> after) {
		Objects.requireNonNull(after);
		return (T t, U u) -> after.applyChecked(applyChecked(t, u));
	}

}
