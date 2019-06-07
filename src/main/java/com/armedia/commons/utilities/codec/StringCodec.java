package com.armedia.commons.utilities.codec;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.armedia.commons.utilities.Tools;

public class StringCodec<T> extends FunctionalCodec<T, String> {

	public static class Builder<VALUE> extends FunctionalCodec.Builder<VALUE, String> {
		@Override
		protected StringCodec<VALUE> newCodec() {
			return new StringCodec<>(this);
		}
	}

	protected StringCodec(Builder<T> builder) {
		super(builder);
	}

	public StringCodec(Function<String, T> decoder) {
		this(Tools::toString, null, null, decoder, null, null);
	}

	public StringCodec(Function<T, String> encoder, Function<String, T> decoder) {
		this(encoder, null, null, decoder, null, null);
	}

	public StringCodec(Function<T, String> encoder, String nullString, Predicate<String> nullStringCheck,
		Function<String, T> decoder, T nullValue, Predicate<T> nullValueCheck) {
		super(encoder, nullValue, nullValueCheck, decoder, nullString, nullStringCheck);
	}

	public static final StringCodec<Boolean> BOOLEAN = new StringCodec<>(Tools::toString, Tools::decodeBoolean);

	public static final StringCodec<Byte> BYTE = new StringCodec<>(Tools::toString,
		(s) -> (s == null ? null : Byte.valueOf(s)));

	public static final StringCodec<Short> SHORT = new StringCodec<>(Tools::toString,
		(s) -> (s == null ? null : Short.valueOf(s)));

	public static final StringCodec<Integer> INTEGER = new StringCodec<>(Tools::toString,
		(s) -> (s == null ? null : Integer.valueOf(s)));

	public static final StringCodec<Long> LONG = new StringCodec<>(Tools::toString,
		(s) -> (s == null ? null : Long.valueOf(s)));

	public static final StringCodec<BigInteger> BIG_INTEGER = new StringCodec<>(Tools::toString,
		(s) -> (s == null ? null : new BigInteger(s)));

	public static final StringCodec<Float> FLOAT = new StringCodec<>(Tools::toString,
		(s) -> (s == null ? null : Float.valueOf(s)));

	public static final StringCodec<Double> DOUBLE = new StringCodec<>(Tools::toString,
		(s) -> (s == null ? null : Double.valueOf(s)));

	public static final StringCodec<BigDecimal> BIG_DECIMAL = new StringCodec<>(Tools::toString,
		(s) -> (s == null ? null : new BigDecimal(s)));

	private static Character decodeChar(String str) {
		return str.charAt(0);
	}

	public static final StringCodec<Character> CHARACTER;
	static {
		Builder<Character> builder = new Builder<>();
		CHARACTER = builder //
			.setEncoder(Tools::toString) //
			.setNullEncodingChecker(StringUtils::isEmpty) //
			.setDecoder(StringCodec::decodeChar) //
			.build();
	}

	public static final StringCodec<String> STRING = new StringCodec<>(Function.identity(), Function.identity());
}