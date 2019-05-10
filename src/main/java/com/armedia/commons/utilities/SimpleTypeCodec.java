package com.armedia.commons.utilities;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

public interface SimpleTypeCodec<T> {
	public Function<T, String> getEncoder();

	public Function<String, T> getDecoder();

	public static final SimpleTypeCodec<Boolean> BOOLEAN = new SimpleTypeCodec<Boolean>() {
		@Override
		public Function<Boolean, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, Boolean> getDecoder() {
			return Tools::decodeBoolean;
		}
	};

	public static final SimpleTypeCodec<Byte> BYTE = new SimpleTypeCodec<Byte>() {
		@Override
		public Function<Byte, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, Byte> getDecoder() {
			return Byte::valueOf;
		}
	};

	public static final SimpleTypeCodec<Short> SHORT = new SimpleTypeCodec<Short>() {
		@Override
		public Function<Short, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, Short> getDecoder() {
			return Short::valueOf;
		}
	};

	public static final SimpleTypeCodec<Integer> INTEGER = new SimpleTypeCodec<Integer>() {
		@Override
		public Function<Integer, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, Integer> getDecoder() {
			return Integer::valueOf;
		}
	};

	public static final SimpleTypeCodec<Long> LONG = new SimpleTypeCodec<Long>() {
		@Override
		public Function<Long, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, Long> getDecoder() {
			return Long::valueOf;
		}
	};

	public static final SimpleTypeCodec<BigInteger> BIG_INTEGER = new SimpleTypeCodec<BigInteger>() {
		@Override
		public Function<BigInteger, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, BigInteger> getDecoder() {
			return BigInteger::new;
		}
	};

	public static final SimpleTypeCodec<Float> FLOAT = new SimpleTypeCodec<Float>() {
		@Override
		public Function<Float, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, Float> getDecoder() {
			return Float::valueOf;
		}
	};

	public static final SimpleTypeCodec<Double> DOUBLE = new SimpleTypeCodec<Double>() {
		@Override
		public Function<Double, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, Double> getDecoder() {
			return Double::valueOf;
		}
	};

	public static final SimpleTypeCodec<BigDecimal> BIG_DECIMAL = new SimpleTypeCodec<BigDecimal>() {
		@Override
		public Function<BigDecimal, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, BigDecimal> getDecoder() {
			return BigDecimal::new;
		}
	};

	public static final SimpleTypeCodec<Character> CHARACTER = new SimpleTypeCodec<Character>() {
		@Override
		public Function<Character, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, Character> getDecoder() {
			return this::decode;
		}

		private Character decode(String str) {
			if (StringUtils.isEmpty(str)) { return null; }
			return str.charAt(0);
		}
	};

	public static final SimpleTypeCodec<String> STRING = new SimpleTypeCodec<String>() {
		@Override
		public Function<String, String> getEncoder() {
			return Function.identity();
		}

		@Override
		public Function<String, String> getDecoder() {
			return Function.identity();
		}
	};
}