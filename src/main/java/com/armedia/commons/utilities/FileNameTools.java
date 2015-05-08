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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author drivera@armedia.com
 *
 */
public class FileNameTools {

	/**
	 * Split the string {@code str} at every {@code sep} character, and add each token found to the
	 * collection {@code c}. Importantly, no empty ({@code ""}) elements will be added.
	 *
	 * @param c
	 * @param str
	 * @param sep
	 */
	public static void tokenize(Collection<String> c, String str, char sep) {
		// First, does the path start or end with a separator
		if (c == null) { throw new IllegalArgumentException("Target Collection may not be null"); }
		if (str == null) { throw new IllegalArgumentException("String to tokenize may not be null"); }
		String sepStr = String.valueOf(sep);

		StringTokenizer tok = new StringTokenizer(str, sepStr);
		while (tok.hasMoreTokens()) {
			c.add(tok.nextToken());
		}
	}

	/**
	 * This is equivalent to calling {@link #tokenize(Collection, String, char)} with
	 * {@link File#separatorChar} as the {@code sep} parameter.
	 *
	 * @param c
	 * @param str
	 */
	public static void tokenize(Collection<String> c, String str) {
		FileNameTools.tokenize(c, str, File.separatorChar);
	}

	/**
	 * Returns a new {@link List} containing all of the tokens in {@code str}, as separated by the
	 * {@code sep} character. This is a convenience call to
	 * {@link #tokenize(Collection, String, char)}.
	 *
	 * @param str
	 * @param sep
	 * @return a new {@link List} containing all of the tokens in {@code str}, as separated by the
	 *         {@code sep} character
	 */
	public static List<String> tokenize(String str, char sep) {
		List<String> l = new ArrayList<String>();
		FileNameTools.tokenize(l, str, sep);
		return l;
	}

	/**
	 * This is equivalent to calling {@link #tokenize(String, char)} with {@link File#separatorChar}
	 * as the {@code sep} parameter.
	 *
	 * @param str
	 * @return a new {@link List} containing all of the tokens in {@code str}, as separated by the
	 *         {@code File#separatorChar} character
	 */
	public static List<String> tokenize(String str) {
		List<String> l = new ArrayList<String>();
		FileNameTools.tokenize(l, str);
		return l;
	}

	/**
	 * Reconstitutes the given path components, separated by the given separator character. The
	 * parameter {@code leading} controls whether the resulting string will or will not have a
	 * leading separator. Similarly, the parameter {@code trailing} controls the trailing separator.
	 * Empty string elements, or {@code null} elements are omitted.
	 *
	 * @param components
	 * @param leading
	 * @param trailing
	 * @param sep
	 * @return the reconstituted path from the given components, separated by the given separator
	 *         character
	 */
	public static String reconstitute(Collection<String> components, boolean leading, boolean trailing, char sep) {
		if (components == null) { throw new IllegalArgumentException("Components to reconstitute may not be null"); }
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (String s : components) {
			if ((s == null) || (s.length() == 0)) {
				continue;
			}
			if (leading || !first) {
				b.append(sep);
			}
			b.append(s);
			first = false;
		}
		if (trailing) {
			b.append(sep);
		}
		return b.toString();
	}

	/**
	 * This is equivalent to calling {@link #reconstitute(Collection, boolean, boolean, char)} with
	 * {@link File#separatorChar} as the {@code sep} parameter.
	 *
	 * @param components
	 * @param leading
	 * @return the reconstituted path from the given components, separated by the
	 *         {@link File#separatorChar} character
	 */
	public static String reconstitute(Collection<String> components, boolean leading, boolean trailing) {
		return FileNameTools.reconstitute(components, leading, trailing, File.separatorChar);
	}

	/**
	 * <p>
	 * Like its name says, returns a version of the string with any leading or trailing separator
	 * characters {@code sep} removed. The separator character can be any character.
	 * </p>
	 * <p>
	 * This is useful when sanitizing paths to a specific format.
	 * </p>
	 *
	 * @param str
	 * @return a version of the string with any leading or trailing separator characters {@code sep}
	 *         removed
	 */
	public static String removeEdgeSeparators(String str, char sep) {
		if (str == null) { return null; }
		return FileNameTools.removeLeadingSeparators(FileNameTools.removeTrailingSeparators(str, sep), sep);
	}

	/**
	 * Identical to invoking {@code removeEdgeSeparators(str, File.separatorChar)}.
	 *
	 * @param str
	 * @return a version of the string with any leading or trailing {@link File#separatorChar}
	 *         instances removed
	 */
	public static String removeEdgeSeparators(String str) {
		return FileNameTools.removeEdgeSeparators(str, File.separatorChar);
	}

