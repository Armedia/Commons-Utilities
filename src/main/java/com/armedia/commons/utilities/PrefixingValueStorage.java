/**
 * *******************************************************************
 * 
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS. REPRODUCTION OF ANY PORTION
 * OF THE SOURCE CODE, CONTAINED HEREIN, OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE, IS
 * STRICTLY PROHIBITED.
 * 
 * Confidential Property of Armedia LLC. (c) Copyright Armedia LLC 2011. All Rights reserved.
 * 
 * *******************************************************************
 */
package com.armedia.commons.utilities;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author drivera@armedia.com
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
		Set<String> names = new TreeSet<String>();
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