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
package com.armedia.commons.utilities;

import java.util.Set;

/**
 *
 *
 */
public class SynchronizedValueStorage<T> extends SimpleValueStorage<T> {

	public SynchronizedValueStorage() {
		super();
	}

	public SynchronizedValueStorage(boolean ordered) {
		super(ordered);
	}

	@Override
	public synchronized T setValue(String name, T value) {
		return super.setValue(name, value);
	}

	@Override
	public synchronized T getValue(String name) {
		return super.getValue(name);
	}

	@Override
	public synchronized boolean hasValue(String name) {
		return super.hasValue(name);
	}

	@Override
	public synchronized Set<String> getValueNames() {
		return super.getValueNames();
	}

	@Override
	public synchronized T clearValue(String name) {
		return super.clearValue(name);
	}

	@Override
	public synchronized void clearAllValues() {
		super.clearAllValues();
	}
}
