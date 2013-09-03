/*
 * Erudicity Toolkit: src/main/java/com/erudicity/toolkit/CompareUtil.java
 * 
 * Copyright (C) 2010 Erudicity
 * 
 * This file is part of the Erudicity Toolkit.
 * 
 * The Erudicity Toolkit is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Erudicity Toolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.armedia.commons.utilities;

/**
 * This class contains multiple static methods that facilitate the comparison of two objects. In the
 * cases
 * where references are compared, null-valued references are properly handled (i.e. null !=
 * non-null, and null == null).
 * 
 * @author diego
 * 
 */
public class CompareUtil {

	/**
	 * Compares the provided references for equality, using equals(), but also taking into account
	 * null-values such that the method returns true if both references are null, or both are
	 * non-null and
	 * invoking a.equals(b) returns true.
	 * 
	 * @param a
	 * @param b
	 * 
	 */
	public static boolean equals(Object a, Object b) {
		if (a == b) { return true; }
		if (a == null) { return false; }
		if (b == null) { return false; }
		return a.equals(b);
	}

	/**
	 * Compares the provided Strings for equality, using equals(), but also taking into account
	 * null-values such that the method returns true if both Strings are null, or both are non-null
	 * and
	 * invoking a.equals(b) returns true.
	 * 
	 * @param a
	 * @param b
	 * 
	 */
	public static boolean equalsIgnoreCase(String a, String b) {
		if (a == b) { return true; }
		if (a == null) { return false; }
		if (b == null) { return false; }
		return a.equalsIgnoreCase(b);
	}

	/**
	 * Returns true if a == b
	 * 
	 * @param a
	 * @param b
	 * 
	 */
	public static boolean equals(boolean a, boolean b) {
		return (a == b);
	}

	/**
	 * Returns true if a == b
	 * 
	 * @param a
	 * @param b
	 * 
	 */
	public static boolean equals(byte a, byte b) {
		return (a == b);
	}

	/**
	 * Returns true if a == b
	 * 
	 * @param a
	 * @param b
	 * 
	 */
	public static boolean equals(short a, short b) {
		return (a == b);
	}

	/**
	 * Returns true if a == b
	 * 
	 * @param a
	 * @param b
	 * 
	 */
	public static boolean equals(int a, int b) {
		return (a == b);
	}

	/**
	 * Returns true if a == b
	 * 
	 * @param a
	 * @param b
	 * 
	 */
	public static boolean equals(long a, long b) {
		return (a == b);
	}
}