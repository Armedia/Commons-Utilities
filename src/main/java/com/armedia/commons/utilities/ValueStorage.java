/**
 * *******************************************************************
 * 
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS.
 * REPRODUCTION OF ANY PORTION OF THE SOURCE CODE, CONTAINED HEREIN,
 * OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE,
 * IS STRICTLY PROHIBITED.
 * 
 * Confidential Property of Armedia LLC.
 * (c) Copyright Armedia LLC 2011.
 * All Rights reserved.
 * 
 * *******************************************************************
 */
package com.armedia.commons.utilities;

import java.util.Set;

/**
 * This interface represents a simplistic key-value pair storage mechanism.
 * 
 * @author drivera@armedia.com
 * 
 */
public interface ValueStorage<T> {

	/**
	 * Assign the given value to the given name. Returns the previously stored
	 * value for the given name, or {@code null} if none was stored.
	 * 
	 * @param name
	 * @param value
	 * @return the previously stored
	 *         value for the given name, or {@code null} if none was stored
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
	 * Returns a {@link Set} containing the names of all the values stored. The returned set
	 * is read-only.
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