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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author drivera@armedia.com
 *
 */
public class ComparisonTest implements GoodServiceTest {

	@Test
	public void testLT() {
		final Comparison comparison = Comparison.LT;
		Object[][] intData = {
			{
				null, null, false
			}, {
				null, 0, true
			}, {
				0, null, false
			}, {
				0, 0, false
			}, {
				0, 1, true
			}, {
				1, 0, false
			}
		};
		for (Object[] o : intData) {
			Integer a = Integer.class.cast(o[0]);
			Integer b = Integer.class.cast(o[1]);
			Boolean result = Boolean.class.cast(o[2]);
			Assertions.assertEquals(result, comparison.matches(a, b));
		}
		Object[][] stringData = {
			{
				null, null, false
			}, {
				null, "", true
			}, {
				"", null, false
			}, {
				"", "ASDF", true
			}, {
				"ASDF", "", false
			}, {
				"asdf", "ASDF".toLowerCase(), false
			}, {
				"asdf", "ASDF", false
			}, {
				"ASDF", "asdf", true
			}
		};
		for (Object[] o : stringData) {
			String a = String.class.cast(o[0]);
			String b = String.class.cast(o[1]);
			Boolean result = Boolean.class.cast(o[2]);
			Assertions.assertEquals(result, comparison.matches(a, b));
		}
	}

	@Test
	public void testLE() {
		final Comparison comparison = Comparison.LE;
		Object[][] intData = {
			{
				null, null, true
			}, {
				null, 0, true
			}, {
				0, null, false
			}, {
				0, 0, true
			}, {
				0, 1, true
			}, {
				1, 0, false
			}
		};
		for (Object[] o : intData) {
			Integer a = Integer.class.cast(o[0]);
			Integer b = Integer.class.cast(o[1]);
			Boolean result = Boolean.class.cast(o[2]);
			Assertions.assertEquals(result, comparison.matches(a, b));
		}
		Object[][] stringData = {
			{
				null, null, true
			}, {
				null, "", true
			}, {
				"", null, false
			}, {
				"", "ASDF", true
			}, {
				"ASDF", "", false
			}, {
				"asdf", "ASDF".toLowerCase(), true
			}, {
				"asdf", "ASDF", false
			}, {
				"ASDF", "asdf", true
			}
		};
		for (Object[] o : stringData) {
			String a = String.class.cast(o[0]);
			String b = String.class.cast(o[1]);
			Boolean result = Boolean.class.cast(o[2]);
			Assertions.assertEquals(result, comparison.matches(a, b));
		}
	}

	@Test
	public void testEQ() {
		final Comparison comparison = Comparison.EQ;
		Object[][] intData = {
			{
				null, null, true
			}, {
				null, 0, false
			}, {
				0, null, false
			}, {
				0, 0, true
			}, {
				0, 1, false
			}, {
				1, 0, false
			}
		};
		for (Object[] o : intData) {
			Integer a = Integer.class.cast(o[0]);
			Integer b = Integer.class.cast(o[1]);
			Boolean result = Boolean.class.cast(o[2]);
			Assertions.assertEquals(result, comparison.matches(a, b));
		}
		Object[][] stringData = {
			{
				null, null, true
			}, {
				null, "", false
			}, {
				"", null, false
			}, {
				"", "ASDF", false
			}, {
				"ASDF", "", false
			}, {
				"asdf", "ASDF".toLowerCase(), true
			}, {
				"asdf", "ASDF", false
			}, {
				"ASDF", "asdf", false
			}
		};
		for (Object[] o : stringData) {
			String a = String.class.cast(o[0]);
			String b = String.class.cast(o[1]);
			Boolean result = Boolean.class.cast(o[2]);
			Assertions.assertEquals(result, comparison.matches(a, b));
		}
	}

	@Test
	public void testGE() {
		final Comparison comparison = Comparison.GE;
		Object[][] intData = {
			{
				null, null, true
			}, {
				null, 0, false
			}, {
				0, null, true
			}, {
				0, 0, true
			}, {
				0, 1, false
			}, {
				1, 0, true
			}
		};
		for (Object[] o : intData) {
			Integer a = Integer.class.cast(o[0]);
			Integer b = Integer.class.cast(o[1]);
			Boolean result = Boolean.class.cast(o[2]);
			Assertions.assertEquals(result, comparison.matches(a, b));
		}
		Object[][] stringData = {
			{
				null, null, true
			}, {
				null, "", false
			}, {
				"", null, true
			}, {
				"", "ASDF", false
			}, {
				"ASDF", "", true
			}, {
				"asdf", "ASDF".toLowerCase(), true
			}, {
				"asdf", "ASDF", true
			}, {
				"ASDF", "asdf", false
			}
		};
		for (Object[] o : stringData) {
			String a = String.class.cast(o[0]);
			String b = String.class.cast(o[1]);
			Boolean result = Boolean.class.cast(o[2]);
			Assertions.assertEquals(result, comparison.matches(a, b));
		}
	}

	@Test
	public void testGT() {
		final Comparison comparison = Comparison.GT;
		Object[][] intData = {
			{
				null, null, false
			}, {
				null, 0, false
			}, {
				0, null, true
			}, {
				0, 0, false
			}, {
				0, 1, false
			}, {
				1, 0, true
			}
		};
		for (Object[] o : intData) {
			Integer a = Integer.class.cast(o[0]);
			Integer b = Integer.class.cast(o[1]);
			Boolean result = Boolean.class.cast(o[2]);
			Assertions.assertEquals(result, comparison.matches(a, b));
		}
		Object[][] stringData = {
			{
				null, null, false
			}, {
				null, "", false
			}, {
				"", null, true
			}, {
				"", "ASDF", false
			}, {
				"ASDF", "", true
			}, {
				"asdf", "ASDF".toLowerCase(), false
			}, {
				"asdf", "ASDF", true
			}, {
				"ASDF", "asdf", false
			}
		};
		for (Object[] o : stringData) {
			String a = String.class.cast(o[0]);
			String b = String.class.cast(o[1]);
			Boolean result = Boolean.class.cast(o[2]);
			Assertions.assertEquals(result, comparison.matches(a, b));
		}
	}

	@Test
	public void testDecode() {
		Object[][] goodData = {
			{
				Comparison.LT, "<", "LT", "lt", "lT", "Lt"
			}, {
				Comparison.LE, "<=", "LE", "le", "lE", "Le"
			}, {
				Comparison.EQ, "=", "==", "EQ", "eq", "eQ", "Eq"
			}, {
				Comparison.GE, ">=", "GE", "ge", "gE", "Ge"
			}, {
				Comparison.GT, ">", "GT", "gt", "gT", "Gt"
			},
		};
		for (Object[] o : goodData) {
			Comparison a = Comparison.class.cast(o[0]);
			for (int i = 1; i < o.length; i++) {
				final String symbol = String.valueOf(o[i]);
				Assertions.assertEquals(a, Comparison.decode(symbol));
			}
		}
		Assertions.assertNull(Comparison.decode(null));
	}
}