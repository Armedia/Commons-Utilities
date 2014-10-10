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
package com.armedia.commons.utilities;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author drivera@armedia.com
 * 
 */
public class CfgToolsTest {
	private static final Random RANDOM = new Random(System.currentTimeMillis());

	private static CfgTools CONFIG = null;

	private static float epsilon = 0.0f;

	private static void assertEquals(float a, float b) {
		if (Math.abs(a - b) > CfgToolsTest.epsilon) {
			Assert.fail(String.format("The given floating point numbers %f and %f do not converge to within %f", a, b,
				CfgToolsTest.epsilon));
		}
	}

	private static void assertEquals(double a, double b) {
		if (Math.abs(a - b) > CfgToolsTest.epsilon) {
			Assert.fail(String.format("The given floating point numbers %f and %f do not converge to within %f", a, b,
				CfgToolsTest.epsilon));
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Properties props = new Properties();
		InputStream in = CfgToolsTest.class.getClassLoader().getResourceAsStream("cfgtools.properties");
		props.load(in);
		IOUtils.closeQuietly(in);
		Map<String, String> cfg = new HashMap<String, String>();
		for (Map.Entry<Object, Object> e : props.entrySet()) {
			cfg.put(e.getKey().toString(), e.getValue().toString());
		}
		CfgToolsTest.CONFIG = new CfgTools(cfg);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		CfgToolsTest.CONFIG = null;
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getBoolean(String, Boolean)} .
	 */
	@Test
	public void testGetBooleanWithDefault() {
		try {
			CfgToolsTest.CONFIG.getBoolean(String.class.cast(null), null);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			CfgToolsTest.CONFIG.getBoolean(String.class.cast(null), Boolean.TRUE);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			CfgToolsTest.CONFIG.getBoolean(String.class.cast(null), Boolean.FALSE);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getBoolean("boolean.empty", null));
		Assert.assertTrue(CfgToolsTest.CONFIG.getBoolean("boolean.empty", Boolean.TRUE));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean("boolean.empty", Boolean.FALSE));

		Assert.assertNull(CfgToolsTest.CONFIG.getBoolean("boolean.true.undef", null));
		Assert.assertTrue(CfgToolsTest.CONFIG.getBoolean("boolean.true.undef", Boolean.TRUE));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean("boolean.true.undef", Boolean.FALSE));

