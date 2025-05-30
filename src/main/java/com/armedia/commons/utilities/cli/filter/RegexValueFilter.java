/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2025 Armedia, LLC
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
package com.armedia.commons.utilities.cli.filter;

import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.armedia.commons.utilities.cli.OptionValueFilter;

public class RegexValueFilter extends OptionValueFilter {

	private final Pattern pattern;
	private final String description;

	public RegexValueFilter(String regex) {
		this(true, regex, null);
	}

	public RegexValueFilter(boolean caseSensitive, String regex) {
		this(caseSensitive, regex, null);
	}

	public RegexValueFilter(String regex, String description) {
		this(true, regex, description);
	}

	public RegexValueFilter(boolean caseSensitive, String regex, String description) {
		this(caseSensitive, Pattern.compile(regex, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE), description);
	}

	public RegexValueFilter(Pattern pattern) {
		this(true, pattern, null);
	}

	public RegexValueFilter(boolean caseSensitive, Pattern pattern) {
		this(caseSensitive, pattern, null);
	}

	public RegexValueFilter(Pattern pattern, String description) {
		this(true, pattern, description);
	}

	public RegexValueFilter(boolean caseSensitive, Pattern pattern, String description) {
		this.pattern = Objects.requireNonNull(pattern, "Must provide a regular expression pattern to use");
		if (StringUtils.isBlank(description)) {
			description = String.format("a string that matches the regex /%s/%s", this.pattern.pattern(),
				caseSensitive ? "" : " (case insensitively)");
		}
		this.description = description;
	}

	public boolean isCaseSensitive() {
		return ((this.pattern.flags() | Pattern.CASE_INSENSITIVE) == 0);
	}

	public Pattern getPattern() {
		return this.pattern;
	}

	@Override
	protected boolean checkValue(String value) {
		return this.pattern.matcher(value).matches();
	}

	@Override
	public String getDefinition() {
		return this.description;
	}
}
