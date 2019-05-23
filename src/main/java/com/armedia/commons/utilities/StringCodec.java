package com.armedia.commons.utilities;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

public class StringCodec<T> extends BaseCodec<T, String> {

	public StringCodec(Function<T, String> encoder, Function<String, T> decoder) {
		this(encoder, null, null, decoder, null, null);
	}

	public StringCodec(Function<T, String> encoder, Function<String, T> decoder, T nullValue) {
		this(encoder, null, null, decoder, nullValue, null);
	}

	public StringCodec(Function<T, String> encoder, Function<String, T> decoder, Predicate<T> nullValueCheck) {
		this(encoder, null, null, decoder, null, nullValueCheck);
	}

	public StringCodec(Function<T, String> encoder, Function<String, T> decoder, T nullValue,
		Predicate<T> nullValueCheck) {
		this(encoder, null, null, decoder, nullValue, nullValueCheck);
	}

	public StringCodec(Function<T, String> encoder, String nullString, Function<String, T> decoder, T nullValue) {
		this(encoder, nullString, null, decoder, nullValue, null);
	}

	public StringCodec(Function<T, String> encoder, String nullString, Function<String, T> decoder,
		Predicate<T> nullValueCheck) {
		this(encoder, nullString, null, decoder, null, nullValueCheck);
	}

	public StringCodec(Function<T, String> encoder, String nullString, Function<String, T> decoder, T nullValue,
		Predicate<T> nullValueCheck) {
		this(encoder, nullString, null, decoder, nullValue, nullValueCheck);
	}

	public StringCodec(Function<T, String> encoder, Predicate<String> nullStringCheck, Function<String, T> decoder) {
		this(encoder, null, nullStringCheck, decoder, null, null);
	}

	public StringCodec(Function<T, String> encoder, Predicate<String> nullStringCheck, Function<String, T> decoder,
		T nullValue) {
		this(encoder, null, nullStringCheck, decoder, nullValue, null);
	}

	public StringCodec(Function<T, String> encoder, Predicate<String> nullStringCheck, Function<String, T> decoder,
		Predicate<T> nullValueCheck) {
		this(encoder, null, nullStringCheck, decoder, null, nullValueCheck);
	}

	public StringCodec(Function<T, String> encoder, Predicate<String> nullStringCheck, Function<String, T> decoder,
		T nullValue, Predicate<T> nullValueCheck) {
		this(encoder, null, nullStringCheck, decoder, nullValue, nullValueCheck);
	}

	public StringCodec(Function<T, String> encoder, String nullString, Predicate<String> nullStringCheck,
		Function<String, T> decoder) {
		this(encoder, nullString, nullStringCheck, decoder, null, null);
	}

	public StringCodec(Function<T, String> encoder, String nullString, Predicate<String> nullStringCheck,
		Function<String, T> decoder, T nullValue) {
		this(encoder, nullString, nullStringCheck, decoder, nullValue, null);
	}

	public StringCodec(Function<T, String> encoder, String nullString, Predicate<String> nullStringCheck,
		Function<String, T> decoder, Predicate<T> nullValueCheck) {
		this(encoder, nullString, nullStringCheck, decoder, null, nullValueCheck);
	}

	public StringCodec(Function<T, String> encoder, String nullString, Predicate<String> nullStringCheck,
		Function<String, T> decoder, T nullValue, Predicate<T> nullValueCheck) {
		super(encoder, nullString, nullStringCheck, decoder, nullValue, nullValueCheck);
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

	public static final StringCodec<Character> CHARACTER;
	static {
		Function<Character, String> encoder = Tools::toString;
		Predicate<String> nullStringCheck = StringUtils::isEmpty;
		Function<String, Character> decoder = (s) -> s.charAt(0);
		CHARACTER = new StringCodec<>(encoder, nullStringCheck, decoder);
	}

	public static final StringCodec<String> STRING = new StringCodec<>(Function.identity(), Function.identity());
}