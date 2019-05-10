package com.armedia.commons.utilities.xml;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.armedia.commons.utilities.Tools;

public interface AnyElementCodec<T> {
	public Function<T, String> getEncoder();

	public Function<String, T> getDecoder();

	public static final AnyElementCodec<Boolean> BOOLEAN = new AnyElementCodec<Boolean>() {
		@Override
		public Function<Boolean, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, Boolean> getDecoder() {
			return Tools::decodeBoolean;
		}
	};

	public static final AnyElementCodec<Byte> BYTE = new AnyElementCodec<Byte>() {
		@Override
		public Function<Byte, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, Byte> getDecoder() {
			return Byte::valueOf;
		}
	};

	public static final AnyElementCodec<Short> SHORT = new AnyElementCodec<Short>() {
		@Override
		public Function<Short, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, Short> getDecoder() {
			return Short::valueOf;
		}
	};

	public static final AnyElementCodec<Integer> INTEGER = new AnyElementCodec<Integer>() {
		@Override
		public Function<Integer, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, Integer> getDecoder() {
			return Integer::valueOf;
		}
	};

	public static final AnyElementCodec<Long> LONG = new AnyElementCodec<Long>() {
		@Override
		public Function<Long, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, Long> getDecoder() {
			return Long::valueOf;
		}
	};

	public static final AnyElementCodec<BigInteger> BIG_INTEGER = new AnyElementCodec<BigInteger>() {
		@Override
		public Function<BigInteger, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, BigInteger> getDecoder() {
			return BigInteger::new;
		}
	};

	public static final AnyElementCodec<Float> FLOAT = new AnyElementCodec<Float>() {
		@Override
		public Function<Float, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, Float> getDecoder() {
			return Float::valueOf;
		}
	};

	public static final AnyElementCodec<Double> DOUBLE = new AnyElementCodec<Double>() {
		@Override
		public Function<Double, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, Double> getDecoder() {
			return Double::valueOf;
		}
	};

	public static final AnyElementCodec<BigDecimal> BIG_DECIMAL = new AnyElementCodec<BigDecimal>() {
		@Override
		public Function<BigDecimal, String> getEncoder() {
			return Tools::toString;
		}

		@Override
		public Function<String, BigDecimal> getDecoder() {
			return BigDecimal::new;
		}
	};

	public static final AnyElementCodec<Character> CHARACTER = new AnyElementCodec<Character>() {
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

	public static final AnyElementCodec<String> STRING = new AnyElementCodec<String>() {
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