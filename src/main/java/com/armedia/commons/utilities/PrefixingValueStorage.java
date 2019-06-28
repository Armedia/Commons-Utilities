/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (C) 2013 - 2019 Armedia
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

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 *
 */
public class PrefixingValueStorage<T> implements ValueStorage<T> {

	private static final String PREFIX_FORMAT = "%s.%%s";
	private static final String PREFIX_PATTERN = "^\\Q%s.\\E(.*)$";

	private final String format;
	private final Pattern pattern;
	private final ValueStorage<T> storage;

	public PrefixingValueStorage(ValueStorage<T> storage, String prefix) {
		if (storage == null) { throw new IllegalArgumentException("Storage argument may not be null"); }
		this.storage = storage;
		prefix = Tools.toString(prefix, true);
		if (prefix == null) {
			this.format = null;
			this.pattern = null;
		} else {
			this.format = String.format(PrefixingValueStorage.PREFIX_FORMAT, prefix);
			this.pattern = Pattern.compile(String.format(PrefixingValueStorage.PREFIX_PATTERN, prefix));
		}
	}

	protected String getName(String name) {
		if (name == null) { throw new IllegalArgumentException("Name may not be null"); }
		if (this.format == null) { return name; }
		return String.format(this.format, name);
	}

	@Override
	public T setValue(String name, T value) {
		return this.storage.setValue(getName(name), value);
	}

	@Override
	public T getValue(String name) {
		return this.storage.getValue(getName(name));
	}

	@Override
	public boolean hasValue(String name) {
		return this.storage.hasValue(getName(name));
	}

	@Override
	public Set<String> getValueNames() {
		Set<String> names = new TreeSet<>();
		for (String str : this.storage.getValueNames()) {
			Matcher m = this.pattern.matcher(str);
			if (m.matches()) {
				names.add(m.group(1));
			}
		}
		return Collections.unmodifiableSet(names);
	}

	@Override
	public T clearValue(String name) {
		return this.storage.clearValue(getName(name));
	}

	@Override
	public void clearAllValues() {
		for (String str : this.storage.getValueNames()) {
			Matcher m = this.pattern.matcher(str);
			if (m.matches()) {
				this.storage.clearValue(str);
			}
		}
	}
}
