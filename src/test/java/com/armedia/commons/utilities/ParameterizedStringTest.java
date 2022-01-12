/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2022 Armedia, LLC
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

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 *
 */
public class ParameterizedStringTest {

	private static String[] PARAMETERS;
	private static String[] VALUES;
	private static String PATTERN;

	@BeforeAll
	public static void beforeClass() {
		String[] parameters = {
			"a", "b", "c", "d", "e", "f", "g", "h"
		};
		StringBuilder b = new StringBuilder();
		for (String p : parameters) {
			b.append(String.format(" ${%s}", p));
		}
		String[] values = new String[parameters.length];
		Random rnd = new Random(System.currentTimeMillis());
		int nullable = 2;
		for (int i = 0; i < values.length; i++) {
			if ((nullable > 0) && rnd.nextBoolean()) {
				values[i] = null;
				nullable--;
				continue;
			}
			values[i] = UUID.randomUUID().toString();
		}
		ParameterizedStringTest.PARAMETERS = parameters;
		ParameterizedStringTest.VALUES = values;
		ParameterizedStringTest.PATTERN = b.toString();
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.ParameterizedString#ParameterizedString(java.lang.String)}.
	 */
	@Test
	public void testParameterizedString() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> new ParameterizedString(null));
		String pattern = UUID.randomUUID().toString();
		ParameterizedString str = new ParameterizedString(pattern);
		Assertions.assertEquals(0, str.getParameterCount());
		Assertions.assertTrue(str.getParameterNames().isEmpty());
		Assertions.assertEquals(pattern, str.getPattern());
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.ParameterizedString#set(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSet() {
		ParameterizedString pstr = new ParameterizedString(ParameterizedStringTest.PATTERN);
		for (int i = 0; i < ParameterizedStringTest.PARAMETERS.length; i++) {
			pstr.set(ParameterizedStringTest.PARAMETERS[i], ParameterizedStringTest.VALUES[i]);
		}
		Assertions.assertFalse(ParameterizedStringTest.PATTERN.equals(pstr.evaluate()));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.ParameterizedString#isSet(java.lang.String)}.
	 */
	@Test
	public void testIsSet() {
		ParameterizedString pstr = new ParameterizedString(ParameterizedStringTest.PATTERN);
		for (int i = 0; i < ParameterizedStringTest.PARAMETERS.length; i++) {
			pstr.set(ParameterizedStringTest.PARAMETERS[i], ParameterizedStringTest.VALUES[i]);
			if (ParameterizedStringTest.VALUES[i] != null) {
				Assertions.assertTrue(pstr.isSet(ParameterizedStringTest.PARAMETERS[i]));
			} else {
				Assertions.assertFalse(pstr.isSet(ParameterizedStringTest.PARAMETERS[i]));
			}
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.ParameterizedString#getParameterValue(java.lang.String)}.
	 */
	@Test
	public void testGetParameterValue() {
		ParameterizedString pstr = new ParameterizedString(ParameterizedStringTest.PATTERN);
		for (int i = 0; i < ParameterizedStringTest.PARAMETERS.length; i++) {
			pstr.set(ParameterizedStringTest.PARAMETERS[i], ParameterizedStringTest.VALUES[i]);
			Assertions.assertEquals(ParameterizedStringTest.VALUES[i],
				pstr.getParameterValue(ParameterizedStringTest.PARAMETERS[i]));
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.ParameterizedString#clear(java.lang.String)}.
	 */
	@Test
	public void testClear() {
		ParameterizedString pstr = new ParameterizedString(ParameterizedStringTest.PATTERN);
		for (int i = 0; i < ParameterizedStringTest.PARAMETERS.length; i++) {
			pstr.set(ParameterizedStringTest.PARAMETERS[i], ParameterizedStringTest.VALUES[i]);
		}
		final String fullEval = pstr.evaluate();
		Assertions.assertFalse(ParameterizedStringTest.PATTERN.equals(fullEval));
		for (int i = 0; i < ParameterizedStringTest.PARAMETERS.length; i++) {
			Assertions.assertEquals(ParameterizedStringTest.VALUES[i],
				pstr.getParameterValue(ParameterizedStringTest.PARAMETERS[i]));
			if (ParameterizedStringTest.VALUES[i] != null) {
				pstr.clear(ParameterizedStringTest.PARAMETERS[i]);
				Assertions.assertNull(pstr.getParameterValue(ParameterizedStringTest.PARAMETERS[i]));
				Assertions.assertFalse(pstr.isSet(ParameterizedStringTest.PARAMETERS[i]));

				// Once it's cleared it should render a different evaluation because it has a value
				Assertions.assertFalse(fullEval.equals(pstr.evaluate()));

				// Re-set it, for additional tests
				pstr.set(ParameterizedStringTest.PARAMETERS[i], ParameterizedStringTest.VALUES[i]);
			}
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.ParameterizedString#getParameterNames()}.
	 */
	@Test
	public void testGetParameterNames() {
		ParameterizedString pstr = new ParameterizedString(ParameterizedStringTest.PATTERN);
		for (int i = 0; i < ParameterizedStringTest.PARAMETERS.length; i++) {
			pstr.set(ParameterizedStringTest.PARAMETERS[i], ParameterizedStringTest.PARAMETERS[i]);
			Object[] a = Arrays.copyOf(ParameterizedStringTest.PARAMETERS, i + 1);
			Arrays.sort(a);
			Object[] b = pstr.getParameterNames().toArray();
			Arrays.sort(b);
			Assertions.assertArrayEquals(a, b);
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.ParameterizedString#getNewCopy()}.
	 */
	@Test
	public void testGetNewCopy() {
		ParameterizedString pstr = new ParameterizedString(ParameterizedStringTest.PATTERN);
		for (int i = 0; i < ParameterizedStringTest.PARAMETERS.length; i++) {
			pstr.set(ParameterizedStringTest.PARAMETERS[i], ParameterizedStringTest.VALUES[i]);
		}
		ParameterizedString newCopy = pstr.getNewCopy();
		Assertions.assertEquals(pstr.getPattern(), newCopy.getPattern());
		Assertions.assertEquals(0, newCopy.getParameterCount());
		Assertions.assertTrue(newCopy.getParameterNames().isEmpty());
		Assertions.assertEquals(pstr.getPattern(), newCopy.evaluate());
		Assertions.assertFalse(pstr.evaluate().equals(newCopy.evaluate()));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.ParameterizedString#getPattern()}.
	 */
	@Test
	public void testGetPattern() {
		ParameterizedString pstr = new ParameterizedString(ParameterizedStringTest.PATTERN);
		Assertions.assertEquals(ParameterizedStringTest.PATTERN, pstr.getPattern());
		String value = UUID.randomUUID().toString();
		pstr = new ParameterizedString(value);
		Assertions.assertEquals(value, pstr.getPattern());
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.ParameterizedString#clearParameters()}.
	 */
	@Test
	public void testClearParameters() {
		ParameterizedString pstr = new ParameterizedString(ParameterizedStringTest.PATTERN);
		for (int i = 0; i < ParameterizedStringTest.PARAMETERS.length; i++) {
			pstr.set(ParameterizedStringTest.PARAMETERS[i], ParameterizedStringTest.VALUES[i]);
		}
		Assertions.assertFalse(ParameterizedStringTest.PATTERN.equals(pstr.evaluate()));
		pstr.clearParameters();
		Assertions.assertTrue(pstr.getParameterNames().isEmpty());
		Assertions.assertEquals(0, pstr.getParameterCount());
		Assertions.assertEquals(ParameterizedStringTest.PATTERN, pstr.evaluate());
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.ParameterizedString#getParameterCount()}.
	 */
	@Test
	public void testGetParameterCount() {
		ParameterizedString pstr = new ParameterizedString(ParameterizedStringTest.PATTERN);
		for (int i = 0; i < ParameterizedStringTest.PARAMETERS.length; i++) {
			pstr.set(ParameterizedStringTest.PARAMETERS[i], ParameterizedStringTest.PARAMETERS[i]);
			Assertions.assertEquals(i + 1, pstr.getParameterCount());
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.ParameterizedString#evaluate()}.
	 */
	@Test
	public void testEvaluate() {
		ParameterizedString pstr = new ParameterizedString(ParameterizedStringTest.PATTERN);
		Assertions.assertEquals(ParameterizedStringTest.PATTERN, pstr.evaluate());
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < ParameterizedStringTest.PARAMETERS.length; i++) {
			pstr.set(ParameterizedStringTest.PARAMETERS[i], ParameterizedStringTest.VALUES[i]);
			b.append(" ");
			if (ParameterizedStringTest.VALUES[i] != null) {
				b.append(ParameterizedStringTest.VALUES[i]);
			} else {
				b.append(String.format("${%s}", ParameterizedStringTest.PARAMETERS[i]));
			}
		}
		Assertions.assertFalse(ParameterizedStringTest.PATTERN.equals(pstr.evaluate()));
		Assertions.assertEquals(b.toString(), pstr.evaluate());
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.ParameterizedString#toString()}.
	 */
	@Test
	public void testToString() {
		ParameterizedString pstr = new ParameterizedString(ParameterizedStringTest.PATTERN);
		Assertions.assertEquals(ParameterizedStringTest.PATTERN, pstr.toString());
		for (int i = 0; i < ParameterizedStringTest.PARAMETERS.length; i++) {
			pstr.set(ParameterizedStringTest.PARAMETERS[i], ParameterizedStringTest.VALUES[i]);
		}
		String full = pstr.evaluate();
		Assertions.assertEquals(full, pstr.toString());
	}
}
