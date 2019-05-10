package com.armedia.commons.utilities.xml;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.SimpleTypeCodec;

public class AnyElementCodecTest {

	private <T> void runTest(Collection<Pair<T, String>> values, SimpleTypeCodec<T> codec) {
		final Function<T, String> encoder = codec.getEncoder();
		final Function<String, T> decoder = codec.getDecoder();
		values.forEach((p) -> {
			String s = encoder.apply(p.getKey());
			Assertions.assertEquals(p.getValue(), s, String.format("Encoded [%s] as [%s]", p.getKey(), s));
			T v = decoder.apply(s);
			Assertions.assertEquals(p.getKey(), v,
				String.format("Decoded [%s] as [%s], should have been [%s]", s, v, p.getValue()));
		});
	}

	@Test
	public void testBoolean() {
		Collection<Pair<Boolean, String>> values = new ArrayList<>();
		values.add(Pair.of(Boolean.TRUE, Boolean.TRUE.toString()));
		values.add(Pair.of(Boolean.FALSE, Boolean.FALSE.toString()));
		values.add(Pair.of(null, null));
		runTest(values, SimpleTypeCodec.BOOLEAN);
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
			values.add(Pair.of(v, String.valueOf(v)));
		}
		runTest(values, SimpleTypeCodec.BYTE);
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
			values.add(Pair.of(v, String.valueOf(v)));
		}
		runTest(values, SimpleTypeCodec.SHORT);
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
			values.add(Pair.of(v, String.valueOf(v)));
		}
		runTest(values, SimpleTypeCodec.INTEGER);
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
			values.add(Pair.of(v, String.valueOf(v)));
		}
		runTest(values, SimpleTypeCodec.LONG);
	}

	@Test
	public void testBigInteger() {
		BigInteger[] s = {

		};
		Collection<Pair<BigInteger, String>> values = new ArrayList<>();
		for (BigInteger v : s) {
			values.add(Pair.of(v, String.valueOf(v)));
		}
		runTest(values, SimpleTypeCodec.BIG_INTEGER);
	}

	@Test
	public void testFloat() {
	}

	@Test
	public void testDouble() {
	}

	@Test
	public void testBigDecimal() {
	}

	@Test
	public void testCharacter() {
	}

	@Test
	public void testString() {
	}
}