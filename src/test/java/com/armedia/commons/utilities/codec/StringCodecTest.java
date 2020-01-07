/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2020 Armedia, LLC
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
package com.armedia.commons.utilities.codec;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.Tools;

public class StringCodecTest {

	private <T> void runTest(Collection<Pair<T, String>> values, final StringCodec<T> codec) {
		values.forEach((p) -> {
			String s = codec.encode(p.getKey());
			Assertions.assertEquals(p.getValue(), s, String.format("Encoded [%s] as [%s]", p.getKey(), s));
			T v = codec.decode(s);
			Assertions.assertEquals(p.getKey(), v,
				String.format("Decoded [%s] as [%s], should have been [%s]", s, v, p.getValue()));
		});

		{
			Pair<T, String> p = Pair.of(null, null);
			String s = codec.encode(p.getKey());
			Assertions.assertEquals(p.getValue(), s, String.format("Encoded [%s] as [%s]", p.getKey(), s));
			T v = codec.decode(s);
			Assertions.assertEquals(p.getKey(), v,
				String.format("Decoded [%s] as [%s], should have been [%s]", s, v, p.getValue()));
		}
	}

	@Test
	public void testBoolean() {
		Collection<Pair<Boolean, String>> values = new ArrayList<>();
		values.add(Pair.of(Boolean.TRUE, Boolean.TRUE.toString()));
		values.add(Pair.of(Boolean.FALSE, Boolean.FALSE.toString()));
		values.add(Pair.of(null, null));
		runTest(values, StringCodec.BOOLEAN);
	}

	@Test
	public void testByte() {
		byte[] s = {
			Byte.MIN_VALUE, //
			Byte.MIN_VALUE / 2, //
			-2, //
			-1, //
			0, //
			1, //
			2, //
			Byte.MAX_VALUE / 2, //
			Byte.MAX_VALUE, //
		};
		Collection<Pair<Byte, String>> values = new ArrayList<>();
		for (byte v : s) {
			values.add(Pair.of(v, Tools.toString(v)));
		}
		runTest(values, StringCodec.BYTE);
	}

	@Test
	public void testShort() {
		short[] s = {
			Short.MIN_VALUE, //
			Short.MIN_VALUE / 2, //
			Byte.MIN_VALUE * 2, //
			Byte.MIN_VALUE - 1, //
			Byte.MIN_VALUE / 2, //
			-2, //
			-1, //
			0, //
			1, //
			2, //
			Byte.MAX_VALUE / 2, //
			Byte.MAX_VALUE - 1, //
			Byte.MAX_VALUE * 2, //
			Short.MAX_VALUE / 2, //
			Short.MAX_VALUE, //
		};
		Collection<Pair<Short, String>> values = new ArrayList<>();
		for (short v : s) {
			values.add(Pair.of(v, Tools.toString(v)));
		}
		runTest(values, StringCodec.SHORT);
	}

	@Test
	public void testInteger() {
		int[] s = {
			Integer.MIN_VALUE, //
			Short.MIN_VALUE * 2, //
			Short.MIN_VALUE - 1, //
			Short.MIN_VALUE / 2, //
			Byte.MIN_VALUE * 2, //
			Byte.MIN_VALUE - 1, //
			Byte.MIN_VALUE / 2, //
			-2, //
			-1, //
			0, //
			1, //
			2, //
			Byte.MAX_VALUE / 2, //
			Byte.MAX_VALUE - 1, //
			Byte.MAX_VALUE * 2, //
			Short.MAX_VALUE / 2, //
			Short.MAX_VALUE + 1, //
			Short.MAX_VALUE * 2, //
			Integer.MAX_VALUE, //
		};
		Collection<Pair<Integer, String>> values = new ArrayList<>();
		for (int v : s) {
			values.add(Pair.of(v, Tools.toString(v)));
		}
		runTest(values, StringCodec.INTEGER);
	}

	@Test
	public void testLong() {
		long[] s = {
			Long.MIN_VALUE, //
			((long) Integer.MIN_VALUE) * 2, //
			((long) Integer.MIN_VALUE) - 1, //
			((long) Integer.MIN_VALUE) / 2, //
			((long) Short.MIN_VALUE) * 2, //
			((long) Short.MIN_VALUE) - 1, //
			((long) Short.MIN_VALUE) / 2, //
			((long) Byte.MIN_VALUE) * 2, //
			((long) Byte.MIN_VALUE) - 1, //
			((long) Byte.MIN_VALUE) / 2, //
			-2, //
			-1, //
			0, //
			1, //
			2, //
			((long) Byte.MAX_VALUE) / 2, //
			((long) Byte.MAX_VALUE) - 1, //
			((long) Byte.MAX_VALUE) * 2, //
			((long) Short.MAX_VALUE) / 2, //
			((long) Short.MAX_VALUE) - 1, //
			((long) Short.MAX_VALUE) * 2, //
			((long) Integer.MAX_VALUE) / 2, //
			((long) Integer.MAX_VALUE) - 1, //
			((long) Integer.MAX_VALUE) * 2, //
			Long.MAX_VALUE //
		};
		Collection<Pair<Long, String>> values = new ArrayList<>();
		for (long v : s) {
			values.add(Pair.of(v, Tools.toString(v)));
		}
		runTest(values, StringCodec.LONG);
	}

