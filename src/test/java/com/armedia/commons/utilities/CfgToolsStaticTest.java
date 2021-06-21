/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2021 Armedia, LLC
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

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 *
 */
public class CfgToolsStaticTest implements GoodService {
	private static final Random RANDOM = new Random(System.currentTimeMillis());

	private static Map<String, String> CONFIG = null;

	private static float epsilon = 0.0f;

	private static void assertEquals(float a, float b) {
		if (Math.abs(a - b) > CfgToolsStaticTest.epsilon) {
			Assertions.fail(String.format("The given floating point numbers %f and %f do not converge to within %f", a,
				b, CfgToolsStaticTest.epsilon));
		}
	}

	private static void assertEquals(double a, double b) {
		if (Math.abs(a - b) > CfgToolsStaticTest.epsilon) {
			Assertions.fail(String.format("The given floating point numbers %f and %f do not converge to within %f", a,
				b, CfgToolsStaticTest.epsilon));
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
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
	@AfterAll
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
		Assertions.assertNull(CfgTools.decodeBoolean("boolean.empty", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertTrue(CfgTools.decodeBoolean("boolean.empty", CfgToolsStaticTest.CONFIG, Boolean.TRUE));
		Assertions.assertFalse(CfgTools.decodeBoolean("boolean.empty", CfgToolsStaticTest.CONFIG, Boolean.FALSE));

		Assertions.assertNull(CfgTools.decodeBoolean("boolean.true.undef", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertTrue(CfgTools.decodeBoolean("boolean.true.undef", CfgToolsStaticTest.CONFIG, Boolean.TRUE));
		Assertions.assertFalse(CfgTools.decodeBoolean("boolean.true.undef", CfgToolsStaticTest.CONFIG, Boolean.FALSE));

		Assertions.assertTrue(CfgTools.decodeBoolean("boolean.true", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertTrue(CfgTools.decodeBoolean("boolean.true", CfgToolsStaticTest.CONFIG, Boolean.TRUE));
		Assertions.assertTrue(CfgTools.decodeBoolean("boolean.true", CfgToolsStaticTest.CONFIG, Boolean.FALSE));

		Assertions.assertFalse(CfgTools.decodeBoolean("boolean.false", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertFalse(CfgTools.decodeBoolean("boolean.false", CfgToolsStaticTest.CONFIG, Boolean.TRUE));
		Assertions.assertFalse(CfgTools.decodeBoolean("boolean.false", CfgToolsStaticTest.CONFIG, Boolean.FALSE));

		Assertions.assertFalse(CfgTools.decodeBoolean("integer.min", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertFalse(CfgTools.decodeBoolean("double.max", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertFalse(CfgTools.decodeBoolean("string.sample", CfgToolsStaticTest.CONFIG, null));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeBoolean(java.lang.String, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeBooleanWithoutDefault() {
		Assertions.assertNull(CfgTools.decodeBoolean("boolean.true.undef", CfgToolsStaticTest.CONFIG));
		Assertions.assertTrue(CfgTools.decodeBoolean("boolean.true", CfgToolsStaticTest.CONFIG));
		Assertions.assertNull(CfgTools.decodeBoolean("boolean.empty", CfgToolsStaticTest.CONFIG));
		Assertions.assertFalse(CfgTools.decodeBoolean("boolean.false", CfgToolsStaticTest.CONFIG));
		Assertions.assertNull(CfgTools.decodeBoolean("boolean.false.undef", CfgToolsStaticTest.CONFIG));

		Assertions.assertFalse(CfgTools.decodeBoolean("integer.min", CfgToolsStaticTest.CONFIG));
		Assertions.assertFalse(CfgTools.decodeBoolean("double.max", CfgToolsStaticTest.CONFIG));
		Assertions.assertFalse(CfgTools.decodeBoolean("string.sample", CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeBoolean(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeBooleanSetting() {
		Assertions.assertNull(CfgTools.decodeBoolean(TestSetting.BOOLEAN_EMPTY, CfgToolsStaticTest.CONFIG));
		Assertions.assertTrue(CfgTools.decodeBoolean(TestSetting.BOOLEAN_TRUE_UNDEF, CfgToolsStaticTest.CONFIG));
		Assertions.assertFalse(CfgTools.decodeBoolean(TestSetting.BOOLEAN_FALSE_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions.assertTrue(CfgTools.decodeBoolean(TestSetting.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG));
		Assertions.assertTrue(CfgTools.decodeBoolean(TestSetting.BOOLEAN_TRUE_UNDEF, CfgToolsStaticTest.CONFIG));
		Assertions.assertFalse(CfgTools.decodeBoolean(TestSetting.BOOLEAN_FALSE, CfgToolsStaticTest.CONFIG));
		Assertions.assertFalse(CfgTools.decodeBoolean(TestSetting.BOOLEAN_FALSE_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions.assertFalse(CfgTools.decodeBoolean(TestSetting.INTEGER_MIN, CfgToolsStaticTest.CONFIG));
		Assertions.assertFalse(CfgTools.decodeBoolean(TestSetting.INTEGER_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assertions.assertFalse(CfgTools.decodeBoolean(TestSetting.DOUBLE_MAX, CfgToolsStaticTest.CONFIG));
		Assertions.assertFalse(CfgTools.decodeBoolean(TestSetting.DOUBLE_MAX_UNDEF, CfgToolsStaticTest.CONFIG));
		Assertions.assertFalse(CfgTools.decodeBoolean(TestSetting.STRING_SAMPLE, CfgToolsStaticTest.CONFIG));

		Assertions.assertNull(CfgTools.decodeBoolean(TestSettingString.BOOLEAN_EMPTY, CfgToolsStaticTest.CONFIG));
		Assertions.assertTrue(CfgTools.decodeBoolean(TestSettingString.BOOLEAN_TRUE_UNDEF, CfgToolsStaticTest.CONFIG));
		Assertions
			.assertFalse(CfgTools.decodeBoolean(TestSettingString.BOOLEAN_FALSE_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions.assertTrue(CfgTools.decodeBoolean(TestSettingString.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG));
		Assertions.assertTrue(CfgTools.decodeBoolean(TestSettingString.BOOLEAN_TRUE_UNDEF, CfgToolsStaticTest.CONFIG));
		Assertions.assertFalse(CfgTools.decodeBoolean(TestSettingString.BOOLEAN_FALSE, CfgToolsStaticTest.CONFIG));
		Assertions
			.assertFalse(CfgTools.decodeBoolean(TestSettingString.BOOLEAN_FALSE_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions.assertFalse(CfgTools.decodeBoolean(TestSettingString.INTEGER_MIN, CfgToolsStaticTest.CONFIG));
		Assertions.assertFalse(CfgTools.decodeBoolean(TestSettingString.INTEGER_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assertions.assertFalse(CfgTools.decodeBoolean(TestSettingString.DOUBLE_MAX, CfgToolsStaticTest.CONFIG));
		Assertions.assertFalse(CfgTools.decodeBoolean(TestSettingString.DOUBLE_MAX_UNDEF, CfgToolsStaticTest.CONFIG));
		Assertions.assertFalse(CfgTools.decodeBoolean(TestSettingString.STRING_SAMPLE, CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#decodeByte(String, Map, Byte)}
	 * .
	 */
	@Test
	public void testDecodeByteWithDefault() {
		Assertions.assertNull(CfgTools.decodeByte("byte.empty", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Byte.MIN_VALUE,
			CfgTools.decodeByte("byte.empty", CfgToolsStaticTest.CONFIG, Byte.MIN_VALUE));
		Assertions.assertEquals(Byte.MAX_VALUE,
			CfgTools.decodeByte("byte.empty", CfgToolsStaticTest.CONFIG, Byte.MAX_VALUE));

		Assertions.assertNull(CfgTools.decodeByte("byte.min.undef", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Byte.MIN_VALUE,
			CfgTools.decodeByte("byte.min.undef", CfgToolsStaticTest.CONFIG, Byte.MIN_VALUE));
		Assertions.assertEquals(Byte.MAX_VALUE,
			CfgTools.decodeByte("byte.min.undef", CfgToolsStaticTest.CONFIG, Byte.MAX_VALUE));

		Assertions.assertEquals(Byte.MIN_VALUE, CfgTools.decodeByte("byte.min", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Byte.MIN_VALUE,
			CfgTools.decodeByte("byte.min", CfgToolsStaticTest.CONFIG, Byte.MIN_VALUE));
		Assertions.assertEquals(Byte.MIN_VALUE,
			CfgTools.decodeByte("byte.min", CfgToolsStaticTest.CONFIG, Byte.MAX_VALUE));

		Assertions.assertEquals(Byte.MAX_VALUE, CfgTools.decodeByte("byte.max", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Byte.MAX_VALUE,
			CfgTools.decodeByte("byte.max", CfgToolsStaticTest.CONFIG, Byte.MIN_VALUE));
		Assertions.assertEquals(Byte.MAX_VALUE,
			CfgTools.decodeByte("byte.max", CfgToolsStaticTest.CONFIG, Byte.MAX_VALUE));

		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeByte("byte.over", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeByte("byte.under", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeByte("boolean.true", CfgToolsStaticTest.CONFIG, null));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeByte(java.lang.String, java.util.Map)} .
	 */
	@Test
	public void testDecodeByteWithoutDefault() {
		Assertions.assertNull(CfgTools.decodeByte("byte.empty", CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Byte.MIN_VALUE, CfgTools.decodeByte("byte.min", CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Byte.MAX_VALUE, CfgTools.decodeByte("byte.max", CfgToolsStaticTest.CONFIG));

		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeByte("byte.over", CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeByte("byte.under", CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeByte("boolean.true", CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeByte(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeByteConfigurationSetting() {
		Assertions.assertNull(CfgTools.decodeByte(TestSetting.BYTE_EMPTY, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Byte.MIN_VALUE, CfgTools.decodeByte(TestSetting.BYTE_MIN, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Byte.MIN_VALUE,
			CfgTools.decodeByte(TestSetting.BYTE_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Byte.MAX_VALUE, CfgTools.decodeByte(TestSetting.BYTE_MAX, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Byte.MAX_VALUE,
			CfgTools.decodeByte(TestSetting.BYTE_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions.assertNull(CfgTools.decodeByte(TestSettingString.BYTE_EMPTY, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Byte.MIN_VALUE,
			CfgTools.decodeByte(TestSettingString.BYTE_MIN, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Byte.MIN_VALUE,
			CfgTools.decodeByte(TestSettingString.BYTE_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Byte.MAX_VALUE,
			CfgTools.decodeByte(TestSettingString.BYTE_MAX, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Byte.MAX_VALUE,
			CfgTools.decodeByte(TestSettingString.BYTE_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeByte(TestSetting.BYTE_OVER, CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeByte(TestSetting.BYTE_UNDER, CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeByte(TestSetting.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeShort(String, Map, Short)} .
	 */
	@Test
	public void testDecodeShortWithDefault() {
		Assertions.assertNull(CfgTools.decodeShort("short.empty", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Short.MIN_VALUE,
			CfgTools.decodeShort("short.empty", CfgToolsStaticTest.CONFIG, Short.MIN_VALUE));
		Assertions.assertEquals(Short.MAX_VALUE,
			CfgTools.decodeShort("short.empty", CfgToolsStaticTest.CONFIG, Short.MAX_VALUE));

		Assertions.assertNull(CfgTools.decodeShort("short.min.undef", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Short.MIN_VALUE,
			CfgTools.decodeShort("short.min.undef", CfgToolsStaticTest.CONFIG, Short.MIN_VALUE));
		Assertions.assertEquals(Short.MAX_VALUE,
			CfgTools.decodeShort("short.min.undef", CfgToolsStaticTest.CONFIG, Short.MAX_VALUE));

		Assertions.assertEquals(Short.MIN_VALUE, CfgTools.decodeShort("short.min", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Short.MIN_VALUE,
			CfgTools.decodeShort("short.min", CfgToolsStaticTest.CONFIG, Short.MIN_VALUE));
		Assertions.assertEquals(Short.MIN_VALUE,
			CfgTools.decodeShort("short.min", CfgToolsStaticTest.CONFIG, Short.MAX_VALUE));

		Assertions.assertEquals(Short.MAX_VALUE, CfgTools.decodeShort("short.max", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Short.MAX_VALUE,
			CfgTools.decodeShort("short.max", CfgToolsStaticTest.CONFIG, Short.MIN_VALUE));
		Assertions.assertEquals(Short.MAX_VALUE,
			CfgTools.decodeShort("short.max", CfgToolsStaticTest.CONFIG, Short.MAX_VALUE));

		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeShort("short.over", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeShort("short.under", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeShort("boolean.true", CfgToolsStaticTest.CONFIG, null));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeShort(java.lang.String, java.util.Map)}.
	 */
	@Test
	public void testDecodeShortWithoutDefault() {
		Assertions.assertNull(CfgTools.decodeShort("short.empty", CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Short.MIN_VALUE, CfgTools.decodeShort("short.min", CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Short.MAX_VALUE, CfgTools.decodeShort("short.max", CfgToolsStaticTest.CONFIG));

		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeShort("short.over", CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeShort("short.under", CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeShort("boolean.true", CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeShort(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeShortConfigurationSetting() {
		Assertions.assertNull(CfgTools.decodeShort(TestSetting.SHORT_EMPTY, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Short.MIN_VALUE,
			CfgTools.decodeShort(TestSetting.SHORT_MIN, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Short.MIN_VALUE,
			CfgTools.decodeShort(TestSetting.SHORT_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Short.MAX_VALUE,
			CfgTools.decodeShort(TestSetting.SHORT_MAX, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Short.MAX_VALUE,
			CfgTools.decodeShort(TestSetting.SHORT_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions.assertNull(CfgTools.decodeShort(TestSettingString.SHORT_EMPTY, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Short.MIN_VALUE,
			CfgTools.decodeShort(TestSettingString.SHORT_MIN, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Short.MIN_VALUE,
			CfgTools.decodeShort(TestSettingString.SHORT_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Short.MAX_VALUE,
			CfgTools.decodeShort(TestSettingString.SHORT_MAX, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Short.MAX_VALUE,
			CfgTools.decodeShort(TestSettingString.SHORT_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeShort(TestSetting.SHORT_OVER, CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeShort(TestSetting.SHORT_UNDER, CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeShort(TestSetting.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeInteger(String, Map, Integer)} .
	 */
	@Test
	public void testDecodeIntegerWithDefault() {
		Assertions.assertNull(CfgTools.decodeInteger("integer.empty", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Integer.MIN_VALUE,
			CfgTools.decodeInteger("integer.empty", CfgToolsStaticTest.CONFIG, Integer.MIN_VALUE));
		Assertions.assertEquals(Integer.MAX_VALUE,
			CfgTools.decodeInteger("integer.empty", CfgToolsStaticTest.CONFIG, Integer.MAX_VALUE));

		Assertions.assertNull(CfgTools.decodeInteger("integer.min.undef", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Integer.MIN_VALUE,
			CfgTools.decodeInteger("integer.min.undef", CfgToolsStaticTest.CONFIG, Integer.MIN_VALUE));
		Assertions.assertEquals(Integer.MAX_VALUE,
			CfgTools.decodeInteger("integer.min.undef", CfgToolsStaticTest.CONFIG, Integer.MAX_VALUE));

		Assertions.assertEquals(Integer.MIN_VALUE,
			CfgTools.decodeInteger("integer.min", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Integer.MIN_VALUE,
			CfgTools.decodeInteger("integer.min", CfgToolsStaticTest.CONFIG, Integer.MIN_VALUE));
		Assertions.assertEquals(Integer.MIN_VALUE,
			CfgTools.decodeInteger("integer.min", CfgToolsStaticTest.CONFIG, Integer.MAX_VALUE));

		Assertions.assertEquals(Integer.MAX_VALUE,
			CfgTools.decodeInteger("integer.max", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Integer.MAX_VALUE,
			CfgTools.decodeInteger("integer.max", CfgToolsStaticTest.CONFIG, Integer.MIN_VALUE));
		Assertions.assertEquals(Integer.MAX_VALUE,
			CfgTools.decodeInteger("integer.max", CfgToolsStaticTest.CONFIG, Integer.MAX_VALUE));

		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeInteger("integer.over", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeInteger("integer.under", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeInteger("boolean.true", CfgToolsStaticTest.CONFIG, null));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeInteger(java.lang.String, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeIntegerWithoutDefault() {
		Assertions.assertNull(CfgTools.decodeInteger("integer.empty", CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Integer.MIN_VALUE, CfgTools.decodeInteger("integer.min", CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Integer.MAX_VALUE, CfgTools.decodeInteger("integer.max", CfgToolsStaticTest.CONFIG));

		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeInteger("integer.over", CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeInteger("integer.under", CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeInteger("boolean.true", CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeInteger(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeIntegerConfigurationSetting() {
		Assertions.assertNull(CfgTools.decodeInteger(TestSetting.INTEGER_EMPTY, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Integer.MIN_VALUE,
			CfgTools.decodeInteger(TestSetting.INTEGER_MIN, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Integer.MIN_VALUE,
			CfgTools.decodeInteger(TestSetting.INTEGER_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Integer.MAX_VALUE,
			CfgTools.decodeInteger(TestSetting.INTEGER_MAX, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Integer.MAX_VALUE,
			CfgTools.decodeInteger(TestSetting.INTEGER_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions.assertNull(CfgTools.decodeInteger(TestSettingString.INTEGER_EMPTY, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Integer.MIN_VALUE,
			CfgTools.decodeInteger(TestSettingString.INTEGER_MIN, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Integer.MIN_VALUE,
			CfgTools.decodeInteger(TestSettingString.INTEGER_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Integer.MAX_VALUE,
			CfgTools.decodeInteger(TestSettingString.INTEGER_MAX, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Integer.MAX_VALUE,
			CfgTools.decodeInteger(TestSettingString.INTEGER_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeInteger(TestSetting.INTEGER_OVER, CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeInteger(TestSetting.INTEGER_UNDER, CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeInteger(TestSetting.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.CfgTools#decodeLong(String, Map, Long)}
	 * .
	 */
	@Test
	public void testDecodeLongWithDefault() {
		Assertions.assertNull(CfgTools.decodeLong("long.empty", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Long.MIN_VALUE,
			CfgTools.decodeLong("long.empty", CfgToolsStaticTest.CONFIG, Long.MIN_VALUE));
		Assertions.assertEquals(Long.MAX_VALUE,
			CfgTools.decodeLong("long.empty", CfgToolsStaticTest.CONFIG, Long.MAX_VALUE));

		Assertions.assertNull(CfgTools.decodeLong("long.min.undef", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Long.MIN_VALUE,
			CfgTools.decodeLong("long.min.undef", CfgToolsStaticTest.CONFIG, Long.MIN_VALUE));
		Assertions.assertEquals(Long.MAX_VALUE,
			CfgTools.decodeLong("long.min.undef", CfgToolsStaticTest.CONFIG, Long.MAX_VALUE));

		Assertions.assertEquals(Long.MIN_VALUE, CfgTools.decodeLong("long.min", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Long.MIN_VALUE,
			CfgTools.decodeLong("long.min", CfgToolsStaticTest.CONFIG, Long.MIN_VALUE));
		Assertions.assertEquals(Long.MIN_VALUE,
			CfgTools.decodeLong("long.min", CfgToolsStaticTest.CONFIG, Long.MAX_VALUE));

		Assertions.assertEquals(Long.MAX_VALUE, CfgTools.decodeLong("long.max", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Long.MAX_VALUE,
			CfgTools.decodeLong("long.max", CfgToolsStaticTest.CONFIG, Long.MIN_VALUE));
		Assertions.assertEquals(Long.MAX_VALUE,
			CfgTools.decodeLong("long.max", CfgToolsStaticTest.CONFIG, Long.MAX_VALUE));

		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeLong("long.over", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeLong("long.under", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeLong("boolean.true", CfgToolsStaticTest.CONFIG, null));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeLong(java.lang.String, java.util.Map)} .
	 */
	@Test
	public void testDecodeLongWithoutDefault() {
		Assertions.assertNull(CfgTools.decodeLong("long.empty", CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Long.MIN_VALUE, CfgTools.decodeLong("long.min", CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Long.MAX_VALUE, CfgTools.decodeLong("long.max", CfgToolsStaticTest.CONFIG));

		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeLong("long.over", CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeLong("long.under", CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeLong("boolean.true", CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeLong(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeLongConfigurationSetting() {
		Assertions.assertNull(CfgTools.decodeLong(TestSetting.LONG_EMPTY, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Long.MIN_VALUE, CfgTools.decodeLong(TestSetting.LONG_MIN, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Long.MIN_VALUE,
			CfgTools.decodeLong(TestSetting.LONG_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Long.MAX_VALUE, CfgTools.decodeLong(TestSetting.LONG_MAX, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Long.MAX_VALUE,
			CfgTools.decodeLong(TestSetting.LONG_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions.assertNull(CfgTools.decodeLong(TestSettingString.LONG_EMPTY, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Long.MIN_VALUE,
			CfgTools.decodeLong(TestSettingString.LONG_MIN, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Long.MIN_VALUE,
			CfgTools.decodeLong(TestSettingString.LONG_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Long.MAX_VALUE,
			CfgTools.decodeLong(TestSettingString.LONG_MAX, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals(Long.MAX_VALUE,
			CfgTools.decodeLong(TestSettingString.LONG_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeLong(TestSetting.LONG_OVER, CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeLong(TestSetting.LONG_UNDER, CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeLong(TestSetting.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeFloat(java.lang.String, java.util.Map, java.lang.Float)}
	 * .
	 */
	@Test
	public void testDecodeFloatWithDefault() {
		Assertions.assertNull(CfgTools.decodeFloat("float.empty", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Float.MIN_VALUE,
			CfgTools.decodeFloat("float.empty", CfgToolsStaticTest.CONFIG, Float.MIN_VALUE));
		Assertions.assertEquals(Float.MAX_VALUE,
			CfgTools.decodeFloat("float.empty", CfgToolsStaticTest.CONFIG, Float.MAX_VALUE));

		Assertions.assertNull(CfgTools.decodeFloat("float.min.undef", CfgToolsStaticTest.CONFIG, null));
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

		Assertions.assertTrue(Float.isInfinite(CfgTools.decodeFloat("float.over", CfgToolsStaticTest.CONFIG, null)));
		Assertions.assertTrue(Float.isInfinite(CfgTools.decodeFloat("float.under", CfgToolsStaticTest.CONFIG, null)));
		Assertions
			.assertTrue(0 < Float.compare(0, CfgTools.decodeFloat("float.under", CfgToolsStaticTest.CONFIG, null)));

		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeFloat("boolean.true", CfgToolsStaticTest.CONFIG, null));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeFloat(java.lang.String, java.util.Map)}.
	 */
	@Test
	public void testDecodeFloatWithoutDefault() {
		Assertions.assertNull(CfgTools.decodeFloat("float.empty", CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Float.MAX_VALUE, CfgTools.decodeFloat("float.min", CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Float.MAX_VALUE, CfgTools.decodeFloat("float.max", CfgToolsStaticTest.CONFIG));

		Assertions.assertTrue(Float.isInfinite(CfgTools.decodeFloat("float.over", CfgToolsStaticTest.CONFIG)));
		Assertions.assertTrue(Float.isInfinite(CfgTools.decodeFloat("float.under", CfgToolsStaticTest.CONFIG)));
		Assertions.assertTrue(0 < Float.compare(0, CfgTools.decodeFloat("float.under", CfgToolsStaticTest.CONFIG)));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeFloat("boolean.true", CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeFloat(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeFloatConfigurationSetting() {
		Assertions.assertNull(CfgTools.decodeFloat(TestSetting.FLOAT_EMPTY, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Float.MAX_VALUE,
			CfgTools.decodeFloat(TestSetting.FLOAT_MIN, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Float.MAX_VALUE,
			CfgTools.decodeFloat(TestSetting.FLOAT_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Float.MAX_VALUE,
			CfgTools.decodeFloat(TestSetting.FLOAT_MAX, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Float.MAX_VALUE,
			CfgTools.decodeFloat(TestSetting.FLOAT_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions
			.assertTrue(Float.isInfinite(CfgTools.decodeFloat(TestSetting.FLOAT_OVER, CfgToolsStaticTest.CONFIG)));
		Assertions
			.assertTrue(Float.isInfinite(CfgTools.decodeFloat(TestSetting.FLOAT_UNDER, CfgToolsStaticTest.CONFIG)));
		Assertions
			.assertTrue(0 < Float.compare(0, CfgTools.decodeFloat(TestSetting.FLOAT_UNDER, CfgToolsStaticTest.CONFIG)));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeFloat(TestSetting.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG));

		Assertions.assertNull(CfgTools.decodeFloat(TestSettingString.FLOAT_EMPTY, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Float.MAX_VALUE,
			CfgTools.decodeFloat(TestSettingString.FLOAT_MIN, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Float.MAX_VALUE,
			CfgTools.decodeFloat(TestSettingString.FLOAT_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Float.MAX_VALUE,
			CfgTools.decodeFloat(TestSettingString.FLOAT_MAX, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Float.MAX_VALUE,
			CfgTools.decodeFloat(TestSettingString.FLOAT_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions.assertTrue(
			Float.isInfinite(CfgTools.decodeFloat(TestSettingString.FLOAT_OVER, CfgToolsStaticTest.CONFIG)));
		Assertions.assertTrue(
			Float.isInfinite(CfgTools.decodeFloat(TestSettingString.FLOAT_UNDER, CfgToolsStaticTest.CONFIG)));
		Assertions.assertTrue(
			0 < Float.compare(0, CfgTools.decodeFloat(TestSettingString.FLOAT_UNDER, CfgToolsStaticTest.CONFIG)));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeFloat(TestSettingString.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeDouble(java.lang.String, java.util.Map, java.lang.Double)}
	 * .
	 */
	@Test
	public void testDecodeDoubleWithDefault() {
		Assertions.assertNull(CfgTools.decodeDouble("double.empty", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals(Double.MIN_VALUE,
			CfgTools.decodeDouble("double.empty", CfgToolsStaticTest.CONFIG, Double.MIN_VALUE));
		Assertions.assertEquals(Double.MAX_VALUE,
			CfgTools.decodeDouble("double.empty", CfgToolsStaticTest.CONFIG, Double.MAX_VALUE));

		Assertions.assertNull(CfgTools.decodeDouble("double.min.undef", CfgToolsStaticTest.CONFIG, null));
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

		Assertions.assertTrue(Double.isInfinite(CfgTools.decodeDouble("double.over", CfgToolsStaticTest.CONFIG, null)));
		Assertions
			.assertTrue(Double.isInfinite(CfgTools.decodeDouble("double.under", CfgToolsStaticTest.CONFIG, null)));
		Assertions
			.assertTrue(0 < Double.compare(0, CfgTools.decodeDouble("double.under", CfgToolsStaticTest.CONFIG, null)));

		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeDouble("boolean.true", CfgToolsStaticTest.CONFIG, null));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeDouble(java.lang.String, java.util.Map)}.
	 */
	@Test
	public void testDecodeDoubleWithoutDefault() {
		Assertions.assertNull(CfgTools.decodeDouble("double.empty", CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Double.MAX_VALUE,
			CfgTools.decodeDouble("double.min", CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Double.MAX_VALUE,
			CfgTools.decodeDouble("double.max", CfgToolsStaticTest.CONFIG));

		Assertions.assertTrue(Double.isInfinite(CfgTools.decodeDouble("double.over", CfgToolsStaticTest.CONFIG)));
		Assertions.assertTrue(Double.isInfinite(CfgTools.decodeDouble("double.under", CfgToolsStaticTest.CONFIG)));
		Assertions.assertTrue(0 < Double.compare(0, CfgTools.decodeDouble("double.under", CfgToolsStaticTest.CONFIG)));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeDouble("boolean.true", CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeDouble(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeDoubleConfigurationSetting() {
		Assertions.assertNull(CfgTools.decodeDouble(TestSetting.DOUBLE_EMPTY, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Double.MAX_VALUE,
			CfgTools.decodeDouble(TestSetting.DOUBLE_MIN, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Double.MAX_VALUE,
			CfgTools.decodeDouble(TestSetting.DOUBLE_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Double.MAX_VALUE,
			CfgTools.decodeDouble(TestSetting.DOUBLE_MAX, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Double.MAX_VALUE,
			CfgTools.decodeDouble(TestSetting.DOUBLE_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions
			.assertTrue(Double.isInfinite(CfgTools.decodeDouble(TestSetting.DOUBLE_OVER, CfgToolsStaticTest.CONFIG)));
		Assertions
			.assertTrue(Double.isInfinite(CfgTools.decodeDouble(TestSetting.DOUBLE_UNDER, CfgToolsStaticTest.CONFIG)));
		Assertions.assertTrue(
			0 < Double.compare(0, CfgTools.decodeDouble(TestSetting.DOUBLE_UNDER, CfgToolsStaticTest.CONFIG)));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeDouble(TestSetting.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG));

		Assertions.assertNull(CfgTools.decodeDouble(TestSettingString.DOUBLE_EMPTY, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Double.MAX_VALUE,
			CfgTools.decodeDouble(TestSettingString.DOUBLE_MIN, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(-Double.MAX_VALUE,
			CfgTools.decodeDouble(TestSettingString.DOUBLE_MIN_UNDEF, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Double.MAX_VALUE,
			CfgTools.decodeDouble(TestSettingString.DOUBLE_MAX, CfgToolsStaticTest.CONFIG));
		CfgToolsStaticTest.assertEquals(Double.MAX_VALUE,
			CfgTools.decodeDouble(TestSettingString.DOUBLE_MAX_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions.assertTrue(
			Double.isInfinite(CfgTools.decodeDouble(TestSettingString.DOUBLE_OVER, CfgToolsStaticTest.CONFIG)));
		Assertions.assertTrue(
			Double.isInfinite(CfgTools.decodeDouble(TestSettingString.DOUBLE_UNDER, CfgToolsStaticTest.CONFIG)));
		Assertions.assertTrue(
			0 < Double.compare(0, CfgTools.decodeDouble(TestSettingString.DOUBLE_UNDER, CfgToolsStaticTest.CONFIG)));
		Assertions.assertThrows(NumberFormatException.class,
			() -> CfgTools.decodeDouble(TestSettingString.BOOLEAN_TRUE, CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeString(java.lang.String, java.util.Map, java.lang.String)}
	 * .
	 */
	@Test
	public void testDecodeStringWithDefault() {
		Assertions.assertNull(CfgTools.decodeString("string.empty", CfgToolsStaticTest.CONFIG, null));
		String uuid = UUID.randomUUID().toString();
		Assertions.assertEquals(uuid, CfgTools.decodeString("string.empty", CfgToolsStaticTest.CONFIG, uuid));

		Assertions.assertNotNull(CfgTools.decodeString("string.sample", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertNotNull(CfgTools.decodeString("string.sample", CfgToolsStaticTest.CONFIG, uuid));
		Assertions.assertFalse(uuid.equals(CfgTools.decodeString("string.sample", CfgToolsStaticTest.CONFIG, uuid)));

		Assertions.assertEquals("Q8'6]mn_{ '3k)X RM3w`;TrB,(`gW9zPG3%k3!&_92$&+4:r-lH @v)z_~l'cHsnj{$ Qrq4}7&#h;_",
			CfgTools.decodeString("string.sample", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertEquals("Q8'6]mn_{ '3k)X RM3w`;TrB,(`gW9zPG3%k3!&_92$&+4:r-lH @v)z_~l'cHsnj{$ Qrq4}7&#h;_",
			CfgTools.decodeString("string.sample", CfgToolsStaticTest.CONFIG, uuid));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeString(java.lang.String, java.util.Map)}.
	 */
	@Test
	public void testDecodeStringWithoutDefault() {
		Assertions.assertNull(CfgTools.decodeString("string.empty", CfgToolsStaticTest.CONFIG));
		Assertions.assertNotNull(CfgTools.decodeString("string.sample", CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals("Q8'6]mn_{ '3k)X RM3w`;TrB,(`gW9zPG3%k3!&_92$&+4:r-lH @v)z_~l'cHsnj{$ Qrq4}7&#h;_",
			CfgTools.decodeString("string.sample", CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeString(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testDecodeStringConfigurationSetting() {
		Assertions.assertNull(CfgTools.decodeString(TestSetting.STRING_EMPTY, CfgToolsStaticTest.CONFIG));
		Assertions.assertNotNull(CfgTools.decodeString(TestSetting.STRING_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals("Q8'6]mn_{ '3k)X RM3w`;TrB,(`gW9zPG3%k3!&_92$&+4:r-lH @v)z_~l'cHsnj{$ Qrq4}7&#h;_",
			CfgTools.decodeString(TestSetting.STRING_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals("'R-jXvzq4H#wF/6 s|?XN&*c7n;zf'!N~};PM/NL8$#<8fn}N7fkKS!n|c 4GN?8;B&V;_qDL&?) 5+_",
			CfgTools.decodeString(TestSetting.STRING_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions.assertNull(CfgTools.decodeString(TestSettingString.STRING_EMPTY, CfgToolsStaticTest.CONFIG));
		Assertions.assertNotNull(CfgTools.decodeString(TestSettingString.STRING_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals("Q8'6]mn_{ '3k)X RM3w`;TrB,(`gW9zPG3%k3!&_92$&+4:r-lH @v)z_~l'cHsnj{$ Qrq4}7&#h;_",
			CfgTools.decodeString(TestSettingString.STRING_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assertions.assertEquals("kn>&V~s*.`_`s5?ngd7;bH :p` 4pmb]: )$~n;b?5?%)2QL3wX!F!M):LC)?(?R:9Kg2g[@589HK$t[",
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

		Assertions.assertNull(CfgTools.decodeBinary("binary.empty", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertArrayEquals(sample, CfgTools.decodeBinary("binary.empty", CfgToolsStaticTest.CONFIG, sample));

		Assertions.assertNotNull(CfgTools.decodeBinary("binary.sample", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertNotNull(CfgTools.decodeBinary("binary.sample", CfgToolsStaticTest.CONFIG, sample));
		Assertions.assertFalse(
			Arrays.equals(sample, CfgTools.decodeBinary("binary.sample", CfgToolsStaticTest.CONFIG, null)));
		Assertions.assertFalse(
			Arrays.equals(sample, CfgTools.decodeBinary("binary.sample", CfgToolsStaticTest.CONFIG, sample)));

		byte[] realData = Base64.decodeBase64(
			"c3aqSlM2IN/HO359hN2nybJ3GRTW/rNE8BckG4fmg0HJHtkIbLdmcXE+9/NI6eieQvVAZyAwPBLucINyeh+xe63eCxfCm9FSXCXytBGDZQm7yCCYxlBz1qRQCTlSzCkZCsotR9ZZ99TUYcVE8UBcEjTXpEXGTxFk3OFbW0jV+gpN5Hp4bSqJsUDsUds+END+nXGFMqQ7CTb1h04Qk6kW/7HstL+JidGLG9d2Qa+x5CfcXs3DMujc2mH461a/8lLLJ/i0NLofyRklJSr1ZWVlqPaq/sk9caCnTPKaDMPDYwTOGnkQVIdnOVjyACAKMs0paAIuo+n7wrG4wRD8f75/GzBPceQwGvVqkRANJtfzGK5zfgYVdNnQtN7U8OnJEl+0C9PMOB272SAx1epwSeLqIJEe9cQkwZyozMvO/md1gQiOsrIT1KcP5F0O4OGneSta9PakvJjsj3Y61N8eksJEFmmKtk69sDsu4ewZmCpDooxtNYRnCj0YNjTkXeL+77joivtz1K147ck7Lkjla1cO1BACCzArsQYVTdRxD9XT/sKHDsv+ahR0GzjuTxH8xUb2zhCT9mYFELcrOiJzeuY2kiYzFBbFohBRqZSdlTjbwHeFKjnpuDzY8imOUJFRw0Dfk3lYPd35A6bHvTEJpyYOufwQWg/5mYwk60YWKbaXcZr5wVFcX2Kxp7C5RrpxGINusROjHvovNUdfFxpm2LVP/NBYLfFnE0QlYDstp1ySipc8fYdRWWNqLxVnCX3YQKhof08934BNknS6WMyPryiIbtm4IblK/kBkCP5uENIvT2EHpGPANKLlwZ/DnA1G/k+AUee4kw1UhAbD+ZOR9/hqWlDFOtVc/69nKu4kp+pY25qtVcPNdDyh2s/yXLJsTkpEGBVFGqOEBY9fajP+knDXsvP8RBt97amfln3Fs/nPRK50KeI9wxzp39SCKJ6k2EIcohFvp/IMHts+QhXmbfjh+0sw5S4EFdfRtfBeBUEbFPtb5hceUK+iU5U4sRCFIi5I81TQLycy3mLkQ9xXqcF6ZUZ/uub97U77sSc/O9OgaWvoYrN69+i1nXYvCUobPT9KnebHxe1qC878Kv0RBi+zDMKO13bVHAmw0BYXXQy/j5H4IStFbW5wROyOdDK659X+2EEk3hDS0XdW8OyEBCntlT5jTcevWDRtSPG2vZSYYEAeYrXOxP3mtg/pfwBIJc+XitgzIvGKdiFEKPwZC4/21tWrT2Tb5oDCjuH6/ssDWfUHyGbco39lk3M0kC3PYLydTRtw5lWyK+Q+vlLKaPfLKXlo0ceap5q21fwZbuDCkioeIG8LPC3cxoOBYVGRhA7Rm+Bw9QTvmgKQwdS87eHkAg==");
		Assertions.assertArrayEquals(realData, CfgTools.decodeBinary("binary.sample", CfgToolsStaticTest.CONFIG, null));
		Assertions.assertArrayEquals(realData,
			CfgTools.decodeBinary("binary.sample", CfgToolsStaticTest.CONFIG, sample));

		Assertions.assertThrows(DecoderException.class,
			() -> CfgTools.decodeBinary("string.sample", CfgToolsStaticTest.CONFIG, null));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#decodeBinary(java.lang.String, java.util.Map)}.
	 */
	@Test
	public void testDecodeBinaryWithoutDefault() throws Throwable {
		byte[] realData = Base64.decodeBase64(
			"c3aqSlM2IN/HO359hN2nybJ3GRTW/rNE8BckG4fmg0HJHtkIbLdmcXE+9/NI6eieQvVAZyAwPBLucINyeh+xe63eCxfCm9FSXCXytBGDZQm7yCCYxlBz1qRQCTlSzCkZCsotR9ZZ99TUYcVE8UBcEjTXpEXGTxFk3OFbW0jV+gpN5Hp4bSqJsUDsUds+END+nXGFMqQ7CTb1h04Qk6kW/7HstL+JidGLG9d2Qa+x5CfcXs3DMujc2mH461a/8lLLJ/i0NLofyRklJSr1ZWVlqPaq/sk9caCnTPKaDMPDYwTOGnkQVIdnOVjyACAKMs0paAIuo+n7wrG4wRD8f75/GzBPceQwGvVqkRANJtfzGK5zfgYVdNnQtN7U8OnJEl+0C9PMOB272SAx1epwSeLqIJEe9cQkwZyozMvO/md1gQiOsrIT1KcP5F0O4OGneSta9PakvJjsj3Y61N8eksJEFmmKtk69sDsu4ewZmCpDooxtNYRnCj0YNjTkXeL+77joivtz1K147ck7Lkjla1cO1BACCzArsQYVTdRxD9XT/sKHDsv+ahR0GzjuTxH8xUb2zhCT9mYFELcrOiJzeuY2kiYzFBbFohBRqZSdlTjbwHeFKjnpuDzY8imOUJFRw0Dfk3lYPd35A6bHvTEJpyYOufwQWg/5mYwk60YWKbaXcZr5wVFcX2Kxp7C5RrpxGINusROjHvovNUdfFxpm2LVP/NBYLfFnE0QlYDstp1ySipc8fYdRWWNqLxVnCX3YQKhof08934BNknS6WMyPryiIbtm4IblK/kBkCP5uENIvT2EHpGPANKLlwZ/DnA1G/k+AUee4kw1UhAbD+ZOR9/hqWlDFOtVc/69nKu4kp+pY25qtVcPNdDyh2s/yXLJsTkpEGBVFGqOEBY9fajP+knDXsvP8RBt97amfln3Fs/nPRK50KeI9wxzp39SCKJ6k2EIcohFvp/IMHts+QhXmbfjh+0sw5S4EFdfRtfBeBUEbFPtb5hceUK+iU5U4sRCFIi5I81TQLycy3mLkQ9xXqcF6ZUZ/uub97U77sSc/O9OgaWvoYrN69+i1nXYvCUobPT9KnebHxe1qC878Kv0RBi+zDMKO13bVHAmw0BYXXQy/j5H4IStFbW5wROyOdDK659X+2EEk3hDS0XdW8OyEBCntlT5jTcevWDRtSPG2vZSYYEAeYrXOxP3mtg/pfwBIJc+XitgzIvGKdiFEKPwZC4/21tWrT2Tb5oDCjuH6/ssDWfUHyGbco39lk3M0kC3PYLydTRtw5lWyK+Q+vlLKaPfLKXlo0ceap5q21fwZbuDCkioeIG8LPC3cxoOBYVGRhA7Rm+Bw9QTvmgKQwdS87eHkAg==");

		Assertions.assertNull(CfgTools.decodeBinary("binary.empty", CfgToolsStaticTest.CONFIG));
		Assertions.assertNotNull(CfgTools.decodeBinary("binary.sample", CfgToolsStaticTest.CONFIG));
		Assertions.assertArrayEquals(realData, CfgTools.decodeBinary("binary.sample", CfgToolsStaticTest.CONFIG));

		Assertions.assertThrows(DecoderException.class,
			() -> CfgTools.decodeBinary("string.sample", CfgToolsStaticTest.CONFIG));
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
		Assertions.assertNull(CfgTools.decodeBinary(TestSetting.BINARY_EMPTY, CfgToolsStaticTest.CONFIG));
		Assertions.assertNotNull(CfgTools.decodeBinary(TestSetting.BINARY_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assertions.assertArrayEquals(realData,
			CfgTools.decodeBinary(TestSetting.BINARY_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assertions.assertArrayEquals(dataTestSetting,
			CfgTools.decodeBinary(TestSetting.BINARY_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions.assertThrows(DecoderException.class,
			() -> CfgTools.decodeBinary(TestSetting.STRING_SAMPLE, CfgToolsStaticTest.CONFIG));

		dataTestSetting = Base64.decodeBase64(
			"mgFLqv8Ljr6mAcqI8RcKfj9v0aMROcZP7MpXq2A6ZIPJmCLHmFP5niuEPu3swJikfHIhPr7e+czQVKsfc63KIsPnxz44BKRPTtoOvFE91mW+SoW5ep2U/IC+ytrRu88qyum+6BHqsnqMfeDf7bkbSgpHEuob9RFT9Ic/OLke6rpjxp/Kwq6+cbfK+nN0jVzV1WmuNJh3/S+H3f2L2uyteCXMHqxe710/n9s2Bu5+KuK607fNPt3jjFU2X1fnBSpofZK4izRMgTmp+SPMNt82+iMwRhuWodnByRrvcll5+qcj95Eeqkgtk0zA+BE1sCgBPHhO5jQn2JuLay+O4VOstLRnI6ZocXs+sS1T3GmSI6Yuvd7cvQQ9NFOA+eZcuphIdEVKB8ddAC2v3bg/AYq5pgYFQ+5x4N5zpivfzuPZcK84o7C9Uc/tTPXozTsWegLEsD14kL6M8Qu1OxnuDciNTEqI7TdMhb01fLny8ICdwv7gxH3IVmE64UqJh3Y7vq3+Xvj1As27/6ox3lQvM74CwOdaxlJSA1bGTtY3rTeb7dHN6BVwqM/vWjQ0q2T++MU6UU3zgRhrl4QCOYDFvjfcL22h5JpVem+hZgGthGi90DpVxraJFg2LPjDQQoWGKW84PU+ks4fX0MDjzpZgW8tgPD/6lh4+TTer2yCdON0ccgEvaCDQse4KGu7yQnQhySsepufuZ3dG5plFdI7dJYs8O1VIb2WOfPe01Kf6Kk06ylaHp71ZvxdTsNJesbXYzo0QETWNS+ECFY4Di/AnXRa56KQosNvyx7seaBsCIGdkLMLMUoB1HVTFe1POAiKB5BDQGyaYW9+WGB8MYG0mnpW3oZD2E5MKKm3iSHGwL2wBW1+OP4SaBQvtDpzcvHs1rM5z5pqmB97S8vNKgb4MQfPVwy/b8FmbYueV1H0am8CPZjCt0a1pSOR9LkmbrHAmwNf/WVYXDbbg4zg2V+rVBH8HAEnZGKe+ZbVMxnY1JCJd+JxdFaS1A2oNSDM91FlPmcQed2sWTV0ZfkarfyS5JZWTbRWwDciyzJ6YFXuMliwbXeOGrJolvDTi7x9t9BWbk9RaRwP9iOr2Wtq51ZWoA4yMo33yavWyqlRuC/v7O8xXlO7Avtug4mldKoldaFpYbCqfVliQiz8o5jO13cQuhkbfUpEwXKN+waBm+wd0jeBAbbu8rHkW+/0U+jSX75B9jXtdkMJivBf8sSCj6ED/iKiejZwnzLOPga3DgXOSJbk7mAeDTxeCwO5dIncOtQMxzsoq2UuRYct1V+DRGZSsJGeNTj3mwTbWCJpZB5PERpis1VDrkXcsDnZr4AHCKCwZShJEEBMipmkwzDFFJPGBrtMPiA==");
		Assertions.assertNull(CfgTools.decodeBinary(TestSettingString.BINARY_EMPTY, CfgToolsStaticTest.CONFIG));
		Assertions.assertNotNull(CfgTools.decodeBinary(TestSettingString.BINARY_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assertions.assertArrayEquals(realData,
			CfgTools.decodeBinary(TestSettingString.BINARY_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assertions.assertArrayEquals(dataTestSetting,
			CfgTools.decodeBinary(TestSettingString.BINARY_UNDEF, CfgToolsStaticTest.CONFIG));

		Assertions.assertThrows(DecoderException.class,
			() -> CfgTools.decodeBinary(TestSettingString.STRING_SAMPLE, CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#hasValue(java.lang.String, java.util.Map)} .
	 */
	@Test
	public void testHasValueString() {
		Assertions.assertTrue(CfgTools.hasValue("string.empty", CfgToolsStaticTest.CONFIG));
		Assertions.assertTrue(CfgTools.hasValue("string.sample", CfgToolsStaticTest.CONFIG));
		Assertions.assertFalse(CfgTools.hasValue("string.unset", CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(IllegalArgumentException.class,
			() -> CfgTools.hasValue((String) null, CfgToolsStaticTest.CONFIG));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.CfgTools#hasValue(com.armedia.commons.utilities.ConfigurationSetting, java.util.Map)}
	 * .
	 */
	@Test
	public void testHasValueConfigurationSetting() {
		Assertions.assertTrue(CfgTools.hasValue(TestSettingString.STRING_EMPTY, CfgToolsStaticTest.CONFIG));
		Assertions.assertTrue(CfgTools.hasValue(TestSettingString.STRING_SAMPLE, CfgToolsStaticTest.CONFIG));
		Assertions.assertFalse(CfgTools.hasValue(TestSettingString.STRING_UNSET, CfgToolsStaticTest.CONFIG));
		Assertions.assertThrows(IllegalArgumentException.class,
			() -> CfgTools.hasValue((ConfigurationSetting) null, CfgToolsStaticTest.CONFIG));
	}
}
