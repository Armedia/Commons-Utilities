/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (c) 2010 - 2019 Armedia LLC
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 *
 *
 */
public class ComparisonTest implements GoodService {

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