	@Test
	public void testBigInteger() {
		final BigInteger two = BigInteger.ONE.add(BigInteger.ONE);
		BigInteger[] s = {
			new BigInteger(Tools.toString(Long.MIN_VALUE)).multiply(BigInteger.TEN), //
			new BigInteger(Tools.toString(Long.MIN_VALUE)).multiply(two), //
			new BigInteger(Tools.toString(Long.MIN_VALUE)).subtract(BigInteger.ONE), //
			new BigInteger(Tools.toString(Long.MIN_VALUE)).divide(two), //
			new BigInteger(Tools.toString(Integer.MIN_VALUE)).multiply(two), //
			new BigInteger(Tools.toString(Integer.MIN_VALUE)).subtract(BigInteger.ONE), //
			new BigInteger(Tools.toString(Integer.MIN_VALUE)).divide(two), //
			new BigInteger(Tools.toString(Short.MIN_VALUE)).multiply(two), //
			new BigInteger(Tools.toString(Short.MIN_VALUE)).subtract(BigInteger.ONE), //
			new BigInteger(Tools.toString(Short.MIN_VALUE)).divide(two), //
			new BigInteger(Tools.toString(Byte.MIN_VALUE)).multiply(two), //
			new BigInteger(Tools.toString(Byte.MIN_VALUE)).subtract(BigInteger.ONE), //
			new BigInteger(Tools.toString(Byte.MIN_VALUE)).divide(two), //
			two.negate(), //
			BigInteger.ONE.negate(), //
			BigInteger.ZERO, //
			BigInteger.ONE, //
			two, //
			new BigInteger(Tools.toString(Byte.MAX_VALUE)).divide(two), //
			new BigInteger(Tools.toString(Byte.MAX_VALUE)).add(BigInteger.ONE), //
			new BigInteger(Tools.toString(Byte.MAX_VALUE)).multiply(two), //
			new BigInteger(Tools.toString(Short.MAX_VALUE)).divide(two), //
			new BigInteger(Tools.toString(Short.MAX_VALUE)).add(BigInteger.ONE), //
			new BigInteger(Tools.toString(Short.MAX_VALUE)).multiply(two), //
			new BigInteger(Tools.toString(Integer.MAX_VALUE)).divide(two), //
			new BigInteger(Tools.toString(Integer.MAX_VALUE)).add(BigInteger.ONE), //
			new BigInteger(Tools.toString(Integer.MAX_VALUE)).multiply(two), //
			new BigInteger(Tools.toString(Long.MAX_VALUE)).divide(two), //
			new BigInteger(Tools.toString(Long.MAX_VALUE)).add(BigInteger.ONE), //
			new BigInteger(Tools.toString(Long.MAX_VALUE)).multiply(two), //
			new BigInteger(Tools.toString(Long.MAX_VALUE)).multiply(BigInteger.TEN), //
		};
		Collection<Pair<BigInteger, String>> values = new ArrayList<>();
		for (BigInteger v : s) {
			values.add(Pair.of(v, Tools.toString(v)));
		}
		runTest(values, StringCodec.BIG_INTEGER);
	}

	@Test
	public void testFloat() {
		float[] s = {
			Float.NEGATIVE_INFINITY, //
			Float.MIN_VALUE, //
			Float.MIN_VALUE / 2, //
			-2, //
			-1, //
			0, //
			Float.NaN, //
			1, //
			2, //
			Float.MAX_VALUE / 2, //
			Float.MAX_VALUE, //
			Float.POSITIVE_INFINITY, //
		};
		Collection<Pair<Float, String>> values = new ArrayList<>();
		for (float v : s) {
			values.add(Pair.of(v, Tools.toString(v)));
		}
		runTest(values, StringCodec.FLOAT);
	}

	@Test
	public void testDouble() {
		double[] s = {
			Double.NEGATIVE_INFINITY, //
			Double.MIN_VALUE, //
			Double.MIN_VALUE / 2, //
			Float.MIN_VALUE * 2, //
			Float.MIN_VALUE - 1, //
			Float.MIN_VALUE / 2, //
			-2, //
			-1, //
			0, //
			Double.NaN, //
			1, //
			2, //
			Float.MAX_VALUE / 2, //
			Float.MAX_VALUE - 1, //
			Float.MAX_VALUE * 2, //
			Double.MAX_VALUE / 2, //
			Double.MAX_VALUE, //
			Double.POSITIVE_INFINITY, //
		};
		Collection<Pair<Double, String>> values = new ArrayList<>();
		for (double v : s) {
			values.add(Pair.of(v, Tools.toString(v)));
		}
		runTest(values, StringCodec.DOUBLE);
	}

