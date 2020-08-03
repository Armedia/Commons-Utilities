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
package com.armedia.commons.utilities.concurrent;

import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;

import com.armedia.commons.utilities.function.CheckedFunction;
import com.armedia.commons.utilities.function.CheckedTools;

public class ConcurrentTools {

	/**
	 * <p>
	 * Added to provide a simpler, checked version of
	 * {@link ConcurrentUtils#createIfAbsent(ConcurrentMap, Object, org.apache.commons.lang3.concurrent.ConcurrentInitializer)}
	 * which will simplify exception handling
	 * </p>
	 */
	public static <K, V, E extends Throwable> V createIfAbsent(final ConcurrentMap<K, V> map, final K key,
		final CheckedFunction<K, V, E> initializer) throws E {
		if ((map == null) || (initializer == null)) { return null; }

		final V value = map.get(key);
		if (value == null) { return ConcurrentUtils.putIfAbsent(map, key, initializer.applyChecked(key)); }
		return value;
	}

	/**
	 * <p>
	 * Added to provide a simpler version of
	 * {@link ConcurrentUtils#createIfAbsent(ConcurrentMap, Object, org.apache.commons.lang3.concurrent.ConcurrentInitializer)}
	 * which will simplify exception handling by not requiring it where not needed
	 * </p>
	 */
	public static <K, V> V createIfAbsent(final ConcurrentMap<K, V> map, final K key,
		final Function<K, V> initializer) {
		return ConcurrentTools.createIfAbsent(map, key, CheckedTools.check(initializer));
	}
}
