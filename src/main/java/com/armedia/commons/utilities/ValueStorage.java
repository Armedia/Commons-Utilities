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
package com.armedia.commons.utilities;

import java.util.Set;

/**
 * This interface represents a simplistic key-value pair storage mechanism.
 *
 *
 *
 */
public interface ValueStorage<T> {

	/**
	 * Assign the given value to the given name. Returns the previously stored value for the given
	 * name, or {@code null} if none was stored.
	 *
	 * @param name
	 * @param value
	 * @return the previously stored value for the given name, or {@code null} if none was stored
	 */
	public T setValue(String name, T value);

	/**
	 * Returns the value stored for the given name, or {@code null} if none was stored.
	 *
	 * @param name
	 * @return the value stored for the given name, or {@code null} if none was stored
	 */
	public T getValue(String name);

	/**
	 * Returns {@code true} if the given name has a value stored, {@code false} otherwise.
	 *
	 * @param name
	 * @return {@code true} if the given name has a value stored, {@code false} otherwise
	 */
	public boolean hasValue(String name);

	/**
	 * Returns a {@link Set} containing the names of all the values stored. The returned set is
	 * read-only.
	 *
	 * @return a read-only {@link Set} containing the names of all the values stored
	 */
	public Set<String> getValueNames();

	/**
	 * Removes the value for the given name. Returns the previously-stored value or {@code null} if
	 * none was stored.
	 *
	 * @param name
	 * @return the previously-stored value or {@code null} if none was stored
	 */
	public T clearValue(String name);

	/**
	 * Clears out all the names and their associated values.
	 */
	public void clearAllValues();
}
