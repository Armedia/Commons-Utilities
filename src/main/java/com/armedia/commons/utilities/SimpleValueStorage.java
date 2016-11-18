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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author drivera@armedia.com
 *
 */
public class SimpleValueStorage<T> implements ValueStorage<T> {

	private final Map<String, T> storage;

	public SimpleValueStorage() {
		this(false);
	}

	public SimpleValueStorage(boolean ordered) {
		Map<String, T> m = (ordered ? new TreeMap<String, T>() : new HashMap<String, T>());
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
		return new TreeSet<String>(this.storage.keySet());
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
