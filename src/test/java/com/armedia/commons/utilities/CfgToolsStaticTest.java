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

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author drivera@armedia.com
 *
 */
public class CfgToolsStaticTest implements GoodServiceTest {
	private static final Random RANDOM = new Random(System.currentTimeMillis());

	private static Map<String, String> CONFIG = null;

	private static float epsilon = 0.0f;

	private static void assertEquals(float a, float b) {
		if (Math.abs(a - b) > CfgToolsStaticTest.epsilon) {
			Assert.fail(String.format("The given floating point numbers %f and %f do not converge to within %f", a, b,
				CfgToolsStaticTest.epsilon));
		}
	}

	private static void assertEquals(double a, double b) {
		if (Math.abs(a - b) > CfgToolsStaticTest.epsilon) {
			Assert.fail(String.format("The given floating point numbers %f and %f do not converge to within %f", a, b,
				CfgToolsStaticTest.epsilon));
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Properties props = new Properties();
		try (InputStream in = CfgToolsStaticTest.class.getClassLoader().getResourceAsStream("cfgtools.properties")) {
			props.load(in);
		}
		Map<String, String> cfg = new HashMap<>();
		for (Map.Entry<Object, Object> e : props.entrySet()) {
			cfg.put(e.getKey().toString(), e.getValue().toString());
		}
		CfgToolsStaticTest.CONFIG = Tools.freezeMap(cfg);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		CfgToolsStaticTest.CONFIG = null;
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeBoolean(java.lang.String, java.util.Map, java.lang.Boolean)}
	 * .
	 */
	@Test
	public void testDecodeBooleanWithDefault() {
		Assert.assertNull(CfgTools.decodeBoolean("boolean.empty", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(CfgTools.decodeBoolean("boolean.empty", CfgToolsStaticTest.CONFIG, Boolean.TRUE));
		Assert.assertFalse(CfgTools.decodeBoolean("boolean.empty", CfgToolsStaticTest.CONFIG, Boolean.FALSE));

		Assert.assertNull(CfgTools.decodeBoolean("boolean.true.undef", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(CfgTools.decodeBoolean("boolean.true.undef", CfgToolsStaticTest.CONFIG, Boolean.TRUE));
		Assert.assertFalse(CfgTools.decodeBoolean("boolean.true.undef", CfgToolsStaticTest.CONFIG, Boolean.FALSE));

		Assert.assertTrue(CfgTools.decodeBoolean("boolean.true", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(CfgTools.decodeBoolean("boolean.true", CfgToolsStaticTest.CONFIG, Boolean.TRUE));
		Assert.assertTrue(CfgTools.decodeBoolean("boolean.true", CfgToolsStaticTest.CONFIG, Boolean.FALSE));

		Assert.assertFalse(CfgTools.decodeBoolean("boolean.false", CfgToolsStaticTest.CONFIG, null));
		Assert.assertFalse(CfgTools.decodeBoolean("boolean.false", CfgToolsStaticTest.CONFIG, Boolean.TRUE));
		Assert.assertFalse(CfgTools.decodeBoolean("boolean.false", CfgToolsStaticTest.CONFIG, Boolean.FALSE));

		Assert.assertFalse(CfgTools.decodeBoolean("integer.min", CfgToolsStaticTest.CONFIG, null));
		Assert.assertFalse(CfgTools.decodeBoolean("double.max", CfgToolsStaticTest.CONFIG, null));
		Assert.assertFalse(CfgTools.decodeBoolean("string.sample", CfgToolsStaticTest.CONFIG, null));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeBoolean(java.lang.String, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeBooleanWithoutDefault() {
		Assert.assertNull(CfgTools.decodeBoolean("boolean.true.undef", CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(CfgTools.decodeBoolean("boolean.true", CfgToolsStaticTest.CONFIG));
		Assert.assertNull(CfgTools.decodeBoolean("boolean.empty", CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.decodeBoolean("boolean.false", CfgToolsStaticTest.CONFIG));
		Assert.assertNull(CfgTools.decodeBoolean("boolean.false.undef", CfgToolsStaticTest.CONFIG));

		Assert.assertFalse(CfgTools.decodeBoolean("integer.min", CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.decodeBoolean("double.max", CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.decodeBoolean("string.sample", CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeBoolean(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeBooleanSetting() {
		Assert.assertNull(CfgTools.decodeBoolean(TestSetting.BOOLEAN_EMPTY, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(CfgTools.decodeBoolean(TestSetting.BOOLEAN_TRUE_UNDEF, CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.decodeBoolean(TestSetting.BOOLEAN_FALSE_UNDEF, CfgToolsStaticTest.CONFIG));

		Assert.assertTrue(CfgTools.decodeBoolean(TestSetting.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(CfgTools.decodeBoolean(TestSetting.BOOLEAN_TRUE_UNDEF, CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.decodeBoolean(TestSetting.BOOLEAN_FALSE, CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.decodeBoolean(TestSetting.BOOLEAN_FALSE_UNDEF, CfgToolsStaticTest.CONFIG));

		Assert.assertFalse(CfgTools.decodeBoolean(TestSetting.INTEGER_MIN, CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.decodeBoolean(TestSetting.INTEGER_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.decodeBoolean(TestSetting.DOUBLE_MAX, CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.decodeBoolean(TestSetting.DOUBLE_MAX_UNDEF, CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.decodeBoolean(TestSetting.STRING_SAMPLE, CfgToolsStaticTest.CONFIG));

		Assert.assertNull(CfgTools.decodeBoolean(TestSettingString.BOOLEAN_EMPTY, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(CfgTools.decodeBoolean(TestSettingString.BOOLEAN_TRUE_UNDEF, CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.decodeBoolean(TestSettingString.BOOLEAN_FALSE_UNDEF, CfgToolsStaticTest.CONFIG));

		Assert.assertTrue(CfgTools.decodeBoolean(TestSettingString.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(CfgTools.decodeBoolean(TestSettingString.BOOLEAN_TRUE_UNDEF, CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.decodeBoolean(TestSettingString.BOOLEAN_FALSE, CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.decodeBoolean(TestSettingString.BOOLEAN_FALSE_UNDEF, CfgToolsStaticTest.CONFIG));

		Assert.assertFalse(CfgTools.decodeBoolean(TestSettingString.INTEGER_MIN, CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.decodeBoolean(TestSettingString.INTEGER_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.decodeBoolean(TestSettingString.DOUBLE_MAX, CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.decodeBoolean(TestSettingString.DOUBLE_MAX_UNDEF, CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.decodeBoolean(TestSettingString.STRING_SAMPLE, CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#decodeByte(String, Map, Byte)}
	 * .
	 */
	@Test
	public void testDecodeByteWithDefault() {
		Assert.assertNull(CfgTools.decodeByte("byte.empty", CfgToolsStaticTest.CONFIG, null));
		Assert
			.assertTrue(Byte.MIN_VALUE == CfgTools.decodeByte("byte.empty", CfgToolsStaticTest.CONFIG, Byte.MIN_VALUE));
		Assert
			.assertTrue(Byte.MAX_VALUE == CfgTools.decodeByte("byte.empty", CfgToolsStaticTest.CONFIG, Byte.MAX_VALUE));

		Assert.assertNull(CfgTools.decodeByte("byte.min.undef", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(
			Byte.MIN_VALUE == CfgTools.decodeByte("byte.min.undef", CfgToolsStaticTest.CONFIG, Byte.MIN_VALUE));
		Assert.assertTrue(
			Byte.MAX_VALUE == CfgTools.decodeByte("byte.min.undef", CfgToolsStaticTest.CONFIG, Byte.MAX_VALUE));

		Assert.assertTrue(Byte.MIN_VALUE == CfgTools.decodeByte("byte.min", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(Byte.MIN_VALUE == CfgTools.decodeByte("byte.min", CfgToolsStaticTest.CONFIG, Byte.MIN_VALUE));
		Assert.assertTrue(Byte.MIN_VALUE == CfgTools.decodeByte("byte.min", CfgToolsStaticTest.CONFIG, Byte.MAX_VALUE));

		Assert.assertTrue(Byte.MAX_VALUE == CfgTools.decodeByte("byte.max", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(Byte.MAX_VALUE == CfgTools.decodeByte("byte.max", CfgToolsStaticTest.CONFIG, Byte.MIN_VALUE));
		Assert.assertTrue(Byte.MAX_VALUE == CfgTools.decodeByte("byte.max", CfgToolsStaticTest.CONFIG, Byte.MAX_VALUE));

		try {
			CfgTools.decodeByte("byte.over", CfgToolsStaticTest.CONFIG, null);
			Assert.fail("Failed when getting byte.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeByte("byte.under", CfgToolsStaticTest.CONFIG, null);
			Assert.fail("Failed when getting byte.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeByte("boolean.true", CfgToolsStaticTest.CONFIG, null);
			Assert.fail("Failed when getting boolean.true as a byte - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeByte(java.lang.String, java.util.Map)} .
	 */
	@Test
	public void testDecodeByteWithoutDefault() {
		Assert.assertNull(CfgTools.decodeByte("byte.empty", CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Byte.MIN_VALUE == CfgTools.decodeByte("byte.min", CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Byte.MAX_VALUE == CfgTools.decodeByte("byte.max", CfgToolsStaticTest.CONFIG));

		try {
			CfgTools.decodeByte("byte.over", CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting byte.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeByte("byte.under", CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting byte.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeByte("boolean.true", CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting boolean.true as a byte - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeByte(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeByteConfigurationSetting() {
		Assert.assertNull(CfgTools.decodeByte(TestSetting.BYTE_EMPTY, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Byte.MIN_VALUE == CfgTools.decodeByte(TestSetting.BYTE_MIN, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Byte.MIN_VALUE == CfgTools.decodeByte(TestSetting.BYTE_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Byte.MAX_VALUE == CfgTools.decodeByte(TestSetting.BYTE_MAX, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Byte.MAX_VALUE == CfgTools.decodeByte(TestSetting.BYTE_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assert.assertNull(CfgTools.decodeByte(TestSettingString.BYTE_EMPTY, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Byte.MIN_VALUE == CfgTools.decodeByte(TestSettingString.BYTE_MIN, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(
			Byte.MIN_VALUE == CfgTools.decodeByte(TestSettingString.BYTE_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Byte.MAX_VALUE == CfgTools.decodeByte(TestSettingString.BYTE_MAX, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(
			Byte.MAX_VALUE == CfgTools.decodeByte(TestSettingString.BYTE_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		try {
			CfgTools.decodeByte(TestSetting.BYTE_OVER, CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting byte.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeByte(TestSetting.BYTE_UNDER, CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting byte.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeByte(TestSetting.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting boolean.true as a byte - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeShort(String, Map, Short)} .
	 */
	@Test
	public void testDecodeShortWithDefault() {
		Assert.assertNull(CfgTools.decodeShort("short.empty", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(
			Short.MIN_VALUE == CfgTools.decodeShort("short.empty", CfgToolsStaticTest.CONFIG, Short.MIN_VALUE));
		Assert.assertTrue(
			Short.MAX_VALUE == CfgTools.decodeShort("short.empty", CfgToolsStaticTest.CONFIG, Short.MAX_VALUE));

		Assert.assertNull(CfgTools.decodeShort("short.min.undef", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(
			Short.MIN_VALUE == CfgTools.decodeShort("short.min.undef", CfgToolsStaticTest.CONFIG, Short.MIN_VALUE));
		Assert.assertTrue(
			Short.MAX_VALUE == CfgTools.decodeShort("short.min.undef", CfgToolsStaticTest.CONFIG, Short.MAX_VALUE));

		Assert.assertTrue(Short.MIN_VALUE == CfgTools.decodeShort("short.min", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(
			Short.MIN_VALUE == CfgTools.decodeShort("short.min", CfgToolsStaticTest.CONFIG, Short.MIN_VALUE));
		Assert.assertTrue(
			Short.MIN_VALUE == CfgTools.decodeShort("short.min", CfgToolsStaticTest.CONFIG, Short.MAX_VALUE));

		Assert.assertTrue(Short.MAX_VALUE == CfgTools.decodeShort("short.max", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(
			Short.MAX_VALUE == CfgTools.decodeShort("short.max", CfgToolsStaticTest.CONFIG, Short.MIN_VALUE));
		Assert.assertTrue(
			Short.MAX_VALUE == CfgTools.decodeShort("short.max", CfgToolsStaticTest.CONFIG, Short.MAX_VALUE));

		try {
			CfgTools.decodeShort("short.over", CfgToolsStaticTest.CONFIG, null);
			Assert.fail("Failed when getting short.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeShort("short.under", CfgToolsStaticTest.CONFIG, null);
			Assert.fail("Failed when getting short.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeShort("boolean.true", CfgToolsStaticTest.CONFIG, null);
			Assert.fail("Failed when getting boolean.true as a short - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeShort(java.lang.String, java.util.Map)}.
	 */
	@Test
	public void testDecodeShortWithoutDefault() {
		Assert.assertNull(CfgTools.decodeShort("short.empty", CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Short.MIN_VALUE == CfgTools.decodeShort("short.min", CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Short.MAX_VALUE == CfgTools.decodeShort("short.max", CfgToolsStaticTest.CONFIG));

		try {
			CfgTools.decodeShort("short.over", CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting short.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeShort("short.under", CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting short.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeShort("boolean.true", CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting boolean.true as a short - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeShort(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeShortConfigurationSetting() {
		Assert.assertNull(CfgTools.decodeShort(TestSetting.SHORT_EMPTY, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Short.MIN_VALUE == CfgTools.decodeShort(TestSetting.SHORT_MIN, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(
			Short.MIN_VALUE == CfgTools.decodeShort(TestSetting.SHORT_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Short.MAX_VALUE == CfgTools.decodeShort(TestSetting.SHORT_MAX, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(
			Short.MAX_VALUE == CfgTools.decodeShort(TestSetting.SHORT_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assert.assertNull(CfgTools.decodeShort(TestSettingString.SHORT_EMPTY, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(
			Short.MIN_VALUE == CfgTools.decodeShort(TestSettingString.SHORT_MIN, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(
			Short.MIN_VALUE == CfgTools.decodeShort(TestSettingString.SHORT_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(
			Short.MAX_VALUE == CfgTools.decodeShort(TestSettingString.SHORT_MAX, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(
			Short.MAX_VALUE == CfgTools.decodeShort(TestSettingString.SHORT_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		try {
			CfgTools.decodeShort(TestSetting.SHORT_OVER, CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting short.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeShort(TestSetting.SHORT_UNDER, CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting short.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeShort(TestSetting.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting boolean.true as a short - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeInteger(String, Map, Integer)} .
	 */
	@Test
	public void testDecodeIntegerWithDefault() {
		Assert.assertNull(CfgTools.decodeInteger("integer.empty", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(
			Integer.MIN_VALUE == CfgTools.decodeInteger("integer.empty", CfgToolsStaticTest.CONFIG, Integer.MIN_VALUE));
		Assert.assertTrue(
			Integer.MAX_VALUE == CfgTools.decodeInteger("integer.empty", CfgToolsStaticTest.CONFIG, Integer.MAX_VALUE));

		Assert.assertNull(CfgTools.decodeInteger("integer.min.undef", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(Integer.MIN_VALUE == CfgTools.decodeInteger("integer.min.undef", CfgToolsStaticTest.CONFIG,
			Integer.MIN_VALUE));
		Assert.assertTrue(Integer.MAX_VALUE == CfgTools.decodeInteger("integer.min.undef", CfgToolsStaticTest.CONFIG,
			Integer.MAX_VALUE));

		Assert.assertTrue(Integer.MIN_VALUE == CfgTools.decodeInteger("integer.min", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(
			Integer.MIN_VALUE == CfgTools.decodeInteger("integer.min", CfgToolsStaticTest.CONFIG, Integer.MIN_VALUE));
		Assert.assertTrue(
			Integer.MIN_VALUE == CfgTools.decodeInteger("integer.min", CfgToolsStaticTest.CONFIG, Integer.MAX_VALUE));

		Assert.assertTrue(Integer.MAX_VALUE == CfgTools.decodeInteger("integer.max", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(
			Integer.MAX_VALUE == CfgTools.decodeInteger("integer.max", CfgToolsStaticTest.CONFIG, Integer.MIN_VALUE));
		Assert.assertTrue(
			Integer.MAX_VALUE == CfgTools.decodeInteger("integer.max", CfgToolsStaticTest.CONFIG, Integer.MAX_VALUE));

		try {
			CfgTools.decodeInteger("integer.over", CfgToolsStaticTest.CONFIG, null);
			Assert.fail("Failed when getting integer.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeInteger("integer.under", CfgToolsStaticTest.CONFIG, null);
			Assert.fail("Failed when getting integer.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeInteger("boolean.true", CfgToolsStaticTest.CONFIG, null);
			Assert.fail("Failed when getting boolean.true as a integer - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeInteger(java.lang.String, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeIntegerWithoutDefault() {
		Assert.assertNull(CfgTools.decodeInteger("integer.empty", CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Integer.MIN_VALUE == CfgTools.decodeInteger("integer.min", CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Integer.MAX_VALUE == CfgTools.decodeInteger("integer.max", CfgToolsStaticTest.CONFIG));

		try {
			CfgTools.decodeInteger("integer.over", CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting integer.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeInteger("integer.under", CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting integer.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeInteger("boolean.true", CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting boolean.true as a integer - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeInteger(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeIntegerConfigurationSetting() {
		Assert.assertNull(CfgTools.decodeInteger(TestSetting.INTEGER_EMPTY, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(
			Integer.MIN_VALUE == CfgTools.decodeInteger(TestSetting.INTEGER_MIN, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(
			Integer.MIN_VALUE == CfgTools.decodeInteger(TestSetting.INTEGER_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(
			Integer.MAX_VALUE == CfgTools.decodeInteger(TestSetting.INTEGER_MAX, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(
			Integer.MAX_VALUE == CfgTools.decodeInteger(TestSetting.INTEGER_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assert.assertNull(CfgTools.decodeInteger(TestSettingString.INTEGER_EMPTY, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(
			Integer.MIN_VALUE == CfgTools.decodeInteger(TestSettingString.INTEGER_MIN, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Integer.MIN_VALUE == CfgTools.decodeInteger(TestSettingString.INTEGER_MIN_UNDEF,
			CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(
			Integer.MAX_VALUE == CfgTools.decodeInteger(TestSettingString.INTEGER_MAX, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Integer.MAX_VALUE == CfgTools.decodeInteger(TestSettingString.INTEGER_MAX_UNDEF,
			CfgToolsStaticTest.CONFIG));

		try {
			CfgTools.decodeInteger(TestSetting.INTEGER_OVER, CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting integer.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeInteger(TestSetting.INTEGER_UNDER, CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting integer.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeInteger(TestSetting.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting boolean.true as a integer - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#decodeLong(String, Map, Long)}
	 * .
	 */
	@Test
	public void testDecodeLongWithDefault() {
		Assert.assertNull(CfgTools.decodeLong("long.empty", CfgToolsStaticTest.CONFIG, null));
		Assert
			.assertTrue(Long.MIN_VALUE == CfgTools.decodeLong("long.empty", CfgToolsStaticTest.CONFIG, Long.MIN_VALUE));
		Assert
			.assertTrue(Long.MAX_VALUE == CfgTools.decodeLong("long.empty", CfgToolsStaticTest.CONFIG, Long.MAX_VALUE));

		Assert.assertNull(CfgTools.decodeLong("long.min.undef", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(
			Long.MIN_VALUE == CfgTools.decodeLong("long.min.undef", CfgToolsStaticTest.CONFIG, Long.MIN_VALUE));
		Assert.assertTrue(
			Long.MAX_VALUE == CfgTools.decodeLong("long.min.undef", CfgToolsStaticTest.CONFIG, Long.MAX_VALUE));

		Assert.assertTrue(Long.MIN_VALUE == CfgTools.decodeLong("long.min", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(Long.MIN_VALUE == CfgTools.decodeLong("long.min", CfgToolsStaticTest.CONFIG, Long.MIN_VALUE));
		Assert.assertTrue(Long.MIN_VALUE == CfgTools.decodeLong("long.min", CfgToolsStaticTest.CONFIG, Long.MAX_VALUE));

		Assert.assertTrue(Long.MAX_VALUE == CfgTools.decodeLong("long.max", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(Long.MAX_VALUE == CfgTools.decodeLong("long.max", CfgToolsStaticTest.CONFIG, Long.MIN_VALUE));
		Assert.assertTrue(Long.MAX_VALUE == CfgTools.decodeLong("long.max", CfgToolsStaticTest.CONFIG, Long.MAX_VALUE));

		try {
			CfgTools.decodeLong("long.over", CfgToolsStaticTest.CONFIG, null);
			Assert.fail("Failed when getting long.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeLong("long.under", CfgToolsStaticTest.CONFIG, null);
			Assert.fail("Failed when getting long.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeLong("boolean.true", CfgToolsStaticTest.CONFIG, null);
			Assert.fail("Failed when getting boolean.true as a long - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeLong(java.lang.String, java.util.Map)} .
	 */
	@Test
	public void testDecodeLongWithoutDefault() {
		Assert.assertNull(CfgTools.decodeLong("long.empty", CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Long.MIN_VALUE == CfgTools.decodeLong("long.min", CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Long.MAX_VALUE == CfgTools.decodeLong("long.max", CfgToolsStaticTest.CONFIG));

		try {
			CfgTools.decodeLong("long.over", CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting long.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeLong("long.under", CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting long.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeLong("boolean.true", CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting boolean.true as a long - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeLong(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeLongConfigurationSetting() {
		Assert.assertNull(CfgTools.decodeLong(TestSetting.LONG_EMPTY, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Long.MIN_VALUE == CfgTools.decodeLong(TestSetting.LONG_MIN, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Long.MIN_VALUE == CfgTools.decodeLong(TestSetting.LONG_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Long.MAX_VALUE == CfgTools.decodeLong(TestSetting.LONG_MAX, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Long.MAX_VALUE == CfgTools.decodeLong(TestSetting.LONG_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assert.assertNull(CfgTools.decodeLong(TestSettingString.LONG_EMPTY, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Long.MIN_VALUE == CfgTools.decodeLong(TestSettingString.LONG_MIN, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(
			Long.MIN_VALUE == CfgTools.decodeLong(TestSettingString.LONG_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(Long.MAX_VALUE == CfgTools.decodeLong(TestSettingString.LONG_MAX, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(
			Long.MAX_VALUE == CfgTools.decodeLong(TestSettingString.LONG_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		try {
			CfgTools.decodeLong(TestSetting.LONG_OVER, CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting long.over - should have overflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeLong(TestSetting.LONG_UNDER, CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting long.under - should have underflowed");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
		try {
			CfgTools.decodeLong(TestSetting.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting boolean.true as a long - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeFloat(java.lang.String, java.util.Map, java.lang.Float)}
	 * .
	 */
	@Test
	public void testDecodeFloatWithDefault() {
		Assert.assertNull(CfgTools.decodeFloat("float.empty", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(
			Float.MIN_VALUE == CfgTools.decodeFloat("float.empty", CfgToolsStaticTest.CONFIG, Float.MIN_VALUE));
		Assert.assertTrue(
			Float.MAX_VALUE == CfgTools.decodeFloat("float.empty", CfgToolsStaticTest.CONFIG, Float.MAX_VALUE));

		Assert.assertNull(CfgTools.decodeFloat("float.min.undef", CfgToolsStaticTest.CONFIG, null));
		CfgToolsStaticTest.assertEquals(-Float.MAX_VALUE,
			CfgTools.decodeFloat("float.min.undef", CfgToolsStaticTest.CONFIG, -Float.MAX_VALUE));
		CfgToolsStaticTest.assertEquals(Float.MAX_VALUE,
			CfgTools.decodeFloat("float.min.undef", CfgToolsStaticTest.CONFIG, Float.MAX_VALUE));

		CfgToolsStaticTest.assertEquals(-Float.MAX_VALUE,
			CfgTools.decodeFloat("float.min", CfgToolsStaticTest.CONFIG, null));
		CfgToolsStaticTest.assertEquals(-Float.MAX_VALUE,
			CfgTools.decodeFloat("float.min", CfgToolsStaticTest.CONFIG, -Float.MAX_VALUE));
		CfgToolsStaticTest.assertEquals(-Float.MAX_VALUE,
			CfgTools.decodeFloat("float.min", CfgToolsStaticTest.CONFIG, Float.MAX_VALUE));

		CfgToolsStaticTest.assertEquals(Float.MAX_VALUE,
			CfgTools.decodeFloat("float.max", CfgToolsStaticTest.CONFIG, null));
		CfgToolsStaticTest.assertEquals(Float.MAX_VALUE,
			CfgTools.decodeFloat("float.max", CfgToolsStaticTest.CONFIG, -Float.MAX_VALUE));
		CfgToolsStaticTest.assertEquals(Float.MAX_VALUE,
			CfgTools.decodeFloat("float.max", CfgToolsStaticTest.CONFIG, Float.MAX_VALUE));

		Assert.assertTrue(Float.isInfinite(CfgTools.decodeFloat("float.over", CfgToolsStaticTest.CONFIG, null)));
		Assert.assertTrue(Float.isInfinite(CfgTools.decodeFloat("float.under", CfgToolsStaticTest.CONFIG, null)));
		Assert.assertTrue(0 < Float.compare(0, CfgTools.decodeFloat("float.under", CfgToolsStaticTest.CONFIG, null)));

		try {
			CfgTools.decodeFloat("boolean.true", CfgToolsStaticTest.CONFIG, null);
			Assert.fail("Failed when getting boolean.true as a float - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeFloat(java.lang.String, java.util.Map)}.
	 */
	@Test
	public void testDecodeFloatWithoutDefault() {
		Assert.assertNull(CfgTools.decodeFloat("float.empty", CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Float.MAX_VALUE, CfgTools.decodeFloat("float.min", CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Float.MAX_VALUE, CfgTools.decodeFloat("float.max", CfgToolsStaticTest.CONFIG));

		Assert.assertTrue(Float.isInfinite(CfgTools.decodeFloat("float.over", CfgToolsStaticTest.CONFIG)));
		Assert.assertTrue(Float.isInfinite(CfgTools.decodeFloat("float.under", CfgToolsStaticTest.CONFIG)));
		Assert.assertTrue(0 < Float.compare(0, CfgTools.decodeFloat("float.under", CfgToolsStaticTest.CONFIG)));
		try {
			CfgTools.decodeFloat("boolean.true", CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting boolean.true as a float - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeFloat(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeFloatConfigurationSetting() {
		Assert.assertNull(CfgTools.decodeFloat(TestSetting.FLOAT_EMPTY, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Float.MAX_VALUE,
			CfgTools.decodeFloat(TestSetting.FLOAT_MIN, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Float.MAX_VALUE,
			CfgTools.decodeFloat(TestSetting.FLOAT_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Float.MAX_VALUE,
			CfgTools.decodeFloat(TestSetting.FLOAT_MAX, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Float.MAX_VALUE,
			CfgTools.decodeFloat(TestSetting.FLOAT_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assert.assertTrue(Float.isInfinite(CfgTools.decodeFloat(TestSetting.FLOAT_OVER, CfgToolsStaticTest.CONFIG)));
		Assert.assertTrue(Float.isInfinite(CfgTools.decodeFloat(TestSetting.FLOAT_UNDER, CfgToolsStaticTest.CONFIG)));
		Assert
			.assertTrue(0 < Float.compare(0, CfgTools.decodeFloat(TestSetting.FLOAT_UNDER, CfgToolsStaticTest.CONFIG)));
		try {
			CfgTools.decodeFloat(TestSetting.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting boolean.true as a float - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}

		Assert.assertNull(CfgTools.decodeFloat(TestSettingString.FLOAT_EMPTY, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Float.MAX_VALUE,
			CfgTools.decodeFloat(TestSettingString.FLOAT_MIN, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Float.MAX_VALUE,
			CfgTools.decodeFloat(TestSettingString.FLOAT_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Float.MAX_VALUE,
			CfgTools.decodeFloat(TestSettingString.FLOAT_MAX, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Float.MAX_VALUE,
			CfgTools.decodeFloat(TestSettingString.FLOAT_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assert.assertTrue(
			Float.isInfinite(CfgTools.decodeFloat(TestSettingString.FLOAT_OVER, CfgToolsStaticTest.CONFIG)));
		Assert.assertTrue(
			Float.isInfinite(CfgTools.decodeFloat(TestSettingString.FLOAT_UNDER, CfgToolsStaticTest.CONFIG)));
		Assert.assertTrue(
			0 < Float.compare(0, CfgTools.decodeFloat(TestSettingString.FLOAT_UNDER, CfgToolsStaticTest.CONFIG)));
		try {
			CfgTools.decodeFloat(TestSettingString.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting boolean.true as a float - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeDouble(java.lang.String, java.util.Map, java.lang.Double)}
	 * .
	 */
	@Test
	public void testDecodeDoubleWithDefault() {
		Assert.assertNull(CfgTools.decodeDouble("double.empty", CfgToolsStaticTest.CONFIG, null));
		Assert.assertTrue(
			Double.MIN_VALUE == CfgTools.decodeDouble("double.empty", CfgToolsStaticTest.CONFIG, Double.MIN_VALUE));
		Assert.assertTrue(
			Double.MAX_VALUE == CfgTools.decodeDouble("double.empty", CfgToolsStaticTest.CONFIG, Double.MAX_VALUE));

		Assert.assertNull(CfgTools.decodeDouble("double.min.undef", CfgToolsStaticTest.CONFIG, null));
		CfgToolsStaticTest.assertEquals(-Double.MAX_VALUE,
			CfgTools.decodeDouble("double.min.undef", CfgToolsStaticTest.CONFIG, -Double.MAX_VALUE));
		CfgToolsStaticTest.assertEquals(Double.MAX_VALUE,
			CfgTools.decodeDouble("double.min.undef", CfgToolsStaticTest.CONFIG, Double.MAX_VALUE));

		CfgToolsStaticTest.assertEquals(-Double.MAX_VALUE,
			CfgTools.decodeDouble("double.min", CfgToolsStaticTest.CONFIG, null));
		CfgToolsStaticTest.assertEquals(-Double.MAX_VALUE,
			CfgTools.decodeDouble("double.min", CfgToolsStaticTest.CONFIG, -Double.MAX_VALUE));
		CfgToolsStaticTest.assertEquals(-Double.MAX_VALUE,
			CfgTools.decodeDouble("double.min", CfgToolsStaticTest.CONFIG, Double.MAX_VALUE));

		CfgToolsStaticTest.assertEquals(Double.MAX_VALUE,
			CfgTools.decodeDouble("double.max", CfgToolsStaticTest.CONFIG, null));
		CfgToolsStaticTest.assertEquals(Double.MAX_VALUE,
			CfgTools.decodeDouble("double.max", CfgToolsStaticTest.CONFIG, -Double.MAX_VALUE));
		CfgToolsStaticTest.assertEquals(Double.MAX_VALUE,
			CfgTools.decodeDouble("double.max", CfgToolsStaticTest.CONFIG, Double.MAX_VALUE));

		Assert.assertTrue(Double.isInfinite(CfgTools.decodeDouble("double.over", CfgToolsStaticTest.CONFIG, null)));
		Assert.assertTrue(Double.isInfinite(CfgTools.decodeDouble("double.under", CfgToolsStaticTest.CONFIG, null)));
		Assert
			.assertTrue(0 < Double.compare(0, CfgTools.decodeDouble("double.under", CfgToolsStaticTest.CONFIG, null)));

		try {
			CfgTools.decodeDouble("boolean.true", CfgToolsStaticTest.CONFIG, null);
			Assert.fail("Failed when getting boolean.true as a double - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeDouble(java.lang.String, java.util.Map)}.
	 */
	@Test
	public void testDecodeDoubleWithoutDefault() {
		Assert.assertNull(CfgTools.decodeDouble("double.empty", CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Double.MAX_VALUE,
			CfgTools.decodeDouble("double.min", CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Double.MAX_VALUE,
			CfgTools.decodeDouble("double.max", CfgToolsStaticTest.CONFIG));

		Assert.assertTrue(Double.isInfinite(CfgTools.decodeDouble("double.over", CfgToolsStaticTest.CONFIG)));
		Assert.assertTrue(Double.isInfinite(CfgTools.decodeDouble("double.under", CfgToolsStaticTest.CONFIG)));
		Assert.assertTrue(0 < Double.compare(0, CfgTools.decodeDouble("double.under", CfgToolsStaticTest.CONFIG)));
		try {
			CfgTools.decodeDouble("boolean.true", CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting boolean.true as a double - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeDouble(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeDoubleConfigurationSetting() {
		Assert.assertNull(CfgTools.decodeDouble(TestSetting.DOUBLE_EMPTY, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Double.MAX_VALUE,
			CfgTools.decodeDouble(TestSetting.DOUBLE_MIN, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Double.MAX_VALUE,
			CfgTools.decodeDouble(TestSetting.DOUBLE_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Double.MAX_VALUE,
			CfgTools.decodeDouble(TestSetting.DOUBLE_MAX, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Double.MAX_VALUE,
			CfgTools.decodeDouble(TestSetting.DOUBLE_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assert.assertTrue(Double.isInfinite(CfgTools.decodeDouble(TestSetting.DOUBLE_OVER, CfgToolsStaticTest.CONFIG)));
		Assert
			.assertTrue(Double.isInfinite(CfgTools.decodeDouble(TestSetting.DOUBLE_UNDER, CfgToolsStaticTest.CONFIG)));
		Assert.assertTrue(
			0 < Double.compare(0, CfgTools.decodeDouble(TestSetting.DOUBLE_UNDER, CfgToolsStaticTest.CONFIG)));
		try {
			CfgTools.decodeDouble(TestSetting.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting boolean.true as a double - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}

		Assert.assertNull(CfgTools.decodeDouble(TestSettingString.DOUBLE_EMPTY, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Double.MAX_VALUE,
			CfgTools.decodeDouble(TestSettingString.DOUBLE_MIN, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Double.MAX_VALUE,
			CfgTools.decodeDouble(TestSettingString.DOUBLE_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Double.MAX_VALUE,
			CfgTools.decodeDouble(TestSettingString.DOUBLE_MAX, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Double.MAX_VALUE,
			CfgTools.decodeDouble(TestSettingString.DOUBLE_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assert.assertTrue(
			Double.isInfinite(CfgTools.decodeDouble(TestSettingString.DOUBLE_OVER, CfgToolsStaticTest.CONFIG)));
		Assert.assertTrue(
			Double.isInfinite(CfgTools.decodeDouble(TestSettingString.DOUBLE_UNDER, CfgToolsStaticTest.CONFIG)));
		Assert.assertTrue(
			0 < Double.compare(0, CfgTools.decodeDouble(TestSettingString.DOUBLE_UNDER, CfgToolsStaticTest.CONFIG)));
		try {
			CfgTools.decodeDouble(TestSettingString.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG);
			Assert.fail("Failed when getting boolean.true as a double - should have failed to parse");
		} catch (NumberFormatException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeString(java.lang.String, java.util.Map, java.lang.String)}
	 * .
	 */
	@Test
	public void testDecodeStringWithDefault() {
		Assert.assertNull(CfgTools.decodeString("string.empty", CfgToolsStaticTest.CONFIG, null));
		String uuid = UUID.randomUUID().toString();
		Assert.assertEquals(uuid, CfgTools.decodeString("string.empty", CfgToolsStaticTest.CONFIG, uuid));

		Assert.assertNotNull(CfgTools.decodeString("string.sample", CfgToolsStaticTest.CONFIG, null));
		Assert.assertNotNull(CfgTools.decodeString("string.sample", CfgToolsStaticTest.CONFIG, uuid));
		Assert.assertFalse(uuid.equals(CfgTools.decodeString("string.sample", CfgToolsStaticTest.CONFIG, uuid)));

		Assert.assertEquals("Q8'6]mn_{ '3k)X RM3w`;TrB,(`gW9zPG3%k3!&_92$&+4:r-lH @v)z_~l'cHsnj{$ Qrq4}7&#h;_",
			CfgTools.decodeString("string.sample", CfgToolsStaticTest.CONFIG, null));
		Assert.assertEquals("Q8'6]mn_{ '3k)X RM3w`;TrB,(`gW9zPG3%k3!&_92$&+4:r-lH @v)z_~l'cHsnj{$ Qrq4}7&#h;_",
			CfgTools.decodeString("string.sample", CfgToolsStaticTest.CONFIG, uuid));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeString(java.lang.String, java.util.Map)}.
	 */
	@Test
	public void testDecodeStringWithoutDefault() {
		Assert.assertNull(CfgTools.decodeString("string.empty", CfgToolsStaticTest.CONFIG));
		Assert.assertNotNull(CfgTools.decodeString("string.sample", CfgToolsStaticTest.CONFIG));
		Assert.assertEquals("Q8'6]mn_{ '3k)X RM3w`;TrB,(`gW9zPG3%k3!&_92$&+4:r-lH @v)z_~l'cHsnj{$ Qrq4}7&#h;_",
			CfgTools.decodeString("string.sample", CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeString(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeStringConfigurationSetting() {
		Assert.assertNull(CfgTools.decodeString(TestSetting.STRING_EMPTY, CfgToolsStaticTest.CONFIG));
		Assert.assertNotNull(CfgTools.decodeString(TestSetting.STRING_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assert.assertEquals("Q8'6]mn_{ '3k)X RM3w`;TrB,(`gW9zPG3%k3!&_92$&+4:r-lH @v)z_~l'cHsnj{$ Qrq4}7&#h;_",
			CfgTools.decodeString(TestSetting.STRING_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assert.assertEquals("'R-jXvzq4H#wF/6 s|?XN&*c7n;zf'!N~};PM/NL8$#<8fn}N7fkKS!n|c 4GN?8;B&V;_qDL&?) 5+_",
			CfgTools.decodeString(TestSetting.STRING_UNDEF, CfgToolsStaticTest.CONFIG));

		Assert.assertNull(CfgTools.decodeString(TestSettingString.STRING_EMPTY, CfgToolsStaticTest.CONFIG));
		Assert.assertNotNull(CfgTools.decodeString(TestSettingString.STRING_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assert.assertEquals("Q8'6]mn_{ '3k)X RM3w`;TrB,(`gW9zPG3%k3!&_92$&+4:r-lH @v)z_~l'cHsnj{$ Qrq4}7&#h;_",
			CfgTools.decodeString(TestSettingString.STRING_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assert.assertEquals("kn>&V~s*.`_`s5?ngd7;bH :p` 4pmb]: )$~n;b?5?%)2QL3wX!F!M):LC)?(?R:9Kg2g[@589HK$t[",
			CfgTools.decodeString(TestSettingString.STRING_UNDEF, CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeBinary(java.lang.String, java.util.Map, byte[])}
	 * .
	 */
	@Test
	public void testDecodeBinaryWithDefault() throws Throwable {
		byte[] sample = new byte[256];
		CfgToolsStaticTest.RANDOM.nextBytes(sample);

		Assert.assertNull(CfgTools.decodeBinary("binary.empty", CfgToolsStaticTest.CONFIG, null));
		Assert.assertArrayEquals(sample, CfgTools.decodeBinary("binary.empty", CfgToolsStaticTest.CONFIG, sample));

		Assert.assertNotNull(CfgTools.decodeBinary("binary.sample", CfgToolsStaticTest.CONFIG, null));
		Assert.assertNotNull(CfgTools.decodeBinary("binary.sample", CfgToolsStaticTest.CONFIG, sample));
		Assert.assertFalse(
			Arrays.equals(sample, CfgTools.decodeBinary("binary.sample", CfgToolsStaticTest.CONFIG, null)));
		Assert.assertFalse(
			Arrays.equals(sample, CfgTools.decodeBinary("binary.sample", CfgToolsStaticTest.CONFIG, sample)));

		byte[] realData = Base64.decodeBase64(
			"c3aqSlM2IN/HO359hN2nybJ3GRTW/rNE8BckG4fmg0HJHtkIbLdmcXE+9/NI6eieQvVAZyAwPBLucINyeh+xe63eCxfCm9FSXCXytBGDZQm7yCCYxlBz1qRQCTlSzCkZCsotR9ZZ99TUYcVE8UBcEjTXpEXGTxFk3OFbW0jV+gpN5Hp4bSqJsUDsUds+END+nXGFMqQ7CTb1h04Qk6kW/7HstL+JidGLG9d2Qa+x5CfcXs3DMujc2mH461a/8lLLJ/i0NLofyRklJSr1ZWVlqPaq/sk9caCnTPKaDMPDYwTOGnkQVIdnOVjyACAKMs0paAIuo+n7wrG4wRD8f75/GzBPceQwGvVqkRANJtfzGK5zfgYVdNnQtN7U8OnJEl+0C9PMOB272SAx1epwSeLqIJEe9cQkwZyozMvO/md1gQiOsrIT1KcP5F0O4OGneSta9PakvJjsj3Y61N8eksJEFmmKtk69sDsu4ewZmCpDooxtNYRnCj0YNjTkXeL+77joivtz1K147ck7Lkjla1cO1BACCzArsQYVTdRxD9XT/sKHDsv+ahR0GzjuTxH8xUb2zhCT9mYFELcrOiJzeuY2kiYzFBbFohBRqZSdlTjbwHeFKjnpuDzY8imOUJFRw0Dfk3lYPd35A6bHvTEJpyYOufwQWg/5mYwk60YWKbaXcZr5wVFcX2Kxp7C5RrpxGINusROjHvovNUdfFxpm2LVP/NBYLfFnE0QlYDstp1ySipc8fYdRWWNqLxVnCX3YQKhof08934BNknS6WMyPryiIbtm4IblK/kBkCP5uENIvT2EHpGPANKLlwZ/DnA1G/k+AUee4kw1UhAbD+ZOR9/hqWlDFOtVc/69nKu4kp+pY25qtVcPNdDyh2s/yXLJsTkpEGBVFGqOEBY9fajP+knDXsvP8RBt97amfln3Fs/nPRK50KeI9wxzp39SCKJ6k2EIcohFvp/IMHts+QhXmbfjh+0sw5S4EFdfRtfBeBUEbFPtb5hceUK+iU5U4sRCFIi5I81TQLycy3mLkQ9xXqcF6ZUZ/uub97U77sSc/O9OgaWvoYrN69+i1nXYvCUobPT9KnebHxe1qC878Kv0RBi+zDMKO13bVHAmw0BYXXQy/j5H4IStFbW5wROyOdDK659X+2EEk3hDS0XdW8OyEBCntlT5jTcevWDRtSPG2vZSYYEAeYrXOxP3mtg/pfwBIJc+XitgzIvGKdiFEKPwZC4/21tWrT2Tb5oDCjuH6/ssDWfUHyGbco39lk3M0kC3PYLydTRtw5lWyK+Q+vlLKaPfLKXlo0ceap5q21fwZbuDCkioeIG8LPC3cxoOBYVGRhA7Rm+Bw9QTvmgKQwdS87eHkAg==");
		Assert.assertArrayEquals(realData, CfgTools.decodeBinary("binary.sample", CfgToolsStaticTest.CONFIG, null));
		Assert.assertArrayEquals(realData, CfgTools.decodeBinary("binary.sample", CfgToolsStaticTest.CONFIG, sample));

		try {
			CfgTools.decodeBinary("string.sample", CfgToolsStaticTest.CONFIG, null);
			Assert.fail("string.sample is known to not be a valid Base64 string, and yet was correctly decoded");
		} catch (DecoderException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeBinary(java.lang.String, java.util.Map)}.
	 */
	@Test
	public void testDecodeBinaryWithoutDefault() throws Throwable {
		byte[] realData = Base64.decodeBase64(
			"c3aqSlM2IN/HO359hN2nybJ3GRTW/rNE8BckG4fmg0HJHtkIbLdmcXE+9/NI6eieQvVAZyAwPBLucINyeh+xe63eCxfCm9FSXCXytBGDZQm7yCCYxlBz1qRQCTlSzCkZCsotR9ZZ99TUYcVE8UBcEjTXpEXGTxFk3OFbW0jV+gpN5Hp4bSqJsUDsUds+END+nXGFMqQ7CTb1h04Qk6kW/7HstL+JidGLG9d2Qa+x5CfcXs3DMujc2mH461a/8lLLJ/i0NLofyRklJSr1ZWVlqPaq/sk9caCnTPKaDMPDYwTOGnkQVIdnOVjyACAKMs0paAIuo+n7wrG4wRD8f75/GzBPceQwGvVqkRANJtfzGK5zfgYVdNnQtN7U8OnJEl+0C9PMOB272SAx1epwSeLqIJEe9cQkwZyozMvO/md1gQiOsrIT1KcP5F0O4OGneSta9PakvJjsj3Y61N8eksJEFmmKtk69sDsu4ewZmCpDooxtNYRnCj0YNjTkXeL+77joivtz1K147ck7Lkjla1cO1BACCzArsQYVTdRxD9XT/sKHDsv+ahR0GzjuTxH8xUb2zhCT9mYFELcrOiJzeuY2kiYzFBbFohBRqZSdlTjbwHeFKjnpuDzY8imOUJFRw0Dfk3lYPd35A6bHvTEJpyYOufwQWg/5mYwk60YWKbaXcZr5wVFcX2Kxp7C5RrpxGINusROjHvovNUdfFxpm2LVP/NBYLfFnE0QlYDstp1ySipc8fYdRWWNqLxVnCX3YQKhof08934BNknS6WMyPryiIbtm4IblK/kBkCP5uENIvT2EHpGPANKLlwZ/DnA1G/k+AUee4kw1UhAbD+ZOR9/hqWlDFOtVc/69nKu4kp+pY25qtVcPNdDyh2s/yXLJsTkpEGBVFGqOEBY9fajP+knDXsvP8RBt97amfln3Fs/nPRK50KeI9wxzp39SCKJ6k2EIcohFvp/IMHts+QhXmbfjh+0sw5S4EFdfRtfBeBUEbFPtb5hceUK+iU5U4sRCFIi5I81TQLycy3mLkQ9xXqcF6ZUZ/uub97U77sSc/O9OgaWvoYrN69+i1nXYvCUobPT9KnebHxe1qC878Kv0RBi+zDMKO13bVHAmw0BYXXQy/j5H4IStFbW5wROyOdDK659X+2EEk3hDS0XdW8OyEBCntlT5jTcevWDRtSPG2vZSYYEAeYrXOxP3mtg/pfwBIJc+XitgzIvGKdiFEKPwZC4/21tWrT2Tb5oDCjuH6/ssDWfUHyGbco39lk3M0kC3PYLydTRtw5lWyK+Q+vlLKaPfLKXlo0ceap5q21fwZbuDCkioeIG8LPC3cxoOBYVGRhA7Rm+Bw9QTvmgKQwdS87eHkAg==");

		Assert.assertNull(CfgTools.decodeBinary("binary.empty", CfgToolsStaticTest.CONFIG));
		Assert.assertNotNull(CfgTools.decodeBinary("binary.sample", CfgToolsStaticTest.CONFIG));
		Assert.assertArrayEquals(realData, CfgTools.decodeBinary("binary.sample", CfgToolsStaticTest.CONFIG));

		try {
			CfgTools.decodeBinary("string.sample", CfgToolsStaticTest.CONFIG);
			Assert.fail("string.sample is known to not be a valid Base64 string, and yet was correctly decoded");
		} catch (DecoderException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeBinary(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeBinaryConfigurationSetting() throws Throwable {
		byte[] realData = Base64.decodeBase64(
			"c3aqSlM2IN/HO359hN2nybJ3GRTW/rNE8BckG4fmg0HJHtkIbLdmcXE+9/NI6eieQvVAZyAwPBLucINyeh+xe63eCxfCm9FSXCXytBGDZQm7yCCYxlBz1qRQCTlSzCkZCsotR9ZZ99TUYcVE8UBcEjTXpEXGTxFk3OFbW0jV+gpN5Hp4bSqJsUDsUds+END+nXGFMqQ7CTb1h04Qk6kW/7HstL+JidGLG9d2Qa+x5CfcXs3DMujc2mH461a/8lLLJ/i0NLofyRklJSr1ZWVlqPaq/sk9caCnTPKaDMPDYwTOGnkQVIdnOVjyACAKMs0paAIuo+n7wrG4wRD8f75/GzBPceQwGvVqkRANJtfzGK5zfgYVdNnQtN7U8OnJEl+0C9PMOB272SAx1epwSeLqIJEe9cQkwZyozMvO/md1gQiOsrIT1KcP5F0O4OGneSta9PakvJjsj3Y61N8eksJEFmmKtk69sDsu4ewZmCpDooxtNYRnCj0YNjTkXeL+77joivtz1K147ck7Lkjla1cO1BACCzArsQYVTdRxD9XT/sKHDsv+ahR0GzjuTxH8xUb2zhCT9mYFELcrOiJzeuY2kiYzFBbFohBRqZSdlTjbwHeFKjnpuDzY8imOUJFRw0Dfk3lYPd35A6bHvTEJpyYOufwQWg/5mYwk60YWKbaXcZr5wVFcX2Kxp7C5RrpxGINusROjHvovNUdfFxpm2LVP/NBYLfFnE0QlYDstp1ySipc8fYdRWWNqLxVnCX3YQKhof08934BNknS6WMyPryiIbtm4IblK/kBkCP5uENIvT2EHpGPANKLlwZ/DnA1G/k+AUee4kw1UhAbD+ZOR9/hqWlDFOtVc/69nKu4kp+pY25qtVcPNdDyh2s/yXLJsTkpEGBVFGqOEBY9fajP+knDXsvP8RBt97amfln3Fs/nPRK50KeI9wxzp39SCKJ6k2EIcohFvp/IMHts+QhXmbfjh+0sw5S4EFdfRtfBeBUEbFPtb5hceUK+iU5U4sRCFIi5I81TQLycy3mLkQ9xXqcF6ZUZ/uub97U77sSc/O9OgaWvoYrN69+i1nXYvCUobPT9KnebHxe1qC878Kv0RBi+zDMKO13bVHAmw0BYXXQy/j5H4IStFbW5wROyOdDK659X+2EEk3hDS0XdW8OyEBCntlT5jTcevWDRtSPG2vZSYYEAeYrXOxP3mtg/pfwBIJc+XitgzIvGKdiFEKPwZC4/21tWrT2Tb5oDCjuH6/ssDWfUHyGbco39lk3M0kC3PYLydTRtw5lWyK+Q+vlLKaPfLKXlo0ceap5q21fwZbuDCkioeIG8LPC3cxoOBYVGRhA7Rm+Bw9QTvmgKQwdS87eHkAg==");

		byte[] dataTestSetting = null;

		dataTestSetting = Base64.decodeBase64(
			"xy0dZm9C5zT/6ZCdq7ttZ2MHbHLpnqGIlWosnTmHD1v/k30tVc++fhtDdyY31k5tgPpfnDVzNgE4Au8eMvmjlr16e3hrFSuWhc7Bjb05vwodNHoIK0j9LHlO0vcp/WeptHpJlop3ThSYRnr3d/Hx1LkOE3gDpcyUmgeVTr97xkjH3tJgQhCEVzyPd593DHnBu8v0yC0QzJmI+H51Px2sLPlVsapDkztv1ORrwva2XFLhWHyI7C+294/DLHimdZD6qrpsktVXpolGkF09SZsnJvziuRgm/Zdq0gfVSVbjJy4ra8FN3iSegEWMGdg/GweyLyvOVqHFBjMTfg1IxltCzqF5xmQIDvox9CyuzAvYcsQk2/LCn6sciz93Wk5/xheiZIFZrtjx+A9aLfPqmMIDDsPJ4yXD0kgRtBBMzyEMkrTTswLOABmKI3JvMwtxH33vMeiqPTtvhts4gbo1xOUJMb6q0KYq6cs5f/vT5V2r8jHidJ+vc5ROYxi8Wb16js/lLW2AQzOO8Qi7KJZYGpFHB0oCXIuyUO8qvAPYDjX4RbT3Z5wX8bssVUglu/u4hr5i1eV6uR6AfnhueoXl8+duCpegQyNgIvuhUM2sfQYo/l7fIHu18kxKVPj2Tit1ruu1gUNKSNQUUliG0gdWi8xMcjaEIEwOPAihRgU5qDLS9kOd/UTNfdlS+TjtcS8zeT1QwedPD5M40hqmPtdnwv6ZUglcZSF2qlcu7WyimwmGfoyJXmXpK0c64yTL/2lKGsXQo5F12UIeZV82bRWY8FtWBZehM+5w50JLsbjb6kUh6T+Ly3xf8ge0OQdbw/Dp9EBxnhtuYUXeOpOwE0+QSFH15V1847gAGZTtRu2UpXG3vwF4Nn9PlY9HhUHg+redI3YMTs5Tgq/nauMrGmbkm33vDOVuGQ+7Amv0x5hX+jjNvR6oNYe5xEwr3mtRA5Tv9o0jcpkySDIygGbrjJRSIyH78EmgtE30WWIx2YT7qB61pD5nk1/UUHcyO6Z+3IB1Oc+n4ArdvXuGvDFpTT7eg1eKKZnPYg3NbOpAZRJYskZJaGAzCKf6DXDbkopASPZAm7yF/oNdYb3yw02dg4bGWrt6uwlnIYbEBcFVjfEqhEERRO92rhRwaw8fOjnWO3Ho33wv3dwkWdJ3gvAvK+xWHe0TSF2WkhlZZx//zzkDG+ZcXf1y7iPP+GeqZLUypLIrxB73sx6S7Evgj9KKHORDPSYr+S3zj/mgpDkCuhdldPr6rdz4F21DpwTcHOrA8he8r6gKYtUVqCBB2qaSZfUZGB+eo0p3ORyItURgLvYs8GnC9izVXv0tlDDLki0fkpFx7wfOHX4WbCcdIN+31RU6sRaj/Q==");
		Assert.assertNull(CfgTools.decodeBinary(TestSetting.BINARY_EMPTY, CfgToolsStaticTest.CONFIG));
		Assert.assertNotNull(CfgTools.decodeBinary(TestSetting.BINARY_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assert.assertArrayEquals(realData, CfgTools.decodeBinary(TestSetting.BINARY_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assert.assertArrayEquals(dataTestSetting,
			CfgTools.decodeBinary(TestSetting.BINARY_UNDEF, CfgToolsStaticTest.CONFIG));

		try {
			CfgTools.decodeBinary(TestSetting.STRING_SAMPLE, CfgToolsStaticTest.CONFIG);
			Assert.fail("string.sample is known to not be a valid Base64 string, and yet was correctly decoded");
		} catch (DecoderException e) {
			// All is well - this is expected
		}

		dataTestSetting = Base64.decodeBase64(
			"mgFLqv8Ljr6mAcqI8RcKfj9v0aMROcZP7MpXq2A6ZIPJmCLHmFP5niuEPu3swJikfHIhPr7e+czQVKsfc63KIsPnxz44BKRPTtoOvFE91mW+SoW5ep2U/IC+ytrRu88qyum+6BHqsnqMfeDf7bkbSgpHEuob9RFT9Ic/OLke6rpjxp/Kwq6+cbfK+nN0jVzV1WmuNJh3/S+H3f2L2uyteCXMHqxe710/n9s2Bu5+KuK607fNPt3jjFU2X1fnBSpofZK4izRMgTmp+SPMNt82+iMwRhuWodnByRrvcll5+qcj95Eeqkgtk0zA+BE1sCgBPHhO5jQn2JuLay+O4VOstLRnI6ZocXs+sS1T3GmSI6Yuvd7cvQQ9NFOA+eZcuphIdEVKB8ddAC2v3bg/AYq5pgYFQ+5x4N5zpivfzuPZcK84o7C9Uc/tTPXozTsWegLEsD14kL6M8Qu1OxnuDciNTEqI7TdMhb01fLny8ICdwv7gxH3IVmE64UqJh3Y7vq3+Xvj1As27/6ox3lQvM74CwOdaxlJSA1bGTtY3rTeb7dHN6BVwqM/vWjQ0q2T++MU6UU3zgRhrl4QCOYDFvjfcL22h5JpVem+hZgGthGi90DpVxraJFg2LPjDQQoWGKW84PU+ks4fX0MDjzpZgW8tgPD/6lh4+TTer2yCdON0ccgEvaCDQse4KGu7yQnQhySsepufuZ3dG5plFdI7dJYs8O1VIb2WOfPe01Kf6Kk06ylaHp71ZvxdTsNJesbXYzo0QETWNS+ECFY4Di/AnXRa56KQosNvyx7seaBsCIGdkLMLMUoB1HVTFe1POAiKB5BDQGyaYW9+WGB8MYG0mnpW3oZD2E5MKKm3iSHGwL2wBW1+OP4SaBQvtDpzcvHs1rM5z5pqmB97S8vNKgb4MQfPVwy/b8FmbYueV1H0am8CPZjCt0a1pSOR9LkmbrHAmwNf/WVYXDbbg4zg2V+rVBH8HAEnZGKe+ZbVMxnY1JCJd+JxdFaS1A2oNSDM91FlPmcQed2sWTV0ZfkarfyS5JZWTbRWwDciyzJ6YFXuMliwbXeOGrJolvDTi7x9t9BWbk9RaRwP9iOr2Wtq51ZWoA4yMo33yavWyqlRuC/v7O8xXlO7Avtug4mldKoldaFpYbCqfVliQiz8o5jO13cQuhkbfUpEwXKN+waBm+wd0jeBAbbu8rHkW+/0U+jSX75B9jXtdkMJivBf8sSCj6ED/iKiejZwnzLOPga3DgXOSJbk7mAeDTxeCwO5dIncOtQMxzsoq2UuRYct1V+DRGZSsJGeNTj3mwTbWCJpZB5PERpis1VDrkXcsDnZr4AHCKCwZShJEEBMipmkwzDFFJPGBrtMPiA==");
		Assert.assertNull(CfgTools.decodeBinary(TestSettingString.BINARY_EMPTY, CfgToolsStaticTest.CONFIG));
		Assert.assertNotNull(CfgTools.decodeBinary(TestSettingString.BINARY_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assert.assertArrayEquals(realData,
			CfgTools.decodeBinary(TestSettingString.BINARY_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assert.assertArrayEquals(dataTestSetting,
			CfgTools.decodeBinary(TestSettingString.BINARY_UNDEF, CfgToolsStaticTest.CONFIG));

		try {
			CfgTools.decodeBinary(TestSettingString.STRING_SAMPLE, CfgToolsStaticTest.CONFIG);
			Assert.fail("string.sample is known to not be a valid Base64 string, and yet was correctly decoded");
		} catch (DecoderException e) {
			// All is well - this is expected
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#hasValue(java.lang.String, java.util.Map)} .
	 */
	@Test
	public void testHasValueString() {
		Assert.assertTrue(CfgTools.hasValue("string.empty", CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(CfgTools.hasValue("string.sample", CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.hasValue("string.unset", CfgToolsStaticTest.CONFIG));
		try {
			CfgTools.hasValue((String) null, CfgToolsStaticTest.CONFIG);
			Assert.fail("Did not fail with a null setting");
		} catch (IllegalArgumentException e) {
			// All is well
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#hasValue(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testHasValueConfigurationSetting() {
		Assert.assertTrue(CfgTools.hasValue(TestSettingString.STRING_EMPTY, CfgToolsStaticTest.CONFIG));
		Assert.assertTrue(CfgTools.hasValue(TestSettingString.STRING_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assert.assertFalse(CfgTools.hasValue(TestSettingString.STRING_UNSET, CfgToolsStaticTest.CONFIG));
		try {
			CfgTools.hasValue((ConfigurationSetting) null, CfgToolsStaticTest.CONFIG);
			Assert.fail("Did not fail with a null setting");
		} catch (IllegalArgumentException e) {
			// All is well
		}
	}
}