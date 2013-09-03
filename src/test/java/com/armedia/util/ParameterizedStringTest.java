/**
 * *******************************************************************
 * 
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS.
 * REPRODUCTION OF ANY PORTION OF THE SOURCE CODE, CONTAINED HEREIN,
 * OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE,
 * IS STRICTLY PROHIBITED.
 * 
 * Confidential Property of Armedia LLC.
 * (c) Copyright Armedia LLC 2011.
 * All Rights reserved.
 * 
 * *******************************************************************
 */
package com.armedia.util;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.armedia.commons.utilities.ParameterizedString;

/**
 * @author drivera@armedia.com
 * 
 */
public class ParameterizedStringTest {

	private static String[] PARAMETERS;
	private static String[] VALUES;
	private static String PATTERN;

	@BeforeClass
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
		try {
			new ParameterizedString(null);
			Assert.fail("The constructor should fail with a null parameter");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		String pattern = UUID.randomUUID().toString();
		ParameterizedString str = new ParameterizedString(pattern);
		Assert.assertEquals(0, str.getParameterCount());
		Assert.assertTrue(str.getParameterNames().isEmpty());
		Assert.assertEquals(pattern, str.getPattern());
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
		Assert.assertFalse(ParameterizedStringTest.PATTERN.equals(pstr.evaluate()));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.ParameterizedString#isSet(java.lang.String)}.
	 */
	@Test
	public void testIsSet() {
		ParameterizedString pstr = new ParameterizedString(ParameterizedStringTest.PATTERN);
		for (int i = 0; i < ParameterizedStringTest.PARAMETERS.length; i++) {
			pstr.set(ParameterizedStringTest.PARAMETERS[i], ParameterizedStringTest.VALUES[i]);
			if (ParameterizedStringTest.VALUES[i] != null) {
				Assert.assertTrue(pstr.isSet(ParameterizedStringTest.PARAMETERS[i]));
			} else {
				Assert.assertFalse(pstr.isSet(ParameterizedStringTest.PARAMETERS[i]));
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
			Assert.assertEquals(ParameterizedStringTest.VALUES[i],
				pstr.getParameterValue(ParameterizedStringTest.PARAMETERS[i]));
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.ParameterizedString#clear(java.lang.String)}.
	 */
	@Test
	public void testClear() {
		ParameterizedString pstr = new ParameterizedString(ParameterizedStringTest.PATTERN);
		for (int i = 0; i < ParameterizedStringTest.PARAMETERS.length; i++) {
			pstr.set(ParameterizedStringTest.PARAMETERS[i], ParameterizedStringTest.VALUES[i]);
		}
		final String fullEval = pstr.evaluate();
		Assert.assertFalse(ParameterizedStringTest.PATTERN.equals(fullEval));
		for (int i = 0; i < ParameterizedStringTest.PARAMETERS.length; i++) {
			Assert.assertEquals(ParameterizedStringTest.VALUES[i],
				pstr.getParameterValue(ParameterizedStringTest.PARAMETERS[i]));
			if (ParameterizedStringTest.VALUES[i] != null) {
				pstr.clear(ParameterizedStringTest.PARAMETERS[i]);
				Assert.assertNull(pstr.getParameterValue(ParameterizedStringTest.PARAMETERS[i]));
				Assert.assertFalse(pstr.isSet(ParameterizedStringTest.PARAMETERS[i]));

				// Once it's cleared it should render a different evaluation because it has a value
				Assert.assertFalse(fullEval.equals(pstr.evaluate()));

				// Re-set it, for additional tests
				pstr.set(ParameterizedStringTest.PARAMETERS[i], ParameterizedStringTest.VALUES[i]);
			}
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.ParameterizedString#getParameterNames()}.
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
			Assert.assertArrayEquals(a, b);
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
		Assert.assertEquals(pstr.getPattern(), newCopy.getPattern());
		Assert.assertEquals(0, newCopy.getParameterCount());
		Assert.assertTrue(newCopy.getParameterNames().isEmpty());
		Assert.assertEquals(pstr.getPattern(), newCopy.evaluate());
		Assert.assertFalse(pstr.evaluate().equals(newCopy.evaluate()));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.ParameterizedString#getPattern()}.
	 */
	@Test
	public void testGetPattern() {
		ParameterizedString pstr = new ParameterizedString(ParameterizedStringTest.PATTERN);
		Assert.assertEquals(ParameterizedStringTest.PATTERN, pstr.getPattern());
		String value = UUID.randomUUID().toString();
		pstr = new ParameterizedString(value);
		Assert.assertEquals(value, pstr.getPattern());
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
		Assert.assertFalse(ParameterizedStringTest.PATTERN.equals(pstr.evaluate()));
		pstr.clearParameters();
		Assert.assertTrue(pstr.getParameterNames().isEmpty());
		Assert.assertEquals(0, pstr.getParameterCount());
		Assert.assertEquals(ParameterizedStringTest.PATTERN, pstr.evaluate());
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.ParameterizedString#getParameterCount()}.
	 */
	@Test
	public void testGetParameterCount() {
		ParameterizedString pstr = new ParameterizedString(ParameterizedStringTest.PATTERN);
		for (int i = 0; i < ParameterizedStringTest.PARAMETERS.length; i++) {
			pstr.set(ParameterizedStringTest.PARAMETERS[i], ParameterizedStringTest.PARAMETERS[i]);
			Assert.assertEquals(i + 1, pstr.getParameterCount());
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.ParameterizedString#evaluate()}.
	 */
	@Test
	public void testEvaluate() {
		ParameterizedString pstr = new ParameterizedString(ParameterizedStringTest.PATTERN);
		Assert.assertEquals(ParameterizedStringTest.PATTERN, pstr.evaluate());
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
		Assert.assertFalse(ParameterizedStringTest.PATTERN.equals(pstr.evaluate()));
		Assert.assertEquals(b.toString(), pstr.evaluate());
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.ParameterizedString#toString()}.
	 */
	@Test
	public void testToString() {
		ParameterizedString pstr = new ParameterizedString(ParameterizedStringTest.PATTERN);
		Assert.assertEquals(ParameterizedStringTest.PATTERN, pstr.toString());
		for (int i = 0; i < ParameterizedStringTest.PARAMETERS.length; i++) {
			pstr.set(ParameterizedStringTest.PARAMETERS[i], ParameterizedStringTest.VALUES[i]);
		}
		String full = pstr.evaluate();
		Assert.assertEquals(full, pstr.toString());
	}
}