		Assert.assertTrue(CfgToolsTest.CONFIG.getBoolean("boolean.true", null));
		Assert.assertTrue(CfgToolsTest.CONFIG.getBoolean("boolean.true", Boolean.TRUE));
		Assert.assertTrue(CfgToolsTest.CONFIG.getBoolean("boolean.true", Boolean.FALSE));

		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean("boolean.false", null));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean("boolean.false", Boolean.TRUE));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean("boolean.false", Boolean.FALSE));

		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean("integer.min", null));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean("double.max", null));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean("string.sample", null));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getBoolean(String)}.
	 */
	@Test
	public void testGetBooleanWithoutDefault() {
		try {
			CfgToolsTest.CONFIG.getBoolean(String.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getBoolean("boolean.true.undef"));
		Assert.assertTrue(CfgToolsTest.CONFIG.getBoolean("boolean.true"));
		Assert.assertNull(CfgToolsTest.CONFIG.getBoolean("boolean.empty"));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean("boolean.false"));
		Assert.assertNull(CfgToolsTest.CONFIG.getBoolean("boolean.false.undef"));

		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean("integer.min"));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean("double.max"));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean("string.sample"));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#getBoolean(com.armedia.commons.utilities.ConfigurationSetting)} .
	 */
	@Test
	public void testGetBooleanSetting() {
		try {
			CfgToolsTest.CONFIG.getBoolean(ConfigurationSetting.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getBoolean(TestSetting.BOOLEAN_EMPTY));
		Assert.assertTrue(CfgToolsTest.CONFIG.getBoolean(TestSetting.BOOLEAN_TRUE_UNDEF));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean(TestSetting.BOOLEAN_FALSE_UNDEF));

		Assert.assertTrue(CfgToolsTest.CONFIG.getBoolean(TestSetting.BOOLEAN_TRUE));
		Assert.assertTrue(CfgToolsTest.CONFIG.getBoolean(TestSetting.BOOLEAN_TRUE_UNDEF));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean(TestSetting.BOOLEAN_FALSE));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean(TestSetting.BOOLEAN_FALSE_UNDEF));

		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean(TestSetting.INTEGER_MIN));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean(TestSetting.INTEGER_MIN_UNDEF));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean(TestSetting.DOUBLE_MAX));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean(TestSetting.DOUBLE_MAX_UNDEF));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean(TestSetting.STRING_SAMPLE));

		Assert.assertNull(CfgToolsTest.CONFIG.getBoolean(TestSettingString.BOOLEAN_EMPTY));
		Assert.assertTrue(CfgToolsTest.CONFIG.getBoolean(TestSettingString.BOOLEAN_TRUE_UNDEF));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean(TestSettingString.BOOLEAN_FALSE_UNDEF));

		Assert.assertTrue(CfgToolsTest.CONFIG.getBoolean(TestSettingString.BOOLEAN_TRUE));
		Assert.assertTrue(CfgToolsTest.CONFIG.getBoolean(TestSettingString.BOOLEAN_TRUE_UNDEF));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean(TestSettingString.BOOLEAN_FALSE));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean(TestSettingString.BOOLEAN_FALSE_UNDEF));

		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean(TestSettingString.INTEGER_MIN));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean(TestSettingString.INTEGER_MIN_UNDEF));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean(TestSettingString.DOUBLE_MAX));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean(TestSettingString.DOUBLE_MAX_UNDEF));
		Assert.assertFalse(CfgToolsTest.CONFIG.getBoolean(TestSettingString.STRING_SAMPLE));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getByte(String, Byte)} .
	 */
	@Test
	public void testGetByteWithDefault() {
		try {
			CfgToolsTest.CONFIG.getByte(String.class.cast(null), null);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			CfgToolsTest.CONFIG.getByte(String.class.cast(null), Byte.MIN_VALUE);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			CfgToolsTest.CONFIG.getByte(String.class.cast(null), Byte.MAX_VALUE);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getByte("byte.empty", null));
		Assert.assertTrue(Byte.MIN_VALUE == CfgToolsTest.CONFIG.getByte("byte.empty", Byte.MIN_VALUE));
		Assert.assertTrue(Byte.MAX_VALUE == CfgToolsTest.CONFIG.getByte("byte.empty", Byte.MAX_VALUE));

		Assert.assertNull(CfgToolsTest.CONFIG.getByte("byte.min.undef", null));
		Assert.assertTrue(Byte.MIN_VALUE == CfgToolsTest.CONFIG.getByte("byte.min.undef", Byte.MIN_VALUE));
		Assert.assertTrue(Byte.MAX_VALUE == CfgToolsTest.CONFIG.getByte("byte.min.undef", Byte.MAX_VALUE));

		Assert.assertTrue(Byte.MIN_VALUE == CfgToolsTest.CONFIG.getByte("byte.min", null));
		Assert.assertTrue(Byte.MIN_VALUE == CfgToolsTest.CONFIG.getByte("byte.min", Byte.MIN_VALUE));
		Assert.assertTrue(Byte.MIN_VALUE == CfgToolsTest.CONFIG.getByte("byte.min", Byte.MAX_VALUE));

		Assert.assertTrue(Byte.MAX_VALUE == CfgToolsTest.CONFIG.getByte("byte.max", null));
		Assert.assertTrue(Byte.MAX_VALUE == CfgToolsTest.CONFIG.getByte("byte.max", Byte.MIN_VALUE));
		Assert.assertTrue(Byte.MAX_VALUE == CfgToolsTest.CONFIG.getByte("byte.max", Byte.MAX_VALUE));

		try {
			CfgToolsTest.CONFIG.getByte("byte.over", null);
			Assert.fail("Failed when getting byte.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getByte("byte.under", null);
			Assert.fail("Failed when getting byte.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getByte("boolean.true", null);
			Assert.fail("Failed when getting boolean.true as a byte - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getByte(String)}.
	 */
	@Test
	public void testGetByteWithoutDefault() {
		try {
			CfgToolsTest.CONFIG.getByte(String.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getByte("byte.empty"));
		Assert.assertTrue(Byte.MIN_VALUE == CfgToolsTest.CONFIG.getByte("byte.min"));
		Assert.assertTrue(Byte.MAX_VALUE == CfgToolsTest.CONFIG.getByte("byte.max"));

		try {
			CfgToolsTest.CONFIG.getByte("byte.over");
			Assert.fail("Failed when getting byte.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getByte("byte.under");
			Assert.fail("Failed when getting byte.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getByte("boolean.true");
			Assert.fail("Failed when getting boolean.true as a byte - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getByte(ConfigurationSetting)} .
	 */
	@Test
	public void testGetByteConfigurationSetting() {
		try {
			CfgToolsTest.CONFIG.getByte(ConfigurationSetting.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getByte(TestSetting.BYTE_EMPTY));
		Assert.assertTrue(Byte.MIN_VALUE == CfgToolsTest.CONFIG.getByte(TestSetting.BYTE_MIN));
		Assert.assertTrue(Byte.MIN_VALUE == CfgToolsTest.CONFIG.getByte(TestSetting.BYTE_MIN_UNDEF));
		Assert.assertTrue(Byte.MAX_VALUE == CfgToolsTest.CONFIG.getByte(TestSetting.BYTE_MAX));
		Assert.assertTrue(Byte.MAX_VALUE == CfgToolsTest.CONFIG.getByte(TestSetting.BYTE_MAX_UNDEF));

		Assert.assertNull(CfgToolsTest.CONFIG.getByte(TestSettingString.BYTE_EMPTY));
		Assert.assertTrue(Byte.MIN_VALUE == CfgToolsTest.CONFIG.getByte(TestSettingString.BYTE_MIN));
		Assert.assertTrue(Byte.MIN_VALUE == CfgToolsTest.CONFIG.getByte(TestSettingString.BYTE_MIN_UNDEF));
		Assert.assertTrue(Byte.MAX_VALUE == CfgToolsTest.CONFIG.getByte(TestSettingString.BYTE_MAX));
		Assert.assertTrue(Byte.MAX_VALUE == CfgToolsTest.CONFIG.getByte(TestSettingString.BYTE_MAX_UNDEF));

		try {
			CfgToolsTest.CONFIG.getByte(TestSetting.BYTE_OVER);
			Assert.fail("Failed when getting byte.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getByte(TestSetting.BYTE_UNDER);
			Assert.fail("Failed when getting byte.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getByte(TestSetting.BOOLEAN_TRUE);
			Assert.fail("Failed when getting boolean.true as a byte - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		Byte b = Byte.valueOf((byte) 64);
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getByte(TestSetting.BYTE_BYTE));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getByte(TestSetting.BYTE_SHORT));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getByte(TestSetting.BYTE_INTEGER));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getByte(TestSetting.BYTE_LONG));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getByte(TestSetting.BYTE_FLOAT));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getByte(TestSetting.BYTE_DOUBLE));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getShort(String, Short)} .
	 */
	@Test
	public void testGetShortWithDefault() {
		try {
			CfgToolsTest.CONFIG.getShort(String.class.cast(null), null);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			CfgToolsTest.CONFIG.getShort(String.class.cast(null), Short.MIN_VALUE);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			CfgToolsTest.CONFIG.getShort(String.class.cast(null), Short.MAX_VALUE);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getShort("short.empty", null));
		Assert.assertTrue(Short.MIN_VALUE == CfgToolsTest.CONFIG.getShort("short.empty", Short.MIN_VALUE));
		Assert.assertTrue(Short.MAX_VALUE == CfgToolsTest.CONFIG.getShort("short.empty", Short.MAX_VALUE));

		Assert.assertNull(CfgToolsTest.CONFIG.getShort("short.min.undef", null));
		Assert.assertTrue(Short.MIN_VALUE == CfgToolsTest.CONFIG.getShort("short.min.undef", Short.MIN_VALUE));
		Assert.assertTrue(Short.MAX_VALUE == CfgToolsTest.CONFIG.getShort("short.min.undef", Short.MAX_VALUE));

		Assert.assertTrue(Short.MIN_VALUE == CfgToolsTest.CONFIG.getShort("short.min", null));
		Assert.assertTrue(Short.MIN_VALUE == CfgToolsTest.CONFIG.getShort("short.min", Short.MIN_VALUE));
		Assert.assertTrue(Short.MIN_VALUE == CfgToolsTest.CONFIG.getShort("short.min", Short.MAX_VALUE));

		Assert.assertTrue(Short.MAX_VALUE == CfgToolsTest.CONFIG.getShort("short.max", null));
		Assert.assertTrue(Short.MAX_VALUE == CfgToolsTest.CONFIG.getShort("short.max", Short.MIN_VALUE));
		Assert.assertTrue(Short.MAX_VALUE == CfgToolsTest.CONFIG.getShort("short.max", Short.MAX_VALUE));

		try {
			CfgToolsTest.CONFIG.getShort("short.over", null);
			Assert.fail("Failed when getting short.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getShort("short.under", null);
			Assert.fail("Failed when getting short.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getShort("boolean.true", null);
			Assert.fail("Failed when getting boolean.true as a short - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getShort(String)}.
	 */
	@Test
	public void testGetShortWithoutDefault() {
		try {
			CfgToolsTest.CONFIG.getShort(String.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getShort("short.empty"));
		Assert.assertTrue(Short.MIN_VALUE == CfgToolsTest.CONFIG.getShort("short.min"));
		Assert.assertTrue(Short.MAX_VALUE == CfgToolsTest.CONFIG.getShort("short.max"));

		try {
			CfgToolsTest.CONFIG.getShort("short.over");
			Assert.fail("Failed when getting short.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getShort("short.under");
			Assert.fail("Failed when getting short.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getShort("boolean.true");
			Assert.fail("Failed when getting boolean.true as a short - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getShort(ConfigurationSetting)} .
	 */
	@Test
	public void testGetShortConfigurationSetting() {
		try {
			CfgToolsTest.CONFIG.getShort(ConfigurationSetting.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getShort(TestSetting.SHORT_EMPTY));
		Assert.assertTrue(Short.MIN_VALUE == CfgToolsTest.CONFIG.getShort(TestSetting.SHORT_MIN));
		Assert.assertTrue(Short.MIN_VALUE == CfgToolsTest.CONFIG.getShort(TestSetting.SHORT_MIN_UNDEF));
		Assert.assertTrue(Short.MAX_VALUE == CfgToolsTest.CONFIG.getShort(TestSetting.SHORT_MAX));
		Assert.assertTrue(Short.MAX_VALUE == CfgToolsTest.CONFIG.getShort(TestSetting.SHORT_MAX_UNDEF));

		Assert.assertNull(CfgToolsTest.CONFIG.getShort(TestSettingString.SHORT_EMPTY));
		Assert.assertTrue(Short.MIN_VALUE == CfgToolsTest.CONFIG.getShort(TestSettingString.SHORT_MIN));
		Assert.assertTrue(Short.MIN_VALUE == CfgToolsTest.CONFIG.getShort(TestSettingString.SHORT_MIN_UNDEF));
		Assert.assertTrue(Short.MAX_VALUE == CfgToolsTest.CONFIG.getShort(TestSettingString.SHORT_MAX));
		Assert.assertTrue(Short.MAX_VALUE == CfgToolsTest.CONFIG.getShort(TestSettingString.SHORT_MAX_UNDEF));

		try {
			CfgToolsTest.CONFIG.getShort(TestSetting.SHORT_OVER);
			Assert.fail("Failed when getting short.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getShort(TestSetting.SHORT_UNDER);
			Assert.fail("Failed when getting short.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getShort(TestSetting.BOOLEAN_TRUE);
			Assert.fail("Failed when getting boolean.true as a short - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		Short b = Short.valueOf((short) 64);
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getShort(TestSetting.SHORT_BYTE));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getShort(TestSetting.SHORT_SHORT));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getShort(TestSetting.SHORT_INTEGER));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getShort(TestSetting.SHORT_LONG));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getShort(TestSetting.SHORT_FLOAT));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getShort(TestSetting.SHORT_DOUBLE));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getInteger(String, Integer)} .
	 */
	@Test
	public void testGetIntegerWithDefault() {
		try {
			CfgToolsTest.CONFIG.getInteger(String.class.cast(null), null);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			CfgToolsTest.CONFIG.getInteger(String.class.cast(null), Integer.MIN_VALUE);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			CfgToolsTest.CONFIG.getInteger(String.class.cast(null), Integer.MAX_VALUE);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getInteger("integer.empty", null));
		Assert.assertTrue(Integer.MIN_VALUE == CfgToolsTest.CONFIG.getInteger("integer.empty", Integer.MIN_VALUE));
		Assert.assertTrue(Integer.MAX_VALUE == CfgToolsTest.CONFIG.getInteger("integer.empty", Integer.MAX_VALUE));

		Assert.assertNull(CfgToolsTest.CONFIG.getInteger("integer.min.undef", null));
		Assert.assertTrue(Integer.MIN_VALUE == CfgToolsTest.CONFIG.getInteger("integer.min.undef", Integer.MIN_VALUE));
		Assert.assertTrue(Integer.MAX_VALUE == CfgToolsTest.CONFIG.getInteger("integer.min.undef", Integer.MAX_VALUE));

		Assert.assertTrue(Integer.MIN_VALUE == CfgToolsTest.CONFIG.getInteger("integer.min", null));
		Assert.assertTrue(Integer.MIN_VALUE == CfgToolsTest.CONFIG.getInteger("integer.min", Integer.MIN_VALUE));
		Assert.assertTrue(Integer.MIN_VALUE == CfgToolsTest.CONFIG.getInteger("integer.min", Integer.MAX_VALUE));

		Assert.assertTrue(Integer.MAX_VALUE == CfgToolsTest.CONFIG.getInteger("integer.max", null));
		Assert.assertTrue(Integer.MAX_VALUE == CfgToolsTest.CONFIG.getInteger("integer.max", Integer.MIN_VALUE));
		Assert.assertTrue(Integer.MAX_VALUE == CfgToolsTest.CONFIG.getInteger("integer.max", Integer.MAX_VALUE));

		try {
			CfgToolsTest.CONFIG.getInteger("integer.over", null);
			Assert.fail("Failed when getting integer.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getInteger("integer.under", null);
			Assert.fail("Failed when getting integer.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getInteger("boolean.true", null);
			Assert.fail("Failed when getting boolean.true as a integer - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getInteger(String)}.
	 */
	@Test
	public void testGetIntegerWithoutDefault() {
		try {
			CfgToolsTest.CONFIG.getInteger(String.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getInteger("integer.empty"));
		Assert.assertTrue(Integer.MIN_VALUE == CfgToolsTest.CONFIG.getInteger("integer.min"));
		Assert.assertTrue(Integer.MAX_VALUE == CfgToolsTest.CONFIG.getInteger("integer.max"));

		try {
			CfgToolsTest.CONFIG.getInteger("integer.over");
			Assert.fail("Failed when getting integer.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getInteger("integer.under");
			Assert.fail("Failed when getting integer.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getInteger("boolean.true");
			Assert.fail("Failed when getting boolean.true as a integer - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getInteger(ConfigurationSetting)} .
	 */
	@Test
	public void testGetIntegerConfigurationSetting() {
		try {
			CfgToolsTest.CONFIG.getInteger(ConfigurationSetting.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getInteger(TestSetting.INTEGER_EMPTY));
		Assert.assertTrue(Integer.MIN_VALUE == CfgToolsTest.CONFIG.getInteger(TestSetting.INTEGER_MIN));
		Assert.assertTrue(Integer.MIN_VALUE == CfgToolsTest.CONFIG.getInteger(TestSetting.INTEGER_MIN_UNDEF));
		Assert.assertTrue(Integer.MAX_VALUE == CfgToolsTest.CONFIG.getInteger(TestSetting.INTEGER_MAX));
		Assert.assertTrue(Integer.MAX_VALUE == CfgToolsTest.CONFIG.getInteger(TestSetting.INTEGER_MAX_UNDEF));

		Assert.assertNull(CfgToolsTest.CONFIG.getInteger(TestSettingString.INTEGER_EMPTY));
		Assert.assertTrue(Integer.MIN_VALUE == CfgToolsTest.CONFIG.getInteger(TestSettingString.INTEGER_MIN));
		Assert.assertTrue(Integer.MIN_VALUE == CfgToolsTest.CONFIG.getInteger(TestSettingString.INTEGER_MIN_UNDEF));
		Assert.assertTrue(Integer.MAX_VALUE == CfgToolsTest.CONFIG.getInteger(TestSettingString.INTEGER_MAX));
		Assert.assertTrue(Integer.MAX_VALUE == CfgToolsTest.CONFIG.getInteger(TestSettingString.INTEGER_MAX_UNDEF));

		try {
			CfgToolsTest.CONFIG.getInteger(TestSetting.INTEGER_OVER);
			Assert.fail("Failed when getting integer.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getInteger(TestSetting.INTEGER_UNDER);
			Assert.fail("Failed when getting integer.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getInteger(TestSetting.BOOLEAN_TRUE);
			Assert.fail("Failed when getting boolean.true as a integer - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		Integer b = Integer.valueOf(64);
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getInteger(TestSetting.INTEGER_BYTE));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getInteger(TestSetting.INTEGER_SHORT));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getInteger(TestSetting.INTEGER_INTEGER));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getInteger(TestSetting.INTEGER_LONG));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getInteger(TestSetting.INTEGER_FLOAT));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getInteger(TestSetting.INTEGER_DOUBLE));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getLong(String, Long)} .
	 */
	@Test
	public void testGetLongWithDefault() {
		try {
			CfgToolsTest.CONFIG.getLong(String.class.cast(null), null);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			CfgToolsTest.CONFIG.getLong(String.class.cast(null), Long.MIN_VALUE);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			CfgToolsTest.CONFIG.getLong(String.class.cast(null), Long.MAX_VALUE);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getLong("long.empty", null));
		Assert.assertTrue(Long.MIN_VALUE == CfgToolsTest.CONFIG.getLong("long.empty", Long.MIN_VALUE));
		Assert.assertTrue(Long.MAX_VALUE == CfgToolsTest.CONFIG.getLong("long.empty", Long.MAX_VALUE));

		Assert.assertNull(CfgToolsTest.CONFIG.getLong("long.min.undef", null));
		Assert.assertTrue(Long.MIN_VALUE == CfgToolsTest.CONFIG.getLong("long.min.undef", Long.MIN_VALUE));
		Assert.assertTrue(Long.MAX_VALUE == CfgToolsTest.CONFIG.getLong("long.min.undef", Long.MAX_VALUE));

		Assert.assertTrue(Long.MIN_VALUE == CfgToolsTest.CONFIG.getLong("long.min", null));
		Assert.assertTrue(Long.MIN_VALUE == CfgToolsTest.CONFIG.getLong("long.min", Long.MIN_VALUE));
		Assert.assertTrue(Long.MIN_VALUE == CfgToolsTest.CONFIG.getLong("long.min", Long.MAX_VALUE));

		Assert.assertTrue(Long.MAX_VALUE == CfgToolsTest.CONFIG.getLong("long.max", null));
		Assert.assertTrue(Long.MAX_VALUE == CfgToolsTest.CONFIG.getLong("long.max", Long.MIN_VALUE));
		Assert.assertTrue(Long.MAX_VALUE == CfgToolsTest.CONFIG.getLong("long.max", Long.MAX_VALUE));

		try {
			CfgToolsTest.CONFIG.getLong("long.over", null);
			Assert.fail("Failed when getting long.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getLong("long.under", null);
			Assert.fail("Failed when getting long.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getLong("boolean.true", null);
			Assert.fail("Failed when getting boolean.true as a long - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getLong(String)}.
	 */
	@Test
	public void testGetLongWithoutDefault() {
		try {
			CfgToolsTest.CONFIG.getLong(String.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getLong("long.empty"));
		Assert.assertTrue(Long.MIN_VALUE == CfgToolsTest.CONFIG.getLong("long.min"));
		Assert.assertTrue(Long.MAX_VALUE == CfgToolsTest.CONFIG.getLong("long.max"));

		try {
			CfgToolsTest.CONFIG.getLong("long.over");
			Assert.fail("Failed when getting long.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getLong("long.under");
			Assert.fail("Failed when getting long.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getLong("boolean.true");
			Assert.fail("Failed when getting boolean.true as a long - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getLong(ConfigurationSetting)} .
	 */
	@Test
	public void testGetLongConfigurationSetting() {
		try {
			CfgToolsTest.CONFIG.getLong(ConfigurationSetting.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getLong(TestSetting.LONG_EMPTY));
		Assert.assertTrue(Long.MIN_VALUE == CfgToolsTest.CONFIG.getLong(TestSetting.LONG_MIN));
		Assert.assertTrue(Long.MIN_VALUE == CfgToolsTest.CONFIG.getLong(TestSetting.LONG_MIN_UNDEF));
		Assert.assertTrue(Long.MAX_VALUE == CfgToolsTest.CONFIG.getLong(TestSetting.LONG_MAX));
		Assert.assertTrue(Long.MAX_VALUE == CfgToolsTest.CONFIG.getLong(TestSetting.LONG_MAX_UNDEF));

		Assert.assertNull(CfgToolsTest.CONFIG.getLong(TestSettingString.LONG_EMPTY));
		Assert.assertTrue(Long.MIN_VALUE == CfgToolsTest.CONFIG.getLong(TestSettingString.LONG_MIN));
		Assert.assertTrue(Long.MIN_VALUE == CfgToolsTest.CONFIG.getLong(TestSettingString.LONG_MIN_UNDEF));
		Assert.assertTrue(Long.MAX_VALUE == CfgToolsTest.CONFIG.getLong(TestSettingString.LONG_MAX));
		Assert.assertTrue(Long.MAX_VALUE == CfgToolsTest.CONFIG.getLong(TestSettingString.LONG_MAX_UNDEF));

		try {
			CfgToolsTest.CONFIG.getLong(TestSetting.LONG_OVER);
			Assert.fail("Failed when getting long.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getLong(TestSetting.LONG_UNDER);
			Assert.fail("Failed when getting long.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgToolsTest.CONFIG.getLong(TestSetting.BOOLEAN_TRUE);
			Assert.fail("Failed when getting boolean.true as a long - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		Long b = Long.valueOf(64L);
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getLong(TestSetting.LONG_BYTE));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getLong(TestSetting.LONG_SHORT));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getLong(TestSetting.LONG_INTEGER));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getLong(TestSetting.LONG_LONG));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getLong(TestSetting.LONG_FLOAT));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getLong(TestSetting.LONG_DOUBLE));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getFloat(String, Number)} .
	 */
	@Test
	public void testGetFloatWithDefault() {
		try {
			CfgToolsTest.CONFIG.getFloat(String.class.cast(null), null);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			CfgToolsTest.CONFIG.getFloat(String.class.cast(null), Float.MIN_VALUE);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			CfgToolsTest.CONFIG.getFloat(String.class.cast(null), Float.MAX_VALUE);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getFloat("float.empty", null));
		Assert.assertTrue(Float.MIN_VALUE == CfgToolsTest.CONFIG.getFloat("float.empty", Float.MIN_VALUE));
		Assert.assertTrue(Float.MAX_VALUE == CfgToolsTest.CONFIG.getFloat("float.empty", Float.MAX_VALUE));

		Assert.assertNull(CfgToolsTest.CONFIG.getFloat("float.min.undef", null));
		CfgToolsTest.assertEquals(-Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat("float.min.undef", -Float.MAX_VALUE));
		CfgToolsTest.assertEquals(Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat("float.min.undef", Float.MAX_VALUE));

		CfgToolsTest.assertEquals(-Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat("float.min", null));
		CfgToolsTest.assertEquals(-Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat("float.min", -Float.MAX_VALUE));
		CfgToolsTest.assertEquals(-Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat("float.min", Float.MAX_VALUE));

		CfgToolsTest.assertEquals(Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat("float.max", null));
		CfgToolsTest.assertEquals(Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat("float.max", -Float.MAX_VALUE));
		CfgToolsTest.assertEquals(Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat("float.max", Float.MAX_VALUE));

		Assert.assertTrue(Float.isInfinite(CfgToolsTest.CONFIG.getFloat("float.over", null)));
		Assert.assertTrue(Float.isInfinite(CfgToolsTest.CONFIG.getFloat("float.under", null)));
		Assert.assertTrue(0 < Float.compare(0, CfgToolsTest.CONFIG.getFloat("float.under", null)));

		try {
			CfgToolsTest.CONFIG.getFloat("boolean.true", null);
			Assert.fail("Failed when getting boolean.true as a float - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getFloat(String)}.
	 */
	@Test
	public void testGetFloatWithoutDefault() {
		try {
			CfgToolsTest.CONFIG.getFloat(String.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getFloat("float.empty"));
		CfgToolsTest.assertEquals(-Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat("float.min"));
		CfgToolsTest.assertEquals(Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat("float.max"));

		Assert.assertTrue(Float.isInfinite(CfgToolsTest.CONFIG.getFloat("float.over")));
		Assert.assertTrue(Float.isInfinite(CfgToolsTest.CONFIG.getFloat("float.under")));
		Assert.assertTrue(0 < Float.compare(0, CfgToolsTest.CONFIG.getFloat("float.under")));
		try {
			CfgToolsTest.CONFIG.getFloat("boolean.true");
			Assert.fail("Failed when getting boolean.true as a float - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getFloat(ConfigurationSetting)} .
	 */
	@Test
	public void testGetFloatConfigurationSetting() {
		try {
			CfgToolsTest.CONFIG.getFloat(ConfigurationSetting.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getFloat(TestSetting.FLOAT_EMPTY));
		CfgToolsTest.assertEquals(-Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat(TestSetting.FLOAT_MIN));
		CfgToolsTest.assertEquals(-Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat(TestSetting.FLOAT_MIN_UNDEF));
		CfgToolsTest.assertEquals(Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat(TestSetting.FLOAT_MAX));
		CfgToolsTest.assertEquals(Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat(TestSetting.FLOAT_MAX_UNDEF));

		Assert.assertTrue(Float.isInfinite(CfgToolsTest.CONFIG.getFloat(TestSetting.FLOAT_OVER)));
		Assert.assertTrue(Float.isInfinite(CfgToolsTest.CONFIG.getFloat(TestSetting.FLOAT_UNDER)));
		Assert.assertTrue(0 < Float.compare(0, CfgToolsTest.CONFIG.getFloat(TestSetting.FLOAT_UNDER)));
		try {
			CfgToolsTest.CONFIG.getFloat(TestSetting.BOOLEAN_TRUE);
			Assert.fail("Failed when getting boolean.true as a float - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}

		Assert.assertNull(CfgToolsTest.CONFIG.getFloat(TestSettingString.FLOAT_EMPTY));
		CfgToolsTest.assertEquals(-Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat(TestSettingString.FLOAT_MIN));
		CfgToolsTest.assertEquals(-Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat(TestSettingString.FLOAT_MIN_UNDEF));
		CfgToolsTest.assertEquals(Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat(TestSettingString.FLOAT_MAX));
		CfgToolsTest.assertEquals(Float.MAX_VALUE, CfgToolsTest.CONFIG.getFloat(TestSettingString.FLOAT_MAX_UNDEF));

		Assert.assertTrue(Float.isInfinite(CfgToolsTest.CONFIG.getFloat(TestSettingString.FLOAT_OVER)));
		Assert.assertTrue(Float.isInfinite(CfgToolsTest.CONFIG.getFloat(TestSettingString.FLOAT_UNDER)));
		Assert.assertTrue(0 < Float.compare(0, CfgToolsTest.CONFIG.getFloat(TestSettingString.FLOAT_UNDER)));
		try {
			CfgToolsTest.CONFIG.getFloat(TestSettingString.BOOLEAN_TRUE);
			Assert.fail("Failed when getting boolean.true as a float - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		Float b = Float.valueOf(64.35f);
		Float t = Double.valueOf(Math.floor(b.floatValue())).floatValue();
		Assert.assertEquals(t, CfgToolsTest.CONFIG.getFloat(TestSetting.FLOAT_BYTE));
		Assert.assertEquals(t, CfgToolsTest.CONFIG.getFloat(TestSetting.FLOAT_SHORT));
		Assert.assertEquals(t, CfgToolsTest.CONFIG.getFloat(TestSetting.FLOAT_INTEGER));
		Assert.assertEquals(t, CfgToolsTest.CONFIG.getFloat(TestSetting.FLOAT_LONG));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getFloat(TestSetting.FLOAT_FLOAT));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getFloat(TestSetting.FLOAT_DOUBLE));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getDouble(String, Number)} .
	 */
	@Test
	public void testGetDoubleWithDefault() {
		try {
			CfgToolsTest.CONFIG.getDouble(String.class.cast(null), null);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			CfgToolsTest.CONFIG.getDouble(String.class.cast(null), Double.MIN_VALUE);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			CfgToolsTest.CONFIG.getDouble(String.class.cast(null), Double.MAX_VALUE);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getDouble("double.empty", null));
		Assert.assertTrue(Double.MIN_VALUE == CfgToolsTest.CONFIG.getDouble("double.empty", Double.MIN_VALUE));
		Assert.assertTrue(Double.MAX_VALUE == CfgToolsTest.CONFIG.getDouble("double.empty", Double.MAX_VALUE));

		Assert.assertNull(CfgToolsTest.CONFIG.getDouble("double.min.undef", null));
		CfgToolsTest.assertEquals(-Double.MAX_VALUE,
			CfgToolsTest.CONFIG.getDouble("double.min.undef", -Double.MAX_VALUE));
		CfgToolsTest
			.assertEquals(Double.MAX_VALUE, CfgToolsTest.CONFIG.getDouble("double.min.undef", Double.MAX_VALUE));

		CfgToolsTest.assertEquals(-Double.MAX_VALUE, CfgToolsTest.CONFIG.getDouble("double.min", null));
		CfgToolsTest.assertEquals(-Double.MAX_VALUE, CfgToolsTest.CONFIG.getDouble("double.min", -Double.MAX_VALUE));
		CfgToolsTest.assertEquals(-Double.MAX_VALUE, CfgToolsTest.CONFIG.getDouble("double.min", Double.MAX_VALUE));

		CfgToolsTest.assertEquals(Double.MAX_VALUE, CfgToolsTest.CONFIG.getDouble("double.max", null));
		CfgToolsTest.assertEquals(Double.MAX_VALUE, CfgToolsTest.CONFIG.getDouble("double.max", -Double.MAX_VALUE));
		CfgToolsTest.assertEquals(Double.MAX_VALUE, CfgToolsTest.CONFIG.getDouble("double.max", Double.MAX_VALUE));

		Assert.assertTrue(Double.isInfinite(CfgToolsTest.CONFIG.getDouble("double.over", null)));
		Assert.assertTrue(Double.isInfinite(CfgToolsTest.CONFIG.getDouble("double.under", null)));
		Assert.assertTrue(0 < Double.compare(0, CfgToolsTest.CONFIG.getDouble("double.under", null)));

		try {
			CfgToolsTest.CONFIG.getDouble("boolean.true", null);
			Assert.fail("Failed when getting boolean.true as a double - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getDouble(String)}.
	 */
	@Test
	public void testGetDoubleWithoutDefault() {
		try {
			CfgToolsTest.CONFIG.getDouble(String.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getDouble("double.empty"));
		CfgToolsTest.assertEquals(-Double.MAX_VALUE, CfgToolsTest.CONFIG.getDouble("double.min"));
		CfgToolsTest.assertEquals(Double.MAX_VALUE, CfgToolsTest.CONFIG.getDouble("double.max"));

		Assert.assertTrue(Double.isInfinite(CfgToolsTest.CONFIG.getDouble("double.over")));
		Assert.assertTrue(Double.isInfinite(CfgToolsTest.CONFIG.getDouble("double.under")));
		Assert.assertTrue(0 < Double.compare(0, CfgToolsTest.CONFIG.getDouble("double.under")));
		try {
			CfgToolsTest.CONFIG.getDouble("boolean.true");
			Assert.fail("Failed when getting boolean.true as a double - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getDouble(ConfigurationSetting)} .
	 */
	@Test
	public void testGetDoubleConfigurationSetting() {
		try {
			CfgToolsTest.CONFIG.getDouble(ConfigurationSetting.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getDouble(TestSetting.DOUBLE_EMPTY));
		CfgToolsTest.assertEquals(-Double.MAX_VALUE, CfgToolsTest.CONFIG.getDouble(TestSetting.DOUBLE_MIN));
		CfgToolsTest.assertEquals(-Double.MAX_VALUE, CfgToolsTest.CONFIG.getDouble(TestSetting.DOUBLE_MIN_UNDEF));
		CfgToolsTest.assertEquals(Double.MAX_VALUE, CfgToolsTest.CONFIG.getDouble(TestSetting.DOUBLE_MAX));
		CfgToolsTest.assertEquals(Double.MAX_VALUE, CfgToolsTest.CONFIG.getDouble(TestSetting.DOUBLE_MAX_UNDEF));

		Assert.assertTrue(Double.isInfinite(CfgToolsTest.CONFIG.getDouble(TestSetting.DOUBLE_OVER)));
		Assert.assertTrue(Double.isInfinite(CfgToolsTest.CONFIG.getDouble(TestSetting.DOUBLE_UNDER)));
		Assert.assertTrue(0 < Double.compare(0, CfgToolsTest.CONFIG.getDouble(TestSetting.DOUBLE_UNDER)));
		try {
			CfgToolsTest.CONFIG.getDouble(TestSetting.BOOLEAN_TRUE);
			Assert.fail("Failed when getting boolean.true as a double - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}

		Assert.assertNull(CfgToolsTest.CONFIG.getDouble(TestSettingString.DOUBLE_EMPTY));
		CfgToolsTest.assertEquals(-Double.MAX_VALUE, CfgToolsTest.CONFIG.getDouble(TestSettingString.DOUBLE_MIN));
		CfgToolsTest.assertEquals(-Double.MAX_VALUE, CfgToolsTest.CONFIG.getDouble(TestSettingString.DOUBLE_MIN_UNDEF));
		CfgToolsTest.assertEquals(Double.MAX_VALUE, CfgToolsTest.CONFIG.getDouble(TestSettingString.DOUBLE_MAX));
		CfgToolsTest.assertEquals(Double.MAX_VALUE, CfgToolsTest.CONFIG.getDouble(TestSettingString.DOUBLE_MAX_UNDEF));

		Assert.assertTrue(Double.isInfinite(CfgToolsTest.CONFIG.getDouble(TestSettingString.DOUBLE_OVER)));
		Assert.assertTrue(Double.isInfinite(CfgToolsTest.CONFIG.getDouble(TestSettingString.DOUBLE_UNDER)));
		Assert.assertTrue(0 < Double.compare(0, CfgToolsTest.CONFIG.getDouble(TestSettingString.DOUBLE_UNDER)));
		try {
			CfgToolsTest.CONFIG.getDouble(TestSettingString.BOOLEAN_TRUE);
			Assert.fail("Failed when getting boolean.true as a double - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		Double b = Double.valueOf(64.35);
		Double t = Math.floor(b);
		Assert.assertEquals(t, CfgToolsTest.CONFIG.getDouble(TestSetting.DOUBLE_BYTE));
		Assert.assertEquals(t, CfgToolsTest.CONFIG.getDouble(TestSetting.DOUBLE_SHORT));
		Assert.assertEquals(t, CfgToolsTest.CONFIG.getDouble(TestSetting.DOUBLE_INTEGER));
		Assert.assertEquals(t, CfgToolsTest.CONFIG.getDouble(TestSetting.DOUBLE_LONG));
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getDouble(TestSetting.DOUBLE_FLOAT), 0.00001);
		Assert.assertEquals(b, CfgToolsTest.CONFIG.getDouble(TestSetting.DOUBLE_DOUBLE));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getString(String, String)} .
	 */
	@Test
	public void testGetStringWithDefault() {
		final String uuid = UUID.randomUUID().toString();
		try {
			CfgToolsTest.CONFIG.getString(String.class.cast(null), null);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			CfgToolsTest.CONFIG.getString(String.class.cast(null), uuid);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getString("string.empty", null));
		Assert.assertEquals(uuid, CfgToolsTest.CONFIG.getString("string.empty", uuid));

		Assert.assertNotNull(CfgToolsTest.CONFIG.getString("string.sample", null));
		Assert.assertNotNull(CfgToolsTest.CONFIG.getString("string.sample", uuid));
		Assert.assertFalse(uuid.equals(CfgToolsTest.CONFIG.getString("string.sample", uuid)));

		Assert.assertEquals("Q8'6]mn_{ '3k)X RM3w`;TrB,(`gW9zPG3%k3!&_92$&+4:r-lH @v)z_~l'cHsnj{$ Qrq4}7&#h;_",
			CfgToolsTest.CONFIG.getString("string.sample", null));
		Assert.assertEquals("Q8'6]mn_{ '3k)X RM3w`;TrB,(`gW9zPG3%k3!&_92$&+4:r-lH @v)z_~l'cHsnj{$ Qrq4}7&#h;_",
			CfgToolsTest.CONFIG.getString("string.sample", uuid));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getString(String)}.
	 */
	@Test
	public void testGetStringWithoutDefault() {
		try {
			CfgToolsTest.CONFIG.getString(String.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getString("string.empty"));
		Assert.assertNotNull(CfgToolsTest.CONFIG.getString("string.sample"));
		Assert.assertEquals("Q8'6]mn_{ '3k)X RM3w`;TrB,(`gW9zPG3%k3!&_92$&+4:r-lH @v)z_~l'cHsnj{$ Qrq4}7&#h;_",
			CfgToolsTest.CONFIG.getString("string.sample"));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getString(ConfigurationSetting)} .
	 */
	@Test
	public void testGetStringConfigurationSetting() {
		try {
			CfgToolsTest.CONFIG.getString(ConfigurationSetting.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		Assert.assertNull(CfgToolsTest.CONFIG.getString(TestSetting.STRING_EMPTY));
		Assert.assertNotNull(CfgToolsTest.CONFIG.getString(TestSetting.STRING_SAMPLE));
		Assert.assertEquals("Q8'6]mn_{ '3k)X RM3w`;TrB,(`gW9zPG3%k3!&_92$&+4:r-lH @v)z_~l'cHsnj{$ Qrq4}7&#h;_",
			CfgToolsTest.CONFIG.getString(TestSetting.STRING_SAMPLE));
		Assert.assertEquals("'R-jXvzq4H#wF/6 s|?XN&*c7n;zf'!N~};PM/NL8$#<8fn}N7fkKS!n|c 4GN?8;B&V;_qDL&?) 5+_",
			CfgToolsTest.CONFIG.getString(TestSetting.STRING_UNDEF));

		Assert.assertNull(CfgToolsTest.CONFIG.getString(TestSettingString.STRING_EMPTY));
		Assert.assertNotNull(CfgToolsTest.CONFIG.getString(TestSettingString.STRING_SAMPLE));
		Assert.assertEquals("Q8'6]mn_{ '3k)X RM3w`;TrB,(`gW9zPG3%k3!&_92$&+4:r-lH @v)z_~l'cHsnj{$ Qrq4}7&#h;_",
			CfgToolsTest.CONFIG.getString(TestSettingString.STRING_SAMPLE));
		Assert.assertEquals("kn>&V~s*.`_`s5?ngd7;bH :p` 4pmb]: )$~n;b?5?%)2QL3wX!F!M):LC)?(?R:9Kg2g[@589HK$t[",
			CfgToolsTest.CONFIG.getString(TestSettingString.STRING_UNDEF));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getBinary(String, byte[])}.
	 */
	@Test
	public void testGetBinaryWithDefault() throws Throwable {
		byte[] sample = new byte[256];
		CfgToolsTest.RANDOM.nextBytes(sample);

		try {
			CfgToolsTest.CONFIG.getBinary(String.class.cast(null), null);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			CfgToolsTest.CONFIG.getBinary(String.class.cast(null), sample);
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}

		Assert.assertNull(CfgToolsTest.CONFIG.getBinary("binary.empty", null));
		Assert.assertArrayEquals(sample, CfgToolsTest.CONFIG.getBinary("binary.empty", sample));

		Assert.assertNotNull(CfgToolsTest.CONFIG.getBinary("binary.sample", null));
		Assert.assertNotNull(CfgToolsTest.CONFIG.getBinary("binary.sample", sample));
		Assert.assertFalse(Arrays.equals(sample, CfgToolsTest.CONFIG.getBinary("binary.sample", null)));
		Assert.assertFalse(Arrays.equals(sample, CfgToolsTest.CONFIG.getBinary("binary.sample", sample)));

		byte[] realData = Base64
			.decodeBase64("c3aqSlM2IN/HO359hN2nybJ3GRTW/rNE8BckG4fmg0HJHtkIbLdmcXE+9/NI6eieQvVAZyAwPBLucINyeh+xe63eCxfCm9FSXCXytBGDZQm7yCCYxlBz1qRQCTlSzCkZCsotR9ZZ99TUYcVE8UBcEjTXpEXGTxFk3OFbW0jV+gpN5Hp4bSqJsUDsUds+END+nXGFMqQ7CTb1h04Qk6kW/7HstL+JidGLG9d2Qa+x5CfcXs3DMujc2mH461a/8lLLJ/i0NLofyRklJSr1ZWVlqPaq/sk9caCnTPKaDMPDYwTOGnkQVIdnOVjyACAKMs0paAIuo+n7wrG4wRD8f75/GzBPceQwGvVqkRANJtfzGK5zfgYVdNnQtN7U8OnJEl+0C9PMOB272SAx1epwSeLqIJEe9cQkwZyozMvO/md1gQiOsrIT1KcP5F0O4OGneSta9PakvJjsj3Y61N8eksJEFmmKtk69sDsu4ewZmCpDooxtNYRnCj0YNjTkXeL+77joivtz1K147ck7Lkjla1cO1BACCzArsQYVTdRxD9XT/sKHDsv+ahR0GzjuTxH8xUb2zhCT9mYFELcrOiJzeuY2kiYzFBbFohBRqZSdlTjbwHeFKjnpuDzY8imOUJFRw0Dfk3lYPd35A6bHvTEJpyYOufwQWg/5mYwk60YWKbaXcZr5wVFcX2Kxp7C5RrpxGINusROjHvovNUdfFxpm2LVP/NBYLfFnE0QlYDstp1ySipc8fYdRWWNqLxVnCX3YQKhof08934BNknS6WMyPryiIbtm4IblK/kBkCP5uENIvT2EHpGPANKLlwZ/DnA1G/k+AUee4kw1UhAbD+ZOR9/hqWlDFOtVc/69nKu4kp+pY25qtVcPNdDyh2s/yXLJsTkpEGBVFGqOEBY9fajP+knDXsvP8RBt97amfln3Fs/nPRK50KeI9wxzp39SCKJ6k2EIcohFvp/IMHts+QhXmbfjh+0sw5S4EFdfRtfBeBUEbFPtb5hceUK+iU5U4sRCFIi5I81TQLycy3mLkQ9xXqcF6ZUZ/uub97U77sSc/O9OgaWvoYrN69+i1nXYvCUobPT9KnebHxe1qC878Kv0RBi+zDMKO13bVHAmw0BYXXQy/j5H4IStFbW5wROyOdDK659X+2EEk3hDS0XdW8OyEBCntlT5jTcevWDRtSPG2vZSYYEAeYrXOxP3mtg/pfwBIJc+XitgzIvGKdiFEKPwZC4/21tWrT2Tb5oDCjuH6/ssDWfUHyGbco39lk3M0kC3PYLydTRtw5lWyK+Q+vlLKaPfLKXlo0ceap5q21fwZbuDCkioeIG8LPC3cxoOBYVGRhA7Rm+Bw9QTvmgKQwdS87eHkAg==");
		Assert.assertArrayEquals(realData, CfgToolsTest.CONFIG.getBinary("binary.sample", null));
		Assert.assertArrayEquals(realData, CfgToolsTest.CONFIG.getBinary("binary.sample", sample));

		try {
			CfgToolsTest.CONFIG.getBinary("string.sample", null);
			Assert.fail("string.sample is known to not be a valid Base64 string, and yet was correctly decoded");
		} catch (DecoderException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#getBinary(ConfigurationSetting)}.
	 */
	@Test
	public void testGetBinaryWithoutDefault() throws Throwable {
		byte[] realData = Base64
			.decodeBase64("c3aqSlM2IN/HO359hN2nybJ3GRTW/rNE8BckG4fmg0HJHtkIbLdmcXE+9/NI6eieQvVAZyAwPBLucINyeh+xe63eCxfCm9FSXCXytBGDZQm7yCCYxlBz1qRQCTlSzCkZCsotR9ZZ99TUYcVE8UBcEjTXpEXGTxFk3OFbW0jV+gpN5Hp4bSqJsUDsUds+END+nXGFMqQ7CTb1h04Qk6kW/7HstL+JidGLG9d2Qa+x5CfcXs3DMujc2mH461a/8lLLJ/i0NLofyRklJSr1ZWVlqPaq/sk9caCnTPKaDMPDYwTOGnkQVIdnOVjyACAKMs0paAIuo+n7wrG4wRD8f75/GzBPceQwGvVqkRANJtfzGK5zfgYVdNnQtN7U8OnJEl+0C9PMOB272SAx1epwSeLqIJEe9cQkwZyozMvO/md1gQiOsrIT1KcP5F0O4OGneSta9PakvJjsj3Y61N8eksJEFmmKtk69sDsu4ewZmCpDooxtNYRnCj0YNjTkXeL+77joivtz1K147ck7Lkjla1cO1BACCzArsQYVTdRxD9XT/sKHDsv+ahR0GzjuTxH8xUb2zhCT9mYFELcrOiJzeuY2kiYzFBbFohBRqZSdlTjbwHeFKjnpuDzY8imOUJFRw0Dfk3lYPd35A6bHvTEJpyYOufwQWg/5mYwk60YWKbaXcZr5wVFcX2Kxp7C5RrpxGINusROjHvovNUdfFxpm2LVP/NBYLfFnE0QlYDstp1ySipc8fYdRWWNqLxVnCX3YQKhof08934BNknS6WMyPryiIbtm4IblK/kBkCP5uENIvT2EHpGPANKLlwZ/DnA1G/k+AUee4kw1UhAbD+ZOR9/hqWlDFOtVc/69nKu4kp+pY25qtVcPNdDyh2s/yXLJsTkpEGBVFGqOEBY9fajP+knDXsvP8RBt97amfln3Fs/nPRK50KeI9wxzp39SCKJ6k2EIcohFvp/IMHts+QhXmbfjh+0sw5S4EFdfRtfBeBUEbFPtb5hceUK+iU5U4sRCFIi5I81TQLycy3mLkQ9xXqcF6ZUZ/uub97U77sSc/O9OgaWvoYrN69+i1nXYvCUobPT9KnebHxe1qC878Kv0RBi+zDMKO13bVHAmw0BYXXQy/j5H4IStFbW5wROyOdDK659X+2EEk3hDS0XdW8OyEBCntlT5jTcevWDRtSPG2vZSYYEAeYrXOxP3mtg/pfwBIJc+XitgzIvGKdiFEKPwZC4/21tWrT2Tb5oDCjuH6/ssDWfUHyGbco39lk3M0kC3PYLydTRtw5lWyK+Q+vlLKaPfLKXlo0ceap5q21fwZbuDCkioeIG8LPC3cxoOBYVGRhA7Rm+Bw9QTvmgKQwdS87eHkAg==");
		try {
			CfgToolsTest.CONFIG.getBinary(String.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}

		Assert.assertNull(CfgToolsTest.CONFIG.getBinary("binary.empty"));
		Assert.assertNotNull(CfgToolsTest.CONFIG.getBinary("binary.sample"));
		Assert.assertArrayEquals(realData, CfgToolsTest.CONFIG.getBinary("binary.sample"));

		try {
			CfgToolsTest.CONFIG.getBinary("string.sample");
			Assert.fail("string.sample is known to not be a valid Base64 string, and yet was correctly decoded");
		} catch (DecoderException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#getBinary(com.armedia.commons.utilities.ConfigurationSetting)} .
	 */
	@Test
	public void testGetBinaryConfigurationSetting() throws Throwable {
		byte[] realData = Base64
			.decodeBase64("c3aqSlM2IN/HO359hN2nybJ3GRTW/rNE8BckG4fmg0HJHtkIbLdmcXE+9/NI6eieQvVAZyAwPBLucINyeh+xe63eCxfCm9FSXCXytBGDZQm7yCCYxlBz1qRQCTlSzCkZCsotR9ZZ99TUYcVE8UBcEjTXpEXGTxFk3OFbW0jV+gpN5Hp4bSqJsUDsUds+END+nXGFMqQ7CTb1h04Qk6kW/7HstL+JidGLG9d2Qa+x5CfcXs3DMujc2mH461a/8lLLJ/i0NLofyRklJSr1ZWVlqPaq/sk9caCnTPKaDMPDYwTOGnkQVIdnOVjyACAKMs0paAIuo+n7wrG4wRD8f75/GzBPceQwGvVqkRANJtfzGK5zfgYVdNnQtN7U8OnJEl+0C9PMOB272SAx1epwSeLqIJEe9cQkwZyozMvO/md1gQiOsrIT1KcP5F0O4OGneSta9PakvJjsj3Y61N8eksJEFmmKtk69sDsu4ewZmCpDooxtNYRnCj0YNjTkXeL+77joivtz1K147ck7Lkjla1cO1BACCzArsQYVTdRxD9XT/sKHDsv+ahR0GzjuTxH8xUb2zhCT9mYFELcrOiJzeuY2kiYzFBbFohBRqZSdlTjbwHeFKjnpuDzY8imOUJFRw0Dfk3lYPd35A6bHvTEJpyYOufwQWg/5mYwk60YWKbaXcZr5wVFcX2Kxp7C5RrpxGINusROjHvovNUdfFxpm2LVP/NBYLfFnE0QlYDstp1ySipc8fYdRWWNqLxVnCX3YQKhof08934BNknS6WMyPryiIbtm4IblK/kBkCP5uENIvT2EHpGPANKLlwZ/DnA1G/k+AUee4kw1UhAbD+ZOR9/hqWlDFOtVc/69nKu4kp+pY25qtVcPNdDyh2s/yXLJsTkpEGBVFGqOEBY9fajP+knDXsvP8RBt97amfln3Fs/nPRK50KeI9wxzp39SCKJ6k2EIcohFvp/IMHts+QhXmbfjh+0sw5S4EFdfRtfBeBUEbFPtb5hceUK+iU5U4sRCFIi5I81TQLycy3mLkQ9xXqcF6ZUZ/uub97U77sSc/O9OgaWvoYrN69+i1nXYvCUobPT9KnebHxe1qC878Kv0RBi+zDMKO13bVHAmw0BYXXQy/j5H4IStFbW5wROyOdDK659X+2EEk3hDS0XdW8OyEBCntlT5jTcevWDRtSPG2vZSYYEAeYrXOxP3mtg/pfwBIJc+XitgzIvGKdiFEKPwZC4/21tWrT2Tb5oDCjuH6/ssDWfUHyGbco39lk3M0kC3PYLydTRtw5lWyK+Q+vlLKaPfLKXlo0ceap5q21fwZbuDCkioeIG8LPC3cxoOBYVGRhA7Rm+Bw9QTvmgKQwdS87eHkAg==");

		byte[] dataTestSetting = null;
		try {
			CfgToolsTest.CONFIG.getBinary(ConfigurationSetting.class.cast(null));
			Assert.fail("Failed to raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}

		dataTestSetting = Base64
			.decodeBase64("xy0dZm9C5zT/6ZCdq7ttZ2MHbHLpnqGIlWosnTmHD1v/k30tVc++fhtDdyY31k5tgPpfnDVzNgE4Au8eMvmjlr16e3hrFSuWhc7Bjb05vwodNHoIK0j9LHlO0vcp/WeptHpJlop3ThSYRnr3d/Hx1LkOE3gDpcyUmgeVTr97xkjH3tJgQhCEVzyPd593DHnBu8v0yC0QzJmI+H51Px2sLPlVsapDkztv1ORrwva2XFLhWHyI7C+294/DLHimdZD6qrpsktVXpolGkF09SZsnJvziuRgm/Zdq0gfVSVbjJy4ra8FN3iSegEWMGdg/GweyLyvOVqHFBjMTfg1IxltCzqF5xmQIDvox9CyuzAvYcsQk2/LCn6sciz93Wk5/xheiZIFZrtjx+A9aLfPqmMIDDsPJ4yXD0kgRtBBMzyEMkrTTswLOABmKI3JvMwtxH33vMeiqPTtvhts4gbo1xOUJMb6q0KYq6cs5f/vT5V2r8jHidJ+vc5ROYxi8Wb16js/lLW2AQzOO8Qi7KJZYGpFHB0oCXIuyUO8qvAPYDjX4RbT3Z5wX8bssVUglu/u4hr5i1eV6uR6AfnhueoXl8+duCpegQyNgIvuhUM2sfQYo/l7fIHu18kxKVPj2Tit1ruu1gUNKSNQUUliG0gdWi8xMcjaEIEwOPAihRgU5qDLS9kOd/UTNfdlS+TjtcS8zeT1QwedPD5M40hqmPtdnwv6ZUglcZSF2qlcu7WyimwmGfoyJXmXpK0c64yTL/2lKGsXQo5F12UIeZV82bRWY8FtWBZehM+5w50JLsbjb6kUh6T+Ly3xf8ge0OQdbw/Dp9EBxnhtuYUXeOpOwE0+QSFH15V1847gAGZTtRu2UpXG3vwF4Nn9PlY9HhUHg+redI3YMTs5Tgq/nauMrGmbkm33vDOVuGQ+7Amv0x5hX+jjNvR6oNYe5xEwr3mtRA5Tv9o0jcpkySDIygGbrjJRSIyH78EmgtE30WWIx2YT7qB61pD5nk1/UUHcyO6Z+3IB1Oc+n4ArdvXuGvDFpTT7eg1eKKZnPYg3NbOpAZRJYskZJaGAzCKf6DXDbkopASPZAm7yF/oNdYb3yw02dg4bGWrt6uwlnIYbEBcFVjfEqhEERRO92rhRwaw8fOjnWO3Ho33wv3dwkWdJ3gvAvK+xWHe0TSF2WkhlZZx//zzkDG+ZcXf1y7iPP+GeqZLUypLIrxB73sx6S7Evgj9KKHORDPSYr+S3zj/mgpDkCuhdldPr6rdz4F21DpwTcHOrA8he8r6gKYtUVqCBB2qaSZfUZGB+eo0p3ORyItURgLvYs8GnC9izVXv0tlDDLki0fkpFx7wfOHX4WbCcdIN+31RU6sRaj/Q==");
		Assert.assertNull(CfgToolsTest.CONFIG.getBinary(TestSetting.BINARY_EMPTY));
		Assert.assertNotNull(CfgToolsTest.CONFIG.getBinary(TestSetting.BINARY_SAMPLE));
		Assert.assertArrayEquals(realData, CfgToolsTest.CONFIG.getBinary(TestSetting.BINARY_SAMPLE));
		Assert.assertArrayEquals(dataTestSetting, CfgToolsTest.CONFIG.getBinary(TestSetting.BINARY_UNDEF));

		try {
			CfgToolsTest.CONFIG.getBinary(TestSetting.STRING_SAMPLE);
			Assert.fail("string.sample is known to not be a valid Base64 string, and yet was correctly decoded");
		} catch (DecoderException e) {
			// All is well - this is expected
		}

		dataTestSetting = Base64
			.decodeBase64("mgFLqv8Ljr6mAcqI8RcKfj9v0aMROcZP7MpXq2A6ZIPJmCLHmFP5niuEPu3swJikfHIhPr7e+czQVKsfc63KIsPnxz44BKRPTtoOvFE91mW+SoW5ep2U/IC+ytrRu88qyum+6BHqsnqMfeDf7bkbSgpHEuob9RFT9Ic/OLke6rpjxp/Kwq6+cbfK+nN0jVzV1WmuNJh3/S+H3f2L2uyteCXMHqxe710/n9s2Bu5+KuK607fNPt3jjFU2X1fnBSpofZK4izRMgTmp+SPMNt82+iMwRhuWodnByRrvcll5+qcj95Eeqkgtk0zA+BE1sCgBPHhO5jQn2JuLay+O4VOstLRnI6ZocXs+sS1T3GmSI6Yuvd7cvQQ9NFOA+eZcuphIdEVKB8ddAC2v3bg/AYq5pgYFQ+5x4N5zpivfzuPZcK84o7C9Uc/tTPXozTsWegLEsD14kL6M8Qu1OxnuDciNTEqI7TdMhb01fLny8ICdwv7gxH3IVmE64UqJh3Y7vq3+Xvj1As27/6ox3lQvM74CwOdaxlJSA1bGTtY3rTeb7dHN6BVwqM/vWjQ0q2T++MU6UU3zgRhrl4QCOYDFvjfcL22h5JpVem+hZgGthGi90DpVxraJFg2LPjDQQoWGKW84PU+ks4fX0MDjzpZgW8tgPD/6lh4+TTer2yCdON0ccgEvaCDQse4KGu7yQnQhySsepufuZ3dG5plFdI7dJYs8O1VIb2WOfPe01Kf6Kk06ylaHp71ZvxdTsNJesbXYzo0QETWNS+ECFY4Di/AnXRa56KQosNvyx7seaBsCIGdkLMLMUoB1HVTFe1POAiKB5BDQGyaYW9+WGB8MYG0mnpW3oZD2E5MKKm3iSHGwL2wBW1+OP4SaBQvtDpzcvHs1rM5z5pqmB97S8vNKgb4MQfPVwy/b8FmbYueV1H0am8CPZjCt0a1pSOR9LkmbrHAmwNf/WVYXDbbg4zg2V+rVBH8HAEnZGKe+ZbVMxnY1JCJd+JxdFaS1A2oNSDM91FlPmcQed2sWTV0ZfkarfyS5JZWTbRWwDciyzJ6YFXuMliwbXeOGrJolvDTi7x9t9BWbk9RaRwP9iOr2Wtq51ZWoA4yMo33yavWyqlRuC/v7O8xXlO7Avtug4mldKoldaFpYbCqfVliQiz8o5jO13cQuhkbfUpEwXKN+waBm+wd0jeBAbbu8rHkW+/0U+jSX75B9jXtdkMJivBf8sSCj6ED/iKiejZwnzLOPga3DgXOSJbk7mAeDTxeCwO5dIncOtQMxzsoq2UuRYct1V+DRGZSsJGeNTj3mwTbWCJpZB5PERpis1VDrkXcsDnZr4AHCKCwZShJEEBMipmkwzDFFJPGBrtMPiA==");
		Assert.assertNull(CfgToolsTest.CONFIG.getBinary(TestSettingString.BINARY_EMPTY));
		Assert.assertNotNull(CfgToolsTest.CONFIG.getBinary(TestSettingString.BINARY_SAMPLE));
		Assert.assertArrayEquals(realData, CfgToolsTest.CONFIG.getBinary(TestSettingString.BINARY_SAMPLE));
		Assert.assertArrayEquals(dataTestSetting, CfgToolsTest.CONFIG.getBinary(TestSettingString.BINARY_UNDEF));

		try {
			CfgToolsTest.CONFIG.getBinary(TestSettingString.STRING_SAMPLE);
			Assert.fail("string.sample is known to not be a valid Base64 string, and yet was correctly decoded");
		} catch (DecoderException e) {
			// All is well - this is expected
		}
	}

	@Test
	public void testSettingsAndCount() {
		String[] keys = {
			"boolean.true", "boolean.empty", "boolean.false", "byte.over", "byte.max", "byte.empty", "byte.min",
			"byte.under", "short.over", "short.max", "short.empty", "short.min", "short.under", "integer.over",
			"integer.max", "integer.empty", "integer.min", "integer.under", "long.over", "long.max", "long.empty",
			"long.min", "long.under", "float.over", "float.max", "float.empty", "float.min", "float.under",
			"double.over", "double.max", "double.empty", "double.min", "double.under", "string.empty", "string.sample",
			"binary.empty", "binary.sample"
		};
		Assert.assertEquals(keys.length, CfgToolsTest.CONFIG.getCount());
		Set<String> s = new HashSet<String>();
		s.addAll(Arrays.asList(keys));
		Assert.assertEquals(s, CfgToolsTest.CONFIG.getSettings());
	}
}