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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 *
 */
public class ToolsTest {

	private static Number toBigNumber(Number n) {
		if (!(n instanceof BigInteger) && !(n instanceof BigDecimal)) {
			if ((n instanceof Float) || (n instanceof Double)) {
				return new BigDecimal(n.doubleValue());
			} else {
				return new BigInteger(String.valueOf(n));
			}
		}
		return n;
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.Tools#coalesce(Object, Object...)}.
	 */
	@Test
	public void testCoalesce() {
		Assertions.assertNull(Tools.coalesce(null));
		Assertions.assertNull(Tools.coalesce(null, null, null));
		Object[] a = {
			1, 2
		};
		Assertions.assertNotNull(Tools.coalesce(a[0]));
		Assertions.assertEquals(a[0], Tools.coalesce(null, a[0]));
		Assertions.assertEquals(a[0], Tools.coalesce(null, a[0], a[1]));
		Assertions.assertEquals(a[0], Tools.coalesce(null, null, a[0]));
		Assertions.assertNotNull(Tools.coalesce(a[1]));
		Assertions.assertEquals(a[1], Tools.coalesce(null, a[1]));
		Assertions.assertEquals(a[1], Tools.coalesce(null, a[1], a[0]));
		Assertions.assertEquals(a[1], Tools.coalesce(null, null, a[1]));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.Tools#coalesce(Object, Object...)}.
	 */
	@Test
	public void testFirstNonNull() {
		Assertions.assertEquals(-1, Tools.firstNonNull(null));
		Assertions.assertEquals(-1, Tools.firstNonNull(null, null, null));
		Object[] a = {
			1, 2
		};
		Assertions.assertEquals(0, Tools.firstNonNull(a[0]));
		Assertions.assertEquals(1, Tools.firstNonNull(null, a[0]));
		Assertions.assertEquals(2, Tools.firstNonNull(null, null, a[1]));
		Assertions.assertEquals(0, Tools.firstNonNull(a[1]));
		Assertions.assertEquals(1, Tools.firstNonNull(null, a[1]));
		Assertions.assertEquals(2, Tools.firstNonNull(null, null, a[0]));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.Tools#coalesce(Object, Object...)}.
	 */
	@Test
	public void testFirstNull() {
		Object[] a = {
			1, 2
		};
		Assertions.assertEquals(0, Tools.firstNull(null));
		Assertions.assertEquals(-1, Tools.firstNull(a[0]));
		Assertions.assertEquals(1, Tools.firstNull(a[0], (Object) null));
		Assertions.assertEquals(2, Tools.firstNull(a[0], a[1], null));
		Assertions.assertEquals(0, Tools.firstNull(null, null, null));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.Tools#toTrimmedString(java.lang.Object, boolean)}.
	 */
	@Test
	public void testAsTrimmedString() {
		Object[][] o = {
			{
				null, null, null
			}, {
				1, "1", "1"
			}, {
				"", "", null
			}, {
				" ", "", null
			}, {
				"     ", "", null
			}, {
				"hello", "hello", "hello"
			}, {
				"     hello     ", "hello", "hello"
			}, {
				"hello     ", "hello", "hello"
			}, {
				"     hello", "hello", "hello"
			}, {
				"hello there", "hello there", "hello there"
			}, {
				"     hello there    ", "hello there", "hello there"
			}, {
				"hello there     ", "hello there", "hello there"
			}, {
				"     hello there", "hello there", "hello there"
			}, {
				"hello     there", "hello     there", "hello     there"
			}, {
				"     hello     there    ", "hello     there", "hello     there"
			}, {
				"hello     there     ", "hello     there", "hello     there"
			}, {
				"     hello     there", "hello     there", "hello     there"
			}
		};
		for (Object[] a : o) {
			Assertions.assertEquals(a[2], Tools.toTrimmedString(a[0], true));
			Assertions.assertEquals(a[1], Tools.toTrimmedString(a[0], false));
			Assertions.assertEquals(a[1], Tools.toTrimmedString(a[0]));
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.Tools#toString(java.lang.Object, boolean)}.
	 */
	@Test
	public void testAsString() {
		Object[][] o = {
			{
				null, null, null
			}, {
				1, "1", "1"
			}, {
				"", "", null
			}, {
				" ", " ", " "
			}, {
				"     ", "     ", "     "
			}, {
				"hello", "hello", "hello"
			}, {
				"     hello     ", "     hello     ", "     hello     "
			}, {
				"hello     ", "hello     ", "hello     "
			}, {
				"     hello", "     hello", "     hello"
			}, {
				"hello there", "hello there", "hello there"
			}, {
				"     hello there    ", "     hello there    ", "     hello there    "
			}, {
				"hello there     ", "hello there     ", "hello there     "
			}, {
				"     hello there", "     hello there", "     hello there"
			}, {
				"hello     there", "hello     there", "hello     there"
			}, {
				"     hello     there    ", "     hello     there    ", "     hello     there    "
			}, {
				"hello     there     ", "hello     there     ", "hello     there     "
			}, {
				"     hello     there", "     hello     there", "     hello     there"
			}, {
				"", "", null
			}
		};
		for (Object[] a : o) {
			Assertions.assertEquals(a[2], Tools.toString(a[0], true));
			Assertions.assertEquals(a[1], Tools.toString(a[0], false));
			Assertions.assertEquals(a[1], Tools.toString(a[0]));
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.Tools#consolidateRepeatedCharacters(java.lang.String, char)}
	 * .
	 */
	@Test
	public void testConsolidateRepeatedCharacters() {
		String[][] o = {
			{
				" ", null, null
			}, {
				"1", "asdf11111111sfdasdf", "asdf1sfdasdf"
			}, {
				"x", "0x1x2xxx3x4x5", "0x1x2x3x4x5"
			}, {
				" ", "hello", "hello", "hello"
			}, {
				" ", "     hello     ", " hello "
			}, {
				" ", "hello     ", "hello "
			}, {
				" ", "     hello", " hello"
			}, {
				" ", "hello there", "hello there"
			}, {
				" ", "     hello there    ", " hello there "
			}, {
				" ", "hello there     ", "hello there "
			}, {
				" ", "     hello there", " hello there"
			}, {
				" ", "hello     there", "hello there"
			}, {
				" ", "     hello     there    ", " hello there "
			}, {
				" ", "hello     there     ", "hello there "
			}, {
				" ", "     hello     there", " hello there"
			}
		};
		for (String[] a : o) {
			final char c = a[0].charAt(0);
			Assertions.assertEquals(a[2], Tools.consolidateRepeatedCharacters(a[1], c));
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.Tools#toNumber(java.lang.Object)}.
	 */
	@Test
	public void testAsNumber() {
		final String maxLong = String.valueOf(Long.MAX_VALUE);
		final String minLong = String.valueOf(Long.MIN_VALUE);
		Object[][] o = {
			{
				null, null
			}, {
				"0", 0
			}, {
				"1", 1
			}, {
				"-1", -1
			}, {
				Long.MAX_VALUE, Long.MAX_VALUE
			}, {
				maxLong, Long.MAX_VALUE
			}, {
				maxLong + "0000", new BigInteger(maxLong + "0000")
			}, {
				Long.MIN_VALUE, Long.MIN_VALUE
			}, {
				minLong, Long.MIN_VALUE
			}, {
				minLong + "0000", new BigInteger(minLong + "0000")
			}, {
				"1 ", new Exception()
			}, {
				" 1", new Exception()
			}, {
				" 1 ", new Exception()
			}, {
				"ASDFASDF", new Exception()
			}, {
				"1.0", 1.0
			}, {
				"-1.0", -1.0
			}, {
				Double.MAX_VALUE, Double.MAX_VALUE
			}, {
				Double.MIN_VALUE, Double.MIN_VALUE
			}, {
				-Double.MAX_VALUE, -Double.MAX_VALUE
			}, {
				-Double.MIN_VALUE, -Double.MIN_VALUE
			}, {
				maxLong + "00000000." + maxLong, new BigDecimal(maxLong + "00000000." + maxLong)
			}, {
				"-" + maxLong + "00000000." + maxLong, new BigDecimal("-" + maxLong + "00000000." + maxLong)
			}
		};
		for (Object[] a : o) {
			if (a[1] != null) {
				if (a[1] instanceof Exception) {
					Assertions.assertThrows(NumberFormatException.class, () -> Tools.toNumber(a[0]));
				} else {
					Number result = ToolsTest.toBigNumber(Tools.toNumber(a[0]));
					Number expected = ToolsTest.toBigNumber(Number.class.cast(a[1]));

					if (result instanceof BigInteger) {
						BigInteger bigExpected = BigInteger.class.cast(expected);
						BigInteger bigResult = BigInteger.class.cast(result);
						Assertions.assertEquals(0, bigExpected.compareTo(bigResult));
					} else {
						BigDecimal bigExpected = BigDecimal.class.cast(expected);
						BigDecimal bigResult = BigDecimal.class.cast(result);
						Assertions.assertEquals(0, bigExpected.compareTo(bigResult));
					}
				}
			} else {
				Assertions.assertEquals(null, Tools.toNumber(a[0]));
			}
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.Tools#strcmp(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testStrcmp() {
		String[][] testData = {
			{
				null, null, "0"
			}, {
				null, "asdf", "-1"
			}, {
				"asdf", null, "1"
			}, {
				"asdf", "asdf", "0"
			}, {
				"asdf", " ASDF ".trim().toLowerCase(), "0" // This is to ensure the strings are
			// equal, but not the same object
			}, {
				"asdf", "ASDF", "1"
			}, {
				"aSdF", "AsDf", "1"
			}, {
				"asdf", "bsdf", "-1"
			}, {
				"bsdf", "asdf", "1"
			}, {
				"asdf", "BSDF", "1"
			}, {
				"bSdF", "AsDf", "1"
			}, {
				"asdf", "atdf", "-1"
			}, {
				"atdf", "asdf", "1"
			}, {
				"asdf", "ATDF", "1"
			}, {
				"aTdF", "AsDf", "1"
			},
		};
		for (String[] a : testData) {
			Integer result = Integer.valueOf(a[2]);
			Assertions.assertEquals(result.intValue(), Tools.strcmp(a[0], a[1]), Arrays.toString(a));
			Assertions.assertEquals(result.intValue(), Tools.STRCMP.compare(a[0], a[1]), Arrays.toString(a));
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.Tools#stricmp(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testStricmp() {
		String[][] testData = {
			{
				null, null, "0"
			}, {
				null, "asdf", "-1"
			}, {
				"asdf", null, "1"
			}, {
				"asdf", "asdf", "0"
			}, {
				"asdf", "ASDF", "0"
			}, {
				"aSdF", "AsDf", "0"
			}, {
				"asdf", "bsdf", "-1"
			}, {
				"bsdf", "asdf", "1"
			}, {
				"asdf", "BSDF", "-1"
			}, {
				"bSdF", "AsDf", "1"
			}, {
				"asdf", "atdf", "-1"
			}, {
				"atdf", "asdf", "1"
			}, {
				"asdf", "ATDF", "-1"
			}, {
				"aTdF", "AsDf", "1"
			},
		};
		for (String[] a : testData) {
			Integer result = Integer.valueOf(a[2]);
			Assertions.assertEquals(result.intValue(), Tools.stricmp(a[0], a[1]), Arrays.toString(a));
			Assertions.assertEquals(result.intValue(), Tools.STRICMP.compare(a[0], a[1]), Arrays.toString(a));
		}
	}

	@Test
	public void testHashToolArray() {
		Random rand = new Random(System.currentTimeMillis());

		boolean[][] arr_boolean = {
			new boolean[10], new boolean[10]
		};
		do {
			for (boolean[] a : arr_boolean) {
				for (int i = 0; i < a.length; i++) {
					a[i] = rand.nextBoolean();
				}
			}
		} while (Arrays.equals(arr_boolean[0], arr_boolean[1]));

		byte[][] arr_byte = {
			new byte[10], new byte[10]
		};
		rand.nextBytes(arr_byte[0]);
		do {
			rand.nextBytes(arr_byte[1]);
		} while (Arrays.equals(arr_byte[0], arr_byte[1]));

		short[][] arr_short = {
			new short[10], new short[10]
		};
		for (int i = 0; i < arr_short[0].length; i++) {
			arr_short[0][i] = (short) rand.nextInt();
		}
		do {
			for (int i = 0; i < arr_short[1].length; i++) {
				arr_short[1][i] = (short) rand.nextInt();
			}
		} while (Arrays.equals(arr_short[0], arr_short[1]));

		int[][] arr_int = {
			new int[10], new int[10]
		};
		for (int i = 0; i < arr_int[0].length; i++) {
			arr_int[0][i] = rand.nextInt();
		}
		do {
			for (int i = 0; i < arr_int[1].length; i++) {
				arr_int[1][i] = rand.nextInt();
			}
		} while (Arrays.equals(arr_int[0], arr_int[1]));

		long[][] arr_long = {
			new long[10], new long[10]
		};
		for (int i = 0; i < arr_long[0].length; i++) {
			arr_long[0][i] = rand.nextLong();
		}
		do {
			for (int i = 0; i < arr_long[1].length; i++) {
				arr_long[1][i] = rand.nextLong();
			}
		} while (Arrays.equals(arr_long[0], arr_long[1]));

		float[][] arr_float = {
			new float[10], new float[10]
		};
		for (int i = 0; i < arr_float[0].length; i++) {
			arr_float[0][i] = rand.nextFloat();
		}
		do {
			for (int i = 0; i < arr_float[1].length; i++) {
				arr_float[1][i] = rand.nextFloat();
			}
		} while (Arrays.equals(arr_float[0], arr_float[1]));

		double[][] arr_double = {
			new double[10], new double[10]
		};
		for (int i = 0; i < arr_double[0].length; i++) {
			arr_double[0][i] = rand.nextDouble();
		}
		do {
			for (int i = 0; i < arr_double[1].length; i++) {
				arr_double[1][i] = rand.nextDouble();
			}
		} while (Arrays.equals(arr_double[0], arr_double[1]));

		char[][] arr_char = {
			null, null
		};
		arr_char[0] = UUID.randomUUID().toString().toCharArray();
		do {
			arr_char[1] = UUID.randomUUID().toString().toCharArray();
		} while (Arrays.equals(arr_char[0], arr_char[1]));

		Object[][] arr_obj = {
			new Object[10], new Object[10]
		};
		Arrays.fill(arr_obj[0], null);
		for (int i = 0; i < arr_obj[0].length; i++) {
			if ((rand.nextInt(10) % 5) == 0) {
				continue;
			}
			arr_obj[0][i] = String.valueOf(Integer.valueOf(i));
		}
		do {
			Arrays.fill(arr_obj[1], null);
			for (int i = 0; i < arr_obj[1].length; i++) {
				if ((rand.nextInt(10) % 5) == 0) {
					continue;
				}
				arr_obj[1][i] = String.valueOf(Integer.valueOf(i));
			}
		} while (Arrays.equals(arr_obj[0], arr_obj[1]));

		int[][] primes = {
			{
				5801, 5813, 5827, 5843, 5851
			}, {
				5807, 5821, 5839, 5849, 5857
			}
		};

		Object a = new Object();
		Object b = new Object();
		for (int i = 0; i < primes[0].length; i++) {
			final int p = primes[0][i];
			final int q = primes[1][i];

			int hashA = 0;
			int hashB = 0;

			hashA = Tools.hashTool(a, null, p, p, arr_boolean[0]);
			hashB = Tools.hashTool(b, null, p, p, arr_boolean[0]);
			Assertions.assertEquals(hashA, hashB, String.format("Failed boolean with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_boolean[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_boolean[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed boolean with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_boolean[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_boolean[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed boolean with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_boolean[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_boolean[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed boolean with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_boolean[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_boolean[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed boolean with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_byte[0]);
			hashB = Tools.hashTool(b, null, p, p, arr_byte[0]);
			Assertions.assertEquals(hashA, hashB, String.format("Failed byte with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_byte[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_byte[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed byte with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_byte[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_byte[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed byte with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_byte[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_byte[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed byte with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_byte[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_byte[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed byte with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_short[0]);
			hashB = Tools.hashTool(b, null, p, p, arr_short[0]);
			Assertions.assertEquals(hashA, hashB, String.format("Failed short with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_short[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_short[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed short with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_short[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_short[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed short with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_short[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_short[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed short with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_short[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_short[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed short with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_int[0]);
			hashB = Tools.hashTool(b, null, p, p, arr_int[0]);
			Assertions.assertEquals(hashA, hashB, String.format("Failed int with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_int[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_int[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed int with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_int[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_int[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed int with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_int[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_int[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed int with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_int[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_int[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed int with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_long[0]);
			hashB = Tools.hashTool(b, null, p, p, arr_long[0]);
			Assertions.assertEquals(hashA, hashB, String.format("Failed long with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_long[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_long[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed long with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_long[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_long[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed long with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_long[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_long[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed long with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_long[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_long[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed long with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_float[0]);
			hashB = Tools.hashTool(b, null, p, p, arr_float[0]);
			Assertions.assertEquals(hashA, hashB, String.format("Failed float with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_float[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_float[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed float with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_float[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_float[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed float with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_float[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_float[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed float with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_float[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_float[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed float with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_double[0]);
			hashB = Tools.hashTool(b, null, p, p, arr_double[0]);
			Assertions.assertEquals(hashA, hashB, String.format("Failed double with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_double[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_double[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed double with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_double[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_double[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed double with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_double[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_double[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed double with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_double[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_double[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed double with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_char[0]);
			hashB = Tools.hashTool(b, null, p, p, arr_char[0]);
			Assertions.assertEquals(hashA, hashB, String.format("Failed char with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_char[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_char[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed char with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_char[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_char[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed char with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_char[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_char[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed char with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_char[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_char[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed char with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_obj[0]);
			hashB = Tools.hashTool(b, null, p, p, arr_obj[0]);
			Assertions.assertEquals(hashA, hashB, String.format("Failed obj with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_obj[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_obj[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed obj with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_obj[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_obj[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed obj with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_obj[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_obj[0]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed obj with prime = %d", p));

			hashA = Tools.hashTool(a, null, p, p, arr_obj[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_obj[1]);
			Assertions.assertNotEquals(hashA, hashB, String.format("Failed obj with prime = %d", p));
		}
	}

	@Test
	public void testAsBoolean() {
		Object[][] arr = {
			{
				"true", true
			}, {
				"TRUE", true
			}, {
				"tRuE", true
			}, {
				"T", true
			}, {
				"t", true
			}, {
				"yes", true
			}, {
				"YES", true
			}, {
				"yEs", true
			}, {
				"y", true
			}, {
				"Y", true
			}, {
				"on", true
			}, {
				"ON", true
			}, {
				"oN", true
			}, {
				"1", true
			}, {
				"   yes", true
			}, {
				"YES   ", true
			}, {
				"   yEs   ", true
			}, {
				"    y", true
			}, {
				"Y   ", true
			}, {
				"false", false
			}, {
				"FALSE", false
			}, {
				"fAlSe", false
			}, {
				"F", false
			}, {
				"f", false
			}, {
				"no", false
			}, {
				"NO", false
			}, {
				"nO", false
			}, {
				"n", false
			}, {
				"N", false
			}, {
				"off", false
			}, {
				"OFF", false
			}, {
				"oFf", false
			}, {
				"0", false
			}, {
				"   no", false
			}, {
				"NO   ", false
			}, {
				"   nO   ", false
			}, {
				"n    ", false
			}, {
				"    N", false
			},
		};
		Assertions.assertNull(Tools.toBoolean(null));
		for (Object[] s : arr) {
			final String str = Tools.toString(s[0]);
			final Boolean expected = Boolean.class.cast(s[1]);
			Assertions.assertEquals(expected, Tools.toBoolean(str), String.format("Failed when checking [%s]", str));
		}
	}

	@Test
	public void testEnsureBetween() {
		Integer[][] arr = {
			{
				Integer.MIN_VALUE, 0, Integer.MAX_VALUE, 0
			}, {
				Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE
			}, {
				Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE
			}, {
				Integer.MAX_VALUE, 0, Integer.MIN_VALUE, 0
			}, {
				Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE
			}, {
				Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE
			}, {
				Integer.MAX_VALUE, 0, Integer.MAX_VALUE, Integer.MAX_VALUE
			}, {
				Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE
			}, {
				Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE
			}, {
				Integer.MIN_VALUE, 0, Integer.MIN_VALUE, Integer.MIN_VALUE
			}, {
				Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE
			}, {
				Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE
			}, {
				0, 1, 2, 1
			}, {
				2, 1, 0, 1
			}, {
				0, 1, 0, 0
			}, {
				1, 0, 1, 1
			}, {
				null, 0, 1
			}, {
				1, null, 1
			}, {
				1, 0, null
			}, {
				null, null, 1
			}, {
				1, null, null
			}, {
				null, 0, null
			}, {
				null, null, null
			},
		};
		for (Integer[] a : arr) {
			if (a.length < 4) {
				// Expect an exception
				Assertions.assertThrows(IllegalArgumentException.class, () -> Tools.ensureBetween(a[0], a[1], a[2]));
			} else {
				Assertions.assertEquals(a[3], Tools.ensureBetween(a[0], a[1], a[2]),
					String.format("Failed when comparing the array %s", Arrays.toString(a)));
			}
		}
	}

	private <C extends Collection<Object>> void validateFrozen(C source, C frozen) {
		final Collection<Object> singleton = Collections.singleton(new Object());
		final String message = String.format("%s is not frozen", source.getClass().getSimpleName());
		Assertions.assertEquals(source, frozen);
		Assertions.assertThrows(UnsupportedOperationException.class, () -> frozen.add(UUID.randomUUID()), message);
		Assertions.assertThrows(UnsupportedOperationException.class, () -> frozen.addAll(singleton), message);
		Assertions.assertThrows(UnsupportedOperationException.class, () -> frozen.remove(0), message);
		Assertions.assertThrows(UnsupportedOperationException.class, () -> frozen.removeAll(singleton), message);
		Assertions.assertThrows(UnsupportedOperationException.class, () -> frozen.retainAll(singleton), message);

		Iterator<Object> it = frozen.iterator();
		Assertions.assertTrue(it.hasNext());
		Assertions.assertThrows(UnsupportedOperationException.class, () -> it.remove(), message);
	}

	private void validateFrozen(Map<Object, Object> source, Map<Object, Object> frozen) {
		final UUID uuid = UUID.randomUUID();
		final Map<?, ?> singletonMap = Collections.singletonMap(uuid, uuid.toString());
		final String message = String.format("%s is not frozen", source.getClass().getSimpleName());
		Assertions.assertEquals(source, frozen);
		Assertions.assertThrows(UnsupportedOperationException.class, () -> frozen.put(uuid, uuid.toString()), message);
		Assertions.assertThrows(UnsupportedOperationException.class, () -> frozen.putAll(singletonMap), message);
		Assertions.assertThrows(UnsupportedOperationException.class, () -> frozen.remove(0), message);
		Assertions.assertThrows(UnsupportedOperationException.class, () -> frozen.remove(uuid), message);

		{
			Iterator<Object> it = frozen.keySet().iterator();
			Assertions.assertTrue(it.hasNext());
			Assertions.assertThrows(UnsupportedOperationException.class, () -> it.remove(), message);
		}

		{
			Iterator<Object> it = frozen.values().iterator();
			Assertions.assertTrue(it.hasNext());
			Assertions.assertThrows(UnsupportedOperationException.class, () -> it.remove(), message);
		}
	}

	@Test
	public void testFreezers() {
		final UUID newUuid = UUID.randomUUID();
		// freezeCopy
		final List<Object> nullList = null;
		final List<Object> list = new ArrayList<>();
		List<Object> frozenList = null;

		final Set<Object> nullSet = null;
		final Set<Object> set = new HashSet<>();
		final Set<Object> sortedSet = new TreeSet<>();
		Set<Object> frozenSet = null;

		final Map<Object, Object> nullMap = null;
		final Map<Object, Object> map = new HashMap<>();
		final Map<Object, Object> sortedMap = new TreeMap<>();
		Map<Object, Object> frozenMap = null;

		for (int i = 0; i < 5; i++) {
			UUID uuid = UUID.randomUUID();
			list.add(uuid);
			set.add(uuid);
			sortedSet.add(uuid);
			map.put(uuid, uuid.toString());
			sortedMap.put(uuid, uuid.toString());
		}

		Assertions.assertNull(Tools.freezeCopy(nullList));
		Assertions.assertNull(Tools.freezeCopy(nullList, false));

		frozenList = Tools.freezeCopy(nullList, true);
		Assertions.assertNotNull(frozenList);
		Assertions.assertTrue(frozenList.isEmpty());
		Assertions.assertSame(Collections.emptyList(), frozenList);

		frozenList = Tools.freezeCopy(list);
		Assertions.assertNotSame(list, frozenList);
		validateFrozen(list, frozenList);
		list.add(newUuid);
		Assertions.assertNotEquals(list, frozenList);

		Assertions.assertNull(Tools.freezeCopy(nullSet));
		Assertions.assertNull(Tools.freezeCopy(nullSet, false));

		frozenSet = Tools.freezeCopy(nullSet, true);
		Assertions.assertNotNull(frozenSet);
		Assertions.assertTrue(frozenSet.isEmpty());
		Assertions.assertSame(Collections.emptySet(), frozenSet);

		frozenSet = Tools.freezeCopy(set);
		Assertions.assertNotSame(set, frozenSet);
		validateFrozen(set, frozenSet);
		set.add(newUuid);
		Assertions.assertNotEquals(set, frozenSet);

		frozenSet = Tools.freezeCopy(sortedSet);
		Assertions.assertNotSame(sortedSet, frozenSet);
		validateFrozen(sortedSet, frozenSet);
		sortedSet.add(newUuid);
		Assertions.assertNotEquals(sortedSet, frozenSet);

		Assertions.assertNull(Tools.freezeCopy(nullMap));
		Assertions.assertNull(Tools.freezeCopy(nullMap, false));

		frozenMap = Tools.freezeCopy(nullMap, true);
		Assertions.assertNotNull(frozenMap);
		Assertions.assertTrue(frozenMap.isEmpty());
		Assertions.assertSame(Collections.emptyMap(), frozenMap);

		frozenMap = Tools.freezeCopy(map);
		validateFrozen(map, frozenMap);
		map.put(newUuid, newUuid.toString());
		Assertions.assertNotEquals(map, frozenMap);

		frozenMap = Tools.freezeCopy(sortedMap);
		validateFrozen(sortedMap, frozenMap);
		sortedMap.put(newUuid, newUuid.toString());
		Assertions.assertNotEquals(sortedMap, frozenMap);

		// freeze{List,Map,Set}
		list.remove(newUuid);
		set.remove(newUuid);
		sortedSet.remove(newUuid);
		map.remove(newUuid);
		sortedMap.remove(newUuid);

		Assertions.assertNull(Tools.freezeList(nullList));
		Assertions.assertNull(Tools.freezeList(nullList, false));

		frozenList = Tools.freezeList(nullList, true);
		Assertions.assertNotNull(frozenList);
		Assertions.assertTrue(frozenList.isEmpty());
		Assertions.assertSame(Collections.emptyList(), frozenList);

		frozenList = Tools.freezeList(list);
		validateFrozen(list, frozenList);
		list.add(newUuid);
		Assertions.assertEquals(list, frozenList);

		Assertions.assertNull(Tools.freezeSet(nullSet));
		Assertions.assertNull(Tools.freezeSet(nullSet, false));

		frozenSet = Tools.freezeSet(nullSet, true);
		Assertions.assertNotNull(frozenSet);
		Assertions.assertTrue(frozenSet.isEmpty());
		Assertions.assertSame(Collections.emptySet(), frozenSet);

		frozenSet = Tools.freezeSet(set);
		validateFrozen(set, frozenSet);
		set.add(newUuid);
		Assertions.assertEquals(set, frozenSet);

		frozenSet = Tools.freezeSet(sortedSet);
		validateFrozen(sortedSet, frozenSet);
		sortedSet.add(newUuid);
		Assertions.assertEquals(sortedSet, frozenSet);

		Assertions.assertNull(Tools.freezeMap(nullMap));
		Assertions.assertNull(Tools.freezeMap(nullMap, false));

		frozenMap = Tools.freezeMap(nullMap, true);
		Assertions.assertNotNull(frozenMap);
		Assertions.assertTrue(frozenMap.isEmpty());
		Assertions.assertSame(Collections.emptyMap(), frozenMap);

		frozenMap = Tools.freezeMap(map);
		validateFrozen(map, frozenMap);
		map.put(newUuid, newUuid.toString());
		Assertions.assertEquals(map, frozenMap);

		frozenMap = Tools.freezeMap(sortedMap);
		validateFrozen(sortedMap, frozenMap);
		sortedMap.put(newUuid, newUuid.toString());
		Assertions.assertEquals(sortedMap, frozenMap);
	}

	private static enum TestEnumA {
		//
		A, B, C, D, E
	}

	private static enum TestEnumB {
		//
		Z, X, Y, W, V
	}

	private static enum TestEnumC {
		//
		a, b, c, d, e, z, x, y, w, v
	}

	@Test
	public void testParseEnumCSV() {
		String str = null;
		Set<TestEnumA> setA = null;
		Set<TestEnumA> expA = null;
		Set<TestEnumB> setB = null;
		Set<TestEnumB> expB = null;
		Set<TestEnumC> setC = null;
		Set<TestEnumC> expC = null;

		Assertions.assertThrows(IllegalArgumentException.class, () -> Tools.parseEnumCSV(null, "", false),
			"Did not fail with a null enum class");

		str = null;
		setA = Tools.parseEnumCSV(TestEnumA.class, str, false);
		expA = EnumSet.noneOf(TestEnumA.class);
		Assertions.assertEquals(expA, setA);
		setB = Tools.parseEnumCSV(TestEnumB.class, str, false);
		expB = EnumSet.noneOf(TestEnumB.class);
		Assertions.assertEquals(expB, setB);
		setC = Tools.parseEnumCSV(TestEnumC.class, str, false);
		expC = EnumSet.noneOf(TestEnumC.class);
		Assertions.assertEquals(expC, setC);

		str = "";
		setA = Tools.parseEnumCSV(TestEnumA.class, str, false);
		expA = EnumSet.noneOf(TestEnumA.class);
		Assertions.assertEquals(expA, setA);
		setB = Tools.parseEnumCSV(TestEnumB.class, str, false);
		expB = EnumSet.noneOf(TestEnumB.class);
		Assertions.assertEquals(expB, setB);
		setC = Tools.parseEnumCSV(TestEnumC.class, str, false);
		expC = EnumSet.noneOf(TestEnumC.class);
		Assertions.assertEquals(expC, setC);

		str = "ALL";
		setA = Tools.parseEnumCSV(TestEnumA.class, str, null, false);
		expA = EnumSet.noneOf(TestEnumA.class);
		Assertions.assertEquals(expA, setA);
		setB = Tools.parseEnumCSV(TestEnumB.class, str, null, false);
		expB = EnumSet.noneOf(TestEnumB.class);
		Assertions.assertEquals(expB, setB);
		setC = Tools.parseEnumCSV(TestEnumC.class, str, null, false);
		expC = EnumSet.noneOf(TestEnumC.class);
		Assertions.assertEquals(expC, setC);

		str = "ALL";
		setA = Tools.parseEnumCSV(TestEnumA.class, str, "ALL", false);
		expA = EnumSet.allOf(TestEnumA.class);
		Assertions.assertEquals(expA, setA);
		setB = Tools.parseEnumCSV(TestEnumB.class, str, "ALL", false);
		expB = EnumSet.allOf(TestEnumB.class);
		Assertions.assertEquals(expB, setB);
		setC = Tools.parseEnumCSV(TestEnumC.class, str, "ALL", false);
		expC = EnumSet.allOf(TestEnumC.class);
		Assertions.assertEquals(expC, setC);

		{
			String str2 = "A,a,C,c,Z,z,X,x";
			Assertions.assertThrows(IllegalArgumentException.class,
				() -> Tools.parseEnumCSV(TestEnumA.class, str2, true),
				String.format("TestEnumA did not fail with values [%s]", str2));
			setA = Tools.parseEnumCSV(TestEnumA.class, str2, false);
			expA = EnumSet.noneOf(TestEnumA.class);
			expA.add(TestEnumA.A);
			expA.add(TestEnumA.C);
			Assertions.assertEquals(expA, setA);
			Assertions.assertThrows(IllegalArgumentException.class,
				() -> Tools.parseEnumCSV(TestEnumB.class, str2, true),
				String.format("TestEnumB did not fail with values [%s]", str2));
			setB = Tools.parseEnumCSV(TestEnumB.class, str2, false);
			expB = EnumSet.noneOf(TestEnumB.class);
			expB.add(TestEnumB.Z);
			expB.add(TestEnumB.X);
			Assertions.assertEquals(expB, setB);
			Assertions.assertThrows(IllegalArgumentException.class,
				() -> Tools.parseEnumCSV(TestEnumC.class, str2, true),
				String.format("TestEnumC did not fail with values [%s]", str2));
			setC = Tools.parseEnumCSV(TestEnumC.class, str2, false);
			expC = EnumSet.noneOf(TestEnumC.class);
			expC.add(TestEnumC.a);
			expC.add(TestEnumC.c);
			expC.add(TestEnumC.z);
			expC.add(TestEnumC.x);
			Assertions.assertEquals(expC, setC);
		}

	}

	@Test
	public void testSplitter() {
		String str = null;
		List<String> expected = null;
		List<String> result = null;

		expected = null;
		result = Tools.splitEscaped(null);
		Assertions.assertNull(result);
		Assertions.assertNull(Tools.joinEscaped(result));
		Assertions.assertEquals(expected, Tools.splitEscaped(Tools.joinEscaped(result)));

		expected = Arrays.asList("");
		result = Tools.splitEscaped("");
		Assertions.assertEquals(expected, result);
		Assertions.assertEquals("", Tools.joinEscaped(result));
		Assertions.assertEquals(expected, Tools.splitEscaped(Tools.joinEscaped(result)));

		expected = null;
		result = Tools.splitEscaped(',', null);
		Assertions.assertNull(result);
		Assertions.assertNull(Tools.joinEscaped(',', result));
		Assertions.assertEquals(expected, Tools.splitEscaped(',', Tools.joinEscaped(',', result)));

		str = "a&b&c&d&&e&f\\&g";
		expected = Arrays.asList("a", "b", "c", "d", "", "e", "f&g");
		result = Tools.splitEscaped('&', str);
		Assertions.assertEquals(expected, result);
		Assertions.assertEquals(str, Tools.joinEscaped('&', result));
		Assertions.assertEquals(expected, Tools.splitEscaped('&', Tools.joinEscaped('&', result)));

		str = "a!b!c!d!!e!f\\!g";
		expected = Arrays.asList("a", "b", "c", "d", "", "e", "f!g");
		result = Tools.splitEscaped('!', str);
		Assertions.assertEquals(expected, result);
		Assertions.assertEquals(str, Tools.joinEscaped('!', result));
		Assertions.assertEquals(expected, Tools.splitEscaped('!', Tools.joinEscaped('!', result)));

		str = "a.b.c.d..e.f\\.g";
		expected = Arrays.asList("a", "b", "c", "d", "", "e", "f.g");
		result = Tools.splitEscaped('.', str);
		Assertions.assertEquals(expected, result);
		Assertions.assertEquals(str, Tools.joinEscaped('.', result));
		Assertions.assertEquals(expected, Tools.splitEscaped('.', Tools.joinEscaped('.', result)));

		str = ",a,b,c,d,,e,f\\,g,";
		expected = Arrays.asList("", "a", "b", "c", "d", "", "e", "f,g", "");
		result = Tools.splitEscaped(',', str);
		Assertions.assertEquals(expected, result);
		Assertions.assertEquals(str, Tools.joinEscaped(',', result));
		Assertions.assertEquals(expected, Tools.splitEscaped(',', Tools.joinEscaped(',', result)));

		str = ",a,b,c,d,,e,f\\,g,";
		expected = Arrays.asList("", "a", "b", "c", "d", "", "e", "f,g", "");
		result = Tools.splitEscaped(str);
		Assertions.assertEquals(expected, result);
		Assertions.assertEquals(str, Tools.joinEscaped(',', result));
		Assertions.assertEquals(expected, Tools.splitEscaped(',', Tools.joinEscaped(',', result)));

		str = ",a,b,c,d,,e,f\\\\,g,";
		expected = Arrays.asList("", "a", "b", "c", "d", "", "e", "f\\,g", "");
		result = Tools.splitEscaped(str);
		Assertions.assertEquals(expected, result);
		Assertions.assertEquals(str, Tools.joinEscaped(',', result));
		Assertions.assertEquals(expected, Tools.splitEscaped(',', Tools.joinEscaped(',', result)));

		str = ",a,b,c,d,,e,f\\\\\\,g,";
		expected = Arrays.asList("", "a", "b", "c", "d", "", "e", "f\\\\,g", "");
		result = Tools.splitEscaped(str);
		Assertions.assertEquals(expected, result);
		Assertions.assertEquals(str, Tools.joinEscaped(',', result));
		Assertions.assertEquals(expected, Tools.splitEscaped(',', Tools.joinEscaped(',', result)));

		str = ",a,b,c,d,,e,f\\\\\\\\,g,";
		expected = Arrays.asList("", "a", "b", "c", "d", "", "e", "f\\\\\\,g", "");
		result = Tools.splitEscaped(str);
		Assertions.assertEquals(expected, result);
		Assertions.assertEquals(str, Tools.joinEscaped(',', result));
		Assertions.assertEquals(expected, Tools.splitEscaped(',', Tools.joinEscaped(',', result)));
	}
}
