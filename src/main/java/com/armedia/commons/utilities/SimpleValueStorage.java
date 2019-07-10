/*******************************************************************************
 * #%L
 * Armedia Caliente
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 *
 */
public class SimpleValueStorage<T> implements ValueStorage<T> {

	private final Map<String, T> storage;

	public SimpleValueStorage() {
		this(false);
	}

	public SimpleValueStorage(boolean ordered) {
		Map<String, T> m = (ordered ? new TreeMap<>() : new HashMap<>());
		this.storage = m;
	}

	@Override
	public T setValue(String name, T value) {
		return this.storage.put(name, value);
	}

	@Override
	public T getValue(String name) {
		return this.storage.get(name);
	}

	@Override
	public boolean hasValue(String name) {
		return this.storage.containsKey(name);
	}

	@Override
	public Set<String> getValueNames() {
		return new TreeSet<>(this.storage.keySet());
	}

	@Override
	public T clearValue(String name) {
		return this.storage.remove(name);
	}

	@Override
	public void clearAllValues() {
		this.storage.clear();
	}
}
