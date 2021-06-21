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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.text.StringSubstitutor;

/**
 * This serves as a utility string substitutor, which allows parameterized strings to be easily
 * implemented using utility classes from Commons Lang v3. Importantly, it doesn't execute the
 * subsitution until the {@link #evaluate()} method is called, and therefore it doesn't support
 * nested substitution. Parameters are specified in the typical <code>${name}</code> format. One
 * other note of importance is that {@code null}-values are not supported in the sense that if you
 * wish a parameter to be substituted for the word {@code "null"}, then you must explicitly set the
 * parameter to the string {@code "null"}. Setting a parameter to the {@code null} value has the
 * effect that the parameter will no longer be substituted in the string.
 *
 *
 *
 */
public class ParameterizedString implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String pattern;
	private final Map<String, String> parameters;

	public ParameterizedString(String pattern) {
		if (pattern == null) { throw new IllegalArgumentException("The pattern may not be null"); }
		this.pattern = pattern;
		this.parameters = new HashMap<>();
	}

	public ParameterizedString getNewCopy() {
		return new ParameterizedString(this.pattern);
	}

	public String getPattern() {
		return this.pattern;
	}

	public Set<String> getParameterNames() {
		return new HashSet<>(this.parameters.keySet());
	}

	public String getParameterValue(String name) {
		return this.parameters.get(name);
	}

	public void clearParameters() {
		this.parameters.clear();
	}

	public int getParameterCount() {
		return this.parameters.size();
	}

	/**
	 * Assigns the given parameter for subsitution. If {@code value} is {@code null} then this
	 * invocation is identical to {@code clear(name)}.
	 *
	 * @param name
	 * @param value
	 * @return this {@link ParameterizedString} instance
	 */
	public ParameterizedString set(String name, String value) {
		if (name != null) {
			if (value == null) { return clear(name); }
			this.parameters.put(name, value);
		}
		return this;
	}

	/**
	 * Returns {@code true} if the named parameter has been set on this instance.
	 *
	 * @param name
	 * @return {@code true} if the named parameter has been set on this instance
	 */
	public boolean isSet(String name) {
		return this.parameters.containsKey(name);
	}

	/**
	 * Removes the given parameter's value from this instance. The parameter will no longer be
	 * substituted.
	 *
	 * @param name
	 * @return this {@link ParameterizedString} instance
	 */
	public ParameterizedString clear(String name) {
		if (name != null) {
			this.parameters.remove(name);
		}
		return this;
	}

	/**
	 * Returns the pattern string with all the parameters substituted to the values set via
	 * {@link #set(String, String)}, and without substituting those that haven't been set.
	 *
	 * @return The evaluated string, with all existent parameters substituted
	 */
	public String evaluate() {
		return StringSubstitutor.replace(this.pattern, this.parameters);
	}

	/**
	 * Returns the evaluated version of this instance. This returns the same result as invoking
	 * {@link #evaluate()} directly.
	 *
	 * @return the evaluated {@link ParameterizedString}
	 */
	@Override
	public String toString() {
		return evaluate();
	}
}
