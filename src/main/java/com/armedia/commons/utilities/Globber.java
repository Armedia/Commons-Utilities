/**
 * *******************************************************************
 *
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS. REPRODUCTION OF ANY PORTION
 * OF THE SOURCE CODE, CONTAINED HEREIN, OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE, IS
 * STRICTLY PROHIBITED.
 *
 * Confidential Property of Armedia LLC. (c) Copyright Armedia LLC 2011-2011. All Rights reserved.
 *
 * *******************************************************************
 */
package com.armedia.commons.utilities;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Utility class that converts a globbing pattern (wildcard match with *, ?, {}, etc) to a regular
 * expression.
 *
 * @author drivera@armedia.com
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