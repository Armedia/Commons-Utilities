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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author drivera@armedia.com
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
		Assert.assertNull(Tools.coalesce(null));
		Assert.assertNull(Tools.coalesce(null, null, null));
		Object[] a = {
			1, 2
		};
		Assert.assertNotNull(Tools.coalesce(a[0]));
		Assert.assertEquals(a[0], Tools.coalesce(null, a[0]));
		Assert.assertEquals(a[0], Tools.coalesce(null, a[0], a[1]));
		Assert.assertEquals(a[0], Tools.coalesce(null, null, a[0]));
		Assert.assertNotNull(Tools.coalesce(a[1]));
		Assert.assertEquals(a[1], Tools.coalesce(null, a[1]));
		Assert.assertEquals(a[1], Tools.coalesce(null, a[1], a[0]));
		Assert.assertEquals(a[1], Tools.coalesce(null, null, a[1]));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.Tools#coalesce(Object, Object...)}.
	 */
	@Test
	public void testFirstNonNull() {
		Assert.assertEquals(-1, Tools.firstNonNull(null));
		Assert.assertEquals(-1, Tools.firstNonNull(null, null, null));
		Object[] a = {
			1, 2
		};
		Assert.assertEquals(0, Tools.firstNonNull(a[0]));
		Assert.assertEquals(1, Tools.firstNonNull(null, a[0]));
		Assert.assertEquals(2, Tools.firstNonNull(null, null, a[1]));
		Assert.assertEquals(0, Tools.firstNonNull(a[1]));
		Assert.assertEquals(1, Tools.firstNonNull(null, a[1]));
		Assert.assertEquals(2, Tools.firstNonNull(null, null, a[0]));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.Tools#coalesce(Object, Object...)}.
	 */
	@Test
	public void testFirstNull() {
		Object[] a = {
			1, 2
		};
		Assert.assertEquals(0, Tools.firstNull(null));
		Assert.assertEquals(-1, Tools.firstNull(a[0]));
		Assert.assertEquals(1, Tools.firstNull(a[0], (Object) null));
		Assert.assertEquals(2, Tools.firstNull(a[0], a[1], null));
		Assert.assertEquals(0, Tools.firstNull(null, null, null));
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
			Assert.assertEquals(a[2], Tools.toTrimmedString(a[0], true));
			Assert.assertEquals(a[1], Tools.toTrimmedString(a[0], false));
			Assert.assertEquals(a[1], Tools.toTrimmedString(a[0]));
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
			Assert.assertEquals(a[2], Tools.toString(a[0], true));
			Assert.assertEquals(a[1], Tools.toString(a[0], false));
			Assert.assertEquals(a[1], Tools.toString(a[0]));
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
			Assert.assertEquals(a[2], Tools.consolidateRepeatedCharacters(a[1], c));
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
					try {
						Number result = Tools.toNumber(a[0]);
						Assert.fail(String.format("Expected a failure with input = [%s] but got %s (%s)", a[0], result,
							result.getClass()));
					} catch (NumberFormatException e) {
						// All is well
					}
				} else {
					Number result = ToolsTest.toBigNumber(Tools.toNumber(a[0]));
					Number expected = ToolsTest.toBigNumber(Number.class.cast(a[1]));

					if (result instanceof BigInteger) {
						BigInteger bigExpected = BigInteger.class.cast(expected);
						BigInteger bigResult = BigInteger.class.cast(result);
						Assert.assertEquals(0, bigExpected.compareTo(bigResult));
					} else {
						BigDecimal bigExpected = BigDecimal.class.cast(expected);
						BigDecimal bigResult = BigDecimal.class.cast(result);
						Assert.assertEquals(0, bigExpected.compareTo(bigResult));
					}
				}
			} else {
				Assert.assertEquals(null, Tools.toNumber(a[0]));
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
			Assert.assertEquals(Arrays.toString(a), result.intValue(), Tools.strcmp(a[0], a[1]));
			Assert.assertEquals(Arrays.toString(a), result.intValue(), Tools.STRCMP.compare(a[0], a[1]));
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
			Assert.assertEquals(Arrays.toString(a), result.intValue(), Tools.stricmp(a[0], a[1]));
			Assert.assertEquals(Arrays.toString(a), result.intValue(), Tools.STRICMP.compare(a[0], a[1]));
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
		for (int i = 0; i < arr_obj[0].length; i++) {
			if ((rand.nextInt(10) % 5) == 0) {
				continue;
			}
			arr_obj[0][i] = String.valueOf(Integer.valueOf(i));
		}
		do {
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
			Assert.assertEquals(String.format("Failed boolean with prime = %d", p), hashA, hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_boolean[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_boolean[0]);
			Assert.assertTrue(String.format("Failed boolean with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_boolean[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_boolean[1]);
			Assert.assertTrue(String.format("Failed boolean with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_boolean[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_boolean[0]);
			Assert.assertTrue(String.format("Failed boolean with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_boolean[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_boolean[1]);
			Assert.assertTrue(String.format("Failed boolean with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_byte[0]);
			hashB = Tools.hashTool(b, null, p, p, arr_byte[0]);
			Assert.assertEquals(String.format("Failed byte with prime = %d", p), hashA, hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_byte[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_byte[0]);
			Assert.assertTrue(String.format("Failed byte with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_byte[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_byte[1]);
			Assert.assertTrue(String.format("Failed byte with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_byte[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_byte[0]);
			Assert.assertTrue(String.format("Failed byte with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_byte[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_byte[1]);
			Assert.assertTrue(String.format("Failed byte with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_short[0]);
			hashB = Tools.hashTool(b, null, p, p, arr_short[0]);
			Assert.assertEquals(String.format("Failed short with prime = %d", p), hashA, hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_short[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_short[0]);
			Assert.assertTrue(String.format("Failed short with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_short[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_short[1]);
			Assert.assertTrue(String.format("Failed short with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_short[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_short[0]);
			Assert.assertTrue(String.format("Failed short with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_short[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_short[1]);
			Assert.assertTrue(String.format("Failed short with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_int[0]);
			hashB = Tools.hashTool(b, null, p, p, arr_int[0]);
			Assert.assertEquals(String.format("Failed int with prime = %d", p), hashA, hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_int[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_int[0]);
			Assert.assertTrue(String.format("Failed int with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_int[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_int[1]);
			Assert.assertTrue(String.format("Failed int with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_int[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_int[0]);
			Assert.assertTrue(String.format("Failed int with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_int[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_int[1]);
			Assert.assertTrue(String.format("Failed int with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_long[0]);
			hashB = Tools.hashTool(b, null, p, p, arr_long[0]);
			Assert.assertEquals(String.format("Failed long with prime = %d", p), hashA, hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_long[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_long[0]);
			Assert.assertTrue(String.format("Failed long with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_long[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_long[1]);
			Assert.assertTrue(String.format("Failed long with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_long[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_long[0]);
			Assert.assertTrue(String.format("Failed long with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_long[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_long[1]);
			Assert.assertTrue(String.format("Failed long with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_float[0]);
			hashB = Tools.hashTool(b, null, p, p, arr_float[0]);
			Assert.assertEquals(String.format("Failed float with prime = %d", p), hashA, hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_float[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_float[0]);
			Assert.assertTrue(String.format("Failed float with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_float[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_float[1]);
			Assert.assertTrue(String.format("Failed float with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_float[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_float[0]);
			Assert.assertTrue(String.format("Failed float with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_float[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_float[1]);
			Assert.assertTrue(String.format("Failed float with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_double[0]);
			hashB = Tools.hashTool(b, null, p, p, arr_double[0]);
			Assert.assertEquals(String.format("Failed double with prime = %d", p), hashA, hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_double[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_double[0]);
			Assert.assertTrue(String.format("Failed double with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_double[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_double[1]);
			Assert.assertTrue(String.format("Failed double with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_double[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_double[0]);
			Assert.assertTrue(String.format("Failed double with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_double[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_double[1]);
			Assert.assertTrue(String.format("Failed double with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_char[0]);
			hashB = Tools.hashTool(b, null, p, p, arr_char[0]);
			Assert.assertEquals(String.format("Failed char with prime = %d", p), hashA, hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_char[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_char[0]);
			Assert.assertTrue(String.format("Failed char with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_char[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_char[1]);
			Assert.assertTrue(String.format("Failed char with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_char[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_char[0]);
			Assert.assertTrue(String.format("Failed char with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_char[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_char[1]);
			Assert.assertTrue(String.format("Failed char with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_obj[0]);
			hashB = Tools.hashTool(b, null, p, p, arr_obj[0]);
			Assert.assertEquals(String.format("Failed obj with prime = %d", p), hashA, hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_obj[0]);
			hashB = Tools.hashTool(a, null, q, p, arr_obj[0]);
			Assert.assertTrue(String.format("Failed obj with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_obj[0]);
			hashB = Tools.hashTool(a, null, p, p, arr_obj[1]);
			Assert.assertTrue(String.format("Failed obj with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_obj[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_obj[0]);
			Assert.assertTrue(String.format("Failed obj with prime = %d", p), hashA != hashB);

			hashA = Tools.hashTool(a, null, p, p, arr_obj[0]);
			hashB = Tools.hashTool(a, null, p, q, arr_obj[1]);
			Assert.assertTrue(String.format("Failed obj with prime = %d", p), hashA != hashB);
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
		Assert.assertNull(Tools.toBoolean(null));
		for (Object[] s : arr) {
			final String str = Tools.toString(s[0]);
			final Boolean expected = Boolean.class.cast(s[1]);
			Assert.assertEquals(String.format("Failed when checking [%s]", str), expected, Tools.toBoolean(str));
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
				try {
					Tools.ensureBetween(a[0], a[1], a[2]);
					Assert.fail(String.format("Failed to raise an exception with array %s", Arrays.toString(a)));
				} catch (IllegalArgumentException e) {
					// Expected, move on
				}
			} else {
				Assert.assertEquals(String.format("Failed when comparing the array %s", Arrays.toString(a)), a[3],
					Tools.ensureBetween(a[0], a[1], a[2]));
			}
		}
	}

	private <C extends Collection<Object>> void validateFrozen(C source, C frozen) {
		final Collection<Object> singleton = Collections.singleton(new Object());
		final String message = String.format("%s is not frozen", source.getClass().getSimpleName());
		Assert.assertEquals(source, frozen);
		try {
			frozen.add(UUID.randomUUID());
			Assert.fail(message);
		} catch (UnsupportedOperationException e) {
			// All is well
		}
		try {
			frozen.addAll(singleton);
			Assert.fail(message);
		} catch (UnsupportedOperationException e) {
			// All is well
		}
		try {
			frozen.remove(0);
			Assert.fail(message);
		} catch (UnsupportedOperationException e) {
			// All is well
		}
		try {
			frozen.removeAll(singleton);
			Assert.fail(message);
		} catch (UnsupportedOperationException e) {
			// All is well
		}
		try {
			frozen.retainAll(singleton);
			Assert.fail(message);
		} catch (UnsupportedOperationException e) {
			// All is well
		}

		Iterator<Object> it = frozen.iterator();
		Assert.assertTrue(it.hasNext());
		try {
			it.remove();
			Assert.fail(message);
		} catch (UnsupportedOperationException e) {
			// All is well
		}
	}

	private void validateFrozen(Map<Object, Object> source, Map<Object, Object> frozen) {
		final UUID uuid = UUID.randomUUID();
		final Map<?, ?> singletonMap = Collections.singletonMap(uuid, uuid.toString());
		final String message = String.format("%s is not frozen", source.getClass().getSimpleName());
		Assert.assertEquals(source, frozen);
		try {
			frozen.put(uuid, uuid.toString());
			Assert.fail(message);
		} catch (UnsupportedOperationException e) {
			// All is well
		}
		try {
			frozen.putAll(singletonMap);
			Assert.fail(message);
		} catch (UnsupportedOperationException e) {
			// All is well
		}
		try {
			frozen.remove(0);
			Assert.fail(message);
		} catch (UnsupportedOperationException e) {
			// All is well
		}
		try {
			frozen.remove(uuid);
			Assert.fail(message);
		} catch (UnsupportedOperationException e) {
			// All is well
		}

		Iterator<Object> it = frozen.keySet().iterator();
		Assert.assertTrue(it.hasNext());
		try {
			it.remove();
			Assert.fail(message);
		} catch (UnsupportedOperationException e) {
			// All is well
		}

		it = frozen.values().iterator();
		Assert.assertTrue(it.hasNext());
		try {
			it.remove();
			Assert.fail(message);
		} catch (UnsupportedOperationException e) {
			// All is well
		}
	}

	@Test
	public void testFreezers() {
		final UUID newUuid = UUID.randomUUID();
		// freezeCopy
		final List<Object> nullList = null;
		final List<Object> list = new ArrayList<Object>();
		List<Object> frozenList = null;

		final Set<Object> nullSet = null;
		final Set<Object> set = new HashSet<Object>();
		final Set<Object> sortedSet = new TreeSet<Object>();
		Set<Object> frozenSet = null;

		final Map<Object, Object> nullMap = null;
		final Map<Object, Object> map = new HashMap<Object, Object>();
		final Map<Object, Object> sortedMap = new TreeMap<Object, Object>();
		Map<Object, Object> frozenMap = null;

		for (int i = 0; i < 5; i++) {
			UUID uuid = UUID.randomUUID();
			list.add(uuid);
			set.add(uuid);
			sortedSet.add(uuid);
			map.put(uuid, uuid.toString());
			sortedMap.put(uuid, uuid.toString());
		}

		Assert.assertNull(Tools.freezeCopy(nullList));
		Assert.assertNull(Tools.freezeCopy(nullList, false));

		frozenList = Tools.freezeCopy(nullList, true);
		Assert.assertNotNull(frozenList);
		Assert.assertTrue(frozenList.isEmpty());
		Assert.assertSame(Collections.emptyList(), frozenList);

		frozenList = Tools.freezeCopy(list);
		Assert.assertNotSame(list, frozenList);
		validateFrozen(list, frozenList);
		list.add(newUuid);
		Assert.assertNotEquals(list, frozenList);

		Assert.assertNull(Tools.freezeCopy(nullSet));
		Assert.assertNull(Tools.freezeCopy(nullSet, false));

		frozenSet = Tools.freezeCopy(nullSet, true);
		Assert.assertNotNull(frozenSet);
		Assert.assertTrue(frozenSet.isEmpty());
		Assert.assertSame(Collections.emptySet(), frozenSet);

		frozenSet = Tools.freezeCopy(set);
		Assert.assertNotSame(set, frozenSet);
		validateFrozen(set, frozenSet);
		set.add(newUuid);
		Assert.assertNotEquals(set, frozenSet);

		frozenSet = Tools.freezeCopy(sortedSet);
		Assert.assertNotSame(sortedSet, frozenSet);
		validateFrozen(sortedSet, frozenSet);
		sortedSet.add(newUuid);
		Assert.assertNotEquals(sortedSet, frozenSet);

		Assert.assertNull(Tools.freezeCopy(nullMap));
		Assert.assertNull(Tools.freezeCopy(nullMap, false));

		frozenMap = Tools.freezeCopy(nullMap, true);
		Assert.assertNotNull(frozenMap);
		Assert.assertTrue(frozenMap.isEmpty());
		Assert.assertSame(Collections.emptyMap(), frozenMap);

		frozenMap = Tools.freezeCopy(map);
		validateFrozen(map, frozenMap);
		map.put(newUuid, newUuid.toString());
		Assert.assertNotEquals(map, frozenMap);

		frozenMap = Tools.freezeCopy(sortedMap);
		validateFrozen(sortedMap, frozenMap);
		sortedMap.put(newUuid, newUuid.toString());
		Assert.assertNotEquals(sortedMap, frozenMap);

		// freeze{List,Map,Set}
		list.remove(newUuid);
		set.remove(newUuid);
		sortedSet.remove(newUuid);
		map.remove(newUuid);
		sortedMap.remove(newUuid);

		Assert.assertNull(Tools.freezeList(nullList));
		Assert.assertNull(Tools.freezeList(nullList, false));

		frozenList = Tools.freezeList(nullList, true);
		Assert.assertNotNull(frozenList);
		Assert.assertTrue(frozenList.isEmpty());
		Assert.assertSame(Collections.emptyList(), frozenList);

		frozenList = Tools.freezeList(list);
		validateFrozen(list, frozenList);
		list.add(newUuid);
		Assert.assertEquals(list, frozenList);

		Assert.assertNull(Tools.freezeSet(nullSet));
		Assert.assertNull(Tools.freezeSet(nullSet, false));

		frozenSet = Tools.freezeSet(nullSet, true);
		Assert.assertNotNull(frozenSet);
		Assert.assertTrue(frozenSet.isEmpty());
		Assert.assertSame(Collections.emptySet(), frozenSet);

		frozenSet = Tools.freezeSet(set);
		validateFrozen(set, frozenSet);
		set.add(newUuid);
		Assert.assertEquals(set, frozenSet);

		frozenSet = Tools.freezeSet(sortedSet);
		validateFrozen(sortedSet, frozenSet);
		sortedSet.add(newUuid);
		Assert.assertEquals(sortedSet, frozenSet);

		Assert.assertNull(Tools.freezeMap(nullMap));
		Assert.assertNull(Tools.freezeMap(nullMap, false));

		frozenMap = Tools.freezeMap(nullMap, true);
		Assert.assertNotNull(frozenMap);
		Assert.assertTrue(frozenMap.isEmpty());
		Assert.assertSame(Collections.emptyMap(), frozenMap);

		frozenMap = Tools.freezeMap(map);
		validateFrozen(map, frozenMap);
		map.put(newUuid, newUuid.toString());
		Assert.assertEquals(map, frozenMap);

		frozenMap = Tools.freezeMap(sortedMap);
		validateFrozen(sortedMap, frozenMap);
		sortedMap.put(newUuid, newUuid.toString());
		Assert.assertEquals(sortedMap, frozenMap);
	}

	private static enum TestEnumA {
		//
		A,
		B,
		C,
		D,
		E
	}

	private static enum TestEnumB {
		//
		Z,
		X,
		Y,
		W,
		V
	}

	private static enum TestEnumC {
		//
		a,
		b,
		c,
		d,
		e,
		z,
		x,
		y,
		w,
		v
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

		try {
			Tools.parseEnumCSV(null, "", false);
			Assert.fail("Did not fail with a null enum class");
		} catch (IllegalArgumentException e) {
			// All is well
		}

		str = null;
		setA = Tools.parseEnumCSV(TestEnumA.class, str, false);
		expA = EnumSet.noneOf(TestEnumA.class);
		Assert.assertEquals(expA, setA);
		setB = Tools.parseEnumCSV(TestEnumB.class, str, false);
		expB = EnumSet.noneOf(TestEnumB.class);
		Assert.assertEquals(expB, setB);
		setC = Tools.parseEnumCSV(TestEnumC.class, str, false);
		expC = EnumSet.noneOf(TestEnumC.class);
		Assert.assertEquals(expC, setC);

		str = "";
		setA = Tools.parseEnumCSV(TestEnumA.class, str, false);
		expA = EnumSet.noneOf(TestEnumA.class);
		Assert.assertEquals(expA, setA);
		setB = Tools.parseEnumCSV(TestEnumB.class, str, false);
		expB = EnumSet.noneOf(TestEnumB.class);
		Assert.assertEquals(expB, setB);
		setC = Tools.parseEnumCSV(TestEnumC.class, str, false);
		expC = EnumSet.noneOf(TestEnumC.class);
		Assert.assertEquals(expC, setC);

		str = "ALL";
		setA = Tools.parseEnumCSV(TestEnumA.class, str, null, false);
		expA = EnumSet.noneOf(TestEnumA.class);
		Assert.assertEquals(expA, setA);
		setB = Tools.parseEnumCSV(TestEnumB.class, str, null, false);
		expB = EnumSet.noneOf(TestEnumB.class);
		Assert.assertEquals(expB, setB);
		setC = Tools.parseEnumCSV(TestEnumC.class, str, null, false);
		expC = EnumSet.noneOf(TestEnumC.class);
		Assert.assertEquals(expC, setC);

		str = "ALL";
		setA = Tools.parseEnumCSV(TestEnumA.class, str, "ALL", false);
		expA = EnumSet.allOf(TestEnumA.class);
		Assert.assertEquals(expA, setA);
		setB = Tools.parseEnumCSV(TestEnumB.class, str, "ALL", false);
		expB = EnumSet.allOf(TestEnumB.class);
		Assert.assertEquals(expB, setB);
		setC = Tools.parseEnumCSV(TestEnumC.class, str, "ALL", false);
		expC = EnumSet.allOf(TestEnumC.class);
		Assert.assertEquals(expC, setC);

		str = "A,a,C,c,Z,z,X,x";
		try {
			setA = Tools.parseEnumCSV(TestEnumA.class, str, true);
			Assert.fail(String.format("TestEnumA did not fail with values [%s]", str));
		} catch (IllegalArgumentException e) {
			// All is well
		}
		setA = Tools.parseEnumCSV(TestEnumA.class, str, false);
		expA = EnumSet.noneOf(TestEnumA.class);
		expA.add(TestEnumA.A);
		expA.add(TestEnumA.C);
		Assert.assertEquals(expA, setA);
		try {
			setB = Tools.parseEnumCSV(TestEnumB.class, str, true);
			Assert.fail(String.format("TestEnumB did not fail with values [%s]", str));
		} catch (IllegalArgumentException e) {
			// All is well
		}
		setB = Tools.parseEnumCSV(TestEnumB.class, str, false);
		expB = EnumSet.noneOf(TestEnumB.class);
		expB.add(TestEnumB.Z);
		expB.add(TestEnumB.X);
		Assert.assertEquals(expB, setB);
		try {
			setC = Tools.parseEnumCSV(TestEnumC.class, str, true);
			Assert.fail(String.format("TestEnumC did not fail with values [%s]", str));
		} catch (IllegalArgumentException e) {
			// All is well
		}
		setC = Tools.parseEnumCSV(TestEnumC.class, str, false);
		expC = EnumSet.noneOf(TestEnumC.class);
		expC.add(TestEnumC.a);
		expC.add(TestEnumC.c);
		expC.add(TestEnumC.z);
		expC.add(TestEnumC.x);
		Assert.assertEquals(expC, setC);

	}
}