	@Test
	public void testBigDecimal() {
		final BigDecimal two = BigDecimal.ONE.add(BigDecimal.ONE);
		BigDecimal[] s = {
			new BigDecimal(Double.MIN_VALUE).multiply(BigDecimal.TEN), //
			new BigDecimal(Double.MIN_VALUE).multiply(two), //
			new BigDecimal(Double.MIN_VALUE).subtract(BigDecimal.ONE), //
			new BigDecimal(Double.MIN_VALUE).divide(two), //
			new BigDecimal(Long.MIN_VALUE).multiply(BigDecimal.TEN), //
			new BigDecimal(Long.MIN_VALUE).multiply(two), //
			new BigDecimal(Long.MIN_VALUE).subtract(BigDecimal.ONE), //
			new BigDecimal(Long.MIN_VALUE).divide(two), //
			new BigDecimal(Float.MIN_VALUE).multiply(two), //
			new BigDecimal(Float.MIN_VALUE).subtract(BigDecimal.ONE), //
			new BigDecimal(Float.MIN_VALUE).divide(two), //
			new BigDecimal(Integer.MIN_VALUE).multiply(two), //
			new BigDecimal(Integer.MIN_VALUE).subtract(BigDecimal.ONE), //
			new BigDecimal(Integer.MIN_VALUE).divide(two), //
			new BigDecimal(Short.MIN_VALUE).multiply(two), //
			new BigDecimal(Short.MIN_VALUE).subtract(BigDecimal.ONE), //
			new BigDecimal(Short.MIN_VALUE).divide(two), //
			new BigDecimal(Byte.MIN_VALUE).multiply(two), //
			new BigDecimal(Byte.MIN_VALUE).subtract(BigDecimal.ONE), //
			new BigDecimal(Byte.MIN_VALUE).divide(two), //
			two.negate(), //
			BigDecimal.ONE.negate(), //
			BigDecimal.ZERO, //
			BigDecimal.ONE, //
			two, //
			new BigDecimal(Byte.MAX_VALUE).divide(two), //
			new BigDecimal(Byte.MAX_VALUE).add(BigDecimal.ONE), //
			new BigDecimal(Byte.MAX_VALUE).multiply(two), //
			new BigDecimal(Short.MAX_VALUE).divide(two), //
			new BigDecimal(Short.MAX_VALUE).add(BigDecimal.ONE), //
			new BigDecimal(Short.MAX_VALUE).multiply(two), //
			new BigDecimal(Integer.MAX_VALUE).divide(two), //
			new BigDecimal(Integer.MAX_VALUE).add(BigDecimal.ONE), //
			new BigDecimal(Integer.MAX_VALUE).multiply(two), //
			new BigDecimal(Float.MAX_VALUE).divide(two), //
			new BigDecimal(Float.MAX_VALUE).add(BigDecimal.ONE), //
			new BigDecimal(Float.MAX_VALUE).multiply(two), //
			new BigDecimal(Long.MAX_VALUE).divide(two), //
			new BigDecimal(Long.MAX_VALUE).add(BigDecimal.ONE), //
			new BigDecimal(Long.MAX_VALUE).multiply(two), //
			new BigDecimal(Long.MAX_VALUE).multiply(BigDecimal.TEN), //
			new BigDecimal(Double.MAX_VALUE).divide(two), //
			new BigDecimal(Double.MAX_VALUE).add(BigDecimal.ONE), //
			new BigDecimal(Double.MAX_VALUE).multiply(two), //
			new BigDecimal(Double.MAX_VALUE).multiply(BigDecimal.TEN), //
		};
		Collection<Pair<BigDecimal, String>> values = new ArrayList<>();
		for (BigDecimal v : s) {
			values.add(Pair.of(v, Tools.toString(v)));
		}
		runTest(values, StringCodec.BIG_DECIMAL);
	}

	@Test
	public void testCharacter() {
		Character[] s = {
			'a', 'b', '\0', '\'', '"', null
		};
		Collection<Pair<Character, String>> values = new ArrayList<>();
		for (Character v : s) {
			values.add(Pair.of(v, Tools.toString(v)));
		}
		runTest(values, StringCodec.CHARACTER);
	}

	@Test
	public void testString() {
		String[] s = {
			UUID.randomUUID().toString(), null
		};
		Collection<Pair<String, String>> values = new ArrayList<>();
		for (String v : s) {
			values.add(Pair.of(v, Tools.toString(v)));
		}
		runTest(values, StringCodec.STRING);
	}
}