	/**
	 * <p>
	 * Like its name says, returns a version of the string with any leading separator characters
	 * {@code sep} removed. The separator character can be any character.
	 * </p>
	 * <p>
	 * This is useful when sanitizing paths to a specific format.
	 * </p>
	 *
	 * @param str
	 * @return a version of the string with any leading separator characters {@code sep} removed
	 */
	public static String removeLeadingSeparators(String str, char sep) {
		if (str == null) { return null; }
		int start = 0;
		final int length = str.length();
		while ((start < length) && (str.charAt(start) == sep)) {
			start++;
		}
		return str.substring(start);
	}

	/**
	 * Identical to invoking {@code removeLeadingSeparators(str, File.separatorChar)}.
	 *
	 * @param str
	 * @return a version of the string with any leading {@link File#separatorChar} instances removed
	 */
	public static String removeLeadingSeparators(String str) {
		return FileNameTools.removeLeadingSeparators(str, File.separatorChar);
	}

	/**
	 * <p>
	 * Like its name says, returns a version of the string with any trailing separator characters
	 * {@code sep} removed. The separator character can be any character.
	 * </p>
	 * <p>
	 * This is useful when sanitizing paths to a specific format.
	 * </p>
	 *
	 * @param str
	 * @return a version of the string with any trailing separator characters {@code sep} removed
	 */
	public static String removeTrailingSeparators(String str, char sep) {
		if (str == null) { return null; }
		int s = str.length() - 1;
		while ((s >= 0) && (str.charAt(s) == sep)) {
			s--;
		}
		return str.substring(0, s + 1);
	}

	/**
	 * Identical to invoking {@code removeTrailingSeparators(str, File.separatorChar)}.
	 *
	 * @param str
	 * @return a version of the string with any trailing {@link File#separatorChar} instances
	 *         removed
	 */
	public static String removeTrailingSeparators(String str) {
		return FileNameTools.removeTrailingSeparators(str, File.separatorChar);
	}

	/**
	 * Removes repeated separators from a path, such that there is exactly a single path separator
	 * between two adjacent path components.
	 *
	 * @param str
	 * @return a version of the string with multiple consecutive {@code sep} characters collapsed
	 *         into a single instance
	 */
	public static String singleSeparators(String str, char sep) {
		if (str == null) { return null; }
		return Tools.consolidateRepeatedCharacters(str, sep);
	}

	/**
	 * Removes repeated separators from a path, such that there is exactly a single path separator
	 * between two adjacent path components.
	 *
	 * @param str
	 * @return a version of the string with multiple consecutive {@code File#separatorChar}
	 *         instances collapsed into a single instance
	 */
	public static String singleSeparators(String str) {
		return FileNameTools.singleSeparators(str, File.separatorChar);
	}

	/**
	 * Analogous to its *nix namesake, returns the given pathname's base name, with all directory
	 * components removed, using {@code sep} as the directory separator character.
	 *
	 * @param fullPath
	 * @param sep
	 * @return the given pathname's base name, with all preceding directory components removed
	 */
	public static String basename(String fullPath, char sep) {
		fullPath = FileNameTools.removeTrailingSeparators(fullPath, sep);
		if (fullPath.length() == 0) { return String.valueOf(sep); }

		int s = fullPath.lastIndexOf(sep);
		if (s == -1) { return fullPath; }
		fullPath = FileNameTools.removeLeadingSeparators(fullPath.substring(s), sep);
		if (fullPath.length() == 0) { return String.valueOf(sep); }
		return fullPath;
	}

	/**
	 * Analogous to its *nix namesake, returns the given pathname's base name, with all directory
	 * components removed, using {@link File#separatorChar} as the directory separator character.
	 *
	 * @param fullPath
	 * @return the given pathname's base name, with all preceding directory components removed
	 */
	public static String basename(String fullPath) {
		return FileNameTools.basename(fullPath, File.separatorChar);
	}

	/**
	 * Analogous to its *nix namesake, returns the directory name in which the given pathname
	 * resides, using {@code sep} as the directory separator character.
	 *
	 * @param fullPath
	 * @param sep
	 * @return the directory name in which the given pathname resides
	 */
	public static String dirname(String fullPath, char sep) {
		fullPath = FileNameTools.removeTrailingSeparators(fullPath, sep);
		if (fullPath.length() == 0) { return String.valueOf(sep); }

		int s = fullPath.lastIndexOf(sep);
		if (s == -1) { return "."; }
		fullPath = FileNameTools.removeTrailingSeparators(fullPath.substring(0, s), sep);
		if (fullPath.length() == 0) { return String.valueOf(sep); }
		return fullPath;
	}

	/**
	 * Analogous to its *nix namesake, returns the directory name in which the given pathname
	 * resides, using {@link File#separatorChar} as the directory separator character.
	 *
	 * @param fullPath
	 * @return the directory name in which the given pathname resides
	 */
	public static String dirname(String fullPath) {
		return FileNameTools.dirname(fullPath, File.separatorChar);
	}
}