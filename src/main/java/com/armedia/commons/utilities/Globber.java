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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Utility class that converts a globbing pattern (wildcard match with *, ?, {}, etc) to a regular
 * expression.
 *
 *
 *
 */
public class Globber {

	private static final int DEFAULT_OPTIONS = 0;

	/**
	 * Converts the given glob into a regular expression pattern. This pattern can then be used to
	 * for string matching. The pattern returned will match the entire pattern space (i.e. starts
	 * with ^ and ends with $).
	 *
	 * @param glob
	 * @return a {@link Pattern} instance that can be used to match the given glob.
	 */
	public static Pattern asPattern(String glob) {
		return Globber.asPattern(glob, Globber.DEFAULT_OPTIONS);
	}

	/**
	 * Converts the given glob into a regular expression pattern with the selected pattern options.
	 * The options are the same as can be fed into {@link Pattern#compile(String, int)}, and are
	 * intended to be added to the returned pattern upon compilation.
	 *
	 * @param glob
	 * @param patternOptions
	 * @return {@link Pattern} instance that can be used to match the given glob
	 */
	public static Pattern asPattern(String glob, int patternOptions) {
		return Pattern.compile(Globber.asRegex(glob), patternOptions);
	}

	/**
	 * Converts the given glob into a regular expression string. If {@code complete} is true, the
	 * resulting expression is constructed such that it matches the entire pattern space (i.e.
	 * starts with ^ and ends with $).
	 *
	 * @param glob
	 * @return regular expression string that can be used to match the given glob
	 */
	public static String asRegex(String glob) {
		glob = glob.trim();
		int length = glob.length();
		StringBuilder buf = new StringBuilder(length);
		buf.append("^");
		int braceIndex = 0;
		int escapeIndex = 0;
		boolean escape = false;
		int braces = 0;
		int pos = -1;
		for (char current : glob.toCharArray()) {
			pos++;
			switch (current) {
				case '*':
					if (escape) {
						buf.append("\\*");
					} else {
						buf.append(".*");
					}
					escape = false;
					escapeIndex = -1;
					break;
				case '?':
					if (escape) {
						buf.append("\\?");
					} else {
						buf.append('.');
					}
					escape = false;
					escapeIndex = -1;
					break;
				case '.':
				case '(':
				case ')':
				case '+':
				case '|':
				case '^':
				case '$':
				case '@':
				case '%':
					buf.append('\\');
					buf.append(current);
					escape = false;
					escapeIndex = -1;
					break;
				case '\\':
					if (escape) {
						buf.append("\\\\");
						escape = false;
						escapeIndex = -1;
					} else {
						escape = true;
						escapeIndex = pos;
					}
					break;
				case '{':
					if (escape) {
						buf.append("\\{");
					} else {
						buf.append('(');
						braces++;
						braceIndex = pos;
					}
					escape = false;
					escapeIndex = -1;
					break;
				case '}':
					if (escape || (braces == 0)) {
						buf.append("\\}");
					} else {
						buf.append(')');
						braces--;
					}
					escape = false;
					escapeIndex = -1;
					break;
				case ',':
					if ((braces > 0) && !escape) {
						buf.append('|');
					} else if (escape) {
						buf.append("\\,");
					} else {
						buf.append(",");
					}
					break;
				default:
					buf.append(current);
					escape = false;
					escapeIndex = -1;
			}
		}
		buf.append("$");
		if (braces > 0) { throw new PatternSyntaxException("Unclosed brace", glob, braceIndex); }
		if (escape) { throw new PatternSyntaxException("Dangling escape character", glob, escapeIndex); }
		return buf.toString();
	}
}
