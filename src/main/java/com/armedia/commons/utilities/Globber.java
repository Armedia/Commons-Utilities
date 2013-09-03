/**
 * *******************************************************************
 * 
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS.
 * REPRODUCTION OF ANY PORTION OF THE SOURCE CODE, CONTAINED HEREIN,
 * OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE,
 * IS STRICTLY PROHIBITED.
 * 
 * Confidential Property of Armedia LLC.
 * (c) Copyright Armedia LLC 2011-2011.
 * All Rights reserved.
 * 
 * *******************************************************************
 */
package com.armedia.commons.utilities;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Utility class that converts a globbing pattern (wildcard match with *, ?, {}, etc) to a
 * regular expression.
 * 
 * @author drivera@armedia.com
 * 
 */
public class Globber {

	private static final boolean DEFAULT_COMPLETE = true;
	private static final int DEFAULT_OPTIONS = 0;

	/**
	 * Converts the given glob into a regular expression pattern. This pattern can then
	 * be used to for string matching. The pattern returned will match the entire pattern
	 * space (i.e. starts with ^ and ends with $).
	 * 
	 * @param glob
	 * @return a {@link Pattern} instance that can be used to match the given glob.
	 */
	public static Pattern asPattern(String glob) {
		return Globber.asPattern(glob, Globber.DEFAULT_COMPLETE, Globber.DEFAULT_OPTIONS);
	}

	/**
	 * Converts the given glob into a regular expression pattern. If {@code complete} is true, the
	 * resulting pattern is constructed such that it matches the entire pattern space (i.e. starts
	 * with ^ and ends with $).
	 * 
	 * @param glob
	 * @param complete
	 * @return {@link Pattern} instance that can be used to match the given glob
	 */
	public static Pattern asPattern(String glob, boolean complete) {
		return Globber.asPattern(glob, complete, Globber.DEFAULT_OPTIONS);
	}

	/**
	 * Converts the given glob into a regular expression pattern with the selected
	 * pattern options. The options are the same as can be fed into
	 * {@link Pattern#compile(String, int)}, and are intended to be added to the returned pattern
	 * upon compilation.
	 * 
	 * @param glob
	 * @param patternOptions
	 * @return {@link Pattern} instance that can be used to match the given glob
	 */
	public static Pattern asPattern(String glob, int patternOptions) {
		return Globber.asPattern(glob, Globber.DEFAULT_COMPLETE, patternOptions);
	}

	/**
	 * Converts the given glob into a regular expression pattern with the selected
	 * pattern options. The options are the same as can be fed into
	 * {@link Pattern#compile(String, int)}, and are intended to be added to the returned pattern
	 * upon compilation.If {@code complete} is true, the resulting pattern is constructed such that
	 * it matches the entire pattern space (i.e. starts with ^ and ends with $).
	 * 
	 * @param glob
	 * @param complete
	 * @param patternOptions
	 * @throws PatternSyntaxException
	 *             if the resulting pattern is not a valid regular expression (possible, but
	 *             unlikely)
	 * @return {@link Pattern} instance that can be used to match the given glob
	 */
	public static Pattern asPattern(String glob, boolean complete, int patternOptions) {
		return Pattern.compile(Globber.asRegex(glob, complete), patternOptions);
	}

	/**
	 * Converts the given glob into a regular expression string.
	 * 
	 * @param glob
	 * @return regular expression string that can be used to match the given glob
	 */
	public static String asRegex(String glob) {
		return Globber.asRegex(glob, Globber.DEFAULT_COMPLETE);
	}

	/**
	 * Converts the given glob into a regular expression string. If {@code complete} is true, the
	 * resulting expression is constructed such that it matches the entire pattern space (i.e.
	 * starts with ^ and ends with $).
	 * 
	 * @param glob
	 * @param complete
	 * @return regular expression string that can be used to match the given glob
	 */
	public static String asRegex(String glob, boolean complete) {
		glob = glob.trim();
		int length = glob.length();
		StringBuilder buf = new StringBuilder(length);
		if (complete) {
			buf.append("^");
		}
		boolean escape = false;
		int braces = 0;
		for (char current : glob.toCharArray()) {
			switch (current) {
				case '*':
					if (escape) {
						buf.append("\\*");
					} else {
						buf.append(".*");
					}
					escape = false;
					break;
				case '?':
					if (escape) {
						buf.append("\\?");
					} else {
						buf.append('.');
					}
					escape = false;
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
					break;
				case '\\':
					if (escape) {
						buf.append("\\\\");
						escape = false;
					} else {
						escape = true;
					}
					break;
				case '{':
					if (escape) {
						buf.append("\\{");
					} else {
						buf.append('(');
						braces++;
					}
					escape = false;
					break;
				case '}':
					if ((braces > 0) && !escape) {
						buf.append(')');
						braces--;
					} else if (escape) {
						buf.append("\\}");
					} else {
						buf.append("}");
					}
					escape = false;
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
					escape = false;
					buf.append(current);
			}
		}
		if (complete) {
			buf.append("$");
		}
		return buf.toString();
	}
}