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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.text.StrSubstitutor;

/**
 * This serves as a utility string substitutor, which allows parameterized strings to be easily
 * implemented using utility classes from Commons Lang v3. Importantly, it doesn't execute
 * the subsitution until the {@link #evaluate()} method is called, and therefore it doesn't
 * support nested substitution. Parameters are specified in the typical <code>${name}</code> format.
 * One other note of importance is that {@code null}-values are not supported in the sense that if
 * you wish a parameter to be substituted for the word {@code "null"}, then you must explicitly set
 * the parameter to the string {@code "null"}. Setting a parameter to the {@code null} value has the
 * effect that the parameter will no longer be substituted in the string.
 * 
 * @author drivera@armedia.com
 * 
 */
public class ParameterizedString implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String pattern;
	private final Map<String, String> parameters;

	public ParameterizedString(String pattern) {
		if (pattern == null) { throw new IllegalArgumentException("The pattern may not be null"); }
		this.pattern = pattern;
		this.parameters = new HashMap<String, String>();
	}

	public ParameterizedString getNewCopy() {
		return new ParameterizedString(this.pattern);
	}

	public String getPattern() {
		return this.pattern;
	}

	public Set<String> getParameterNames() {
		return new HashSet<String>(this.parameters.keySet());
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
	 * Assigns the given parameter for subsitution. If {@code value} is {@code null} then
	 * this invocation is identical to {@code clear(name)}.
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
	 * Removes the given parameter's value from this instance. The parameter will no longer
	 * be substituted.
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
		return StrSubstitutor.replace(this.pattern, this.parameters);
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